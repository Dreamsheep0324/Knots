package com.tang.prm.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * T-10 修复：补 AlbumPhoto.stableId 关键哈希函数测试。
 *
 * stableId 用作收藏键 [favoritePhotoIds: Set<Long>]，碰撞会导致"照片 A 被标记为已收藏
 * 但实际是照片 B"。本测试覆盖一致性、不同 id 不碰撞、空 id、超长 id、Unicode id 等边界。
 */
class AlbumPhotoTest {

    private fun buildPhoto(id: String) = AlbumPhoto(
        id = id,
        uri = "uri",
        sourceType = "event",
        sourceId = 1L,
        sourceTitle = "",
        contactId = null,
        contactName = null,
        contactAvatar = null,
        date = 0L,
        location = null
    )

    @Nested
    @DisplayName("stableId 一致性")
    inner class ConsistencyTest {
        @Test
        fun `same id produces same stableId`() {
            val photo1 = buildPhoto("event_123_0")
            val photo2 = buildPhoto("event_123_0")

            assertThat(photo1.stableId).isEqualTo(photo2.stableId)
        }

        @Test
        fun `stableId matches string hashCode toLong`() {
            val id = "event_456_7"
            val photo = buildPhoto(id)

            assertThat(photo.stableId).isEqualTo(id.hashCode().toLong())
        }

        @Test
        fun `stableId is deterministic across instances`() {
            val id = "gift_999"
            val stableId1 = buildPhoto(id).stableId
            val stableId2 = buildPhoto(id).stableId

            assertThat(stableId1).isEqualTo(stableId2)
        }
    }

    @Nested
    @DisplayName("stableId 碰撞")
    inner class CollisionTest {
        @Test
        fun `different ids produce different stableIds`() {
            val ids = listOf(
                "event_1_0", "event_1_1", "event_2_0",
                "gift_1_0", "event_100_5", "event_1_00"
            )
            val stableIds = ids.map { buildPhoto(it).stableId }

            // 不要求零碰撞（hashCode 不保证），但本组样本必须无碰撞
            assertThat(stableIds.toSet().size).isEqualTo(stableIds.size)
        }

        @Test
        fun `prefix different same suffix produce different stableIds`() {
            val photo1 = buildPhoto("event_123")
            val photo2 = buildPhoto("gift_123")

            assertThat(photo1.stableId).isNotEqualTo(photo2.stableId)
        }
    }

    @Nested
    @DisplayName("stableId 边界")
    inner class BoundaryTest {
        @Test
        fun `empty id produces zero stableId`() {
            val photo = buildPhoto("")

            // String("").hashCode() == 0
            assertThat(photo.stableId).isEqualTo(0L)
        }

        @Test
        fun `single char id does not throw`() {
            val photo = buildPhoto("a")

            // 不抛异常即可
            assertThat(photo.stableId).isEqualTo("a".hashCode().toLong())
        }

        @Test
        fun `long id does not overflow`() {
            // 原 fold 实现在 15+ 字符时会溢出 Long 范围；新实现委托 hashCode 无此风险
            val longId = "event_${"x".repeat(200)}_${System.currentTimeMillis()}"
            val photo = buildPhoto(longId)

            // 不抛 ArithmeticException（Kotlin 无此异常，但验证结果与 hashCode 一致）
            assertThat(photo.stableId).isEqualTo(longId.hashCode().toLong())
        }

        @Test
        fun `unicode id produces valid stableId`() {
            val unicodeId = "事件_123_中文_🎬"
            val photo = buildPhoto(unicodeId)

            assertThat(photo.stableId).isEqualTo(unicodeId.hashCode().toLong())
        }
    }
}
