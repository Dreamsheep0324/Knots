package com.tang.prm.data.repository

import com.tang.prm.domain.model.UpdateResult
import com.tang.prm.domain.repository.UpdateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class UpdateRepositoryImpl @Inject constructor(
    @Named("ai") private val okHttpClient: OkHttpClient
) : UpdateRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun checkForUpdate(currentVersion: String): UpdateResult =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(GITHUB_RELEASE_URL)
                    .header("Accept", "application/vnd.github+json")
                    .build()

                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext UpdateResult.Error("HTTP ${response.code}")
                    }

                    val body = response.body?.string()
                        ?: return@withContext UpdateResult.Error("空响应")

                    val jsonObj = json.parseToJsonElement(body).jsonObject
                    val tagName = jsonObj["tag_name"]?.jsonPrimitive?.content
                        ?.removePrefix("v") ?: ""
                    val htmlUrl = jsonObj["html_url"]?.jsonPrimitive?.content ?: ""

                    if (tagName.isNotEmpty() && isNewerVersion(tagName, currentVersion)) {
                        UpdateResult.HasUpdate(tagName, htmlUrl)
                    } else {
                        UpdateResult.NoUpdate
                    }
                }
            } catch (e: Exception) {
                UpdateResult.Error(e.message ?: "未知错误")
            }
        }

    private fun isNewerVersion(remote: String, current: String): Boolean {
        val remoteParts = remote.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }
        val maxLen = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }

    companion object {
        private const val GITHUB_RELEASE_URL =
            "https://api.github.com/repos/dreamsheep0324/Knots/releases/latest"
    }
}
