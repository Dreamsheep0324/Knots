package com.tang.prm.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class CardRarityTest {

    @Test
    fun getCardRarity_0_returnsN() {
        assertThat(getCardRarity(0)).isEqualTo(CardRarity.N)
    }

    @Test
    fun getCardRarity_20_returnsN() {
        assertThat(getCardRarity(20)).isEqualTo(CardRarity.N)
    }

    @Test
    fun getCardRarity_21_returnsR() {
        assertThat(getCardRarity(21)).isEqualTo(CardRarity.R)
    }

    @Test
    fun getCardRarity_40_returnsR() {
        assertThat(getCardRarity(40)).isEqualTo(CardRarity.R)
    }

    @Test
    fun getCardRarity_41_returnsSR() {
        assertThat(getCardRarity(41)).isEqualTo(CardRarity.SR)
    }

    @Test
    fun getCardRarity_60_returnsSR() {
        assertThat(getCardRarity(60)).isEqualTo(CardRarity.SR)
    }

    @Test
    fun getCardRarity_61_returnsSSR() {
        assertThat(getCardRarity(61)).isEqualTo(CardRarity.SSR)
    }

    @Test
    fun getCardRarity_80_returnsSSR() {
        assertThat(getCardRarity(80)).isEqualTo(CardRarity.SSR)
    }

    @Test
    fun getCardRarity_81_returnsUR() {
        assertThat(getCardRarity(81)).isEqualTo(CardRarity.UR)
    }

    @Test
    fun getCardRarity_100_returnsUR() {
        assertThat(getCardRarity(100)).isEqualTo(CardRarity.UR)
    }

    @Test
    fun getCardRarity_negative_returnsN() {
        assertThat(getCardRarity(-1)).isEqualTo(CardRarity.N)
    }

    @Test
    fun cardRarity_values_has5Entries() {
        assertThat(CardRarity.entries).hasSize(5)
    }

    @Test
    fun cardRarity_labels_areCorrect() {
        assertThat(CardRarity.N.label).isEqualTo("初识")
        assertThat(CardRarity.R.label).isEqualTo("泛交")
        assertThat(CardRarity.SR.label).isEqualTo("朋友")
        assertThat(CardRarity.SSR.label).isEqualTo("密友")
        assertThat(CardRarity.UR.label).isEqualTo("至亲")
    }
}
