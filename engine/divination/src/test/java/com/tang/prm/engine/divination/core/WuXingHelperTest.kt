package com.tang.prm.engine.core

import com.tang.prm.engine.divination.core.WuXingHelper
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class WuXingHelperTest {

    @Test
    fun `getWuXing_子_returns水`() {
        assertThat(WuXingHelper.getWuXing("子")).isEqualTo("水")
    }

    @Test
    fun `getWuXing_午_returns火`() {
        assertThat(WuXingHelper.getWuXing("午")).isEqualTo("火")
    }

    @Test
    fun `getWuXing_寅_returns木`() {
        assertThat(WuXingHelper.getWuXing("寅")).isEqualTo("木")
    }

    @Test
    fun `getWuXing_申_returns金`() {
        assertThat(WuXingHelper.getWuXing("申")).isEqualTo("金")
    }

    @Test
    fun `getWuXing_辰_returns土`() {
        assertThat(WuXingHelper.getWuXing("辰")).isEqualTo("土")
    }

    @Test
    fun `getLiuQin_金金_returns兄弟`() {
        assertThat(WuXingHelper.getLiuQin("金", "金")).isEqualTo("兄弟")
    }

    @Test
    fun `getLiuQin_金木_returns妻财`() {
        assertThat(WuXingHelper.getLiuQin("金", "木")).isEqualTo("妻财")
    }

    @Test
    fun `getLiuQin_金水_returns子孙`() {
        assertThat(WuXingHelper.getLiuQin("金", "水")).isEqualTo("子孙")
    }

    @Test
    fun `getLiuQin_金火_returns官鬼`() {
        assertThat(WuXingHelper.getLiuQin("金", "火")).isEqualTo("官鬼")
    }

    @Test
    fun `getLiuQin_金土_returns父母`() {
        assertThat(WuXingHelper.getLiuQin("金", "土")).isEqualTo("父母")
    }

    @Test
    fun `getLiuQin_木木_returns兄弟`() {
        assertThat(WuXingHelper.getLiuQin("木", "木")).isEqualTo("兄弟")
    }

    @Test
    fun `getSixAnimals_甲日_青龙起首`() {
        val animals = WuXingHelper.getSixAnimals("甲")
        assertThat(animals.first()).isEqualTo("青龙")
    }

    @Test
    fun `getSixAnimals_丙日_朱雀起首`() {
        val animals = WuXingHelper.getSixAnimals("丙")
        assertThat(animals.first()).isEqualTo("朱雀")
    }

    @Test
    fun `getSixAnimals_庚日_白虎起首`() {
        val animals = WuXingHelper.getSixAnimals("庚")
        assertThat(animals.first()).isEqualTo("白虎")
    }

    @Test
    fun `getSixAnimals_returns6elements`() {
        assertThat(WuXingHelper.getSixAnimals("甲")).hasSize(6)
    }

    @Test
    fun `getVoidBranches_甲子日_returns戌亥`() {
        assertThat(WuXingHelper.getVoidBranches("甲子")).containsExactly("戌", "亥").inOrder()
    }

    @Test
    fun `getVoidBranches_甲戌日_returns申酉`() {
        assertThat(WuXingHelper.getVoidBranches("甲戌")).containsExactly("申", "酉").inOrder()
    }

    @Test
    fun `getVoidBranches_甲午日_returns辰巳`() {
        assertThat(WuXingHelper.getVoidBranches("甲午")).containsExactly("辰", "巳").inOrder()
    }

    @Test
    fun `getElementRelation_同五行_returns体用比和`() {
        assertThat(WuXingHelper.getElementRelation("金", "金")).isEqualTo("体用比和")
    }

    @Test
    fun `getElementRelation_木火_returns用生体`() {
        assertThat(WuXingHelper.getElementRelation("木", "火")).isEqualTo("用生体")
    }

    @Test
    fun `getElementRelation_火木_returns体生用`() {
        assertThat(WuXingHelper.getElementRelation("火", "木")).isEqualTo("体生用")
    }

    @Test
    fun `getElementRelation_木土_returns用克体`() {
        assertThat(WuXingHelper.getElementRelation("木", "土")).isEqualTo("用克体")
    }

    @Test
    fun `getElementRelation_金木_returns用克体`() {
        assertThat(WuXingHelper.getElementRelation("金", "木")).isEqualTo("用克体")
    }

    @Test
    fun `getElementRelation_木金_returns体克用`() {
        assertThat(WuXingHelper.getElementRelation("木", "金")).isEqualTo("体克用")
    }

    @Test
    fun `getElementSeasonState_木春_returns旺`() {
        assertThat(WuXingHelper.getElementSeasonState("木", "春")).isEqualTo("旺")
    }

    @Test
    fun `getElementSeasonState_火夏_returns旺`() {
        assertThat(WuXingHelper.getElementSeasonState("火", "夏")).isEqualTo("旺")
    }

    @Test
    fun `getElementSeasonState_金秋_returns旺`() {
        assertThat(WuXingHelper.getElementSeasonState("金", "秋")).isEqualTo("旺")
    }

    @Test
    fun `getElementSeasonState_水冬_returns旺`() {
        assertThat(WuXingHelper.getElementSeasonState("水", "冬")).isEqualTo("旺")
    }

    @Test
    fun `getElementSeasonState_火春_returns相`() {
        assertThat(WuXingHelper.getElementSeasonState("火", "春")).isEqualTo("相")
    }

    @Test
    fun `getElementSeasonState_木秋_returns死`() {
        assertThat(WuXingHelper.getElementSeasonState("木", "秋")).isEqualTo("死")
    }

    @Test
    fun `getSeasonByMonth_1to3_returns春`() {
        assertThat(WuXingHelper.getSeasonByMonth(1)).isEqualTo("春")
        assertThat(WuXingHelper.getSeasonByMonth(2)).isEqualTo("春")
        assertThat(WuXingHelper.getSeasonByMonth(3)).isEqualTo("春")
    }

    @Test
    fun `getSeasonByMonth_4to6_returns夏`() {
        assertThat(WuXingHelper.getSeasonByMonth(4)).isEqualTo("夏")
        assertThat(WuXingHelper.getSeasonByMonth(5)).isEqualTo("夏")
        assertThat(WuXingHelper.getSeasonByMonth(6)).isEqualTo("夏")
    }

    @Test
    fun `getSeasonByMonth_7to9_returns秋`() {
        assertThat(WuXingHelper.getSeasonByMonth(7)).isEqualTo("秋")
        assertThat(WuXingHelper.getSeasonByMonth(8)).isEqualTo("秋")
        assertThat(WuXingHelper.getSeasonByMonth(9)).isEqualTo("秋")
    }

    @Test
    fun `getSeasonByMonth_10to12_returns冬`() {
        assertThat(WuXingHelper.getSeasonByMonth(10)).isEqualTo("冬")
        assertThat(WuXingHelper.getSeasonByMonth(11)).isEqualTo("冬")
        assertThat(WuXingHelper.getSeasonByMonth(12)).isEqualTo("冬")
    }
}
