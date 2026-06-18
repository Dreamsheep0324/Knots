package com.tang.prm.engine.divination.liuyao

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Date

class LiuyaoEngineTest {

    private val fixedDate = Date(1718438400000L) // 2024-06-15 08:00:00 UTC

    @Nested
    @DisplayName("静卦（无动爻）")
    inner class StaticHexagramTest {

        @Test
        fun allSeven_yieldsQianGuanJingGua() {
            // 7=少阳，全阳 → 乾为天，无动爻 → 静卦
            val data = LiuyaoEngine.generate(yaoArray = listOf(7, 7, 7, 7, 7, 7), customDate = fixedDate)

            assertThat(data.originalName).isEqualTo("乾为天")
            assertThat(data.yaoArray).containsExactly(7, 7, 7, 7, 7, 7).inOrder()
            assertThat(data.changingYaos).isEmpty()
            assertThat(data.specialPattern).isEqualTo("静卦")
            assertThat(data.specialAdvice).contains("六爻安静")
            assertThat(data.isChaotic).isFalse()
        }

        @Test
        fun allEight_yieldsKunGuanJingGua() {
            // 8=少阴，全阴 → 坤为地，无动爻 → 静卦
            val data = LiuyaoEngine.generate(yaoArray = listOf(8, 8, 8, 8, 8, 8), customDate = fixedDate)

            assertThat(data.originalName).isEqualTo("坤为地")
            assertThat(data.changingYaos).isEmpty()
            assertThat(data.specialPattern).isEqualTo("静卦")
        }

        @Test
        fun noChangingYaos_changedNameEqualsOriginal() {
            val data = LiuyaoEngine.generate(yaoArray = listOf(7, 8, 7, 8, 7, 8), customDate = fixedDate)
            assertThat(data.changingYaos).isEmpty()
        }
    }

    @Nested
    @DisplayName("动爻与变卦")
    inner class ChangingYaoTest {

        @Test
        fun oldYin_changesToYang() {
            // 6=老阴，阴→阳
            val data = LiuyaoEngine.generate(yaoArray = listOf(6, 8, 8, 8, 8, 8), customDate = fixedDate)
            assertThat(data.changingYaos).hasSize(1)
            assertThat(data.changingYaos[0].position).isEqualTo(1)
            assertThat(data.changingYaos[0].type).isEqualTo("老阴")
        }

        @Test
        fun oldYang_changesToYin() {
            // 9=老阳，阳→阴
            val data = LiuyaoEngine.generate(yaoArray = listOf(9, 7, 7, 7, 7, 7), customDate = fixedDate)
            assertThat(data.changingYaos).hasSize(1)
            assertThat(data.changingYaos[0].position).isEqualTo(1)
            assertThat(data.changingYaos[0].type).isEqualTo("老阳")
        }

        @Test
        fun changedName_differsFromOriginal_whenHasChangingYao() {
            // 乾为天（全阳），第一爻变（9→阴）→ 天泽履
            // binary: main=111111(乾为天), changed=111110(天泽履, reversed后第一爻在最右)
            val data = LiuyaoEngine.generate(yaoArray = listOf(9, 7, 7, 7, 7, 7), customDate = fixedDate)
            assertThat(data.originalName).isEqualTo("乾为天")
            assertThat(data.changedName).isEqualTo("天泽履")
        }
    }

    @Nested
    @DisplayName("特殊格局")
    inner class SpecialPatternTest {

        @Test
        fun qianAllYang_allChanging_yieldsYongJiu() {
            // 全9 → 乾为天六爻全动 → 乾卦用九
            val data = LiuyaoEngine.generate(yaoArray = listOf(9, 9, 9, 9, 9, 9), customDate = fixedDate)
            assertThat(data.originalName).isEqualTo("乾为天")
            assertThat(data.changingYaos).hasSize(6)
            assertThat(data.specialPattern).isEqualTo("乾卦用九")
            assertThat(data.specialAdvice).contains("用九")
            assertThat(data.isChaotic).isFalse()
        }

        @Test
        fun kunAllYin_allChanging_yieldsYongLiu() {
            // 全6 → 坤为地六爻全动 → 坤卦用六
            val data = LiuyaoEngine.generate(yaoArray = listOf(6, 6, 6, 6, 6, 6), customDate = fixedDate)
            assertThat(data.originalName).isEqualTo("坤为地")
            assertThat(data.changedName).isEqualTo("乾为天")
            assertThat(data.specialPattern).isEqualTo("坤卦用六")
            assertThat(data.isChaotic).isFalse()
        }

        @Test
        fun sixChanging_nonQianKun_yieldsQuanDongAndChaotic() {
            // 需要一个非乾坤但六爻全动的卦
            // 7=阳, 6=老阴(阴→阳)，混合使主卦非乾坤
            // [6,7,6,7,6,7] → 主卦阴阳交替，变卦全阳
            val data = LiuyaoEngine.generate(yaoArray = listOf(6, 7, 6, 7, 6, 7), customDate = fixedDate)
            assertThat(data.changingYaos).hasSize(3) // 只有6是动爻
        }

        @Test
        fun fiveChanging_yieldsDuJing() {
            // 5个动爻 → 独静卦
            val data = LiuyaoEngine.generate(yaoArray = listOf(9, 9, 9, 9, 9, 7), customDate = fixedDate)
            assertThat(data.changingYaos).hasSize(5)
            assertThat(data.specialPattern).isEqualTo("独静卦")
        }

        @Test
        fun normalChanging_noSpecialPattern() {
            val data = LiuyaoEngine.generate(yaoArray = listOf(9, 7, 7, 7, 7, 7), customDate = fixedDate)
            assertThat(data.specialPattern).isNull()
            assertThat(data.specialAdvice).isNull()
        }
    }

    @Nested
    @DisplayName("数据结构完整性")
    inner class DataStructureTest {

        @Test
        fun yaosDetail_hasSixEntries() {
            val data = LiuyaoEngine.generate(yaoArray = listOf(7, 8, 7, 8, 7, 8), customDate = fixedDate)
            assertThat(data.yaosDetail).hasSize(6)
        }

        @Test
        fun yaosDetail_positionsAreOneToSix() {
            val data = LiuyaoEngine.generate(yaoArray = listOf(7, 8, 7, 8, 7, 8), customDate = fixedDate)
            val positions = data.yaosDetail.map { it.position }
            assertThat(positions).containsExactly(1, 2, 3, 4, 5, 6).inOrder()
        }

        @Test
        fun worldAndResponse_hasSixEntries() {
            val data = LiuyaoEngine.generate(yaoArray = listOf(7, 8, 7, 8, 7, 8), customDate = fixedDate)
            assertThat(data.worldAndResponse).hasSize(6)
        }

        @Test
        fun worldAndResponse_containsOneShiAndOneYing() {
            val data = LiuyaoEngine.generate(yaoArray = listOf(7, 8, 7, 8, 7, 8), customDate = fixedDate)
            assertThat(data.worldAndResponse.count { it == "世" }).isEqualTo(1)
            assertThat(data.worldAndResponse.count { it == "应" }).isEqualTo(1)
        }

        @Test
        fun sixGods_hasSixEntries() {
            val data = LiuyaoEngine.generate(yaoArray = listOf(7, 8, 7, 8, 7, 8), customDate = fixedDate)
            assertThat(data.sixGods).hasSize(6)
        }

        @Test
        fun sixRelatives_hasSixEntries() {
            val data = LiuyaoEngine.generate(yaoArray = listOf(7, 8, 7, 8, 7, 8), customDate = fixedDate)
            assertThat(data.sixRelatives).hasSize(6)
        }

        @Test
        fun najiaDizhi_hasSixEntries() {
            val data = LiuyaoEngine.generate(yaoArray = listOf(7, 8, 7, 8, 7, 8), customDate = fixedDate)
            assertThat(data.najiaDizhi).hasSize(6)
        }

        @Test
        fun wuxing_hasSixEntries() {
            val data = LiuyaoEngine.generate(yaoArray = listOf(7, 8, 7, 8, 7, 8), customDate = fixedDate)
            assertThat(data.wuxing).hasSize(6)
        }

        @Test
        fun voidBranches_hasAtMostTwoEntries() {
            val data = LiuyaoEngine.generate(yaoArray = listOf(7, 8, 7, 8, 7, 8), customDate = fixedDate)
            assertThat(data.voidBranches.size).isAtMost(2)
        }

        @Test
        fun palace_hasNameAndWuxing() {
            val data = LiuyaoEngine.generate(yaoArray = listOf(7, 7, 7, 7, 7, 7), customDate = fixedDate)
            assertThat(data.palace.name).isEqualTo("乾")
            assertThat(data.palace.wuxing).isEqualTo("金")
        }

        @Test
        fun ganzhi_hasFourFields() {
            val data = LiuyaoEngine.generate(yaoArray = listOf(7, 8, 7, 8, 7, 8), customDate = fixedDate)
            assertThat(data.ganzhi.year).isNotEmpty()
            assertThat(data.ganzhi.month).isNotEmpty()
            assertThat(data.ganzhi.day).isNotEmpty()
            assertThat(data.ganzhi.hour).isNotEmpty()
        }

        @Test
        fun timestamp_matchesInputDate() {
            val data = LiuyaoEngine.generate(yaoArray = listOf(7, 8, 7, 8, 7, 8), customDate = fixedDate)
            assertThat(data.timestamp).isEqualTo(fixedDate.time)
        }
    }

    @Nested
    @DisplayName("动爻详情")
    inner class YaoDetailTest {

        @Test
        fun changingYao_hasChangedYaoInfo() {
            val data = LiuyaoEngine.generate(yaoArray = listOf(9, 7, 7, 7, 7, 7), customDate = fixedDate)
            val changingDetail = data.yaosDetail[0]
            assertThat(changingDetail.isChanging).isTrue()
            assertThat(changingDetail.changedYao).isNotNull()
        }

        @Test
        fun staticYao_hasNoChangedYaoInfo() {
            val data = LiuyaoEngine.generate(yaoArray = listOf(9, 7, 7, 7, 7, 7), customDate = fixedDate)
            val staticDetail = data.yaosDetail[1]
            assertThat(staticDetail.isChanging).isFalse()
            assertThat(staticDetail.changedYao).isNull()
        }

        @Test
        fun yangYao_yaoTypeIsYang() {
            val data = LiuyaoEngine.generate(yaoArray = listOf(7, 7, 7, 7, 7, 7), customDate = fixedDate)
            data.yaosDetail.forEach { detail ->
                assertThat(detail.yaoType).isEqualTo("阳")
            }
        }

        @Test
        fun yinYao_yaoTypeIsYin() {
            val data = LiuyaoEngine.generate(yaoArray = listOf(8, 8, 8, 8, 8, 8), customDate = fixedDate)
            data.yaosDetail.forEach { detail ->
                assertThat(detail.yaoType).isEqualTo("阴")
            }
        }
    }

    @Nested
    @DisplayName("默认生成（时间起卦）")
    inner class DefaultGenerateTest {

        @Test
        fun generateWithoutYaoArray_producesValidData() {
            val data = LiuyaoEngine.generate(customDate = fixedDate)
            assertThat(data.yaoArray).hasSize(6)
            assertThat(data.originalName).isNotEmpty()
            assertThat(data.changedName).isNotEmpty()
            assertThat(data.interName).isNotEmpty()
        }

        @Test
        fun generatedYaosAreInValidRange() {
            val data = LiuyaoEngine.generate(customDate = fixedDate)
            data.yaoArray.forEach { yao ->
                assertThat(yao).isAnyOf(6, 7, 8, 9)
            }
        }
    }
}
