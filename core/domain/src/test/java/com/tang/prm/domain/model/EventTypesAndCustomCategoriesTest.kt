package com.tang.prm.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.lang.reflect.Modifier

class EventTypesAndCustomCategoriesTest {

    @Test
    fun eventType_has8Entries() {
        assertThat(EventType.entries).hasSize(8)
    }

    @Test
    fun eventType_namesAreUnique() {
        val names = EventType.entries.map { it.name }
        assertThat(names).hasSize(names.toSet().size)
    }

    @Test
    fun sourceTypes_has8Constants() {
        val fields = SourceTypes::class.java.declaredFields
            .filter { Modifier.isPublic(it.modifiers) && Modifier.isStatic(it.modifiers) && Modifier.isFinal(it.modifiers) && it.type == String::class.java }
        assertThat(fields).hasSize(8)
    }

    @Test
    fun sourceTypes_valuesAreUnique() {
        val fields = SourceTypes::class.java.declaredFields
            .filter { Modifier.isPublic(it.modifiers) && Modifier.isStatic(it.modifiers) && Modifier.isFinal(it.modifiers) && it.type == String::class.java }
        val values = fields.map { it.get(null) as String }
        assertThat(values).hasSize(values.toSet().size)
    }

    @Test
    fun customCategories_has11Constants() {
        val fields = CustomCategories::class.java.declaredFields
            .filter { Modifier.isPublic(it.modifiers) && Modifier.isStatic(it.modifiers) && Modifier.isFinal(it.modifiers) && it.type == String::class.java }
        assertThat(fields).hasSize(11)
    }

    @Test
    fun customCategories_valuesAreUnique() {
        val fields = CustomCategories::class.java.declaredFields
            .filter { Modifier.isPublic(it.modifiers) && Modifier.isStatic(it.modifiers) && Modifier.isFinal(it.modifiers) && it.type == String::class.java }
        val values = fields.map { it.get(null) as String }
        assertThat(values).hasSize(values.toSet().size)
    }
}
