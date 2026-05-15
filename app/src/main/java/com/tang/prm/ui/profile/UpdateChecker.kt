package com.tang.prm.ui.profile

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

sealed class UpdateResult {
    data class HasUpdate(val latestVersion: String, val releaseUrl: String) : UpdateResult()
    data object NoUpdate : UpdateResult()
    data class Error(val message: String) : UpdateResult()
}

suspend fun checkForUpdate(currentVersion: String): UpdateResult {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.github.com/repos/dreamsheep0324/Knots/releases/latest")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.instanceFollowRedirects = true

            val responseCode = connection.responseCode
            if (responseCode != 200) {
                return@withContext UpdateResult.Error("HTTP $responseCode")
            }

            val response = connection.inputStream.bufferedReader().readText()
            val json = JSONObject(response)

            val tagName = json.optString("tag_name", "").removePrefix("v")
            val htmlUrl = json.optString("html_url", "")

            if (tagName.isNotEmpty() && tagName != currentVersion) {
                UpdateResult.HasUpdate(tagName, htmlUrl)
            } else {
                UpdateResult.NoUpdate
            }
        } catch (e: Exception) {
            UpdateResult.Error(e.message ?: "未知错误")
        }
    }
}
