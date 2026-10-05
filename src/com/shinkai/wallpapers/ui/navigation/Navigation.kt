package com.shinkai.wallpapers.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Home
import androidx.compose.ui.graphics.vector.ImageVector
import com.shinkai.wallpapers.R
import com.shinkai.wallpapers.data.model.WallCategory
import com.shinkai.wallpapers.data.model.Wallpaper

enum class TopLevelDestination(
    val icon: ImageVector,
    @param:StringRes val titleRes: Int,
) {
  HOME(Icons.Rounded.Home, R.string.nav_home),
  WALLS(Icons.Rounded.Category, R.string.nav_walls),
}

sealed interface Screen {
  data object Home : Screen

  data object Walls : Screen

  data class CategoryDetail(val category: WallCategory) : Screen

  data class Preview(val wallpaper: Wallpaper) : Screen
}
