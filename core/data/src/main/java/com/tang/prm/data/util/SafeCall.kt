package com.tang.prm.data.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 统一的数据库调用包装，捕获异常并返回 Result
 */
suspend inline fun <T> safeDbCall(crossinline block: suspend () -> T): Result<T> {
    return try {
        withContext(Dispatchers.IO) {
            Result.success(block())
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * 统一的网络调用包装，捕获异常并返回 Result
 */
suspend inline fun <T> safeApiCall(crossinline block: suspend () -> T): Result<T> {
    return try {
        withContext(Dispatchers.IO) {
            Result.success(block())
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
