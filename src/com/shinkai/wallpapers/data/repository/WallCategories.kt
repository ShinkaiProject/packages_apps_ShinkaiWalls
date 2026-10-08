package com.shinkai.wallpapers.data.repository

import android.content.Context
import com.shinkai.wallpapers.R
import com.shinkai.wallpapers.data.model.WallCategory
import com.shinkai.wallpapers.data.model.WallCategoryDefinition
import com.shinkai.wallpapers.data.model.Wallpaper

object WallCategories {

  const val DEFAULT_HOME_CATEGORY_ID = "pixel"

  val DEFINITIONS: List<WallCategoryDefinition> =
      listOf(
          WallCategoryDefinition("pixel", R.string.category_pixel_walls, "Pixel-Walls"),
          WallCategoryDefinition("yaemiko", R.string.category_yaemiko_walls, "Yaemiko-Walls"),
          WallCategoryDefinition("lumina", R.string.category_lumina_walls, "Lumina-Walls"),
          WallCategoryDefinition("neon_orbs", R.string.category_neon_orbs_walls, "Neon-Orbs-Walls"),
          WallCategoryDefinition("elysia", R.string.category_elysia_walls, "Elysia-Walls"),
          WallCategoryDefinition("mavuika", R.string.category_mavuika_walls, "Mavuika-Walls"),
      )

  private val KNOWN_LOCALIZED_TITLES: Map<String, Int> =
      mapOf(
          "pixel" to R.string.category_pixel_walls,
          "pixel_walls" to R.string.category_pixel_walls,
          "yaemiko" to R.string.category_yaemiko_walls,
          "yaemiko_walls" to R.string.category_yaemiko_walls,
          "lumina" to R.string.category_lumina_walls,
          "lumina_walls" to R.string.category_lumina_walls,
          "neon_orbs" to R.string.category_neon_orbs_walls,
          "neon_orbs_walls" to R.string.category_neon_orbs_walls,
          "elysia" to R.string.category_elysia_walls,
          "elysia_walls" to R.string.category_elysia_walls,
          "mavuika" to R.string.category_mavuika_walls,
          "mavuika_walls" to R.string.category_mavuika_walls,
      )

  /**
   * Dynamically resolves categories from the provided wallpapers list.
   * If new categories are added in the JSON, they will be discovered and formatted automatically.
   */
  fun resolve(context: Context? = null, wallpapers: List<Wallpaper>): List<WallCategory> {
    if (wallpapers.isEmpty()) return emptyList()

    val grouped = linkedMapOf<String, MutableList<Wallpaper>>()
    for (wallpaper in wallpapers) {
      val rawCat = wallpaper.category ?: wallpaper.folder ?: "Other"
      val trimmed = rawCat.trim()
      if (trimmed.isNotEmpty()) {
        grouped.getOrPut(trimmed) { mutableListOf() }.add(wallpaper)
      }
    }

    return grouped.map { (catKey, matching) ->
      val normalizedId = catKey.lowercase().replace("-", "_").replace(" ", "_")
      val titleRes = KNOWN_LOCALIZED_TITLES[normalizedId]
      val title =
          if (context != null && titleRes != null) {
            context.getString(titleRes)
          } else {
            formatDynamicCategoryTitle(catKey)
          }
      WallCategory(
          id = normalizedId,
          title = title,
          wallpapers = matching,
      )
    }
  }

  fun wallpapersIn(wallpapers: List<Wallpaper>, categoryId: String): List<Wallpaper> {
    val normTarget = categoryId.lowercase().replace("-", "_").replace(" ", "_")
    return wallpapers.filter {
      val cat = it.category ?: it.folder ?: ""
      val normCat = cat.lowercase().replace("-", "_").replace(" ", "_")
      normCat == normTarget || normCat.startsWith(normTarget) || normTarget.startsWith(normCat)
    }
  }

  private fun formatDynamicCategoryTitle(raw: String): String {
    return raw.replace("-", " ")
        .replace("_", " ")
        .split(" ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
  }
}
