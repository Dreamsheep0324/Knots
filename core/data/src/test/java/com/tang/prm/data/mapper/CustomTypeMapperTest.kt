package com.tang.prm.data.mapper

import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.entity.CustomTypeEntity
import com.tang.prm.domain.model.CustomType
import org.junit.jupiter.api.Test

class CustomTypeMapperTest {

    @Test
    fun customTypeEntity_toDomain_mapsAllFields() {
        val entity = CustomTypeEntity(
            id = 1, category = "EVENT_TYPE", name = "面试", key = "INTERVIEW",
            color = "#FF0000", icon = "briefcase", sortOrder = 3,
            isDefault = true, createdAt = 1000L
        )

        val domain = entity.toDomain()

        assertThat(domain.id).isEqualTo(1)
        assertThat(domain.category).isEqualTo("EVENT_TYPE")
        assertThat(domain.name).isEqualTo("面试")
        assertThat(domain.key).isEqualTo("INTERVIEW")
        assertThat(domain.color).isEqualTo("#FF0000")
        assertThat(domain.icon).isEqualTo("briefcase")
        assertThat(domain.sortOrder).isEqualTo(3)
        assertThat(domain.isDefault).isTrue()
        assertThat(domain.createdAt).isEqualTo(1000L)
    }

    @Test
    fun customType_toEntity_mapsAllFields() {
        val domain = CustomType(
            id = 1, category = "EMOTION", name = "感动", key = "TOUCHED",
            color = "#00FF00", icon = "heart", sortOrder = 5,
            isDefault = false, createdAt = 2000L
        )

        val entity = domain.toEntity()

        assertThat(entity.id).isEqualTo(1)
        assertThat(entity.category).isEqualTo("EMOTION")
        assertThat(entity.name).isEqualTo("感动")
        assertThat(entity.key).isEqualTo("TOUCHED")
        assertThat(entity.color).isEqualTo("#00FF00")
        assertThat(entity.icon).isEqualTo("heart")
        assertThat(entity.sortOrder).isEqualTo(5)
        assertThat(entity.isDefault).isFalse()
        assertThat(entity.createdAt).isEqualTo(2000L)
    }

    @Test
    fun customTypeEntity_roundtrip_preservesAllFields() {
        val original = CustomTypeEntity(
            id = 1, category = "RELATIONSHIP", name = "同学", key = "CLASSMATE",
            color = null, icon = null, sortOrder = 0,
            isDefault = false, createdAt = 1000L
        )

        val roundtrip = original.toDomain().toEntity()

        assertThat(roundtrip.id).isEqualTo(original.id)
        assertThat(roundtrip.category).isEqualTo(original.category)
        assertThat(roundtrip.name).isEqualTo(original.name)
        assertThat(roundtrip.key).isEqualTo(original.key)
        assertThat(roundtrip.color).isEqualTo(original.color)
        assertThat(roundtrip.icon).isEqualTo(original.icon)
        assertThat(roundtrip.sortOrder).isEqualTo(original.sortOrder)
        assertThat(roundtrip.isDefault).isEqualTo(original.isDefault)
        assertThat(roundtrip.createdAt).isEqualTo(original.createdAt)
    }

    @Test
    fun customTypeEntity_category_preserved() {
        val entity = CustomTypeEntity(
            id = 1, category = "ANNIVERSARY_TYPE", name = "纪念日", key = "ANNIV",
            color = "#0000FF", icon = "calendar", sortOrder = 1,
            isDefault = true, createdAt = 1000L
        )

        val domain = entity.toDomain()
        val roundtrip = domain.toEntity()

        assertThat(domain.category).isEqualTo("ANNIVERSARY_TYPE")
        assertThat(roundtrip.category).isEqualTo("ANNIVERSARY_TYPE")
    }
}
