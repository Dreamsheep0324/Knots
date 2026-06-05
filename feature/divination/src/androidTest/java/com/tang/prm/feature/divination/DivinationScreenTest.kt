package com.tang.prm.feature.divination

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavController
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

/**
 * DivinationScreen 冒烟测试
 *
 * 验证占卜首页基本渲染：方式选择卡片、历史按钮。
 */
class DivinationScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val navController: NavController = mockk(relaxed = true)

    @Test
    fun divinationScreen_displaysMethodSelectionTabs() {
        composeTestRule.setContent {
            MaterialTheme {
                DivinationScreen(navController = navController)
            }
        }
        composeTestRule.waitForIdle()
        // 验证选择占卜方式标题
        composeTestRule.onNodeWithText("选择占卜方式").assertIsDisplayed()
        // 验证梅花和六爻方式卡片
        composeTestRule.onNodeWithText("梅花").assertIsDisplayed()
        composeTestRule.onNodeWithText("六爻").assertIsDisplayed()
    }

    @Test
    fun divinationScreen_meihuaMethodCanBeSelected() {
        composeTestRule.setContent {
            MaterialTheme {
                DivinationScreen(navController = navController)
            }
        }
        composeTestRule.waitForIdle()
        // 验证梅花卡片存在并可点击
        composeTestRule.onNodeWithText("梅花").performClick()
        composeTestRule.onNodeWithText("梅花").assertIsDisplayed()
    }

    @Test
    fun divinationScreen_liuyaoMethodCanBeSelected() {
        composeTestRule.setContent {
            MaterialTheme {
                DivinationScreen(navController = navController)
            }
        }
        composeTestRule.waitForIdle()
        // 验证六爻卡片存在并可点击
        composeTestRule.onNodeWithText("六爻").performClick()
        composeTestRule.onNodeWithText("六爻").assertIsDisplayed()
    }

    @Test
    fun divinationScreen_historyButtonIsAccessible() {
        composeTestRule.setContent {
            MaterialTheme {
                DivinationScreen(navController = navController)
            }
        }
        composeTestRule.waitForIdle()
        // 验证历史记录按钮存在
        composeTestRule.onNodeWithContentDescription("历史记录").assertIsDisplayed()
    }
}
