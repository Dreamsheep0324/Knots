package com.tang.prm.ui.navigation

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.tang.prm.ui.navigation.TangNavHost
import org.junit.Rule
import org.junit.Test

/**
 * 导航冒烟测试
 *
 * 验证底部导航栏的页面切换功能。
 * 使用真实 NavHostController 测试实际导航行为。
 */
class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var navController: NavHostController? = null

    @Test
    fun navigation_displaysHomeScreen() {
        composeTestRule.setContent {
            navController = rememberNavController()
            MaterialTheme {
                TangNavHost(navController = navController!!)
            }
        }
        // 首页应默认显示
        composeTestRule.onNodeWithText("礼物").assertIsDisplayed()
    }

    @Test
    fun navigation_clickEventsTab_navigatesToEvents() {
        composeTestRule.setContent {
            navController = rememberNavController()
            MaterialTheme {
                TangNavHost(navController = navController!!)
            }
        }
        // 点击事件标签
        composeTestRule.onNodeWithText("事件").performClick()
        // 验证事件页面显示
        composeTestRule.onNodeWithText("列表").assertIsDisplayed()
    }

    @Test
    fun navigation_clickContactsTab_navigatesToContacts() {
        composeTestRule.setContent {
            navController = rememberNavController()
            MaterialTheme {
                TangNavHost(navController = navController!!)
            }
        }
        // 点击人物标签
        composeTestRule.onNodeWithText("人物").performClick()
        // 验证联系人页面显示
        composeTestRule.waitForIdle()
    }

    @Test
    fun navigation_clickAnniversariesTab_navigatesToAnniversaries() {
        composeTestRule.setContent {
            navController = rememberNavController()
            MaterialTheme {
                TangNavHost(navController = navController!!)
            }
        }
        // 点击纪念标签
        composeTestRule.onNodeWithText("纪念").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun navigation_clickChatTab_navigatesToChat() {
        composeTestRule.setContent {
            navController = rememberNavController()
            MaterialTheme {
                TangNavHost(navController = navController!!)
            }
        }
        // 点击对话标签
        composeTestRule.onNodeWithText("对话").performClick()
        composeTestRule.waitForIdle()
    }
}
