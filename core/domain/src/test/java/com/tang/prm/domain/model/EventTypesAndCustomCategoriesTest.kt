package com.tang.prm.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.lang.reflect.Modifier

class EventTypesAndCustomCategoriesTest {

    @Test
    fun `eventType has 8 entries`() {
        assertThat(EventType.entries).hasSize(8)
    }

    @Test
    fun `eventType names are unique`() {
        val names = EventType.entries.map { it.name }
        assertThat(names).hasSize(names.toSet().size)
    }

    @Test
    fun `sourceTypes has 8 constants`() {
        val fields = SourceTypes::class.java.declaredFields
            .filter { Modifier.isPublic(it.modifiers) && Modifier.isStatic(it.modifiers) && Modifier.isFinal(it.modifiers) && it.type == String::class.java }
        assertThat(fields).hasSize(8)
    }

    @Test
    fun `sourceTypes values are unique`() {
        val fields = SourceTypes::class.java.declaredFields
            .filter { Modifier.isPublic(it.modifiers) && Modifier.isStatic(it.modifiers) && Modifier.isFinal(it.modifiers) && it.type == String::class.java }
        val values = fields.map { it.get(null) as String }
        assertThat(values).hasSize(values.toSet().size)
    }

    @Test
    fun `customCategories has 12 constants`() {
        val fields = CustomCategories::class.java.declaredFields
            .filter { Modifier.isPublic(it.modifiers) && Modifier.isStatic(it.modifiers) && Modifier.isFinal(it.modifiers) && it.type == String::class.java }
        assertThat(fields).hasSize(12)
    }

    @Test
    fun `customCategories values are unique`() {
        val fields = CustomCategories::class.java.declaredFields
            .filter { Modifier.isPublic(it.modifiers) && Modifier.isStatic(it.modifiers) && Modifier.isFinal(it.modifiers) && it.type == String::class.java }
        val values = fields.map { it.get(null) as String }
        assertThat(values).hasSize(values.toSet().size)
    }
}
