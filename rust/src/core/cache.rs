use crate::core::client::http_client;
use crate::core::error::ShinkaiError;
use reqwest::{StatusCode, header::CONTENT_TYPE};
use std::io::ErrorKind;
use std::path::{Path, PathBuf};
use std::sync::atomic::{AtomicBool, AtomicU64, Ordering};
use std::time::{Duration, SystemTime};
use tokio::fs;
use tokio::io::{AsyncWriteExt, BufWriter};

static TMP_SEQ: AtomicU64 = AtomicU64::new(0);
static HIT_COUNTER: AtomicU64 = AtomicU64::new(0);
static PRUNING: AtomicBool = AtomicBool::new(false);

const MAX_CACHE_BYTES: u64 = 150 * 1024 * 1024; // hard limit that triggers a prune
const TARGET_CACHE_BYTES: u64 = 120 * 1024 * 1024; // prune down to this
const MAX_IMAGE_BYTES: u64 = 20 * 1024 * 1024; // per-image cap
const MAX_RETRIES: u32 = 2;
const PRUNE_EVERY_N_HITS: u64 = 50;
const STALE_TMP_AGE: Duration = Duration::from_secs(10 * 60);

/// Computes an MD5 hex string. Used as a stable on-disk file name, so do NOT swap this
/// for `DefaultHasher` (its output is not stable across Rust versions).
pub fn hash_key(key: &str) -> String {
    let digest = md5::compute(key.as_bytes());
    format!("{digest:x}")
}

/// Extension hint from the URL path (query and fragment stripped, case-insensitive).
/// Falls back to "jpg" for anything unknown.
pub fn get_extension(url: &str) -> &'static str {
    let path = url.split(['?', '#']).next().unwrap_or(url);
    match path.rsplit_once('.').map(|(_, ext)| ext) {
        Some(e) if e.eq_ignore_ascii_case("png") => "png",
        Some(e) if e.eq_ignore_ascii_case("webp") => "webp",
        _ => "jpg",
    }
}

/// Transient = timeouts, 5xx, 408, 429. Every other status (404, 403, ...) is permanent.
pub fn status_retryable(s: StatusCode) -> bool {
    s.is_server_error() || s == StatusCode::REQUEST_TIMEOUT || s == StatusCode::TOO_MANY_REQUESTS
}

/// Retry classification is decided where the typed `reqwest::Error` is still available,
/// so we never need to pattern-match on `ShinkaiError` variants.
enum Failure {
    Retry(ShinkaiError),
    Fatal(ShinkaiError),
}

fn net_err(e: reqwest::Error) -> Failure {
    let retry = match e.status() {
        Some(s) => status_retryable(s),
        None => !e.is_builder(), // connect / timeout / body stream errors
    };
    let err = ShinkaiError::from(e);
    if retry {
        Failure::Retry(err)
    } else {
        Failure::Fatal(err)
    }
}

fn io_err(e: std::io::Error) -> Failure {
    Failure::Retry(ShinkaiError::from(e))
}

fn invalid_data(msg: &str) -> Failure {
    Failure::Fatal(ShinkaiError::Io(std::io::Error::new(
        ErrorKind::InvalidData,
        msg.to_string(),
    )))
}

fn path_to_string(p: &Path) -> Result<String, ShinkaiError> {
    p.to_str()
        .map(str::to_owned)
        .ok_or_else(|| ShinkaiError::InvalidPath("Invalid UTF-8 in cache path".into()))
}

/// Bumps mtime so the pruner behaves like LRU instead of FIFO.
fn touch(path: PathBuf) {
    tokio::task::spawn_blocking(move || {
        let _ = std::fs::OpenOptions::new()
            .write(true)
            .open(&path)
            .and_then(|f| f.set_modified(SystemTime::now()));
    });
}

/// At most one prune runs at a time.
fn spawn_prune(dir: PathBuf) {
    if PRUNING
        .compare_exchange(false, true, Ordering::AcqRel, Ordering::Acquire)
        .is_err()
    {
        return;
    }
    tokio::spawn(async move {
        struct Guard;
        impl Drop for Guard {
            fn drop(&mut self) {
                PRUNING.store(false, Ordering::Release);
            }
        }
        let _guard = Guard; // released even if prune_cache panics
        prune_cache(&dir).await;
    });
}

/// One download attempt: GET -> validate -> stream to tmp -> fsync -> atomic rename.
async fn fetch_to_file(
    client: &reqwest::Client,
    url: &str,
    tmp_path: &Path,
    dest: &Path,
) -> Result<(), Failure> {
    let mut resp = client
        .get(url)
        .send()
        .await
        .map_err(net_err)?
        .error_for_status()
        .map_err(net_err)?;

    // Reject obvious non-images (captive portal / CDN error pages served as 200).
    // A missing or generic Content-Type (octet-stream) is tolerated.
    if let Some(ct) = resp
        .headers()
        .get(CONTENT_TYPE)
        .and_then(|v| v.to_str().ok())
    {
        let ct = ct.trim().to_ascii_lowercase();
        if ct.starts_with("text/") || ct.starts_with("application/json") {
            return Err(invalid_data("response is not an image"));
        }
    }
    if resp.content_length().is_some_and(|n| n > MAX_IMAGE_BYTES) {
        return Err(invalid_data("image exceeds size limit"));
    }

    let file = fs::File::create(tmp_path).await.map_err(io_err)?;
    let mut writer = BufWriter::with_capacity(64 * 1024, file);
    let mut written: u64 = 0;

    // `chunk()` instead of `bytes_stream()`: no dependency on reqwest's `stream` feature.
    while let Some(chunk) = resp.chunk().await.map_err(net_err)? {
        written += chunk.len() as u64;
        if written > MAX_IMAGE_BYTES {
            return Err(invalid_data("image exceeds size limit"));
        }
        writer.write_all(&chunk).await.map_err(io_err)?;
    }
    writer.flush().await.map_err(io_err)?;

    if written == 0 {
        return Err(invalid_data("empty response body"));
    }

    // Data must hit the disk before the rename makes it visible under the final name.
    writer.into_inner().sync_data().await.map_err(io_err)?;
    fs::rename(tmp_path, dest).await.map_err(io_err)?;
    Ok(())
}

/// Streams and caches an image with atomic swap, bounded retry on transient failures,
/// and background LRU pruning.
pub async fn download_image(url: &str, cache_dir: &str) -> Result<String, ShinkaiError> {
    let file_name = hash_key(url);
    let ext = get_extension(url);
    let cache_path = Path::new(cache_dir).join("image_cache");

    fs::create_dir_all(&cache_path).await?;

    let file_path = cache_path.join(format!("{file_name}.{ext}"));
    if fs::try_exists(&file_path).await.unwrap_or(false) {
        touch(file_path.clone());
        if HIT_COUNTER.fetch_add(1, Ordering::Relaxed) % PRUNE_EVERY_N_HITS == 0 {
            spawn_prune(cache_path.clone());
        }
        return path_to_string(&file_path);
    }

    // Legacy file without extension from an older cache layout.
    let legacy_path = cache_path.join(&file_name);
    if fs::try_exists(&legacy_path).await.unwrap_or(false) {
        return path_to_string(&legacy_path);
    }

    // pid in the name: TMP_SEQ resets on every process start, so a leftover .tmp
    // from a killed process could otherwise collide with a new download.
    let seq = TMP_SEQ.fetch_add(1, Ordering::Relaxed);
    let tmp_path = cache_path.join(format!("{file_name}.{}.{seq}.tmp", std::process::id()));

    let client = http_client()?;
    let mut attempt: u32 = 0;

    loop {
        match fetch_to_file(&client, url, &tmp_path, &file_path).await {
            Ok(()) => break,
            Err(Failure::Fatal(e)) => {
                let _ = fs::remove_file(&tmp_path).await;
                return Err(e);
            }
            Err(Failure::Retry(e)) => {
                let _ = fs::remove_file(&tmp_path).await;
                if attempt >= MAX_RETRIES {
                    return Err(e);
                }
                attempt += 1;
                tokio::time::sleep(Duration::from_millis(200 * u64::from(attempt))).await;
            }
        }
    }

    spawn_prune(cache_path);
    path_to_string(&file_path)
}

async fn prune_cache(cache_dir: &Path) {
    let mut entries = match fs::read_dir(cache_dir).await {
        Ok(e) => e,
        Err(_) => return,
    };

    let now = SystemTime::now();
    let mut files = Vec::new();
    let mut total_size: u64 = 0;

    while let Ok(Some(entry)) = entries.next_entry().await {
        let path = entry.path();
        let Ok(meta) = entry.metadata().await else {
            continue;
        };
        if !meta.is_file() {
            continue;
        }
        let mtime = meta.modified().unwrap_or(SystemTime::UNIX_EPOCH);

        if path.extension().is_some_and(|ext| ext == "tmp") {
            // Fresh .tmp = active download, leave it alone.
            // Old .tmp = orphan from a crash/kill, remove it.
            let age = now.duration_since(mtime).unwrap_or(Duration::ZERO);
            if age > STALE_TMP_AGE {
                let _ = fs::remove_file(&path).await;
            }
            continue;
        }

        let size = meta.len();
        total_size += size;
        files.push((path, size, mtime));
    }

    if total_size > MAX_CACHE_BYTES {
        // Oldest mtime first; hits bump mtime, so this is LRU.
        files.sort_by_key(|&(_, _, mtime)| mtime);

        for (path, size, _) in files {
            if total_size <= TARGET_CACHE_BYTES {
                break;
            }
            if fs::remove_file(&path).await.is_ok() {
                total_size = total_size.saturating_sub(size);
            }
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_hash_key_deterministic() {
        assert_eq!(hash_key(""), "d41d8cd98f00b204e9800998ecf8427e");
        assert_eq!(hash_key("hello world"), "5eb63bbbe01eeed093cb22bb8f5acdc3");
    }

    #[test]
    fn test_get_extension_parsing() {
        assert_eq!(get_extension("https://cdn.example.com/wall.png"), "png");
        assert_eq!(get_extension("https://cdn.example.com/wall.PNG"), "png");
        assert_eq!(
            get_extension("https://cdn.example.com/wall.webp?v=1"),
            "webp"
        );
        assert_eq!(
            get_extension("https://cdn.example.com/wall.png#frag"),
            "png"
        );
        assert_eq!(get_extension("https://cdn.example.com/wall.jpg"), "jpg");
        assert_eq!(get_extension("https://cdn.example.com/wall.jpeg"), "jpg");
        assert_eq!(get_extension("https://cdn.example.com/no_ext"), "jpg");
    }

    #[test]
    fn test_unique_tmp_sequence() {
        let seq1 = TMP_SEQ.fetch_add(1, Ordering::Relaxed);
        let seq2 = TMP_SEQ.fetch_add(1, Ordering::Relaxed);
        assert_ne!(seq1, seq2);
    }

    #[test]
    fn test_status_retryable_classification() {
        assert!(status_retryable(StatusCode::INTERNAL_SERVER_ERROR));
        assert!(status_retryable(StatusCode::BAD_GATEWAY));
        assert!(status_retryable(StatusCode::SERVICE_UNAVAILABLE));
        assert!(status_retryable(StatusCode::TOO_MANY_REQUESTS));
        assert!(status_retryable(StatusCode::REQUEST_TIMEOUT));
        assert!(!status_retryable(StatusCode::NOT_FOUND));
        assert!(!status_retryable(StatusCode::FORBIDDEN));
        assert!(!status_retryable(StatusCode::OK));
    }

    #[test]
    fn test_path_to_string_ok() {
        assert_eq!(
            path_to_string(Path::new("/tmp/a.png")).unwrap(),
            "/tmp/a.png"
        );
    }
}
