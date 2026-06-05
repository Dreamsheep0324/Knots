package com.tang.prm.data.mapper

import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.entity.ThoughtEntity
import com.tang.prm.domain.model.Thought
import com.tang.prm.domain.model.ThoughtType
import org.junit.jupiter.api.Test

class ThoughtMapperTest {

    @Test
    fun thoughtEntity_toDomain_mapsAllFields() {
        val entity = ThoughtEntity(
            id = 1, contactId = 10, content = "是个好朋友", type = "friend",
            isPrivate = true, isTodo = false, isDone = false,
            dueDate = null, createdAt = 1000L, updatedAt = 2000L
        )

        val domain = entity.toDomain()

        assertThat(domain.id).isEqualTo(1)
        assertThat(domain.contactId).isEqualTo(10)
        assertThat(domain.content).isEqualTo("是个好朋友")
        assertThat(domain.type).isEqualTo(ThoughtType.FRIEND)
        assertThat(domain.isPrivate).isTrue()
        assertThat(domain.isTodo).isFalse()
        assertThat(domain.isDone).isFalse()
        assertThat(domain.dueDate).isNull()
        assertThat(domain.createdAt).isEqualTo(1000L)
        assertThat(domain.updatedAt).isEqualTo(2000L)
    }

    @Test
    fun thoughtEntity_toDomain_typeFriend() {
        val entity = ThoughtEntity(id = 1, contactId = null, content = "朋友想法", type = "friend", createdAt = 1000L, updatedAt = 2000L)

        val domain = entity.toDomain()

        assertThat(domain.type).isEqualTo(ThoughtType.FRIEND)
    }

    @Test
    fun thoughtEntity_toDomain_typePlan() {
        val entity = ThoughtEntity(id = 1, contactId = null, content = "计划", type = "plan", createdAt = 1000L, updatedAt = 2000L)

        val domain = entity.toDomain()

        assertThat(domain.type).isEqualTo(ThoughtType.PLAN)
    }

    @Test
    fun thoughtEntity_toDomain_typeMurmur() {
        val entity = ThoughtEntity(id = 1, contactId = null, content = "碎碎念", type = "murmur", createdAt = 1000L, updatedAt = 2000L)

        val domain = entity.toDomain()

        assertThat(domain.type).isEqualTo(ThoughtType.MURMUR)
    }

    @Test
    fun thoughtEntity_toDomain_unknownType_defaultsToMurmur() {
        val entity = ThoughtEntity(id = 1, contactId = null, content = "未知", type = "unknown", createdAt = 1000L, updatedAt = 2000L)

        val domain = entity.toDomain()

        assertThat(domain.type).isEqualTo(ThoughtType.MURMUR)
    }

    @Test
    fun thought_toEntity_mapsAllFields() {
        val domain = Thought(
            id = 1, contactId = 10, content = "计划内容", type = ThoughtType.PLAN,
            isPrivate = false, isTodo = true, isDone = false,
            dueDate = 3000L, createdAt = 1000L, updatedAt = 2000L
        )

        val entity = domain.toEntity()

        assertThat(entity.id).isEqualTo(1)
        assertThat(entity.contactId).isEqualTo(10)
        assertThat(entity.content).isEqualTo("计划内容")
        assertThat(entity.type).isEqualTo("plan")
        assertThat(entity.isPrivate).isFalse()
        assertThat(entity.isTodo).isTrue()
        assertThat(entity.isDone).isFalse()
        assertThat(entity.dueDate).isEqualTo(3000L)
        assertThat(entity.createdAt).isEqualTo(1000L)
        assertThat(entity.updatedAt).isEqualTo(2000L)
    }

    @Test
    fun thought_toEntity_typeFriend_keyIsFriend() {
        val domain = Thought(id = 1, content = "朋友", type = ThoughtType.FRIEND, createdAt = 1000L, updatedAt = 2000L)

        val entity = domain.toEntity()

        assertThat(entity.type).isEqualTo("friend")
    }

    @Test
    fun thought_toEntity_typePlan_keyIsPlan() {
        val domain = Thought(id = 1, content = "计划", type = ThoughtType.PLAN, createdAt = 1000L, updatedAt = 2000L)

        val entity = domain.toEntity()

        assertThat(entity.type).isEqualTo("plan")
    }

    @Test
    fun thoughtEntity_roundtrip_preservesAllFields() {
        val original = ThoughtEntity(
            id = 1, contactId = 10, content = "想法", type = "friend",
            isPrivate = true, isTodo = true, isDone = true,
            dueDate = 3000L, createdAt = 1000L, updatedAt = 2000L
        )

        val roundtrip = original.toDomain().toEntity()

        assertThat(roundtrip).isEqualTo(original)
    }
}
