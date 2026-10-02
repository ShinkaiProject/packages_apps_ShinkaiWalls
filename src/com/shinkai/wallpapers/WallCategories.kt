package com.shinkai.wallpapers

import android.content.Context

/**
 * Single source of truth for the wall categories of the app.
 *
 * Category data lives here and never inside a view: adding an entry to [definitions] (plus
 * its label string) is enough to make a new category appear on the Walls screen, to become
 * browsable and to receive its own filtered wallpaper grid.
 *
 * Wallpapers that are not covered by [definitions] yet are grouped into a category derived
 * from their own name, so a newly published wallpaper is never invisible and no wallpaper is
 * ever lost. Categories without any wallpaper are dropped instead of leading to a dead end.
 */
object WallCategories {

    val definitions: List<WallCategoryDefinition> = listOf(
        WallCategoryDefinition(
            "emerald",
            R.string.category_emerald,
            listOf("emerald", "jade", "malachite")
        ),
        WallCategoryDefinition(
            "chalcedony",
            R.string.category_chalcedony,
            listOf("chalcedony", "agate", "onyx")
        ),
        WallCategoryDefinition(
            "amoled",
            R.string.category_amoled,
            listOf("amoled", "oled", "deep black", "pure black")
        ),
        WallCategoryDefinition(
            "nature",
            R.string.category_nature,
            listOf("nature", "forest", "mountain", "sea", "sunset", "flower", "tree", "water", "sky")
        ),
        WallCategoryDefinition(
            "abstract",
            R.string.category_abstract,
            listOf("abstract", "geometric", "gradient", "pattern", "shape")
        ),
        WallCategoryDefinition(
            "anime",
            R.string.category_anime,
            listOf("anime", "kuromi", "cinnamoroll", "sanrio", "manga", "waifu")
        ),
        WallCategoryDefinition(
            "minimal",
            R.string.category_minimal,
            listOf("minimal", "monochrome", "plain", "simple", "clean")
        ),
        WallCategoryDefinition(
            "custom",
            R.string.category_custom,
            listOf("custom", "myself", "personal", "user", "upload")
        )
    )

    private val SEPARATOR = Regex("[^a-z0-9]+")

    private val FILLER_WORDS = setOf(
        "wall", "wallpaper", "wallpapers", "shinkai", "hd", "fhd", "qhd", "4k", "official"
    )

    private val VARIANT_WORDS = setOf(
        "light", "dark", "soft", "vivid", "blue", "red", "green", "white", "black",
        "night", "day", "mode", "theme", "version", "edition"
    )

    /**
     * Groups [wallpapers] into the categories of [definitions], keeping the declaration order
     * and appending the derived categories afterwards.
     */
    fun resolve(context: Context, wallpapers: List<Wallpaper>): List<WallCategory> {
        if (wallpapers.isEmpty()) return emptyList()

        val predefinedTitles = definitions.associate { it.id to context.getString(it.titleRes) }
        val derivedIds = LinkedHashMap<String, String>()
        val titles = HashMap<String, String>()
        val buckets = LinkedHashMap<String, MutableList<Wallpaper>>()

        for (wallpaper in wallpapers) {
            val definition = definitions.firstOrNull { it.matches(wallpaper) }
            val id: String

            if (definition != null) {
                id = definition.id
                titles[id] = predefinedTitles.getValue(id)
            } else {
                val title = deriveTitle(wallpaper.name)
                val key = title.lowercase()
                val known = derivedIds[key]
                if (known != null) {
                    id = known
                } else {
                    id = "auto_${key.replace(SEPARATOR, "_").trim('_')}"
                    derivedIds[key] = id
                    titles[id] = title
                }
            }

            buckets.getOrPut(id) { mutableListOf() }.add(wallpaper)
        }

        return buckets.map { (id, items) ->
            WallCategory(id = id, title = titles.getValue(id), wallpapers = items)
        }
    }

    /**
     * Builds a readable category name out of a wallpaper name by dropping the generic and the
     * light/dark style words, e.g. "Emerald Light Shinkai wallpaper" becomes "Emerald".
     */
    private fun deriveTitle(name: String): String {
        val tokens = name.lowercase().split(SEPARATOR).filter { it.isNotEmpty() }
        val meaningful = tokens.filter { it !in FILLER_WORDS && it !in VARIANT_WORDS }
        val kept = (meaningful.ifEmpty { tokens }).take(3)
        return kept.joinToString(" ") { token -> token.replaceFirstChar { it.uppercase() } }
            .ifEmpty { name }
    }
}
