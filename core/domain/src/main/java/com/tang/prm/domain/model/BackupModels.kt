package com.tang.prm.domain.model

data class BackupInfo(
    val fileName: String,
    val fileSize: Long,
    val timestamp: Long
)

sealed class BackupResult {
    data class Success(val info: BackupInfo) : BackupResult()
    data class Error(val message: String) : BackupResult()
}

sealed class RestoreResult {
    data object Success : RestoreResult()
    data class Error(val message: String) : RestoreResult()
    /**
     * 恢复失败且回滚也失败：数据库文件可能已损坏，应用下次启动可能崩溃。
     * UI 应提示用户应用可能无法正常启动，建议重新安装或联系支持。
     */
    data class FatalError(val message: String, val cause: Throwable? = null) : RestoreResult()
}

sealed class ClearDataResult {
    data object Success : ClearDataResult()
    data class Error(val message: String) : ClearDataResult()
}

data class BackupFileInfo(
    val fileName: String,
    val fileSize: Long,
    val timestamp: Long
)
