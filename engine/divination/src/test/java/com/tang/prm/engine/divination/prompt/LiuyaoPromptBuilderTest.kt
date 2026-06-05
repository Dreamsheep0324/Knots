package com.tang.prm.engine.prompt

import com.tang.prm.engine.divination.model.*
import com.tang.prm.engine.divination.prompt.LiuyaoPromptBuilder
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LiuyaoPromptBuilderTest {

    @Nested
    @DisplayName("getSystemPrompt")
    inner class GetSystemPrompt {

        @Test
        fun getSystemPrompt_returnsNonEmptyString() {
            val prompt = LiuyaoPromptBuilder.getSystemPrompt()
            assertThat(prompt).isNotEmpty()
        }

        @Test
        fun getSystemPrompt_contains用神() {
            val prompt = LiuyaoPromptBuilder.getSystemPrompt()
            assertThat(prompt).contains("用神")
        }

        @Test
        fun getSystemPrompt_contains六亲() {
            val prompt = LiuyaoPromptBuilder.getSystemPrompt()
            assertThat(prompt).contains("六亲")
        }

        @Test
        fun getSystemPrompt_contains纳甲() {
            val prompt = LiuyaoPromptBuilder.buildUserPrompt(
                data = buildLiuyaoData(),
                gender = "女",
                birthDate = "1995-08-20",
                question = "测试"
            )
            assertThat(prompt).contains("纳甲")
        }
    }

    @Nested
    @DisplayName("buildUserPrompt")
    inner class BuildUserPrompt {

        private val testLiuyaoData = buildLiuyaoData()
        private val prompt = LiuyaoPromptBuilder.buildUserPrompt(
            data = testLiuyaoData,
            gender = "女",
            birthDate = "1995-08-20",
            question = "这段感情能修成正果吗？"
        )

        @Test
        fun buildUserPrompt_contains占法六爻() {
            assertThat(prompt).contains("占法：六爻")
        }

        @Test
        fun buildUserPrompt_contains性别() {
            assertThat(prompt).contains("性别：女")
        }

        @Test
        fun buildUserPrompt_contains时间干支() {
            assertThat(prompt).contains("时间干支：")
            assertThat(prompt).contains(testLiuyaoData.ganzhi.year + "年")
            assertThat(prompt).contains(testLiuyaoData.ganzhi.month + "月")
            assertThat(prompt).contains(testLiuyaoData.ganzhi.day + "日")
            assertThat(prompt).contains(testLiuyaoData.ganzhi.hour + "时")
        }

        @Test
        fun buildUserPrompt_contains卦宫() {
            assertThat(prompt).contains(testLiuyaoData.palace.name + "宫")
            assertThat(prompt).contains(testLiuyaoData.palace.wuxing)
        }

        @Test
        fun buildUserPrompt_contains动爻信息() {
            assertThat(prompt).contains("动爻：")
            val changingYao = testLiuyaoData.changingYaos.first()
            assertThat(prompt).contains(changingYao.type)
        }
    }

    private fun buildLiuyaoData(): LiuyaoData {
        val ganzhi = buildGanZhiInfo()
        return LiuyaoData(
            originalName = "天风姤",
            changedName = "天水讼",
            interName = "天泽履",
            ganzhi = ganzhi,
            timestamp = 1700000000000L,
            yaoArray = listOf(7, 7, 7, 9, 7, 7),
            changingYaos = listOf(
                ChangingYao(position = 4, isChanging = true, type = "老阳")
            ),
            sixGods = listOf("青龙", "朱雀", "勾陈", "螣蛇", "白虎", "玄武"),
            sixRelatives = listOf("父母", "兄弟", "官鬼", "妻财", "子孙", "父母"),
            najiaDizhi = listOf("戌", "申", "午", "亥", "酉", "未"),
            wuxing = listOf("土", "金", "火", "水", "金", "土"),
            worldAndResponse = listOf("", "", "世", "", "应", ""),
            voidBranches = listOf("申", "酉"),
            palace = PalaceInfo(name = "乾", wuxing = "金"),
            yaosDetail = listOf(
                buildLiuyaoYaoDetail(1, "阳", false, "静爻", "青龙", "父母", "戌", "土", false, false, false, null),
                buildLiuyaoYaoDetail(2, "阳", false, "静爻", "朱雀", "兄弟", "申", "金", false, false, true, null),
                buildLiuyaoYaoDetail(3, "阳", false, "静爻", "勾陈", "官鬼", "午", "火", true, false, false, null),
                buildLiuyaoYaoDetail(4, "阳", true, "老阳", "螣蛇", "妻财", "亥", "水", false, false, false,
                    ChangedYaoInfo(dizhi = "午", wuxing = "火", liuqin = "官鬼", isVoid = false)
                ),
                buildLiuyaoYaoDetail(5, "阳", false, "静爻", "白虎", "子孙", "酉", "金", false, true, true, null),
                buildLiuyaoYaoDetail(6, "阳", false, "静爻", "玄武", "父母", "未", "土", false, false, false, null)
            ),
            specialPattern = null,
            specialAdvice = null,
            isChaotic = false,
            chaoticReason = null
        )
    }

    private fun buildLiuyaoYaoDetail(
        position: Int,
        yaoType: String,
        isChanging: Boolean,
        changeType: String,
        sixGod: String,
        sixRelative: String,
        najiaDizhi: String,
        wuxing: String,
        isWorld: Boolean,
        isResponse: Boolean,
        isVoid: Boolean,
        changedYao: ChangedYaoInfo?
    ): LiuyaoYaoDetail = LiuyaoYaoDetail(
        position = position,
        rawValue = if (isChanging) 9 else 7,
        yaoType = yaoType,
        isChanging = isChanging,
        changeType = changeType,
        sixGod = sixGod,
        sixRelative = sixRelative,
        najiaDizhi = najiaDizhi,
        wuxing = wuxing,
        isWorld = isWorld,
        isResponse = isResponse,
        isVoid = isVoid,
        changedYao = changedYao
    )

    private fun buildGanZhiInfo(): GanZhiInfo = GanZhiInfo(
        year = "甲辰",
        month = "己巳",
        day = "丙午",
        hour = "甲午"
    )
}
