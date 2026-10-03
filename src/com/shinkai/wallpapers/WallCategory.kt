package com.shinkai.wallpapers

import androidx.annotation.StringRes

/**
 * Declarative description of a wall category.
 *
 * A category is bound to one folder of the wallpapers assets rather than to keywords, so what the
 * Walls screen shows always matches what is actually published: a wallpaper belongs to a category
 * because of the folder its image is stored in, never because of how its name happens to read.
 */
data class WallCategoryDefinition(
    val id: String,
    @param:StringRes val titleRes: Int,
    val folder: String
) {
    fun matches(wallpaper: Wallpaper): Boolean {
        val folder = wallpaper.folder ?: return false
        return folder.equals(this.folder, ignoreCase = true)
    }
}

/**
 * A category resolved against the wallpapers that are currently loaded.
 *
 * [previewUrl] points at the thumbnail of the first wallpaper of the category, so category
 * cards always show real artwork and never need hardcoded image URLs.
 */
data class WallCategory(
    val id: String,
    val title: String,
    val wallpapers: List<Wallpaper>
) {
    val count: Int get() = wallpapers.size

    val previewUrl: String? get() = wallpapers.firstOrNull()?.assetPath
}