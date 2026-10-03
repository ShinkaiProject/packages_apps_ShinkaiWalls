package com.shinkai.wallpapers

/**
 * One wallpaper of the library.
 *
 * The asset URLs point straight at the folders of the wallpapers repository
 * (`.../images/<Folder>/<file>`), so the folder the image lives in is the natural grouping of the
 * library: it is decided by the assets themselves instead of by guessing from the wallpaper name.
 */
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