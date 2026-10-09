package com.shinkai.wallpapers.data.repository

import com.shinkai.wallpapers.NativeLib
import com.shinkai.wallpapers.data.model.Wallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray

object WallpaperRepository {

  private const val JSON_URL =  "https://raw.githubusercontent.com/ShinkaiProject/shinkai-walls-assets/heptakaideka/wallpapers.json"

  private val mutex = Mutex()

  @Volatile private var cache: List<Wallpaper> = emptyList()

  suspend fun load(forceRefresh: Boolean = false): List<Wallpaper> = mutex.withLock {
    if (!forceRefresh && cache.isNotEmpty()) {
      return@withLock cache
    }
    try {
      val wallpapers = withContext(Dispatchers.IO) { fetch() }
      if (wallpapers.isNotEmpty()) {
        cache = wallpapers
      }
      wallpapers
    } catch (e: Exception) {
      if (cache.isNotEmpty()) {
        cache
      } else {
        throw e
      }
    }
  }

  private fun fetch(): List<Wallpaper> {
    val rawJson = NativeLib.fetchWallpapers(JSON_URL)
    val array = JSONArray(rawJson)
    return (0 until array.length()).map { index ->
      val item = array.getJSONObject(index)
      val name = item.getString("name")
      val thumbnailUrl = item.getString("thumbnail_url")
      var fullUrl = item.getString("full_url")

      // Fix known case-sensitivity bug in remote asset repository (e.g. Violet_halo -> violet_halo)
      if (fullUrl.contains("Violet_halo_shinkai.png")) {
        fullUrl = fullUrl.replace("Violet_halo_shinkai.png", "violet_halo_shinkai.png")
      }

      Wallpaper(
          name = name,
          assetPath = thumbnailUrl,
          fullUrl = fullUrl,
          category = if (item.has("category")) item.optString("category").nullIfBlank() else null,
      )
    }
  }

  private fun String?.nullIfBlank(): String? = if (isNullOrBlank()) null else this
}
