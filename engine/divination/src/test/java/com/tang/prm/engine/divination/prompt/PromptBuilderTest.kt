package com.tang.prm.engine.divination.prompt

import com.google.common.truth.Truth.assertThat
import com.tang.prm.engine.divination.liuyao.LiuyaoEngine
import com.tang.prm.engine.divination.meihua.MeihuaEngine
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Date

class PromptBuilderTest {

    private val fixedDate = Date(1718438400000L)

    @Nested
    @DisplayName("LiuyaoPromptBuilder")
    inner class LiuyaoPromptBuilderTest {

        @Test
        fun getSystemPrompt_isNotEmpty() {
            val prompt = LiuyaoPromptBuilder.getSystemPrompt()
            assertThat(prompt).isNotEmpty()
        }

        @Test
        fun getSystemPrompt_containsKeyPrinciples() {
            val prompt = LiuyaoPromptBuilder.getSystemPrompt()
            assertThat(prompt).contains("断卦原则")
            assertThat(prompt).contains("用神中心")
            assertThat(prompt).contains("输出规则")
        }

        @Test
        fun buildUserPrompt_containsBasicInfo() {
            val data = LiuyaoEngine.generate(yaoArray = listOf(7, 7, 7, 7, 7, 7), customDate = fixedDate)
            val prompt = LiuyaoPromptBuilder.buildUserPrompt(data, "男", "1990-01-01", "测试问题")
            assertThat(prompt).contains("占法：六爻")
            assertThat(prompt).contains("性别：男")
            assertThat(prompt).contains("出生日期：1990-01-01")
            assertThat(prompt).contains("测试问题")
        }

        @Test
        fun buildUserPrompt_containsHexagramInfo() {
            val data = LiuyaoEngine.generate(yaoArray = listOf(7, 7, 7, 7, 7, 7), customDate = fixedDate)
            val prompt = LiuyaoPromptBuilder.buildUserPrompt(data, "男", "1990-01-01", "问事")
            assertThat(prompt).contains("主卦乾为天")
            assertThat(prompt).contains("乾宫金")
        }

        @Test
        fun buildUserPrompt_containsYaoDetails() {
            val data = LiuyaoEngine.generate(yaoArray = listOf(9, 7, 7, 7, 7, 7), customDate = fixedDate)
            val prompt = LiuyaoPromptBuilder.buildUserPrompt(data, "男", "1990-01-01", "问事")
            assertThat(prompt).contains("初爻")
            assertThat(prompt).contains("上爻")
            assertThat(prompt).contains("六亲")
            assertThat(prompt).contains("六神")
        }

        @Test
        fun buildUserPrompt_containsChangingYaoInfo() {
            val data = LiuyaoEngine.generate(yaoArray = listOf(9, 7, 7, 7, 7, 7), customDate = fixedDate)
            val prompt = LiuyaoPromptBuilder.buildUserPrompt(data, "男", "1990-01-01", "问事")
            assertThat(prompt).contains("动爻")
            assertThat(prompt).contains("老阳")
        }

        @Test
        fun buildUserPrompt_containsWorldAndResponse() {
            val data = LiuyaoEngine.generate(yaoArray = listOf(7, 7, 7, 7, 7, 7), customDate = fixedDate)
            val prompt = LiuyaoPromptBuilder.buildUserPrompt(data, "男", "1990-01-01", "问事")
            assertThat(prompt).contains("世爻")
            assertThat(prompt).contains("应爻")
        }

        @Test
        fun buildUserPrompt_containsVoidBranches() {
            val data = LiuyaoEngine.generate(yaoArray = listOf(7, 7, 7, 7, 7, 7), customDate = fixedDate)
            val prompt = LiuyaoPromptBuilder.buildUserPrompt(data, "男", "1990-01-01", "问事")
            assertThat(prompt).contains("空亡")
        }

        @Test
        fun buildUserPrompt_includesSpecialPattern_whenPresent() {
            val data = LiuyaoEngine.generate(yaoArray = listOf(7, 7, 7, 7, 7, 7), customDate = fixedDate)
            val prompt = LiuyaoPromptBuilder.buildUserPrompt(data, "男", "1990-01-01", "问事")
            assertThat(prompt).contains("静卦")
        }
    }

    @Nested
    @DisplayName("MeihuaPromptBuilder")
    inner class MeihuaPromptBuilderTest {

        @Test
        fun getSystemPrompt_isNotEmpty() {
            val prompt = MeihuaPromptBuilder.getSystemPrompt()
            assertThat(prompt).isNotEmpty()
        }

        @Test
        fun getSystemPrompt_containsKeyPrinciples() {
            val prompt = MeihuaPromptBuilder.getSystemPrompt()
            assertThat(prompt).contains("体用中心")
            assertThat(prompt).contains("旺衰判断")
            assertThat(prompt).contains("输出规则")
        }

        @Test
        fun getSystemPrompt_doesNotContainLiuyaoTerms() {
            val prompt = MeihuaPromptBuilder.getSystemPrompt()
            // 系统提示中明确禁止六爻术语
            assertThat(prompt).contains("禁止混入六爻概念")
        }

        @Test
        fun buildUserPrompt_containsBasicInfo() {
            val data = MeihuaEngine.generate(
                method = MeihuaEngine.Method.NUMBER,
                number = 1, numberB = 1,
                customDate = fixedDate
            )
            val prompt = MeihuaPromptBuilder.buildUserPrompt(data, "女", "1995-05-05", "测试问题")
            assertThat(prompt).contains("占法：梅花易数")
            assertThat(prompt).contains("性别：女")
            assertThat(prompt).contains("出生日期：1995-05-05")
            assertThat(prompt).contains("测试问题")
        }

        @Test
        fun buildUserPrompt_containsTiYongInfo() {
            val data = MeihuaEngine.generate(
                method = MeihuaEngine.Method.NUMBER,
                number = 1, numberB = 1,
                customDate = fixedDate
            )
            val prompt = MeihuaPromptBuilder.buildUserPrompt(data, "男", "1990-01-01", "问事")
            assertThat(prompt).contains("体卦")
            assertThat(prompt).contains("用卦")
            assertThat(prompt).contains("动爻")
        }

        @Test
        fun buildUserPrompt_containsHexagramStructure() {
            val data = MeihuaEngine.generate(
                method = MeihuaEngine.Method.NUMBER,
                number = 1, numberB = 1,
                customDate = fixedDate
            )
            val prompt = MeihuaPromptBuilder.buildUserPrompt(data, "男", "1990-01-01", "问事")
            assertThat(prompt).contains("主卦")
            assertThat(prompt).contains("互卦")
            assertThat(prompt).contains("变卦")
        }

        @Test
        fun buildUserPrompt_containsAnalysisInfo() {
            val data = MeihuaEngine.generate(
                method = MeihuaEngine.Method.NUMBER,
                number = 1, numberB = 1,
                customDate = fixedDate
            )
            val prompt = MeihuaPromptBuilder.buildUserPrompt(data, "男", "1990-01-01", "问事")
            assertThat(prompt).contains("四时旺衰")
            assertThat(prompt).contains("体用关系")
        }

        @Test
        fun buildUserPrompt_containsYaoDetails() {
            val data = MeihuaEngine.generate(
                method = MeihuaEngine.Method.NUMBER,
                number = 1, numberB = 1,
                customDate = fixedDate
            )
            val prompt = MeihuaPromptBuilder.buildUserPrompt(data, "男", "1990-01-01", "问事")
            assertThat(prompt).contains("初爻")
            assertThat(prompt).contains("上爻")
        }

        @Test
        fun buildUserPrompt_marksPureHexagram() {
            // 乾为天 is a pure hexagram (上卦=下卦=乾)
            val data = MeihuaEngine.generate(
                method = MeihuaEngine.Method.NUMBER,
                number = 1, numberB = 1,
                customDate = fixedDate
            )
            val prompt = MeihuaPromptBuilder.buildUserPrompt(data, "男", "1990-01-01", "问事")
            assertThat(prompt).contains("八纯卦")
        }
    }
}
