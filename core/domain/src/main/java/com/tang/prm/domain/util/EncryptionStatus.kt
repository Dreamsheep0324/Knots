package com.tang.prm.domain.util

/**
 * 加密存储状态标记。
 *
 * 当 EncryptedSharedPreferences 初始化失败时，[markDegraded] 会被调用，
 * UI 层可通过 [isDegraded] 检查并提示用户敏感数据未加密。
 */
object EncryptionStatus {
    @Volatile
    var isDegraded: Boolean = false
        private set

    fun markDegraded() {
        isDegraded = true
    }
}
