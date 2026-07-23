package com.tang.prm.domain.model

/**
 * 相册照片项。
 *
 * B-5 修复：新增 [allContactIds] 保存全部参与者 ID，过滤时匹配任意参与者而非仅首参与者。
 * 原 [contactId] 仅为首参与者，按其他参与者过滤时会丢失该照片。
 */
data class AlbumPhoto(
    val id: String,
    val uri: String,
    val sourceType: String,
    val sourceId: Long,
    val sourceTitle: String,
    val contactId: Long?,
    val contactName: String?,
    val contactAvatar: String?,
    val allContactIds: List<Long> = emptyList(),
    val allContactNames: List<String> = emptyList(),
    val allContactAvatars: List<String?> = emptyList(),
    val date: Long,
    val location: String?
) {
    /**
     * 用作收藏键 [favoritePhotoIds: Set<Long>] 的稳定标识。
     *
     * B-14 修复：原实现 `fold(0L) { acc, c -> acc * 31L + c.code.toLong() }`
     * 在 id 较长（15+ 字符）时累乘溢出 Long 范围，且与 Java String.hashCode()
     * 行为不一致。改用标准库 `id.hashCode().toLong()`，行为文档化、零溢出风险。
     */
    val stableId: Long
        get() = id.hashCode().toLong()
}
