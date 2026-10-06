package com.shinkai.wallpapers.data.repository

import com.shinkai.wallpapers.data.model.GitHubContributor
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

object ContributorRepository {
    private const val GITHUB_API_URL =
        "https://api.github.com/repos/ShinkaiProject/packages_apps_ShinkaiWalls/contributors"

    suspend fun getContributors(): List<GitHubContributor> = withContext(Dispatchers.IO) {
        try {
            val url = URL(GITHUB_API_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8_000
                readTimeout = 8_000
                setRequestProperty("User-Agent", "ShinkaiWalls-App")
                setRequestProperty("Accept", "application/vnd.github.v3+json")
            }

            if (connection.responseCode in 200..299) {
                val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(jsonString)
                val list = mutableListOf<GitHubContributor>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        GitHubContributor(
                            login = obj.optString("login", ""),
                            avatarUrl = obj.optString("avatar_url", ""),
                            htmlUrl = obj.optString("html_url", ""),
                            contributions = obj.optInt("contributions", 0),
                        )
                    )
                }
                list
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
