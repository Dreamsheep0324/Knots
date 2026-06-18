package com.tang.prm.data.remote

import com.google.common.truth.Truth.assertThat
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class RetryInterceptorTest {

    private val interceptor = RetryInterceptor()

    private fun buildRequest(method: String = "GET"): Request {
        val builder = Request.Builder().url("https://example.com/api/test")
        if (method == "PUT" || method == "POST") {
            builder.method(method, "".toRequestBody("text/plain".toMediaType()))
        } else {
            builder.method(method, null)
        }
        return builder.build()
    }

    private fun buildResponse(request: Request, code: Int = 200): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("OK")
            .build()
    }

    @Nested
    @DisplayName("PUT 请求不重试")
    inner class PutNoRetryTest {

        @Test
        fun putRequest_doesNotRetry_evenOnException() {
            var callCount = 0
            val chain = object : Interceptor.Chain {
                override fun request() = buildRequest("PUT")
                override fun proceed(request: Request): Response {
                    callCount++
                    throw SocketTimeoutException("timeout")
                }
                override fun connection() = null
                override fun readTimeoutMillis() = 0
                override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
                override fun writeTimeoutMillis() = 0
                override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
                override fun connectTimeoutMillis() = 0
                override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this

                override fun call() = throw UnsupportedOperationException()
            }

            try {
                interceptor.intercept(chain)
            } catch (e: SocketTimeoutException) {
                // expected
            }

            assertThat(callCount).isEqualTo(1)
        }
    }

    @Nested
    @DisplayName("可重试异常")
    inner class RetryableExceptionTest {

        @Test
        fun socketTimeout_retriesUpToMaxTimes() {
            var callCount = 0
            val chain = object : Interceptor.Chain {
                override fun request() = buildRequest()
                override fun proceed(request: Request): Response {
                    callCount++
                    throw SocketTimeoutException("timeout")
                }
                override fun connection() = null
                override fun readTimeoutMillis() = 0
                override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
                override fun writeTimeoutMillis() = 0
                override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
                override fun connectTimeoutMillis() = 0
                override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this

                override fun call() = throw UnsupportedOperationException()
            }

            try {
                interceptor.intercept(chain)
            } catch (e: SocketTimeoutException) {
                // expected
            }

            assertThat(callCount).isEqualTo(3) // MAX_RETRIES = 3
        }

        @Test
        fun succeedsOnSecondAttempt_returnsResponse() {
            var callCount = 0
            val chain = object : Interceptor.Chain {
                override fun request() = buildRequest()
                override fun proceed(request: Request): Response {
                    callCount++
                    if (callCount == 1) throw SocketTimeoutException("timeout")
                    return buildResponse(request)
                }
                override fun connection() = null
                override fun readTimeoutMillis() = 0
                override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
                override fun writeTimeoutMillis() = 0
                override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
                override fun connectTimeoutMillis() = 0
                override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this

                override fun call() = throw UnsupportedOperationException()
            }

            val response = interceptor.intercept(chain)
            assertThat(response.code).isEqualTo(200)
            assertThat(callCount).isEqualTo(2)
        }
    }

    @Nested
    @DisplayName("非重试异常直接抛出")
    inner class NonRetryableExceptionTest {

        @Test
        fun runtimeException_thrownImmediately() {
            var callCount = 0
            val chain = object : Interceptor.Chain {
                override fun request() = buildRequest()
                override fun proceed(request: Request): Response {
                    callCount++
                    throw RuntimeException("unexpected")
                }
                override fun connection() = null
                override fun readTimeoutMillis() = 0
                override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
                override fun writeTimeoutMillis() = 0
                override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
                override fun connectTimeoutMillis() = 0
                override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this

                override fun call() = throw UnsupportedOperationException()
            }

            try {
                interceptor.intercept(chain)
            } catch (e: RuntimeException) {
                // expected
            }

            assertThat(callCount).isEqualTo(1)
        }
    }

    @Nested
    @DisplayName("成功请求")
    inner class SuccessTest {

        @Test
        fun successfulRequest_returnsImmediately() {
            var callCount = 0
            val chain = object : Interceptor.Chain {
                override fun request() = buildRequest()
                override fun proceed(request: Request): Response {
                    callCount++
                    return buildResponse(request)
                }
                override fun connection() = null
                override fun readTimeoutMillis() = 0
                override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
                override fun writeTimeoutMillis() = 0
                override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
                override fun connectTimeoutMillis() = 0
                override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this

                override fun call() = throw UnsupportedOperationException()
            }

            val response = interceptor.intercept(chain)
            assertThat(response.code).isEqualTo(200)
            assertThat(callCount).isEqualTo(1)
        }
    }
}
