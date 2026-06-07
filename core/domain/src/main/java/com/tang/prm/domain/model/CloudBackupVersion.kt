package com.tang.prm.domain.model

data class CloudBackupVersion(
    val fileName: String,
    val fileSize: Long,
    val lastModified: String,
    val displayName: String,
    val imageCount: Int = 0,
    val isIncremental: Boolean = false
)
