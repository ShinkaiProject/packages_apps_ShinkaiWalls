package com.shinkai.wallpapers.data.repository

import android.content.Context
import com.shinkai.wallpapers.R
import com.shinkai.wallpapers.data.model.WallCategory
import com.shinkai.wallpapers.data.model.WallCategoryDefinition
import com.shinkai.wallpapers.data.model.Wallpaper

object WallCategories {

    const val DEFAULT_HOME_CATEGORY_ID = "pixel"

    private val DEFINITIONS: List<WallCategoryDefinition> = listOf(
        WallCategoryDefinition("pixel", R.string.category_pixel_walls, "Pixel-Walls"),
        WallCategoryDefinition("yaemiko", R.string.category_yaemiko_walls, "Yaemiko-Walls"),
        WallCategoryDefinition("lumina", R.string.category_lumina_walls, "Lumina-Walls"),
        WallCategoryDefinition("neon_orbs", R.string.category_neon_orbs_walls, "Neon-Orbs-Walls"),
        WallCategoryDefinition("elysia", R.string.category_elysia_walls, "Elysia-Walls"),
        WallCategoryDefinition("mavuika", R.string.category_mavuika_walls, "Mavuika-Walls")
    )

    fun resolve(context: Context, wallpapers: List<Wallpaper>): List<WallCategory> =
        DEFINITIONS.mapNotNull { def ->
            val matching = wallpapers.filter { def.matches(it) }
            if (matching.isEmpty()) null
            else WallCategory(
                id = def.id,
                title = context.getString(def.titleRes),
                wallpapers = matching
            )
        }

    fun wallpapersIn(wallpapers: List<Wallpaper>, categoryId: String): List<Wallpaper> {
        val def = DEFINITIONS.firstOrNull { it.id == categoryId } ?: return emptyList()
        return wallpapers.filter { def.matches(it) }
    }
}
