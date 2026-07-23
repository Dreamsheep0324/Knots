package com.tang.prm.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

class IntimacyTierTest {

    @Nested
    @DisplayName("of() 阈值映射")
    inner class OfTest {

        @ParameterizedTest
        @CsvSource(
            "0, NEW", "1, NEW", "14, NEW",
            "15, ACQUAINTANCE", "20, ACQUAINTANCE", "39, ACQUAINTANCE",
            "40, FRIEND", "50, FRIEND", "74, FRIEND",
            "75, CLOSE", "80, CLOSE", "89, CLOSE",
            "90, FAMILY", "95, FAMILY", "100, FAMILY"
        )
        fun `score maps to correct tier`(score: Int, expected: IntimacyTier) {
            assertThat(IntimacyTier.of(score)).isEqualTo(expected)
        }

        @Test
        fun `negative score coerced to 0`() {
            assertThat(IntimacyTier.of(-1)).isEqualTo(IntimacyTier.NEW)
        }

        @Test
        fun `score above 100 coerced to 100`() {
            assertThat(IntimacyTier.of(200)).isEqualTo(IntimacyTier.FAMILY)
        }
    }

    @Nested
    @DisplayName("边界值验证")
    inner class BoundaryTest {

        @Test
        fun `new range is 0 to 14`() {
            assertThat(IntimacyTier.NEW.minScore).isEqualTo(0)
            assertThat(IntimacyTier.NEW.maxScore).isEqualTo(14)
        }

        @Test
        fun `acquaintance range is 15 to 39`() {
            assertThat(IntimacyTier.ACQUAINTANCE.minScore).isEqualTo(15)
            assertThat(IntimacyTier.ACQUAINTANCE.maxScore).isEqualTo(39)
        }

        @Test
        fun `friend range is 40 to 74`() {
            assertThat(IntimacyTier.FRIEND.minScore).isEqualTo(40)
            assertThat(IntimacyTier.FRIEND.maxScore).isEqualTo(74)
        }

        @Test
        fun `close range is 75 to 89`() {
            assertThat(IntimacyTier.CLOSE.minScore).isEqualTo(75)
            assertThat(IntimacyTier.CLOSE.maxScore).isEqualTo(89)
        }

        @Test
        fun `family range is 90 to 100`() {
            assertThat(IntimacyTier.FAMILY.minScore).isEqualTo(90)
            assertThat(IntimacyTier.FAMILY.maxScore).isEqualTo(100)
        }

        @Test
        fun `all ranges are contiguous`() {
            val tiers = IntimacyTier.entries
            for (i in 0 until tiers.size - 1) {
                assertThat(tiers[i].maxScore + 1).isEqualTo(tiers[i + 1].minScore)
            }
        }
    }

    @Nested
    @DisplayName("属性验证")
    inner class PropertyTest {

        @Test
        fun `has 5 tiers`() {
            assertThat(IntimacyTier.entries).hasSize(5)
        }

        @ParameterizedTest
        @ValueSource(strings = ["NEW", "ACQUAINTANCE", "FRIEND", "CLOSE", "FAMILY"])
        fun `each tier has card rarity`(tierName: String) {
            val tier = IntimacyTier.valueOf(tierName)
            assertThat(tier.cardRarity).isNotEmpty()
        }

        @ParameterizedTest
        @ValueSource(strings = ["NEW", "ACQUAINTANCE", "FRIEND", "CLOSE", "FAMILY"])
        fun `each tier has label`(tierName: String) {
            val tier = IntimacyTier.valueOf(tierName)
            assertThat(tier.label).isNotEmpty()
        }

        @Test
        fun `stars are 1 to 5`() {
            IntimacyTier.entries.forEach { tier ->
                assertThat(tier.stars).isIn(1..5)
            }
        }
    }

    @Nested
    @DisplayName("扩展函数")
    inner class ExtensionTest {

        @Test
        fun `getIntimacyLabel returns correct label`() {
            assertThat(getIntimacyLabel(0)).isEqualTo("初识")
            assertThat(getIntimacyLabel(50)).isEqualTo("朋友")
            assertThat(getIntimacyLabel(100)).isEqualTo("至亲")
        }

        @Test
        fun `getCardRarityLabel returns correct rarity`() {
            assertThat(getCardRarityLabel(0)).isEqualTo("N")
            assertThat(getCardRarityLabel(50)).isEqualTo("SR")
            assertThat(getCardRarityLabel(100)).isEqualTo("UR")
        }

        @Test
        fun `getIntimacyColor returns non null color`() {
            IntimacyTier.entries.forEach { tier ->
                assertThat(tier.colorValue).isNotEqualTo(0L)
            }
        }
    }
}
