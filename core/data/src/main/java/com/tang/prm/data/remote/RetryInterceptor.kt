package com.tang.prm.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class RetryInterceptor @Inject constructor() : Interceptor {
    companion object {
        private const val MAX_RETRIES = 2
        private val RETRYABLE_EXCEPTIONS = setOf(
            SocketTimeoutException::class.java,
            UnknownHostException::class.java,
            IOException::class.java
        )
        private val RANDOM = java.util.Random()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        // 上传请求不重试，避免大文件重复上传
        if (request.method == "PUT") {
            return chain.proceed(request)
        }

        var lastException: Exception? = null

        repeat(MAX_RETRIES) { attempt ->
            try {
                return chain.proceed(request)
            } catch (e: Exception) {
                lastException = e
                if (e::class.java !in RETRYABLE_EXCEPTIONS) throw e
                if (attempt < MAX_RETRIES - 1) {
                    // 指数退避 + 随机抖动，避免多客户端同步重试导致雪崩
                    val baseDelay = 1000L * (1 shl attempt)
                    val jitter = RANDOM.nextInt(300).toLong()
                    Thread.sleep(baseDelay + jitter)
                }
            }
        }
        throw lastException ?: IOException("Unknown error after $MAX_RETRIES retries")
    }
}
