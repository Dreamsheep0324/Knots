package com.tang.prm.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class CardRarityTest {

    @Test
    fun `getCardRarityLabel 0 returns N`() {
        assertThat(getCardRarityLabel(0)).isEqualTo("N")
    }

    @Test
    fun `getCardRarityLabel 14 returns N`() {
        assertThat(getCardRarityLabel(14)).isEqualTo("N")
    }

    @Test
    fun `getCardRarityLabel 15 returns R`() {
        assertThat(getCardRarityLabel(15)).isEqualTo("R")
    }

    @Test
    fun `getCardRarityLabel 39 returns R`() {
        assertThat(getCardRarityLabel(39)).isEqualTo("R")
    }

    @Test
    fun `getCardRarityLabel 40 returns SR`() {
        assertThat(getCardRarityLabel(40)).isEqualTo("SR")
    }

    @Test
    fun `getCardRarityLabel 74 returns SR`() {
        assertThat(getCardRarityLabel(74)).isEqualTo("SR")
    }

    @Test
    fun `getCardRarityLabel 75 returns SSR`() {
        assertThat(getCardRarityLabel(75)).isEqualTo("SSR")
    }

    @Test
    fun `getCardRarityLabel 89 returns SSR`() {
        assertThat(getCardRarityLabel(89)).isEqualTo("SSR")
    }

    @Test
    fun `getCardRarityLabel 90 returns UR`() {
        assertThat(getCardRarityLabel(90)).isEqualTo("UR")
    }

    @Test
    fun `getCardRarityLabel 100 returns UR`() {
        assertThat(getCardRarityLabel(100)).isEqualTo("UR")
    }

    @Test
    fun `getCardRarityLabel negative returns N`() {
        assertThat(getCardRarityLabel(-1)).isEqualTo("N")
    }

    @Test
    fun `intimacyTier entries has 5 entries`() {
        assertThat(IntimacyTier.entries).hasSize(5)
    }

    @Test
    fun `intimacyTier cardRarityLabels are correct`() {
        assertThat(IntimacyTier.NEW.cardRarity).isEqualTo("N")
        assertThat(IntimacyTier.ACQUAINTANCE.cardRarity).isEqualTo("R")
        assertThat(IntimacyTier.FRIEND.cardRarity).isEqualTo("SR")
        assertThat(IntimacyTier.CLOSE.cardRarity).isEqualTo("SSR")
        assertThat(IntimacyTier.FAMILY.cardRarity).isEqualTo("UR")
    }
}
