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

    private val FallbackContributors =
        listOf(
            GitHubContributor(
                login = "Pavelc4",
                avatarUrl = "https://avatars.githubusercontent.com/u/101870119?v=4",
                htmlUrl = "https://github.com/Pavelc4",
                contributions = 16,
            ),
            GitHubContributor(
                login = "ShinkaiProject",
                avatarUrl = "https://avatars.githubusercontent.com/u/144186595?v=4",
                htmlUrl = "https://github.com/ShinkaiProject",
                contributions = 18,
            ),
        )

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
                    val login = obj.optString("login", "")
                    val htmlUrl = obj.optString("html_url", "").ifBlank { "https://github.com/$login" }
                    list.add(
                        GitHubContributor(
                            login = login,
                            avatarUrl = obj.optString("avatar_url", ""),
                            htmlUrl = htmlUrl,
                            contributions = obj.optInt("contributions", 0),
                        )
                    )
                }
                if (list.isNotEmpty()) list else FallbackContributors
            } else {
                FallbackContributors
            }
        } catch (_: Exception) {
            FallbackContributors
        }
    }
}
