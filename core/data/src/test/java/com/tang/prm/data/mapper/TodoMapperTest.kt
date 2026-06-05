package com.tang.prm.data.mapper

import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.entity.TodoItemEntity
import com.tang.prm.domain.model.TodoItem
import org.junit.jupiter.api.Test

class TodoMapperTest {

    @Test
    fun todoItemEntity_toDomain_mapsAllFields() {
        val entity = TodoItemEntity(
            id = 1, contactId = 10, eventId = 20, title = "买礼物",
            isCompleted = true, priority = 2, dueDate = 3000L, createdAt = 1000L
        )

        val domain = entity.toDomain()

        assertThat(domain.id).isEqualTo(1)
        assertThat(domain.contactId).isEqualTo(10)
        assertThat(domain.eventId).isEqualTo(20)
        assertThat(domain.title).isEqualTo("买礼物")
        assertThat(domain.isCompleted).isTrue()
        assertThat(domain.priority).isEqualTo(2)
        assertThat(domain.dueDate).isEqualTo(3000L)
        assertThat(domain.createdAt).isEqualTo(1000L)
    }

    @Test
    fun todoItem_toEntity_mapsAllFields() {
        val domain = TodoItem(
            id = 1, contactId = 10, eventId = 20, title = "买礼物",
            isCompleted = true, priority = 2, dueDate = 3000L, createdAt = 1000L
        )

        val entity = domain.toEntity()

        assertThat(entity.id).isEqualTo(1)
        assertThat(entity.contactId).isEqualTo(10)
        assertThat(entity.eventId).isEqualTo(20)
        assertThat(entity.title).isEqualTo("买礼物")
        assertThat(entity.isCompleted).isTrue()
        assertThat(entity.priority).isEqualTo(2)
        assertThat(entity.dueDate).isEqualTo(3000L)
        assertThat(entity.createdAt).isEqualTo(1000L)
    }

    @Test
    fun todoItemEntity_roundtrip_preservesAllFields() {
        val original = TodoItemEntity(
            id = 1, contactId = null, eventId = null, title = "待办",
            isCompleted = false, priority = 0, dueDate = null, createdAt = 1000L
        )

        val roundtrip = original.toDomain().toEntity()

        assertThat(roundtrip).isEqualTo(original)
    }

    @Test
    fun todoItemEntity_isCompleted_preserved() {
        val entity = TodoItemEntity(id = 1, title = "任务", isCompleted = true)

        val domain = entity.toDomain()
        val roundtrip = domain.toEntity()

        assertThat(domain.isCompleted).isTrue()
        assertThat(roundtrip.isCompleted).isTrue()
    }

    @Test
    fun todoItemEntity_isCompleted_false_preserved() {
        val entity = TodoItemEntity(id = 1, title = "任务", isCompleted = false)

        val domain = entity.toDomain()
        val roundtrip = domain.toEntity()

        assertThat(domain.isCompleted).isFalse()
        assertThat(roundtrip.isCompleted).isFalse()
    }
}
