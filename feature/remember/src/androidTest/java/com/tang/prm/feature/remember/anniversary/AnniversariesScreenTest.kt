package com.tang.prm.feature.remember.anniversary

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.NavController
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

/**
 * AnniversariesScreen 冒烟测试
 *
 * 验证纪念日列表页基本渲染：空状态、添加按钮、筛选标签、列表项。
 */
class AnniversariesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val navController: NavController = mockk(relaxed = true)

    @Test
    fun anniversariesScreen_displaysEmptyState() {
        composeTestRule.setContent {
            MaterialTheme {
                AnniversariesScreen(navController = navController)
            }
        }
        composeTestRule.waitForIdle()
        // 无纪念日时应显示空状态
        composeTestRule.onNodeWithText("暂无纪念日").assertExists()
    }

    @Test
    fun anniversariesScreen_displaysAddFab() {
        composeTestRule.setContent {
            MaterialTheme {
                AnniversariesScreen(navController = navController)
            }
        }
        composeTestRule.waitForIdle()
        // 验证新建纪念日按钮存在
        composeTestRule.onNodeWithContentDescription("新建纪念日").assertExists()
    }

    @Test
    fun anniversariesScreen_displaysFilterTabs() {
        composeTestRule.setContent {
            MaterialTheme {
                AnniversariesScreen(navController = navController)
            }
        }
        composeTestRule.waitForIdle()
        // 验证筛选标签显示
        composeTestRule.onNodeWithText("全部").assertIsDisplayed()
        composeTestRule.onNodeWithText("即将到来").assertIsDisplayed()
        composeTestRule.onNodeWithText("已过期").assertIsDisplayed()
    }

    @Test
    fun anniversariesScreen_displaysAnniversaryCard_whenDataProvided() {
        val testAnniversary = com.tang.prm.domain.model.Anniversary(
            id = 1,
            name = "测试生日",
            type = com.tang.prm.domain.model.AnniversaryType.BIRTHDAY,
            date = System.currentTimeMillis() + 86400000L // 明天
        )
        composeTestRule.setContent {
            MaterialTheme {
                AnniversaryCard(
                    anniversary = testAnniversary,
                    onClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        // 验证纪念日卡片显示名称和类型
        composeTestRule.onNodeWithText("测试生日").assertIsDisplayed()
        composeTestRule.onNodeWithText("生日").assertIsDisplayed()
    }
}
