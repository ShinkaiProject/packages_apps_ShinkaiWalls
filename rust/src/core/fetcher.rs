use crate::core::client::http_client;
use crate::core::error::ShinkaiError;

/// Asynchronously fetches wallpapers JSON catalog directly without redundant serde roundtrips.
pub async fn fetch_wallpapers(url: &str) -> Result<String, ShinkaiError> {
    let client = http_client()?;
    let response = client.get(url).send().await?.error_for_status()?;
    let text = response.text().await?;
    Ok(text)
}
