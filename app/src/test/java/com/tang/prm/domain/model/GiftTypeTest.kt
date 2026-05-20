package com.tang.prm.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class GiftTypeTest {

    @Test
    fun giftType_values_has10Entries() {
        assertThat(GiftType.entries).hasSize(10)
    }

    @Test
    fun giftType_digital_keyIsDIGITAL() {
        assertThat(GiftType.DIGITAL.key).isEqualTo("DIGITAL")
    }

    @Test
    fun giftType_digital_displayNameIs数码() {
        assertThat(GiftType.DIGITAL.displayName).isEqualTo("数码")
    }

    @Test
    fun giftType_ordinalsAreConsecutive() {
        val ordinals = GiftType.entries.map { it.ordinal }
        assertThat(ordinals).containsExactlyElementsIn(0 until GiftType.entries.size).inOrder()
    }

    @Test
    fun giftType_allKeysUnique() {
        val keys = GiftType.entries.map { it.key }
        assertThat(keys).hasSize(keys.toSet().size)
    }
}
