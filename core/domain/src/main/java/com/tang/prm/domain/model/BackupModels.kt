package com.tang.prm.domain.model

import kotlinx.serialization.Serializable

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

data class BackupFileInfo(
    val fileName: String,
    val fileSize: Long,
    val timestamp: Long
)

enum class BackupImageQuality(val displayName: String, val maxDimension: Int, val jpegQuality: Int, val targetSizeKb: Long) {
    ORIGINAL("原始质量", Int.MAX_VALUE, 100, Long.MAX_VALUE),
    STANDARD("标准质量", 1200, 70, 200),
    HIGH_COMPRESS("高压缩", 800, 50, 100)
}

@Serializable
data class BackupManifest(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val entries: List<ManifestEntry> = emptyList()
)

@Serializable
data class ManifestEntry(
    val name: String,
    val size: Long,
    val lastModified: Long,
    val compressed: Boolean = false
)
