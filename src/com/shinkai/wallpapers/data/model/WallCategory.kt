package com.shinkai.wallpapers.data.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

data class WallCategoryDefinition(
    val id: String,
    @param:StringRes val titleRes: Int,
    val folder: String,
) {
  fun matches(wallpaper: Wallpaper): Boolean {
    val folder = wallpaper.folder ?: return false
    return folder.equals(this.folder, ignoreCase = true)
  }
}

@Immutable
data class WallCategory(
    val id: String,
    val title: String,
    val wallpapers: List<Wallpaper>,
) {
  val count: Int
    get() = wallpapers.size

  val previewUrl: String?
    get() = wallpapers.firstOrNull()?.assetPath
}
