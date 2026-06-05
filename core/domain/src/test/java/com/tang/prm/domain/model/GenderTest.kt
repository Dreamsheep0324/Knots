package com.tang.prm.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class GenderTest {

    @Test
    fun gender_hasThreeEntries() {
        assertThat(Gender.entries).hasSize(3)
    }

    @Test
    fun gender_valuesAreCorrect() {
        assertThat(Gender.UNKNOWN.value).isEqualTo(0)
        assertThat(Gender.MALE.value).isEqualTo(1)
        assertThat(Gender.FEMALE.value).isEqualTo(2)
    }

    @Test
    fun fromValue_zero_returnsUnknown() {
        assertThat(Gender.fromValue(0)).isEqualTo(Gender.UNKNOWN)
    }

    @Test
    fun fromValue_one_returnsMale() {
        assertThat(Gender.fromValue(1)).isEqualTo(Gender.MALE)
    }

    @Test
    fun fromValue_two_returnsFemale() {
        assertThat(Gender.fromValue(2)).isEqualTo(Gender.FEMALE)
    }

    @Test
    fun fromValue_invalid_returnsUnknown() {
        assertThat(Gender.fromValue(-1)).isEqualTo(Gender.UNKNOWN)
        assertThat(Gender.fromValue(99)).isEqualTo(Gender.UNKNOWN)
    }

    @Test
    fun roundtrip_preservesGender() {
        Gender.entries.forEach { gender ->
            assertThat(Gender.fromValue(gender.value)).isEqualTo(gender)
        }
    }
}
