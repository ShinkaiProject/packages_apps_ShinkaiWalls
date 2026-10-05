package com.shinkai.wallpapers.data.model

import androidx.compose.runtime.Immutable

/**
 * One wallpaper of the library.
 */
@Immutable
data class Wallpaper(
    val name: String,
    val assetPath: String,
    val fullUrl: String,
    val category: String? = null
) {
    /** Folder the image lives in, e.g. `Pixel-Walls`, or null when the URL carries none. */
    val folder: String? = ASSET_FOLDER.find(assetPath)?.groupValues?.get(1)

    private companion object {
        val ASSET_FOLDER = Regex("""/images/([^/?#]+)/""")
    }
}
