package com.tang.prm.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class ThoughtTypeTest {

    @Test
    fun `thoughtType values has 3 entries`() {
        assertThat(ThoughtType.entries).hasSize(3)
    }

    @Test
    fun `thoughtType friend key is friend`() {
        assertThat(ThoughtType.FRIEND.key).isEqualTo("friend")
    }

    @Test
    fun `thoughtType plan key is plan`() {
        assertThat(ThoughtType.PLAN.key).isEqualTo("plan")
    }

    @Test
    fun `thoughtType murmur key is murmur`() {
        assertThat(ThoughtType.MURMUR.key).isEqualTo("murmur")
    }

    @Test
    fun `thoughtType fromKey friend returns FRIEND`() {
        assertThat(ThoughtType.fromKey("friend")).isEqualTo(ThoughtType.FRIEND)
    }

    @Test
    fun `thoughtType fromKey unknown returns MURMUR`() {
        assertThat(ThoughtType.fromKey("unknown")).isEqualTo(ThoughtType.MURMUR)
    }

    @Test
    fun `thoughtType fromKey empty returns MURMUR`() {
        assertThat(ThoughtType.fromKey("")).isEqualTo(ThoughtType.MURMUR)
    }
}
