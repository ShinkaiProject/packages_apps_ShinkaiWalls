use crate::core::error::ShinkaiError;
use std::future::Future;
use std::sync::OnceLock;
use std::time::Duration;

static RUNTIME: OnceLock<tokio::runtime::Runtime> = OnceLock::new();
static HTTP_CLIENT: OnceLock<reqwest::Client> = OnceLock::new();

/// Returns the shared multi-threaded Tokio runtime for async tasks without panic.
pub fn runtime() -> Result<&'static tokio::runtime::Runtime, ShinkaiError> {
    if let Some(rt) = RUNTIME.get() {
        return Ok(rt);
    }
    let rt = tokio::runtime::Builder::new_multi_thread()
        .worker_threads(4)
        .enable_all()
        .thread_name("shinkai-worker")
        .build()
        .map_err(ShinkaiError::Io)?;
    let _ = RUNTIME.set(rt);
    RUNTIME
        .get()
        .ok_or_else(|| ShinkaiError::Io(std::io::Error::other("Failed to initialize runtime")))
}

/// Runs a future to completion on the Tokio runtime.
pub fn block_on<F: Future>(future: F) -> Result<F::Output, ShinkaiError> {
    let rt = runtime()?;
    Ok(rt.block_on(future))
}

/// Returns the shared non-blocking reqwest Client with connection pooling and compression.
pub fn http_client() -> Result<&'static reqwest::Client, ShinkaiError> {
    if let Some(client) = HTTP_CLIENT.get() {
        return Ok(client);
    }
    let client = reqwest::Client::builder()
        .connect_timeout(Duration::from_secs(6))
        .timeout(Duration::from_secs(15))
        .tcp_keepalive(Duration::from_secs(60))
        .pool_idle_timeout(Duration::from_secs(90))
        .pool_max_idle_per_host(10)
        .gzip(true)
        .brotli(true)
        .build()
        .map_err(ShinkaiError::Network)?;
    let _ = HTTP_CLIENT.set(client);
    HTTP_CLIENT
        .get()
        .ok_or_else(|| ShinkaiError::Io(std::io::Error::other("Failed to initialize HTTP client")))
}
