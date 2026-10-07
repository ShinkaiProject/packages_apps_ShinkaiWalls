mod core;

use crate::core::{ShinkaiError, block_on, download_image, fetch_wallpapers, hash_key};

use jni::errors::ThrowRuntimeExAndDefault;
use jni::objects::{JClass, JString};
use jni::sys::jstring;
use jni::{Env, EnvUnowned};

/// Safely executes native operations across the FFI boundary using JNI `EnvUnowned`.
/// Any Rust `ShinkaiError` is translated into a Java `RuntimeException` via `ThrowRuntimeExAndDefault`.
fn safe_jni_call<'local, F>(mut env_unowned: EnvUnowned<'local>, f: F) -> jstring
where
    F: FnOnce(&mut Env<'local>) -> Result<String, ShinkaiError>,
{
    let outcome = env_unowned.with_env(|env| -> Result<jstring, ShinkaiError> {
        let result_str = f(env)?;
        let jstr = JString::from_str(env, &result_str)?;
        Ok(jstr.into_raw())
    });

    outcome.resolve::<ThrowRuntimeExAndDefault>()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_shinkai_wallpapers_NativeLib_fetchWallpapersNative<'local>(
    env: EnvUnowned<'local>,
    _class: JClass<'local>,
    url: JString<'local>,
) -> jstring {
    safe_jni_call(env, |env_mut| {
        let url_str = url.try_to_string(env_mut)?;
        block_on(fetch_wallpapers(&url_str))?
    })
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_shinkai_wallpapers_NativeLib_downloadImageNative<'local>(
    env: EnvUnowned<'local>,
    _class: JClass<'local>,
    url: JString<'local>,
    cache_dir: JString<'local>,
) -> jstring {
    safe_jni_call(env, |env_mut| {
        let url_str = url.try_to_string(env_mut)?;
        let cache_str = cache_dir.try_to_string(env_mut)?;
        block_on(download_image(&url_str, &cache_str))?
    })
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_shinkai_wallpapers_NativeLib_hashKeyNative<'local>(
    env: EnvUnowned<'local>,
    _class: JClass<'local>,
    key: JString<'local>,
) -> jstring {
    safe_jni_call(env, |env_mut| {
        let key_str = key.try_to_string(env_mut)?;
        Ok(hash_key(&key_str))
    })
}
