package com.tang.prm.engine.prompt

import com.tang.prm.engine.divination.model.*
import com.tang.prm.engine.divination.prompt.MeihuaPromptBuilder
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MeihuaPromptBuilderTest {

    @Nested
    @DisplayName("getSystemPrompt")
    inner class GetSystemPrompt {

        @Test
        fun getSystemPrompt_returnsNonEmptyString() {
            val prompt = MeihuaPromptBuilder.getSystemPrompt()
            assertThat(prompt).isNotEmpty()
        }

        @Test
        fun getSystemPrompt_contains体用() {
            val prompt = MeihuaPromptBuilder.getSystemPrompt()
            assertThat(prompt).contains("体用")
        }

        @Test
        fun getSystemPrompt_contains旺衰() {
            val prompt = MeihuaPromptBuilder.getSystemPrompt()
            assertThat(prompt).contains("旺衰")
        }

        @Test
        fun getSystemPrompt_contains自检() {
            val prompt = MeihuaPromptBuilder.getSystemPrompt()
            assertThat(prompt).contains("自检")
        }
    }

    @Nested
    @DisplayName("buildUserPrompt")
    inner class BuildUserPrompt {

        private val testMeihuaData = buildMeihuaData(upper = "离", lower = "坎")
        private val pureHexagramData = buildMeihuaData(upper = "离", lower = "离")
        private val prompt = MeihuaPromptBuilder.buildUserPrompt(
            data = testMeihuaData,
            gender = "男",
            birthDate = "1990-05-15",
            question = "最近工作能否顺利？"
        )

        @Test
        fun buildUserPrompt_contains占法梅花易数() {
            assertThat(prompt).contains("占法：梅花易数")
        }

        @Test
        fun buildUserPrompt_contains性别() {
            assertThat(prompt).contains("性别：男")
        }

        @Test
        fun buildUserPrompt_contains出生日期() {
            assertThat(prompt).contains("出生日期：1990-05-15")
        }

        @Test
        fun buildUserPrompt_contains主卦名() {
            assertThat(prompt).contains("主卦${testMeihuaData.originalName}")
        }

        @Test
        fun buildUserPrompt_contains体卦信息() {
            assertThat(prompt).contains("体卦${testMeihuaData.tiGua.name}")
            assertThat(prompt).contains(testMeihuaData.tiGua.element)
        }

        @Test
        fun buildUserPrompt_contains用卦信息() {
            assertThat(prompt).contains("用卦${testMeihuaData.yongGua.name}")
            assertThat(prompt).contains(testMeihuaData.yongGua.element)
        }

        @Test
        fun buildUserPrompt_contains动爻() {
            assertThat(prompt).contains("动爻第${testMeihuaData.movingYao.position}爻")
        }

        @Test
        fun buildUserPrompt_contains问题内容() {
            assertThat(prompt).contains("最近工作能否顺利？")
        }

        @Test
        fun buildUserPrompt_pureHexagram_contains八纯卦() {
            val purePrompt = MeihuaPromptBuilder.buildUserPrompt(
                data = pureHexagramData,
                gender = "男",
                birthDate = "1990-05-15",
                question = "测试"
            )
            assertThat(purePrompt).contains("八纯卦")
        }

        @Test
        fun buildUserPrompt_nonPureHexagram_no八纯卦() {
            assertThat(prompt).doesNotContain("此卦为八纯卦")
        }
    }

    private fun buildMeihuaData(upper: String, lower: String): MeihuaData {
        val isPure = upper == lower
        return MeihuaData(
            originalName = if (isPure) "离为火" else "火水未济",
            changedName = "雷水解",
            interName = "水火既济",
            ganzhi = buildGanZhiInfo(),
            timestamp = 1700000000000L,
            tiGua = TiYongGuaInfo(name = "离", element = "火", nature = "丽"),
            yongGua = TiYongGuaInfo(name = "坎", element = "水", nature = "险"),
            changedTiGua = TiYongGuaInfo(name = "震", element = "木", nature = "动"),
            changedYongGua = TiYongGuaInfo(name = "坎", element = "水", nature = "险"),
            movingYao = MovingYaoInfo(position = 3, description = "第3爻动", yaoName = "三爻"),
            analysis = MeihuaAnalysis(
                season = "夏",
                tiYongRelation = "用克体",
                tiSeasonState = "旺",
                yongSeasonState = "囚",
                inter1Relation = "生",
                inter2Relation = "克",
                changedRelation = "体克变",
                changedTiYongRelation = "用生体"
            ),
            mainHexagram = HexagramDetail(
                name = if (isPure) "离为火" else "火水未济",
                symbol = "☲☵",
                upper = upper,
                lower = lower,
                description = "未济：亨，小狐汔济，濡其尾，无攸利。"
            ),
            interHexagram = HexagramDetail(
                name = "水火既济",
                symbol = "☵☲",
                upper = "坎",
                lower = "离",
                description = "既济：亨小，利贞，初吉终乱。"
            ),
            changedHexagram = HexagramDetail(
                name = "雷水解",
                symbol = "☳☵",
                upper = "震",
                lower = "坎",
                description = "解：利西南，无所往，其来复吉。"
            ),
            yaosDetail = listOf(
                MeihuaYaoDetail(position = 1, yaoType = "阳", isChanging = false, tiYong = "用"),
                MeihuaYaoDetail(position = 2, yaoType = "阴", isChanging = false, tiYong = "用"),
                MeihuaYaoDetail(position = 3, yaoType = "阳", isChanging = true, tiYong = "用"),
                MeihuaYaoDetail(position = 4, yaoType = "阴", isChanging = false, tiYong = "体"),
                MeihuaYaoDetail(position = 5, yaoType = "阳", isChanging = false, tiYong = "体"),
                MeihuaYaoDetail(position = 6, yaoType = "阳", isChanging = false, tiYong = "体")
            ),
            calculation = MeihuaCalculation(
                method = "时间起卦",
                methodKey = "TIME",
                month = 5,
                day = 15,
                timeZhiIndex = 6
            )
        )
    }

    private fun buildGanZhiInfo(): GanZhiInfo = GanZhiInfo(
        year = "甲辰",
        month = "己巳",
        day = "丙午",
        hour = "甲午"
    )
}
