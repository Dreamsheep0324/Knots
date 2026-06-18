package com.tang.prm.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class CardRarityTest {

    @Test
    fun getCardRarityLabel_0_returnsN() {
        assertThat(getCardRarityLabel(0)).isEqualTo("N")
    }

    @Test
    fun getCardRarityLabel_14_returnsN() {
        assertThat(getCardRarityLabel(14)).isEqualTo("N")
    }

    @Test
    fun getCardRarityLabel_15_returnsR() {
        assertThat(getCardRarityLabel(15)).isEqualTo("R")
    }

    @Test
    fun getCardRarityLabel_39_returnsR() {
        assertThat(getCardRarityLabel(39)).isEqualTo("R")
    }

    @Test
    fun getCardRarityLabel_40_returnsSR() {
        assertThat(getCardRarityLabel(40)).isEqualTo("SR")
    }

    @Test
    fun getCardRarityLabel_74_returnsSR() {
        assertThat(getCardRarityLabel(74)).isEqualTo("SR")
    }

    @Test
    fun getCardRarityLabel_75_returnsSSR() {
        assertThat(getCardRarityLabel(75)).isEqualTo("SSR")
    }

    @Test
    fun getCardRarityLabel_89_returnsSSR() {
        assertThat(getCardRarityLabel(89)).isEqualTo("SSR")
    }

    @Test
    fun getCardRarityLabel_90_returnsUR() {
        assertThat(getCardRarityLabel(90)).isEqualTo("UR")
    }

    @Test
    fun getCardRarityLabel_100_returnsUR() {
        assertThat(getCardRarityLabel(100)).isEqualTo("UR")
    }

    @Test
    fun getCardRarityLabel_negative_returnsN() {
        assertThat(getCardRarityLabel(-1)).isEqualTo("N")
    }

    @Test
    fun intimacyTier_entries_has5Entries() {
        assertThat(IntimacyTier.entries).hasSize(5)
    }

    @Test
    fun intimacyTier_cardRarityLabels_areCorrect() {
        assertThat(IntimacyTier.NEW.cardRarity).isEqualTo("N")
        assertThat(IntimacyTier.ACQUAINTANCE.cardRarity).isEqualTo("R")
        assertThat(IntimacyTier.FRIEND.cardRarity).isEqualTo("SR")
        assertThat(IntimacyTier.CLOSE.cardRarity).isEqualTo("SSR")
        assertThat(IntimacyTier.FAMILY.cardRarity).isEqualTo("UR")
    }
}
