package com.tang.prm.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class GiftTypeTest {

    @Test
    fun `giftType values has 10 entries`() {
        assertThat(GiftType.entries).hasSize(10)
    }

    @Test
    fun `giftType digital key is DIGITAL`() {
        assertThat(GiftType.DIGITAL.key).isEqualTo("DIGITAL")
    }

    @Test
    fun `giftType digital displayName is 数码`() {
        assertThat(GiftType.DIGITAL.displayName).isEqualTo("数码")
    }

    @Test
    fun `giftType ordinals are consecutive`() {
        val ordinals = GiftType.entries.map { it.ordinal }
        assertThat(ordinals).containsExactlyElementsIn(0 until GiftType.entries.size).inOrder()
    }

    @Test
    fun `giftType all keys unique`() {
        val keys = GiftType.entries.map { it.key }
        assertThat(keys).hasSize(keys.toSet().size)
    }
}
