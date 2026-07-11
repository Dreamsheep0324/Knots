package com.tang.prm.data.repository

import com.google.gson.Gson
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
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
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
        val apiKey = settingsRepository.aiApiKey.first()
        val baseUrl = settingsRepository.aiBaseUrl.first()
        val model = settingsRepository.aiModel.first()

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

        val call = okHttpClient.newCall(httpRequest)

        // 在独立协程中执行阻塞 IO，主协程立即注册 awaitClose 以响应取消。
        // 旧实现将 awaitClose 放在所有阻塞调用之后，导致消费者取消 Flow 时
        // call.cancel() 不会被调用，阻塞读取继续直到流耗尽或超时。
        val job: Job = launch(Dispatchers.IO) {
            try {
                call.execute().use { response ->
                    if (!response.isSuccessful) {
                        val errorCode = response.code
                        val errorMsg = when (errorCode) {
                            401 -> "ERROR:INVALID_API_KEY"
                            429 -> "ERROR:RATE_LIMIT"
                            500, 502, 503 -> "ERROR:SERVER_ERROR"
                            else -> "ERROR:HTTP_$errorCode"
                        }
                        trySend(errorMsg)
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
                // 显式关闭 Flow，消费者收到完成信号。
                // 旧实现收到 [DONE] 后 break 进入 awaitClose，但未调用 close()，
                // 导致 Flow 永远挂起，collect 永不完成。
                close()
            }
        }

        // 立即注册取消回调：消费者取消 Flow 时取消 OkHttp Call + 子协程
        awaitClose {
            call.cancel()
            job.cancel()
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun testConnection(): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = settingsRepository.aiApiKey.first()
        if (apiKey.isBlank()) return@withContext Result.failure(Exception("API密钥未配置"))

        val baseUrl = settingsRepository.aiBaseUrl.first()
        val model = settingsRepository.aiModel.first()
        val url = "${baseUrl.trimEnd('/')}/v1/chat/completions"

        val request = ChatRequest(
            model = model,
            messages = listOf(ChatMessage(role = "user", content = "Hi")),
            stream = false,
            maxTokens = 5
        )

        val jsonBody = gson.toJson(request)
        val httpRequest = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toRequestBody(jsonMediaType))
            .build()

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
