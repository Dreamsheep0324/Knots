package com.tang.prm.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SyncManifest(
    val version: Int = 1,
    val lastSyncTime: Long = 0L,
    val dbBackup: String = "",
    val dbTimestamp: Long = 0L,
    val dbFingerprint: String = "",
    val images: Map<String, FileEntry> = emptyMap(),
    val giftPhotos: Map<String, FileEntry> = emptyMap()
)

@Serializable
data class FileEntry(
    val name: String = "",
    val size: Long = 0,
    val modified: Long = 0,
    val uploadedAt: Long = 0
)
