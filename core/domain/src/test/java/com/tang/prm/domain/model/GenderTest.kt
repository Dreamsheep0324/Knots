package com.tang.prm.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class GenderTest {

    @Test
    fun `gender has three entries`() {
        assertThat(Gender.entries).hasSize(3)
    }

    @Test
    fun `gender values are correct`() {
        assertThat(Gender.UNKNOWN.value).isEqualTo(0)
        assertThat(Gender.MALE.value).isEqualTo(1)
        assertThat(Gender.FEMALE.value).isEqualTo(2)
    }

    @Test
    fun `fromValue zero returns UNKNOWN`() {
        assertThat(Gender.fromValue(0)).isEqualTo(Gender.UNKNOWN)
    }

    @Test
    fun `fromValue one returns MALE`() {
        assertThat(Gender.fromValue(1)).isEqualTo(Gender.MALE)
    }

    @Test
    fun `fromValue two returns FEMALE`() {
        assertThat(Gender.fromValue(2)).isEqualTo(Gender.FEMALE)
    }

    @Test
    fun `fromValue invalid returns UNKNOWN`() {
        assertThat(Gender.fromValue(-1)).isEqualTo(Gender.UNKNOWN)
        assertThat(Gender.fromValue(99)).isEqualTo(Gender.UNKNOWN)
    }

    @Test
    fun `roundtrip preserves gender`() {
        Gender.entries.forEach { gender ->
            assertThat(Gender.fromValue(gender.value)).isEqualTo(gender)
        }
    }
}
