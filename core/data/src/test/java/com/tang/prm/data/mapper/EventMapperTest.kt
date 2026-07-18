package com.tang.prm.data.mapper

import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.entity.EventEntity
import com.tang.prm.data.local.entity.EventWithParticipants
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.EventType
import org.junit.jupiter.api.Test

class EventMapperTest {

    @Test
    fun event_toEntity_mapsAllFields() {
        val domain = Event(
            id = 1, type = EventType.DINING, title = "聚餐", description = "火锅",
            time = 1000L, endTime = 2000L, location = "海底捞",
            latitude = 31.2, longitude = 121.5, photos = listOf("p1.jpg"),
            emotion = "兴奋", weather = "阴", amount = 200.0,
            remarks = "AA制", promise = "下次请客", conversationSummary = "聊了很多",
            giftName = null, participants = emptyList(),
            createdAt = 3000L, updatedAt = 4000L
        )

        val entity = domain.toEntity()

        assertThat(entity.id).isEqualTo(1)
        assertThat(entity.type).isEqualTo("DINING")
        assertThat(entity.title).isEqualTo("聚餐")
        assertThat(entity.description).isEqualTo("火锅")
        assertThat(entity.time).isEqualTo(1000L)
        assertThat(entity.endTime).isEqualTo(2000L)
        assertThat(entity.location).isEqualTo("海底捞")
        assertThat(entity.latitude).isEqualTo(31.2)
        assertThat(entity.longitude).isEqualTo(121.5)
        assertThat(entity.photos).containsExactly("p1.jpg").inOrder()
        assertThat(entity.emotion).isEqualTo("兴奋")
        assertThat(entity.weather).isEqualTo("阴")
        assertThat(entity.amount).isEqualTo(200.0)
        assertThat(entity.remarks).isEqualTo("AA制")
        assertThat(entity.promise).isEqualTo("下次请客")
        assertThat(entity.conversationSummary).isEqualTo("聊了很多")
        assertThat(entity.giftName).isNull()
        assertThat(entity.createdAt).isEqualTo(3000L)
        assertThat(entity.updatedAt).isEqualTo(4000L)
    }

    @Test
    fun eventWithParticipants_toDomain_mapsAllFields() {
        val entity = EventEntity(
            id = 1, type = "MEETUP", title = "见面", description = "咖啡厅见面",
            time = 1000L, endTime = 2000L, location = "星巴克",
            latitude = 39.9, longitude = 116.4, photos = listOf("photo1.jpg", "photo2.jpg"),
            emotion = "开心", weather = "晴", amount = 100.0,
            remarks = "备注", promise = "承诺", conversationSummary = "对话摘要",
            giftName = "礼物", createdAt = 3000L, updatedAt = 4000L
        )
        val withParticipants = EventWithParticipants(event = entity, participants = emptyList())

        val domain = withParticipants.toDomain()

        assertThat(domain.id).isEqualTo(1)
        assertThat(domain.type).isEqualTo(EventType.MEETUP)
        assertThat(domain.title).isEqualTo("见面")
        assertThat(domain.description).isEqualTo("咖啡厅见面")
        assertThat(domain.time).isEqualTo(1000L)
        assertThat(domain.endTime).isEqualTo(2000L)
        assertThat(domain.location).isEqualTo("星巴克")
        assertThat(domain.latitude).isEqualTo(39.9)
        assertThat(domain.longitude).isEqualTo(116.4)
        assertThat(domain.photos).containsExactly("photo1.jpg", "photo2.jpg").inOrder()
        assertThat(domain.emotion).isEqualTo("开心")
        assertThat(domain.weather).isEqualTo("晴")
        assertThat(domain.amount).isEqualTo(100.0)
        assertThat(domain.remarks).isEqualTo("备注")
        assertThat(domain.promise).isEqualTo("承诺")
        assertThat(domain.conversationSummary).isEqualTo("对话摘要")
        assertThat(domain.giftName).isEqualTo("礼物")
        assertThat(domain.participants).isEmpty()
        assertThat(domain.createdAt).isEqualTo(3000L)
        assertThat(domain.updatedAt).isEqualTo(4000L)
    }

    @Test
    fun eventWithParticipants_roundtrip_preservesEntityFields() {
        val original = EventEntity(
            id = 1, type = "CALL", title = "通话", description = "电话沟通",
            time = 1000L, endTime = null, location = null,
            latitude = null, longitude = null, photos = emptyList(),
            emotion = null, weather = null, amount = null,
            remarks = null, promise = null, conversationSummary = null,
            giftName = null, createdAt = 3000L, updatedAt = 4000L
        )
        val withParticipants = EventWithParticipants(event = original, participants = emptyList())

        val roundtrip = withParticipants.toDomain().toEntity()

        assertThat(roundtrip).isEqualTo(original)
    }

    @Test
    fun event_toEntity_doesNotIncludeParticipants() {
        val domain = Event(
            id = 1, type = EventType.MEETUP, title = "见面", time = 1000L,
            participants = listOf(com.tang.prm.domain.model.Contact(id = 99, name = "张三"))
        )

        val entity = domain.toEntity()

        assertThat(entity.id).isEqualTo(1)
        assertThat(entity.title).isEqualTo("见面")
    }
}
