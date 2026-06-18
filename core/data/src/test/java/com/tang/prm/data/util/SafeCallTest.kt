package com.tang.prm.data.util

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.IOException

class SafeCallTest {

    @Nested
    @DisplayName("成功路径")
    inner class SuccessPathTest {

        @Test
        fun successfulBlock_returnsSuccessResult() = runTest {
            val result = safeCall { 42 }
            assertThat(result.isSuccess).isTrue()
            assertThat(result.getOrNull()).isEqualTo(42)
        }

        @Test
        fun successfulBlockWithString_returnsSuccessResult() = runTest {
            val result = safeCall { "hello" }
            assertThat(result.isSuccess).isTrue()
            assertThat(result.getOrNull()).isEqualTo("hello")
        }

        @Test
        fun successfulBlockWithNull_returnsSuccessWithNull() = runTest {
            val result = safeCall<String?> { null }
            assertThat(result.isSuccess).isTrue()
            assertThat(result.getOrNull()).isNull()
        }
    }

    @Nested
    @DisplayName("异常捕获")
    inner class ExceptionHandlingTest {

        @Test
        fun exception_returnsFailureResult() = runTest {
            val result = safeCall<Int> { throw IOException("network error") }
            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isInstanceOf(IOException::class.java)
            assertThat(result.exceptionOrNull()?.message).isEqualTo("network error")
        }

        @Test
        fun runtimeException_returnsFailureResult() = runTest {
            val result = safeCall<Int> { throw IllegalStateException("bad state") }
            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        }

        @Test
        fun illegalArgument_returnsFailureResult() = runTest {
            val result = safeCall<Int> { throw IllegalArgumentException("invalid arg") }
            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        }
    }

    @Nested
    @DisplayName("CancellationException 传播")
    inner class CancellationTest {

        @Test
        fun cancellationException_isPropagatedNotCaught() = runTest {
            var caught = false
            try {
                safeCall<Int> { throw CancellationException("cancelled") }
            } catch (e: CancellationException) {
                caught = true
            }
            assertThat(caught).isTrue()
        }
    }
}
