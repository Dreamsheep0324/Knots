package com.tang.prm.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class CardRarityTest {

    @Test
    fun getCardRarity_0_returnsN() {
        assertThat(getCardRarity(0)).isEqualTo(CardRarity.N)
    }

    @Test
    fun getCardRarity_14_returnsN() {
        assertThat(getCardRarity(14)).isEqualTo(CardRarity.N)
    }

    @Test
    fun getCardRarity_15_returnsR() {
        assertThat(getCardRarity(15)).isEqualTo(CardRarity.R)
    }

    @Test
    fun getCardRarity_39_returnsR() {
        assertThat(getCardRarity(39)).isEqualTo(CardRarity.R)
    }

    @Test
    fun getCardRarity_40_returnsSR() {
        assertThat(getCardRarity(40)).isEqualTo(CardRarity.SR)
    }

    @Test
    fun getCardRarity_74_returnsSR() {
        assertThat(getCardRarity(74)).isEqualTo(CardRarity.SR)
    }

    @Test
    fun getCardRarity_75_returnsSSR() {
        assertThat(getCardRarity(75)).isEqualTo(CardRarity.SSR)
    }

    @Test
    fun getCardRarity_89_returnsSSR() {
        assertThat(getCardRarity(89)).isEqualTo(CardRarity.SSR)
    }

    @Test
    fun getCardRarity_90_returnsUR() {
        assertThat(getCardRarity(90)).isEqualTo(CardRarity.UR)
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
