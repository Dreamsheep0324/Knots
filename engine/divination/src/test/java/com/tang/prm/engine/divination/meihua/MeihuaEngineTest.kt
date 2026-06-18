package com.tang.prm.engine.divination.meihua

import com.google.common.truth.Truth.assertThat
import com.tang.prm.engine.divination.data.ExternalOmenData
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Date

class MeihuaEngineTest {

    private val fixedDate = Date(1718438400000L) // 2024-06-15 08:00:00 UTC

    @Nested
    @DisplayName("数字起卦法")
    inner class NumberMethodTest {

        @Test
        fun number1and1_yieldsQianGua() {
            // mod8(1)=1 → 乾(上卦)，mod8(1)=1 → 乾(下卦) → 乾为天
            val data = MeihuaEngine.generate(
                method = MeihuaEngine.Method.NUMBER,
                number = 1,
                numberB = 1,
                customDate = fixedDate
            )
            assertThat(data.originalName).isEqualTo("乾为天")
            assertThat(data.mainHexagram.upper).isEqualTo("乾")
            assertThat(data.mainHexagram.lower).isEqualTo("乾")
        }

        @Test
        fun number8and8_yieldsKunGua() {
            // mod8(8)=8 → 坤(上卦)，mod8(8)=8 → 坤(下卦) → 坤为地
            val data = MeihuaEngine.generate(
                method = MeihuaEngine.Method.NUMBER,
                number = 8,
                numberB = 8,
                customDate = fixedDate
            )
            assertThat(data.originalName).isEqualTo("坤为地")
        }

        @Test
        fun number16and8_yieldsKunGua() {
            // mod8(16)=8 → 坤(上卦)
            val data = MeihuaEngine.generate(
                method = MeihuaEngine.Method.NUMBER,
                number = 16,
                numberB = 8,
                customDate = fixedDate
            )
            assertThat(data.mainHexagram.upper).isEqualTo("坤")
        }

        @Test
        fun movingYao_mod6OfSum() {
            // mod6(1+1)=mod6(2)=2
            val data = MeihuaEngine.generate(
                method = MeihuaEngine.Method.NUMBER,
                number = 1,
                numberB = 1,
                customDate = fixedDate
            )
            assertThat(data.movingYao.position).isEqualTo(2)
        }

        @Test
        fun movingYao_mod6Of12_returns6() {
            // mod6(6+6)=mod6(12)=12%6=0→6
            val data = MeihuaEngine.generate(
                method = MeihuaEngine.Method.NUMBER,
                number = 6,
                numberB = 6,
                customDate = fixedDate
            )
            assertThat(data.movingYao.position).isEqualTo(6)
        }

        @Test
        fun zeroNumber_throwsException() {
            assertThrows<IllegalArgumentException> {
                MeihuaEngine.generate(
                    method = MeihuaEngine.Method.NUMBER,
                    number = 0,
                    numberB = 1,
                    customDate = fixedDate
                )
            }
        }

        @Test
        fun zeroNumberB_throwsException() {
            assertThrows<IllegalArgumentException> {
                MeihuaEngine.generate(
                    method = MeihuaEngine.Method.NUMBER,
                    number = 1,
                    numberB = 0,
                    customDate = fixedDate
                )
            }
        }

        @Test
        fun negativeNumber_throwsException() {
            assertThrows<IllegalArgumentException> {
                MeihuaEngine.generate(
                    method = MeihuaEngine.Method.NUMBER,
                    number = -1,
                    numberB = 1,
                    customDate = fixedDate
                )
            }
        }

        @Test
        fun calculation_hasNumberMethodKey() {
            val data = MeihuaEngine.generate(
                method = MeihuaEngine.Method.NUMBER,
                number = 3,
                numberB = 5,
                customDate = fixedDate
            )
            assertThat(data.calculation?.methodKey).isEqualTo("number")
            assertThat(data.calculation?.number).isEqualTo(3)
            assertThat(data.calculation?.numberB).isEqualTo(5)
        }
    }

    @Nested
    @DisplayName("时间起卦法")
    inner class TimeMethodTest {

        @Test
        fun producesValidHexagram() {
            val data = MeihuaEngine.generate(
                method = MeihuaEngine.Method.TIME,
                customDate = fixedDate
            )
            assertThat(data.originalName).isNotEmpty()
            assertThat(data.mainHexagram.upper).isNotEmpty()
            assertThat(data.mainHexagram.lower).isNotEmpty()
        }

        @Test
        fun calculation_hasTimeMethodKey() {
            val data = MeihuaEngine.generate(
                method = MeihuaEngine.Method.TIME,
                customDate = fixedDate
            )
            assertThat(data.calculation?.methodKey).isEqualTo("time")
            assertThat(data.calculation?.yearZhi).isNotNull()
            assertThat(data.calculation?.month).isNotNull()
            assertThat(data.calculation?.day).isNotNull()
            assertThat(data.calculation?.timeZhi).isNotNull()
        }

        @Test
        fun sameDate_yieldsSameResult() {
            val a = MeihuaEngine.generate(method = MeihuaEngine.Method.TIME, customDate = fixedDate)
            val b = MeihuaEngine.generate(method = MeihuaEngine.Method.TIME, customDate = fixedDate)
            assertThat(a.originalName).isEqualTo(b.originalName)
            assertThat(a.movingYao.position).isEqualTo(b.movingYao.position)
        }
    }

    @Nested
    @DisplayName("随机起卦法")
    inner class RandomMethodTest {

        @Test
        fun producesValidHexagram() {
            val data = MeihuaEngine.generate(
                method = MeihuaEngine.Method.RANDOM,
                customDate = fixedDate
            )
            assertThat(data.originalName).isNotEmpty()
            assertThat(data.movingYao.position).isIn(1..6)
        }

        @Test
        fun calculation_hasRandomMethodKey() {
            val data = MeihuaEngine.generate(
                method = MeihuaEngine.Method.RANDOM,
                customDate = fixedDate
            )
            assertThat(data.calculation?.methodKey).isEqualTo("random")
        }
    }

    @Nested
    @DisplayName("外应起卦法")
    inner class ExternalMethodTest {

        private val selections = mapOf(
            "direction" to ExternalOmenData.directionOptions[0], // 东 → 震(4)
            "person" to ExternalOmenData.personOptions[0]         // 老父 → 乾(1)
        )

        @Test
        fun producesValidHexagram() {
            val data = MeihuaEngine.generate(
                method = MeihuaEngine.Method.EXTERNAL,
                externalSelections = selections,
                externalCount = 3,
                customDate = fixedDate
            )
            assertThat(data.originalName).isNotEmpty()
        }

        @Test
        fun upperTrigramFromFirstSelection() {
            val data = MeihuaEngine.generate(
                method = MeihuaEngine.Method.EXTERNAL,
                externalSelections = selections,
                externalCount = 3,
                customDate = fixedDate
            )
            // direction=东→震(4) is first in priority → upper trigram
            assertThat(data.mainHexagram.upper).isEqualTo("震")
            // person=老父→乾(1) is second → lower trigram
            assertThat(data.mainHexagram.lower).isEqualTo("乾")
        }

        @Test
        fun movingYaoFromCount() {
            // mod6(3)=3
            val data = MeihuaEngine.generate(
                method = MeihuaEngine.Method.EXTERNAL,
                externalSelections = selections,
                externalCount = 3,
                customDate = fixedDate
            )
            assertThat(data.movingYao.position).isEqualTo(3)
        }

        @Test
        fun insufficientSelections_throwsException() {
            val oneSelection = mapOf("direction" to ExternalOmenData.directionOptions[0])
            assertThrows<IllegalArgumentException> {
                MeihuaEngine.generate(
                    method = MeihuaEngine.Method.EXTERNAL,
                    externalSelections = oneSelection,
                    externalCount = 3,
                    customDate = fixedDate
                )
            }
        }

        @Test
        fun zeroCount_throwsException() {
            assertThrows<IllegalArgumentException> {
                MeihuaEngine.generate(
                    method = MeihuaEngine.Method.EXTERNAL,
                    externalSelections = selections,
                    externalCount = 0,
                    customDate = fixedDate
                )
            }
        }

        @Test
        fun calculation_hasExternalMethodKey() {
            val data = MeihuaEngine.generate(
                method = MeihuaEngine.Method.EXTERNAL,
                externalSelections = selections,
                externalCount = 3,
                customDate = fixedDate
            )
            assertThat(data.calculation?.methodKey).isEqualTo("external")
            assertThat(data.calculation?.externalSummary).isNotNull()
        }
    }

    @Nested
    @DisplayName("体用关系")
    inner class TiYongTest {

        @Test
        fun movingYaoInLower_trigram_makesUpperTi() {
            // number=8(坤), numberB=8(坤), movingYao=mod6(16)=4 → 上卦动 → 下卦为体
            val data = MeihuaEngine.generate(
                method = MeihuaEngine.Method.NUMBER,
                number = 8,
                numberB = 8,
                customDate = fixedDate
            )
            // movingYao=4 > 3 → 上卦为用，下卦为体
            assertThat(data.tiGua.name).isEqualTo("坤")
            assertThat(data.yongGua.name).isEqualTo("坤")
        }

        @Test
        fun movingYaoInUpper_trigram_makesLowerTi() {
            // number=1(乾), numberB=1(乾), movingYao=mod6(2)=2 → 下卦动 → 上卦为体
            val data = MeihuaEngine.generate(
                method = MeihuaEngine.Method.NUMBER,
                number = 1,
                numberB = 1,
                customDate = fixedDate
            )
            // movingYao=2 ≤ 3 → 上卦为体，下卦为用
            assertThat(data.tiGua.name).isEqualTo("乾")
            assertThat(data.yongGua.name).isEqualTo("乾")
        }

        @Test
        fun yaosDetail_hasTiYongLabels() {
            // 使用不同上下卦避免纯卦情况：number=1(乾), numberB=8(坤)
            // movingYao=mod6(9)=3 → 下卦动 → 上卦为体
            val data = MeihuaEngine.generate(
                method = MeihuaEngine.Method.NUMBER,
                number = 1,
                numberB = 8,
                customDate = fixedDate
            )
            val tiCount = data.yaosDetail.count { it.tiYong == "体" }
            val yongCount = data.yaosDetail.count { it.tiYong == "用" }
            assertThat(tiCount).isEqualTo(3)
            assertThat(yongCount).isEqualTo(3)
        }
    }

    @Nested
    @DisplayName("数据结构完整性")
    inner class DataStructureTest {

        @Test
        fun yaosDetail_hasSixEntries() {
            val data = MeihuaEngine.generate(
                method = MeihuaEngine.Method.NUMBER,
                number = 1, numberB = 1,
                customDate = fixedDate
            )
            assertThat(data.yaosDetail).hasSize(6)
        }

        @Test
        fun yaosDetail_positionsAreOneToSix() {
            val data = MeihuaEngine.generate(
                method = MeihuaEngine.Method.NUMBER,
                number = 1, numberB = 1,
                customDate = fixedDate
            )
            val positions = data.yaosDetail.map { it.position }
            assertThat(positions).containsExactly(1, 2, 3, 4, 5, 6).inOrder()
        }

        @Test
        fun exactlyOneChangingYao() {
            val data = MeihuaEngine.generate(
                method = MeihuaEngine.Method.NUMBER,
                number = 1, numberB = 1,
                customDate = fixedDate
            )
            val changingCount = data.yaosDetail.count { it.isChanging }
            assertThat(changingCount).isEqualTo(1)
        }

        @Test
        fun movingYao_hasCorrectName() {
            val data = MeihuaEngine.generate(
                method = MeihuaEngine.Method.NUMBER,
                number = 1, numberB = 1,
                customDate = fixedDate
            )
            // movingYao=2 → 二爻
            assertThat(data.movingYao.yaoName).isEqualTo("二爻")
            assertThat(data.movingYao.description).contains("第2爻动")
        }

        @Test
        fun analysis_hasAllFields() {
            val data = MeihuaEngine.generate(
                method = MeihuaEngine.Method.NUMBER,
                number = 1, numberB = 1,
                customDate = fixedDate
            )
            assertThat(data.analysis.season).isNotEmpty()
            assertThat(data.analysis.tiYongRelation).isNotEmpty()
            assertThat(data.analysis.tiSeasonState).isNotEmpty()
            assertThat(data.analysis.yongSeasonState).isNotEmpty()
        }

        @Test
        fun ganzhi_hasFourFields() {
            val data = MeihuaEngine.generate(
                method = MeihuaEngine.Method.NUMBER,
                number = 1, numberB = 1,
                customDate = fixedDate
            )
            assertThat(data.ganzhi.year).isNotEmpty()
            assertThat(data.ganzhi.month).isNotEmpty()
            assertThat(data.ganzhi.day).isNotEmpty()
            assertThat(data.ganzhi.hour).isNotEmpty()
        }

        @Test
        fun timestamp_matchesInputDate() {
            val data = MeihuaEngine.generate(
                method = MeihuaEngine.Method.NUMBER,
                number = 1, numberB = 1,
                customDate = fixedDate
            )
            assertThat(data.timestamp).isEqualTo(fixedDate.time)
        }

        @Test
        fun changedHexagram_isNotEmpty() {
            val data = MeihuaEngine.generate(
                method = MeihuaEngine.Method.NUMBER,
                number = 1, numberB = 1,
                customDate = fixedDate
            )
            assertThat(data.changedName).isNotEmpty()
            assertThat(data.changedHexagram).isNotNull()
        }

        @Test
        fun interHexagram_isNotEmpty() {
            val data = MeihuaEngine.generate(
                method = MeihuaEngine.Method.NUMBER,
                number = 1, numberB = 1,
                customDate = fixedDate
            )
            assertThat(data.interName).isNotEmpty()
            assertThat(data.interHexagram).isNotNull()
        }
    }
}
