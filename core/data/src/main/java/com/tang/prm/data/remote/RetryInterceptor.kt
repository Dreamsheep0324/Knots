package com.tang.prm.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

/**
 * OkHttp 重试拦截器。
 *
 * 设计要点：
 * 1. [MAX_ATTEMPTS] 语义为"总尝试次数"（含首次），与测试期望一致；
 *    MAX_ATTEMPTS=3 表示首次失败后再重试 2 次。
 * 2. 写操作（POST/PUT/DELETE/PATCH）默认不重试——这类请求可能产生副作用
 *    （如 AI 对话接口 POST 重复计费、订单重复提交、文件重复上传等）。
 * 3. 异常匹配用 `is IOException` 子类判定，覆盖所有 IO 子类
 *    （SocketTimeoutException、UnknownHostException、ConnectException、SSLException 等），
 *    而非精确类匹配。
 * 4. 5xx/429 响应视为可重试失败（仅对幂等 GET/HEAD）。
 */
class RetryInterceptor @Inject constructor() : Interceptor {
    companion object {
        /** 总尝试次数（含首次）。3 = 首次 + 2 次重试 */
        private const val MAX_ATTEMPTS = 3

        /** 不重试的 HTTP 方法：写操作可能产生副作用 */
        private val NON_RETRYABLE_METHODS = setOf("POST", "PUT", "DELETE", "PATCH")

        /** 可重试的响应码：服务器错误 + 限流 */
        private val RETRYABLE_RESPONSE_CODES = setOf(429, 500, 502, 503, 504)

        private val RANDOM = java.util.Random()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // 写操作默认不重试，避免副作用放大（如 AI API 重复计费）
        if (request.method in NON_RETRYABLE_METHODS) {
            return chain.proceed(request)
        }

        var lastException: IOException? = null
        var attempt = 0

        while (attempt < MAX_ATTEMPTS) {
            attempt++
            try {
                val response = chain.proceed(request)
                // 5xx/429 视为可重试失败（仅对幂等方法，已在上方过滤写操作）
                if (response.code in RETRYABLE_RESPONSE_CODES && attempt < MAX_ATTEMPTS) {
                    response.close()
                    backoff(attempt)
                    continue
                }
                return response
            } catch (e: IOException) {
                // 所有 IOException 子类一律可重试；非 IOException（如 RuntimeException）不捕获，直接向上抛
                lastException = e
                if (attempt < MAX_ATTEMPTS) {
                    backoff(attempt)
                }
            }
        }
        throw lastException ?: IOException("Unknown error after $MAX_ATTEMPTS attempts")
    }

    /**
     * 指数退避 + 随机抖动，避免多客户端同步重试导致雪崩。
     * attempt=1 → 1000ms + jitter(0-299ms)
     * attempt=2 → 2000ms + jitter
     */
    private fun backoff(attempt: Int) {
        val baseDelay = 1000L * (1 shl (attempt - 1))
        val jitter = RANDOM.nextInt(300).toLong()
        Thread.sleep(baseDelay + jitter)
    }
}
