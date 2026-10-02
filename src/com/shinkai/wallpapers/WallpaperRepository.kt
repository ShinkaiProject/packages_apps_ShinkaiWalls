package com.shinkai.wallpapers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray

/**
 * Single entry point to the wallpaper list.
 *
 * The list is shared by every screen that browses wallpapers, so opening a category reuses the
 * data that Home already downloaded instead of triggering another network round trip.
 */
object WallpaperRepository {

    private const val JSON_URL =
        "https://raw.githubusercontent.com/ShinkaiProject/shinkai-walls-assets/heptakaideka/wallpapers.json"

    private val mutex = Mutex()

    @Volatile
    private var cache: List<Wallpaper> = emptyList()

    suspend fun load(forceRefresh: Boolean = false): List<Wallpaper> = mutex.withLock {
        if (!forceRefresh && cache.isNotEmpty()) {
            return@withLock cache
        }
        val wallpapers = withContext(Dispatchers.IO) { fetch() }
        cache = wallpapers
        wallpapers
    }

    private fun fetch(): List<Wallpaper> {
        val array = JSONArray(NativeLib.fetchWallpapers(JSON_URL))
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            Wallpaper(
                name = item.getString("name"),
                assetPath = item.getString("thumbnail_url"),
                fullUrl = item.getString("full_url"),
                category = if (item.has("category")) item.optString("category").nullIfBlank() else null
            )
        }
    }

    private fun String?.nullIfBlank(): String? = if (isNullOrBlank()) null else this
}
