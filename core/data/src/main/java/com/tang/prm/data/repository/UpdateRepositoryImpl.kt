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
    // A-4 修复：检查更新是普通短请求，不应复用 AI 流式专用 client（readTimeout 300s 过长且无重试）。
    // 改用 webdav client：超时配置合理（readTimeout 120s）且附加 RetryInterceptor 自动重试 GET 请求。
    @Named("webdav") private val okHttpClient: OkHttpClient
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
