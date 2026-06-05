package com.tang.prm.data.mapper

import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.entity.FavoriteEntity
import com.tang.prm.domain.model.Favorite
import org.junit.jupiter.api.Test

class FavoriteMapperTest {

    @Test
    fun favoriteEntity_toDomain_mapsAllFields() {
        val entity = FavoriteEntity(
            id = 1, sourceType = "event", sourceId = 100,
            title = "重要事件", description = "描述", createdAt = 1000L
        )

        val domain = entity.toDomain()

        assertThat(domain.id).isEqualTo(1)
        assertThat(domain.sourceType).isEqualTo("event")
        assertThat(domain.sourceId).isEqualTo(100)
        assertThat(domain.title).isEqualTo("重要事件")
        assertThat(domain.description).isEqualTo("描述")
        assertThat(domain.createdAt).isEqualTo(1000L)
    }

    @Test
    fun favorite_toEntity_mapsAllFields() {
        val domain = Favorite(
            id = 1, sourceType = "contact", sourceId = 200,
            title = "重要联系人", description = "备注", createdAt = 1000L
        )

        val entity = domain.toEntity()

        assertThat(entity.id).isEqualTo(1)
        assertThat(entity.sourceType).isEqualTo("contact")
        assertThat(entity.sourceId).isEqualTo(200)
        assertThat(entity.title).isEqualTo("重要联系人")
        assertThat(entity.description).isEqualTo("备注")
        assertThat(entity.createdAt).isEqualTo(1000L)
    }

    @Test
    fun favoriteEntity_roundtrip_preservesAllFields() {
        val original = FavoriteEntity(
            id = 1, sourceType = "thought", sourceId = 300,
            title = "收藏想法", description = null, createdAt = 1000L
        )

        val roundtrip = original.toDomain().toEntity()

        assertThat(roundtrip).isEqualTo(original)
    }

    @Test
    fun favoriteEntity_sourceType_preserved() {
        val entity = FavoriteEntity(id = 1, sourceType = "anniversary", sourceId = 50, title = "收藏", createdAt = 1000L)

        val domain = entity.toDomain()
        val roundtrip = domain.toEntity()

        assertThat(domain.sourceType).isEqualTo("anniversary")
        assertThat(roundtrip.sourceType).isEqualTo("anniversary")
    }

    @Test
    fun favoriteEntity_nullDescription_toDomain_isNull() {
        val entity = FavoriteEntity(id = 1, sourceType = "event", sourceId = 100, title = "标题", description = null, createdAt = 1000L)

        val domain = entity.toDomain()

        assertThat(domain.description).isNull()
    }
}
