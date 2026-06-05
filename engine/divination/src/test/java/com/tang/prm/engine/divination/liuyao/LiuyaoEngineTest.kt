package com.tang.prm.engine.liuyao

import com.google.common.truth.Truth.assertThat
import com.tang.prm.engine.divination.liuyao.LiuyaoEngine
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.GregorianCalendar

class LiuyaoEngineTest {

    private val fixedDate = GregorianCalendar(2024, 5, 15, 10, 30).time

    @Nested
    inner class AllStaticYaos {

        private val data = LiuyaoEngine.generate(
            yaoArray = listOf(7, 8, 7, 8, 7, 8),
            customDate = fixedDate
        )

        @Test
        @DisplayName("generate_allStaticYaos_returnsValidLiuyaoData")
        fun generate_allStaticYaos_returnsValidLiuyaoData() {
            assertThat(data.originalName).isNotEmpty()
            assertThat(data.changedName).isNotEmpty()
            assertThat(data.interName).isNotEmpty()
            assertThat(data.ganzhi).isNotNull()
            assertThat(data.timestamp).isGreaterThan(0L)
            assertThat(data.yaoArray).containsExactly(7, 8, 7, 8, 7, 8).inOrder()
            assertThat(data.voidBranches).isNotNull()
            assertThat(data.palace).isNotNull()
            assertThat(data.yaosDetail).isNotNull()
            assertThat(data.specialPattern).isNotNull()
            assertThat(data.isChaotic).isFalse()
        }

        @Test
        @DisplayName("generate_allStaticYaos_noChangingYaos")
        fun generate_allStaticYaos_noChangingYaos() {
            assertThat(data.changingYaos).isEmpty()
        }

        @Test
        @DisplayName("generate_allStaticYaos_specialPatternIs静卦")
        fun generate_allStaticYaos_specialPatternIs静卦() {
            assertThat(data.specialPattern).isEqualTo("静卦")
        }
    }

    @Test
    @DisplayName("generate_oldYin_changesToYang")
    fun generate_oldYin_changesToYang() {
        val data = LiuyaoEngine.generate(
            yaoArray = listOf(6, 8, 7, 8, 7, 8),
            customDate = fixedDate
        )
        val detail = data.yaosDetail[0]
        assertThat(detail.rawValue).isEqualTo(6)
        assertThat(detail.yaoType).isEqualTo("阴")
        assertThat(detail.isChanging).isTrue()
        assertThat(detail.changeType).isEqualTo("老阴")
        assertThat(detail.changedYao).isNotNull()
    }

    @Test
    @DisplayName("generate_oldYang_changesToYin")
    fun generate_oldYang_changesToYin() {
        val data = LiuyaoEngine.generate(
            yaoArray = listOf(9, 8, 7, 8, 7, 8),
            customDate = fixedDate
        )
        val detail = data.yaosDetail[0]
        assertThat(detail.rawValue).isEqualTo(9)
        assertThat(detail.yaoType).isEqualTo("阳")
        assertThat(detail.isChanging).isTrue()
        assertThat(detail.changeType).isEqualTo("老阳")
        assertThat(detail.changedYao).isNotNull()
    }

    @Test
    @DisplayName("generate_mixedYaos_changingYaosCorrect")
    fun generate_mixedYaos_changingYaosCorrect() {
        val data = LiuyaoEngine.generate(
            yaoArray = listOf(6, 7, 8, 9, 7, 8),
            customDate = fixedDate
        )
        assertThat(data.changingYaos).hasSize(2)
        assertThat(data.changingYaos[0].position).isEqualTo(1)
        assertThat(data.changingYaos[0].isChanging).isTrue()
        assertThat(data.changingYaos[0].type).isEqualTo("老阴")
        assertThat(data.changingYaos[1].position).isEqualTo(4)
        assertThat(data.changingYaos[1].isChanging).isTrue()
        assertThat(data.changingYaos[1].type).isEqualTo("老阳")
    }

    @Test
    @DisplayName("generate_fiveChangingYaos_specialPatternIs独静卦")
    fun generate_fiveChangingYaos_specialPatternIs独静卦() {
        val data = LiuyaoEngine.generate(
            yaoArray = listOf(6, 9, 6, 9, 6, 8),
            customDate = fixedDate
        )
        assertThat(data.specialPattern).isEqualTo("独静卦")
        assertThat(data.isChaotic).isFalse()
    }

    @Test
    @DisplayName("generate_sixChangingYaos_nonPure_specialPatternIs全动卦")
    fun generate_sixChangingYaos_nonPure_specialPatternIs全动卦() {
        val data = LiuyaoEngine.generate(
            yaoArray = listOf(6, 9, 6, 9, 6, 9),
            customDate = fixedDate
        )
        assertThat(data.specialPattern).isEqualTo("全动卦")
        assertThat(data.isChaotic).isTrue()
        assertThat(data.chaoticReason).isNotEmpty()
    }

    @Test
    @DisplayName("generate_yaosDetailHas6Items")
    fun generate_yaosDetailHas6Items() {
        val data = LiuyaoEngine.generate(
            yaoArray = listOf(7, 8, 7, 8, 7, 8),
            customDate = fixedDate
        )
        assertThat(data.yaosDetail).hasSize(6)
        data.yaosDetail.forEachIndexed { index, detail ->
            assertThat(detail.position).isEqualTo(index + 1)
        }
    }

    @Test
    @DisplayName("generate_sixGodsHas6Items")
    fun generate_sixGodsHas6Items() {
        val data = LiuyaoEngine.generate(
            yaoArray = listOf(7, 8, 7, 8, 7, 8),
            customDate = fixedDate
        )
        assertThat(data.sixGods).hasSize(6)
        data.sixGods.forEach { god ->
            assertThat(god).isNotEmpty()
        }
    }

    @Test
    @DisplayName("generate_sixRelativesHas6Items")
    fun generate_sixRelativesHas6Items() {
        val data = LiuyaoEngine.generate(
            yaoArray = listOf(7, 8, 7, 8, 7, 8),
            customDate = fixedDate
        )
        assertThat(data.sixRelatives).hasSize(6)
        data.sixRelatives.forEach { relative ->
            assertThat(relative).isNotEmpty()
        }
    }

    @Test
    @DisplayName("generate_palaceFieldsNonEmpty")
    fun generate_palaceFieldsNonEmpty() {
        val data = LiuyaoEngine.generate(
            yaoArray = listOf(7, 8, 7, 8, 7, 8),
            customDate = fixedDate
        )
        assertThat(data.palace.name).isNotEmpty()
        assertThat(data.palace.wuxing).isNotEmpty()
    }

    @Test
    @DisplayName("generate_worldAndResponseHasWorldAndResponse")
    fun generate_worldAndResponseHasWorldAndResponse() {
        val data = LiuyaoEngine.generate(
            yaoArray = listOf(7, 8, 7, 8, 7, 8),
            customDate = fixedDate
        )
        assertThat(data.worldAndResponse).hasSize(6)
        assertThat(data.worldAndResponse).contains("世")
        assertThat(data.worldAndResponse).contains("应")
        assertThat(data.yaosDetail.any { it.isWorld }).isTrue()
        assertThat(data.yaosDetail.any { it.isResponse }).isTrue()
    }

    @Test
    @DisplayName("generate_nullYaoArray_returnsValidData")
    fun generate_nullYaoArray_returnsValidData() {
        val data = LiuyaoEngine.generate(
            yaoArray = null,
            customDate = fixedDate
        )
        assertThat(data.yaoArray).hasSize(6)
        assertThat(data.originalName).isNotEmpty()
        assertThat(data.changedName).isNotEmpty()
        data.yaoArray.forEach { yao ->
            assertThat(yao).isAnyOf(6, 7, 8, 9)
        }
    }

    @Test
    @DisplayName("generate_customDate_usedInGanZhi")
    fun generate_customDate_usedInGanZhi() {
        val date1 = GregorianCalendar(2024, 0, 1, 12, 0).time
        val date2 = GregorianCalendar(2025, 5, 15, 14, 0).time
        val data1 = LiuyaoEngine.generate(yaoArray = listOf(7, 8, 7, 8, 7, 8), customDate = date1)
        val data2 = LiuyaoEngine.generate(yaoArray = listOf(7, 8, 7, 8, 7, 8), customDate = date2)
        assertThat(data1.ganzhi.day).isNotEqualTo(data2.ganzhi.day)
    }
}
