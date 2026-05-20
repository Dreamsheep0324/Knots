package com.tang.prm.data.mapper

import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.entity.GiftEntity
import com.tang.prm.domain.model.Gift
import org.junit.jupiter.api.Test

class GiftMapperTest {

    @Test
    fun giftEntity_toDomain_mapsAllFields() {
        val entity = GiftEntity(
            id = 1, contactId = 10, giftName = "手表", giftType = "LUXURY",
            date = 1000L, isSent = true, amount = 5000.0,
            occasion = "生日", description = "精工手表",
            location = "商场", photos = listOf("watch1.jpg", "watch2.jpg"),
            createdAt = 2000L, updatedAt = 3000L
        )

        val domain = entity.toDomain()

        assertThat(domain.id).isEqualTo(1)
        assertThat(domain.contactId).isEqualTo(10)
        assertThat(domain.giftName).isEqualTo("手表")
        assertThat(domain.giftType).isEqualTo("LUXURY")
        assertThat(domain.date).isEqualTo(1000L)
        assertThat(domain.isSent).isTrue()
        assertThat(domain.amount).isEqualTo(5000.0)
        assertThat(domain.occasion).isEqualTo("生日")
        assertThat(domain.description).isEqualTo("精工手表")
        assertThat(domain.location).isEqualTo("商场")
        assertThat(domain.photos).containsExactly("watch1.jpg", "watch2.jpg").inOrder()
        assertThat(domain.createdAt).isEqualTo(2000L)
        assertThat(domain.updatedAt).isEqualTo(3000L)
    }

    @Test
    fun gift_toEntity_mapsAllFields() {
        val domain = Gift(
            id = 1, contactId = 10, giftName = "手表", giftType = "LUXURY",
            date = 1000L, isSent = true, amount = 5000.0,
            occasion = "生日", description = "精工手表",
            location = "商场", photos = listOf("watch1.jpg"),
            createdAt = 2000L, updatedAt = 3000L
        )

        val entity = domain.toEntity()

        assertThat(entity.id).isEqualTo(1)
        assertThat(entity.contactId).isEqualTo(10)
        assertThat(entity.giftName).isEqualTo("手表")
        assertThat(entity.giftType).isEqualTo("LUXURY")
        assertThat(entity.date).isEqualTo(1000L)
        assertThat(entity.isSent).isTrue()
        assertThat(entity.amount).isEqualTo(5000.0)
        assertThat(entity.occasion).isEqualTo("生日")
        assertThat(entity.description).isEqualTo("精工手表")
        assertThat(entity.location).isEqualTo("商场")
        assertThat(entity.photos).containsExactly("watch1.jpg").inOrder()
        assertThat(entity.createdAt).isEqualTo(2000L)
        assertThat(entity.updatedAt).isEqualTo(3000L)
    }

    @Test
    fun giftEntity_roundtrip_preservesAllFields() {
        val original = GiftEntity(
            id = 1, contactId = 10, giftName = "书", giftType = "BOOK",
            date = 1000L, isSent = false, amount = null,
            occasion = null, description = null,
            location = null, photos = emptyList(),
            createdAt = 2000L, updatedAt = 3000L
        )

        val roundtrip = original.toDomain().toEntity()

        assertThat(roundtrip).isEqualTo(original)
    }

    @Test
    fun giftEntity_isSent_preserved() {
        val entity = GiftEntity(
            id = 1, contactId = 10, giftName = "花", giftType = "FLOWER",
            date = 1000L, isSent = true, amount = null,
            occasion = null, description = null,
            location = null, photos = emptyList()
        )

        val domain = entity.toDomain()
        val roundtrip = domain.toEntity()

        assertThat(domain.isSent).isTrue()
        assertThat(roundtrip.isSent).isTrue()
    }

    @Test
    fun giftEntity_isSent_false_preserved() {
        val entity = GiftEntity(
            id = 1, contactId = 10, giftName = "花", giftType = "FLOWER",
            date = 1000L, isSent = false, amount = null,
            occasion = null, description = null,
            location = null, photos = emptyList()
        )

        val domain = entity.toDomain()
        val roundtrip = domain.toEntity()

        assertThat(domain.isSent).isFalse()
        assertThat(roundtrip.isSent).isFalse()
    }
}
