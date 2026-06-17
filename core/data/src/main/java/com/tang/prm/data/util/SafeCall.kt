package com.tang.prm.data.util

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 统一的异步调用包装，捕获异常并返回 Result。
 * 正确传播 CancellationException 以保持协程取消语义。
 */
suspend inline fun <T> safeCall(crossinline block: suspend () -> T): Result<T> {
    return try {
        withContext(Dispatchers.IO) {
            Result.success(block())
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
