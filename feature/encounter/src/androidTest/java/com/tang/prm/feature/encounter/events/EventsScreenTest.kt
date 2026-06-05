package com.tang.prm.feature.encounter.events

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.NavController
import com.tang.prm.feature.encounter.events.EventsScreen
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

/**
 * EventsScreen 冒烟测试
 *
 * 验证事件列表页基本渲染和 FAB 按钮。
 */
class EventsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val navController: NavController = mockk(relaxed = true)

    @Test
    fun eventsScreen_displaysEmptyState() {
        composeTestRule.setContent {
            MaterialTheme {
                EventsScreen(navController = navController)
            }
        }
        composeTestRule.waitForIdle()
        // 无事件时应显示空状态
        composeTestRule.onNodeWithText("暂无事件").assertExists()
    }

    @Test
    fun eventsScreen_displaysFabButton() {
        composeTestRule.setContent {
            MaterialTheme {
                EventsScreen(navController = navController)
            }
        }
        composeTestRule.waitForIdle()
        // 验证添加事件按钮存在
        composeTestRule.onNodeWithContentDescription("添加事件").assertExists()
    }

    @Test
    fun eventsScreen_displaysViewModeToggle() {
        composeTestRule.setContent {
            MaterialTheme {
                EventsScreen(navController = navController)
            }
        }
        composeTestRule.waitForIdle()
        // 验证视图模式切换存在
        composeTestRule.onNodeWithText("列表").assertIsDisplayed()
    }
}
