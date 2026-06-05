package com.tang.prm.domain.model

data class AlbumPhoto(
    val id: String,
    val uri: String,
    val sourceType: String,
    val sourceId: Long,
    val sourceTitle: String,
    val contactId: Long?,
    val contactName: String?,
    val contactAvatar: String?,
    val date: Long,
    val location: String?
) {
    val stableId: Long
        get() = id.toList().fold(0L) { acc, c -> acc * 31L + c.code.toLong() }
}
