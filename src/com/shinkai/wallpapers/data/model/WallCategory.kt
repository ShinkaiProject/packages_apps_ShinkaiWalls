package com.shinkai.wallpapers.data.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

data class WallCategoryDefinition(
    val id: String,
    @param:StringRes val titleRes: Int,
    val folder: String,
) {
  fun matches(wallpaper: Wallpaper): Boolean {
    val cat = wallpaper.category ?: wallpaper.folder ?: return false
    if (cat.equals(folder, ignoreCase = true) || cat.equals(id, ignoreCase = true)) {
      return true
    }
    val strippedCat = cat.filter { it.isLetterOrDigit() }
    val strippedFolder = folder.filter { it.isLetterOrDigit() }
    val strippedId = id.filter { it.isLetterOrDigit() }
    return strippedCat.equals(strippedFolder, ignoreCase = true) ||
           strippedCat.equals(strippedId, ignoreCase = true)
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
