package com.tang.prm.data.repository

import android.util.Log
import com.tang.prm.data.remote.ChatMessage
import com.tang.prm.data.remote.ChatRequest
import com.tang.prm.data.remote.ChatStreamResponse
import com.tang.prm.domain.repository.AiRepository
import com.tang.prm.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class AiRepositoryImpl @Inject constructor(
    @Named("ai") private val okHttpClient: OkHttpClient,
    private val settingsRepository: SettingsRepository
) : AiRepository {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun buildChatHttpRequest(
        apiKey: String, baseUrl: String, model: String,
        systemPrompt: String, userPrompt: String,
        stream: Boolean = true,
        maxTokens: Int = 2048
    ): Request {
        val request = ChatRequest(
            model = model,
            messages = listOf(
                ChatMessage(role = "system", content = systemPrompt),
                ChatMessage(role = "user", content = userPrompt)
            ),
            stream = stream,
            maxTokens = maxTokens
        )
        val jsonBody = json.encodeToString(ChatRequest.serializer(), request)
        // B-8 修复：统一 trimEnd('/')，与 testConnection 行为一致，避免尾部带 / 时拼出 //v1/...
        return Request.Builder()
            .url("${baseUrl.trimEnd('/')}/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toRequestBody(jsonMediaType))
            .build()
    }

    private fun parseSseChunk(data: String): String? {
        return try {
            json.decodeFromString(ChatStreamResponse.serializer(), data)
                ?.choices?.firstOrNull()?.delta?.content
        } catch (e: Exception) {
            Log.w("AiRepository", "解析流式响应 chunk 失败，跳过", e)
            null
        }
    }

    private fun errorCodeToMessage(code: Int): String = when (code) {
        401 -> "ERROR:INVALID_API_KEY"
        429 -> "ERROR:RATE_LIMIT"
        500, 502, 503 -> "ERROR:SERVER_ERROR"
        else -> "ERROR:HTTP_$code"
    }

    override fun streamChat(systemPrompt: String, userPrompt: String): Flow<String> = callbackFlow {
        val apiKey = settingsRepository.aiApiKey.first()
        val baseUrl = settingsRepository.aiBaseUrl.first()
        val model = settingsRepository.aiModel.first()

        if (apiKey.isBlank()) {
            trySend("ERROR:NO_API_KEY")
            close()
            return@callbackFlow
        }

        val httpRequest = buildChatHttpRequest(apiKey, baseUrl, model, systemPrompt, userPrompt)
        val call = okHttpClient.newCall(httpRequest)

        // 在独立协程中执行阻塞 IO，主协程立即注册 awaitClose 以响应取消。
        val job: Job = launch(Dispatchers.IO) {
            try {
                call.execute().use { response ->
                    if (!response.isSuccessful) {
                        trySend(errorCodeToMessage(response.code))
                        return@use
                    }

                    val source = response.body?.source() ?: run {
                        trySend("ERROR:EMPTY_RESPONSE")
                        return@use
                    }

                    while (!source.exhausted() && currentCoroutineContext().isActive) {
                        val line = source.readUtf8Line() ?: break
                        if (line.startsWith("data: ")) {
                            val data = line.removePrefix("data: ").trim()
                            if (data == "[DONE]") break
                            parseSseChunk(data)?.let { trySend(it) }
                        }
                    }
                }
            } catch (e: SocketTimeoutException) {
                trySend("ERROR:TIMEOUT")
            } catch (e: UnknownHostException) {
                trySend("ERROR:DNS_FAILED")
            } catch (e: IOException) {
                trySend("ERROR:NETWORK")
            } catch (e: Exception) {
                trySend("ERROR:${e.javaClass.simpleName}")
            } finally {
                close()
            }
        }

        awaitClose {
            call.cancel()
            job.cancel()
        }
        // B-18 修复：默认缓冲 64，UI 重组卡顿/慢收集时 trySend 静默丢 token，
        // 改为 UNLIMITED 容量，背压时由无界缓冲兜住 token，避免 AI 回复缺字少句。
    }.buffer(kotlinx.coroutines.channels.Channel.UNLIMITED).flowOn(Dispatchers.IO)

    override suspend fun testConnection(): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = settingsRepository.aiApiKey.first()
        if (apiKey.isBlank()) return@withContext Result.failure(Exception("API密钥未配置"))

        val baseUrl = settingsRepository.aiBaseUrl.first()
        val model = settingsRepository.aiModel.first()
        // B-8 修复：复用 buildChatHttpRequest，消除请求体构建重复 + 统一 trimEnd
        val httpRequest = buildChatHttpRequest(
            apiKey = apiKey, baseUrl = baseUrl, model = model,
            systemPrompt = "", userPrompt = "Hi",
            stream = false, maxTokens = 5
        )

        try {
            okHttpClient.newCall(httpRequest).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success("连接成功，模型: $model")
                } else {
                    when (response.code) {
                        401 -> Result.failure(Exception("API密钥无效"))
                        429 -> Result.failure(Exception("请求过于频繁，请稍后重试"))
                        in 500..599 -> Result.failure(Exception("服务器错误(${response.code})"))
                        else -> Result.failure(Exception("请求失败(${response.code})"))
                    }
                }
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
