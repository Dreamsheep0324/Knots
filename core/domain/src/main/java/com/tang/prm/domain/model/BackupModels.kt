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
}

sealed class ClearDataResult {
    data object Success : ClearDataResult()
    data class Error(val message: String) : ClearDataResult()
}
