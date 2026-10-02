package com.shinkai.wallpapers

import androidx.annotation.StringRes

/**
 * Declarative description of a wall category.
 *
 * A definition only carries data, never UI, so categories can be added later by appending
 * an entry to [WallCategories] without touching the navigation or the screens.
 */
data class WallCategoryDefinition(
    val id: String,
    @param:StringRes val titleRes: Int,
    val keywords: List<String>
) {
    fun matches(wallpaper: Wallpaper): Boolean {
        if (wallpaper.category != null && wallpaper.category.equals(id, ignoreCase = true)) {
            return true
        }
        val name = wallpaper.name.lowercase()
        return keywords.any { name.contains(it) }
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
