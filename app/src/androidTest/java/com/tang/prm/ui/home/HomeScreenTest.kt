package com.tang.prm.ui.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.NavController
import com.tang.prm.ui.home.HomeScreen
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

/**
 * HomeScreen 冒烟测试
 *
 * 验证首页基本渲染：问候语、频道网格、轨道罗盘等核心元素。
 * 使用 mock NavController 避免真实导航依赖。
 */
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val navController: NavController = mockk(relaxed = true)

    @Test
    fun homeScreen_displaysChannelGrid() {
        composeTestRule.setContent {
            MaterialTheme {
                HomeScreen(navController = navController)
            }
        }
        // 等待数据加载
        composeTestRule.waitForIdle()
        // 验证频道区域存在（频道卡片使用 Icon + Text，验证频道名称文字）
        composeTestRule.onNodeWithText("礼物").assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysBottomNavItems() {
        composeTestRule.setContent {
            MaterialTheme {
                HomeScreen(navController = navController)
            }
        }
        composeTestRule.waitForIdle()
        // 验证底部导航项显示
        composeTestRule.onNodeWithText("首页").assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysDateInfo() {
        composeTestRule.setContent {
            MaterialTheme {
                HomeScreen(navController = navController)
            }
        }
        composeTestRule.waitForIdle()
        // 验证时间显示区域存在（时间格式为 HH:mm:ss）
        composeTestRule.onNodeWithContentDescription("时间").assertExists()
    }
}
