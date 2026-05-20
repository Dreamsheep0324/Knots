package com.tang.prm.data.repository

import com.google.gson.Gson
import com.tang.prm.data.remote.ChatMessage
import com.tang.prm.data.remote.ChatRequest
import com.tang.prm.data.remote.ChatStreamResponse
import com.tang.prm.domain.repository.AiRepository
import com.tang.prm.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRepositoryImpl @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val settingsRepository: SettingsRepository
) : AiRepository {

    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    override fun streamChat(systemPrompt: String, userPrompt: String): Flow<String> = callbackFlow {
        val apiKey = settingsRepository.getAiApiKey()
        val baseUrl = settingsRepository.getAiBaseUrl()
        val model = settingsRepository.getAiModel()

        if (apiKey.isBlank()) {
            trySend("ERROR:NO_API_KEY")
            close()
            return@callbackFlow
        }

        val request = ChatRequest(
            model = model,
            messages = listOf(
                ChatMessage(role = "system", content = systemPrompt),
                ChatMessage(role = "user", content = userPrompt)
            )
        )

        val jsonBody = gson.toJson(request)
        val httpRequest = Request.Builder()
            .url("$baseUrl/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toRequestBody(jsonMediaType))
            .build()

        try {
            val response = okHttpClient.newCall(httpRequest).execute()
            if (!response.isSuccessful) {
                val errorCode = response.code
                val errorMsg = when (errorCode) {
                    401 -> "ERROR:INVALID_API_KEY"
                    429 -> "ERROR:RATE_LIMIT"
                    500, 502, 503 -> "ERROR:SERVER_ERROR"
                    else -> "ERROR:HTTP_$errorCode"
                }
                trySend(errorMsg)
                close()
                return@callbackFlow
            }

            val source = response.body?.source() ?: run {
                trySend("ERROR:EMPTY_RESPONSE")
                close()
                return@callbackFlow
            }

            while (!source.exhausted()) {
                val line = source.readUtf8Line()
                if (line == null) continue
                if (line.startsWith("data: ")) {
                    val data = line.removePrefix("data: ").trim()
                    if (data == "[DONE]") break
                    try {
                        val streamResp = gson.fromJson(data, ChatStreamResponse::class.java)
                        val content = streamResp?.choices?.firstOrNull()?.delta?.content
                        if (content != null) {
                            trySend(content)
                        }
                    } catch (_: Exception) {
                        continue
                    }
                }
            }
        } catch (e: Exception) {
            trySend("ERROR:NETWORK")
        }

        close()
        awaitClose()
    }.flowOn(Dispatchers.IO)

    override suspend fun testConnection(): Result<String> {
        val apiKey = settingsRepository.getAiApiKey()
        if (apiKey.isBlank()) return Result.failure(Exception("API密钥未配置"))

        val baseUrl = settingsRepository.getAiBaseUrl()
        val model = settingsRepository.getAiModel()

        val request = ChatRequest(
            model = model,
            messages = listOf(ChatMessage(role = "user", content = "Hi")),
            stream = false,
            maxTokens = 5
        )

        val jsonBody = gson.toJson(request)
        val httpRequest = Request.Builder()
            .url("$baseUrl/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toRequestBody(jsonMediaType))
            .build()

        return try {
            val response = okHttpClient.newCall(httpRequest).execute()
            when {
                response.isSuccessful -> Result.success("连接成功，模型: $model")
                response.code == 401 -> Result.failure(Exception("API密钥无效"))
                response.code == 429 -> Result.failure(Exception("请求过于频繁，请稍后重试"))
                response.code >= 500 -> Result.failure(Exception("服务器错误(${response.code})"))
                else -> Result.failure(Exception("请求失败(${response.code})"))
            }
        } catch (e: java.net.SocketTimeoutException) {
            Result.failure(Exception("连接超时，请检查网络或API地址"))
        } catch (e: java.net.UnknownHostException) {
            Result.failure(Exception("无法解析主机名，请检查API地址"))
        } catch (e: java.net.ConnectException) {
            Result.failure(Exception("连接被拒绝，请检查API地址"))
        } catch (e: Exception) {
            Result.failure(Exception("网络错误: ${e.message ?: e.javaClass.simpleName}"))
        }
    }
}
