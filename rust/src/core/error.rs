use thiserror::Error;

#[derive(Debug, Error)]
pub enum ShinkaiError {
    #[error("Network error: {0}")]
    Network(#[from] reqwest::Error),

    #[error("I/O error: {0}")]
    Io(#[from] std::io::Error),

    #[error("JNI error: {0}")]
    Jni(#[from] jni::errors::Error),

    #[error("Invalid path: {0}")]
    InvalidPath(String),
}
