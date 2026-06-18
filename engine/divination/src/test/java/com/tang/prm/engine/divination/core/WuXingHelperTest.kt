package com.tang.prm.engine.divination.core

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class WuXingHelperTest {

    @Nested
    @DisplayName("getWuXing 地支→五行")
    inner class GetWuXingTest {

        @ParameterizedTest
        @CsvSource("申,金", "酉,金", "寅,木", "卯,木", "子,水", "亥,水", "巳,火", "午,火", "辰,土", "戌,土", "丑,土", "未,土")
        fun mapsCorrectly(dizhi: String, expected: String) {
            assertThat(WuXingHelper.getWuXing(dizhi)).isEqualTo(expected)
        }

        @Test
        fun unknownDizhi_returnsUnknown() {
            assertThat(WuXingHelper.getWuXing("X")).isEqualTo("未知")
        }
    }

    @Nested
    @DisplayName("getLiuQin 六亲关系")
    inner class GetLiuQinTest {

        @ParameterizedTest
        @CsvSource("金,金,兄弟", "金,木,妻财", "金,水,子孙", "金,火,官鬼", "金,土,父母")
        fun jinPalace(palace: String, yao: String, expected: String) {
            assertThat(WuXingHelper.getLiuQin(palace, yao)).isEqualTo(expected)
        }

        @ParameterizedTest
        @CsvSource("木,木,兄弟", "木,土,妻财", "木,火,子孙", "木,金,官鬼", "木,水,父母")
        fun muPalace(palace: String, yao: String, expected: String) {
            assertThat(WuXingHelper.getLiuQin(palace, yao)).isEqualTo(expected)
        }

        @Test
        fun unknownInput_returnsUnknown() {
            assertThat(WuXingHelper.getLiuQin("X", "金")).isEqualTo("未知")
        }
    }

    @Nested
    @DisplayName("getSixAnimals 六神排列")
    inner class GetSixAnimalsTest {

        @Test
        fun jiaStartsWithQingLong() {
            val animals = WuXingHelper.getSixAnimals("甲")
            assertThat(animals[0]).isEqualTo("青龙")
            assertThat(animals).hasSize(6)
        }

        @Test
        fun bingStartsWithZhuQue() {
            val animals = WuXingHelper.getSixAnimals("丙")
            assertThat(animals[0]).isEqualTo("朱雀")
        }

        @Test
        fun gengStartsWithBaiHu() {
            val animals = WuXingHelper.getSixAnimals("庚")
            assertThat(animals[0]).isEqualTo("白虎")
        }

        @Test
        fun renStartsWithXuanWu() {
            val animals = WuXingHelper.getSixAnimals("壬")
            assertThat(animals[0]).isEqualTo("玄武")
        }

        @Test
        fun allSixAnimalsAreUnique() {
            val animals = WuXingHelper.getSixAnimals("甲")
            assertThat(animals.toSet()).hasSize(6)
        }

        @Test
        fun unknownGan_defaultsToQingLong() {
            val animals = WuXingHelper.getSixAnimals("X")
            assertThat(animals[0]).isEqualTo("青龙")
        }
    }

    @Nested
    @DisplayName("getVoidBranches 旬空")
    inner class GetVoidBranchesTest {

        @Test
        fun jiaZi_returnsXuHai() {
            assertThat(WuXingHelper.getVoidBranches("甲子")).containsExactly("戌", "亥")
        }

        @Test
        fun jiaXu_returnsShenYou() {
            assertThat(WuXingHelper.getVoidBranches("甲戌")).containsExactly("申", "酉")
        }

        @Test
        fun unknownGanZhi_returnsEmpty() {
            assertThat(WuXingHelper.getVoidBranches("XX")).isEmpty()
        }
    }

    @Nested
    @DisplayName("getElementRelation 五行关系")
    inner class GetElementRelationTest {

        @Test
        fun sameElement_returnsBiHe() {
            assertThat(WuXingHelper.getElementRelation("金", "金")).isEqualTo("体用比和")
        }

        @Test
        fun yongShengTi_returnsYongShengTi() {
            // 金生水
            assertThat(WuXingHelper.getElementRelation("金", "水")).isEqualTo("用生体")
        }

        @Test
        fun tiShengYong_returnsTiShengYong() {
            // 水生木 → 用(水)生体(木)? No: yong=水, ti=木 → SHENG_MAP[水]=木 → 用生体
            // Actually: yong=木, ti=水 → SHENG_MAP[水]=木 → tiShengYong
            assertThat(WuXingHelper.getElementRelation("木", "水")).isEqualTo("体生用")
        }

        @Test
        fun yongKeTi_returnsYongKeTi() {
            // 金克木
            assertThat(WuXingHelper.getElementRelation("金", "木")).isEqualTo("用克体")
        }

        @Test
        fun tiKeYong_returnsTiKeYong() {
            // 木克土 → yong=土, ti=木 → KE_MAP[木]=土 → 体克用
            assertThat(WuXingHelper.getElementRelation("土", "木")).isEqualTo("体克用")
        }

        @Test
        fun emptyElement_returnsUnknown() {
            assertThat(WuXingHelper.getElementRelation("", "金")).isEqualTo("未知")
            assertThat(WuXingHelper.getElementRelation("金", "")).isEqualTo("未知")
        }
    }

    @Nested
    @DisplayName("getElementSeasonState 旺相休囚死")
    inner class GetElementSeasonStateTest {

        @ParameterizedTest
        @CsvSource("木,春,旺", "火,春,相", "水,春,休", "金,春,囚", "土,春,死")
        fun springStates(element: String, season: String, expected: String) {
            assertThat(WuXingHelper.getElementSeasonState(element, season)).isEqualTo(expected)
        }

        @ParameterizedTest
        @CsvSource("火,夏,旺", "金,秋,旺", "水,冬,旺")
        fun peakSeason(element: String, season: String, expected: String) {
            assertThat(WuXingHelper.getElementSeasonState(element, season)).isEqualTo(expected)
        }

        @Test
        fun unknownSeason_returnsUnknown() {
            assertThat(WuXingHelper.getElementSeasonState("金", "X")).isEqualTo("未知")
        }
    }

    @Nested
    @DisplayName("getSeasonByMonth")
    inner class GetSeasonByMonthTest {

        @ParameterizedTest
        @CsvSource("1,春", "2,春", "3,春", "4,夏", "5,夏", "6,夏", "7,秋", "8,秋", "9,秋", "10,冬", "11,冬", "12,冬")
        fun mapsCorrectly(month: Int, expected: String) {
            assertThat(WuXingHelper.getSeasonByMonth(month)).isEqualTo(expected)
        }
    }
}
