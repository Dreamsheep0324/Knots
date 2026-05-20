package com.tang.prm.data.mapper

import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.entity.ReminderEntity
import com.tang.prm.domain.model.Reminder
import org.junit.jupiter.api.Test

class ReminderMapperTest {

    @Test
    fun reminderEntity_toDomain_mapsAllFields() {
        val entity = ReminderEntity(
            id = 1, contactId = 10, eventId = 20, anniversaryId = 30,
            type = "anniversary", title = "生日提醒", content = "明天是张三的生日",
            time = 1000L, isCompleted = false, isIgnored = false,
            repeatInterval = 86400000L, createdAt = 2000L
        )

        val domain = entity.toDomain()

        assertThat(domain.id).isEqualTo(1)
        assertThat(domain.contactId).isEqualTo(10)
        assertThat(domain.eventId).isEqualTo(20)
        assertThat(domain.anniversaryId).isEqualTo(30)
        assertThat(domain.type).isEqualTo("anniversary")
        assertThat(domain.title).isEqualTo("生日提醒")
        assertThat(domain.content).isEqualTo("明天是张三的生日")
        assertThat(domain.time).isEqualTo(1000L)
        assertThat(domain.isCompleted).isFalse()
        assertThat(domain.isIgnored).isFalse()
        assertThat(domain.repeatInterval).isEqualTo(86400000L)
        assertThat(domain.createdAt).isEqualTo(2000L)
    }

    @Test
    fun reminder_toEntity_mapsAllFields() {
        val domain = Reminder(
            id = 1, contactId = 10, eventId = 20, anniversaryId = 30,
            type = "event", title = "事件提醒", content = "下午有聚餐",
            time = 1000L, isCompleted = true, isIgnored = true,
            repeatInterval = 3600000L, createdAt = 2000L
        )

        val entity = domain.toEntity()

        assertThat(entity.id).isEqualTo(1)
        assertThat(entity.contactId).isEqualTo(10)
        assertThat(entity.eventId).isEqualTo(20)
        assertThat(entity.anniversaryId).isEqualTo(30)
        assertThat(entity.type).isEqualTo("event")
        assertThat(entity.title).isEqualTo("事件提醒")
        assertThat(entity.content).isEqualTo("下午有聚餐")
        assertThat(entity.time).isEqualTo(1000L)
        assertThat(entity.isCompleted).isTrue()
        assertThat(entity.isIgnored).isTrue()
        assertThat(entity.repeatInterval).isEqualTo(3600000L)
        assertThat(entity.createdAt).isEqualTo(2000L)
    }

    @Test
    fun reminderEntity_roundtrip_preservesAllFields() {
        val original = ReminderEntity(
            id = 1, contactId = null, eventId = null, anniversaryId = null,
            type = "custom", title = "提醒", content = "内容",
            time = 1000L, isCompleted = false, isIgnored = false,
            repeatInterval = null, createdAt = 2000L
        )

        val roundtrip = original.toDomain().toEntity()

        assertThat(roundtrip).isEqualTo(original)
    }

    @Test
    fun reminderEntity_repeatInterval_preserved() {
        val entity = ReminderEntity(
            id = 1, type = "anniversary", title = "提醒", content = "内容",
            time = 1000L, repeatInterval = 604800000L
        )

        val domain = entity.toDomain()
        val roundtrip = domain.toEntity()

        assertThat(domain.repeatInterval).isEqualTo(604800000L)
        assertThat(roundtrip.repeatInterval).isEqualTo(604800000L)
    }

    @Test
    fun reminderEntity_nullRepeatInterval_preserved() {
        val entity = ReminderEntity(
            id = 1, type = "once", title = "提醒", content = "内容",
            time = 1000L, repeatInterval = null
        )

        val domain = entity.toDomain()
        val roundtrip = domain.toEntity()

        assertThat(domain.repeatInterval).isNull()
        assertThat(roundtrip.repeatInterval).isNull()
    }
}
