package com.tang.prm.data.mapper

import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.entity.CircleEntity
import com.tang.prm.domain.model.Circle
import org.junit.jupiter.api.Test

class CircleMapperTest {

    @Test
    fun circleEntity_toDomain_mapsAllFields() {
        val entity = CircleEntity(
            id = 1, name = "密友", description = "亲密朋友", color = "#FF0000",
            icon = "heart", waveform = "sine", parentCircleId = 5L,
            intimacyThreshold = 80, sortOrder = 2, createdAt = 1000L, updatedAt = 2000L
        )

        val domain = entity.toDomain(memberIds = listOf(1L, 2L))

        assertThat(domain.id).isEqualTo(1)
        assertThat(domain.name).isEqualTo("密友")
        assertThat(domain.description).isEqualTo("亲密朋友")
        assertThat(domain.color).isEqualTo("#FF0000")
        assertThat(domain.icon).isEqualTo("heart")
        assertThat(domain.waveform).isEqualTo("sine")
        assertThat(domain.memberIds).containsExactly(1L, 2L).inOrder()
        assertThat(domain.parentCircleId).isEqualTo(5L)
        assertThat(domain.intimacyThreshold).isEqualTo(80)
        assertThat(domain.sortOrder).isEqualTo(2)
        assertThat(domain.createdAt).isEqualTo(1000L)
        assertThat(domain.updatedAt).isEqualTo(2000L)
    }

    @Test
    fun circle_toEntity_mapsAllFields() {
        val domain = Circle(
            id = 1, name = "密友", description = "亲密朋友", color = "#FF0000",
            icon = "heart", waveform = "sine", memberIds = listOf(1L, 2L, 3L),
            parentCircleId = 5L, intimacyThreshold = 80, sortOrder = 2,
            createdAt = 1000L, updatedAt = 2000L
        )

        val entity = domain.toEntity()

        assertThat(entity.id).isEqualTo(1)
        assertThat(entity.name).isEqualTo("密友")
        assertThat(entity.description).isEqualTo("亲密朋友")
        assertThat(entity.color).isEqualTo("#FF0000")
        assertThat(entity.icon).isEqualTo("heart")
        assertThat(entity.waveform).isEqualTo("sine")
        assertThat(entity.parentCircleId).isEqualTo(5L)
        assertThat(entity.intimacyThreshold).isEqualTo(80)
        assertThat(entity.sortOrder).isEqualTo(2)
        assertThat(entity.createdAt).isEqualTo(1000L)
        assertThat(entity.updatedAt).isEqualTo(2000L)
    }

    @Test
    fun circleEntity_roundtrip_preservesEntityFields() {
        val original = CircleEntity(
            id = 1, name = "同事", description = null, color = "#6366F1",
            icon = "people", waveform = "sine", parentCircleId = null,
            intimacyThreshold = 0, sortOrder = 0, createdAt = 1000L, updatedAt = 2000L
        )

        val roundtrip = original.toDomain(memberIds = listOf(10L, 20L)).toEntity()

        assertThat(roundtrip).isEqualTo(original)
    }

    @Test
    fun circleEntity_toDomain_defaultMemberIdsIsEmpty() {
        val entity = CircleEntity(id = 1, name = "圈子")

        val domain = entity.toDomain()

        assertThat(domain.memberIds).isEmpty()
    }

    @Test
    fun circle_toEntity_doesNotIncludeMemberIds() {
        val domain = Circle(id = 1, name = "圈子", memberIds = listOf(1L, 2L, 3L))

        val entity = domain.toEntity()

        assertThat(entity.id).isEqualTo(1)
        assertThat(entity.name).isEqualTo("圈子")
    }
}
