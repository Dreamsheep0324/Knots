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
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLException

class RetryInterceptorTest {

    private val interceptor = RetryInterceptor()

    private fun buildRequest(method: String = "GET"): Request {
        val builder = Request.Builder().url("https://example.com/api/test")
        if (method == "PUT" || method == "POST" || method == "DELETE" || method == "PATCH") {
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

    private fun buildChain(
        request: Request,
        proceedBehavior: (Request) -> Response
    ): Interceptor.Chain = object : Interceptor.Chain {
        override fun request() = request
        override fun proceed(request: Request): Response = proceedBehavior(request)
        override fun connection() = null
        override fun readTimeoutMillis() = 0
        override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
        override fun writeTimeoutMillis() = 0
        override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
        override fun connectTimeoutMillis() = 0
        override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
        override fun call() = throw UnsupportedOperationException()
    }

    @Nested
    @DisplayName("写操作不重试（避免副作用放大）")
    inner class WriteOperationNoRetryTest {

        @Test
        fun putRequest_doesNotRetry_evenOnException() {
            var callCount = 0
            val chain = buildChain(buildRequest("PUT")) {
                callCount++
                throw SocketTimeoutException("timeout")
            }

            try {
                interceptor.intercept(chain)
            } catch (e: SocketTimeoutException) {
                // expected
            }

            assertThat(callCount).isEqualTo(1)
        }

        @Test
        fun postRequest_doesNotRetry_evenOnException() {
            // POST 不重试，避免 AI 对话接口重复计费
            var callCount = 0
            val chain = buildChain(buildRequest("POST")) {
                callCount++
                throw SocketTimeoutException("timeout")
            }

            try {
                interceptor.intercept(chain)
            } catch (e: SocketTimeoutException) {
                // expected
            }

            assertThat(callCount).isEqualTo(1)
        }

        @Test
        fun deleteRequest_doesNotRetry_evenOnException() {
            var callCount = 0
            val chain = buildChain(buildRequest("DELETE")) {
                callCount++
                throw SocketTimeoutException("timeout")
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
    @DisplayName("可重试异常：所有 IOException 子类")
    inner class RetryableExceptionTest {

        @Test
        fun socketTimeout_retriesUpToMaxAttempts() {
            var callCount = 0
            val chain = buildChain(buildRequest()) {
                callCount++
                throw SocketTimeoutException("timeout")
            }

            try {
                interceptor.intercept(chain)
            } catch (e: SocketTimeoutException) {
                // expected
            }

            // MAX_ATTEMPTS = 3（总尝试次数，含首次）
            assertThat(callCount).isEqualTo(3)
        }

        @Test
        fun connectException_isRetried_asIOExceptionSubclass() {
            // ConnectException 是 IOException 子类，原精确类匹配会漏掉
            var callCount = 0
            val chain = buildChain(buildRequest()) {
                callCount++
                throw ConnectException("connection refused")
            }

            try {
                interceptor.intercept(chain)
            } catch (e: ConnectException) {
                // expected
            }

            assertThat(callCount).isEqualTo(3)
        }

        @Test
        fun sslException_isRetried_asIOExceptionSubclass() {
            // SSLException 是 IOException 子类，原精确类匹配会漏掉
            var callCount = 0
            val chain = buildChain(buildRequest()) {
                callCount++
                throw SSLException("ssl handshake failed")
            }

            try {
                interceptor.intercept(chain)
            } catch (e: SSLException) {
                // expected
            }

            assertThat(callCount).isEqualTo(3)
        }

        @Test
        fun succeedsOnSecondAttempt_returnsResponse() {
            var callCount = 0
            val chain = buildChain(buildRequest()) {
                callCount++
                if (callCount == 1) throw SocketTimeoutException("timeout")
                buildResponse(it)
            }

            val response = interceptor.intercept(chain)
            assertThat(response.code).isEqualTo(200)
            assertThat(callCount).isEqualTo(2)
        }
    }

    @Nested
    @DisplayName("非 IOException 直接抛出")
    inner class NonRetryableExceptionTest {

        @Test
        fun runtimeException_thrownImmediately() {
            var callCount = 0
            val chain = buildChain(buildRequest()) {
                callCount++
                error("unexpected")
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
    @DisplayName("5xx/429 响应可重试")
    inner class ServerErrorRetryTest {

        @Test
        fun serverError500_isRetried() {
            var callCount = 0
            val chain = buildChain(buildRequest()) {
                callCount++
                buildResponse(it, code = 500)
            }

            val response = interceptor.intercept(chain)
            assertThat(response.code).isEqualTo(500)
            assertThat(callCount).isEqualTo(3) // 重试到最大次数后返回最后一次响应
        }

        @Test
        fun rateLimited429_isRetried() {
            var callCount = 0
            val chain = buildChain(buildRequest()) {
                callCount++
                buildResponse(it, code = 429)
            }

            val response = interceptor.intercept(chain)
            assertThat(response.code).isEqualTo(429)
            assertThat(callCount).isEqualTo(3)
        }

        @Test
        fun serverErrorRecoversOnSecondAttempt_returnsSuccess() {
            var callCount = 0
            val chain = buildChain(buildRequest()) {
                callCount++
                if (callCount == 1) buildResponse(it, code = 503) else buildResponse(it, code = 200)
            }

            val response = interceptor.intercept(chain)
            assertThat(response.code).isEqualTo(200)
            assertThat(callCount).isEqualTo(2)
        }

        @Test
        fun clientError4xx_isNotRetried() {
            var callCount = 0
            val chain = buildChain(buildRequest()) {
                callCount++
                buildResponse(it, code = 404)
            }

            val response = interceptor.intercept(chain)
            assertThat(response.code).isEqualTo(404)
            assertThat(callCount).isEqualTo(1)
        }
    }

    @Nested
    @DisplayName("成功请求")
    inner class SuccessTest {

        @Test
        fun successfulRequest_returnsImmediately() {
            var callCount = 0
            val chain = buildChain(buildRequest()) {
                callCount++
                buildResponse(it)
            }

            val response = interceptor.intercept(chain)
            assertThat(response.code).isEqualTo(200)
            assertThat(callCount).isEqualTo(1)
        }
    }
}
