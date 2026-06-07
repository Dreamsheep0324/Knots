package com.tang.prm.domain.model

sealed class SyncResult {
    data class UploadProgress(
        val phase: String,
        val current: Int,
        val total: Int,
        val detail: String
    ) : SyncResult() {
        val percent: Int get() = if (total > 0) (current * 100 / total).coerceIn(0, 100) else 0
    }

    data class UploadSuccess(
        val fileName: String,
        val uploadedImages: Int = 0,
        val skippedImages: Int = 0
    ) : SyncResult()

    data class DownloadProgress(
        val phase: String,
        val current: Int,
        val total: Int,
        val detail: String
    ) : SyncResult() {
        val percent: Int get() = if (total > 0) (current * 100 / total).coerceIn(0, 100) else 0
    }

    data class DownloadSuccess(
        val fileName: String,
        val downloadedImages: Int = 0,
        val skippedImages: Int = 0
    ) : SyncResult()

    data class Error(val message: String) : SyncResult()
}

sealed class ConnectionTestResult {
    data object Success : ConnectionTestResult()
    data class Error(val message: String) : ConnectionTestResult()
}
