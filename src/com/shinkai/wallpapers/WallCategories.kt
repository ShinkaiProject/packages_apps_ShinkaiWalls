package com.shinkai.wallpapers

import android.content.Context

/**
 * Single source of truth for the wall categories of the app.
 *
 * The categories mirror the folders of the wallpapers assets (`images/<Folder>`), so a category
 * only has to be declared once here to appear on the Walls screen, to become browsable and to
 * receive its own filtered wallpaper grid. Wallpapers stored outside of [definitions] are not
 * categorised, and a category without any wallpaper is dropped rather than leading to a dead end.
 */
object WallCategories {

    /** Category whose wallpapers the Home screen lists by default. */
    const val DEFAULT_HOME_CATEGORY_ID = "pixel-walls"

    val definitions: List<WallCategoryDefinition> = listOf(
        WallCategoryDefinition(
            "yaemiko-walls",
            R.string.category_yaemiko_walls,
            "Yaemiko-Walls"
        ),
        WallCategoryDefinition(
            DEFAULT_HOME_CATEGORY_ID,
            R.string.category_pixel_walls,
            "Pixel-Walls"
        ),
        WallCategoryDefinition(
            "lumina-walls",
            R.string.category_lumina_walls,
            "Lumina-Walls"
        ),
        WallCategoryDefinition(
            "Neon-Orbs-walls",
            R.string.category_neon_orbs_walls,
            "Neon-Orbs-Walls"
        ),
        WallCategoryDefinition(
            "elysia-walls",
            R.string.category_elysia_walls,
            "Elysia-Walls"
        ),
        WallCategoryDefinition(
            "mavuika-walls",
            R.string.category_mavuika_walls,
            "Mavuika-Walls"
        )
    )

    /**
     * Groups [wallpapers] into the categories of [definitions], keeping the declaration order.
     */
    fun resolve(context: Context, wallpapers: List<Wallpaper>): List<WallCategory> {
        if (wallpapers.isEmpty()) return emptyList()

        val buckets = LinkedHashMap<String, MutableList<Wallpaper>>()
        for (wallpaper in wallpapers) {
            val definition = definitions.firstOrNull { it.matches(wallpaper) } ?: continue
            buckets.getOrPut(definition.id) { mutableListOf() }.add(wallpaper)
        }

        return definitions.mapNotNull { definition ->
            val items = buckets[definition.id]
            if (items.isNullOrEmpty()) return@mapNotNull null
            WallCategory(
                id = definition.id,
                title = context.getString(definition.titleRes),
                wallpapers = items
            )
        }
    }

    /** Wallpapers stored in the folder of [categoryId], in the order the repository returned them. */
    fun wallpapersIn(wallpapers: List<Wallpaper>, categoryId: String): List<Wallpaper> {
        val definition = definitions.firstOrNull { it.id == categoryId } ?: return emptyList()
        return wallpapers.filter { definition.matches(it) }
    }
}
