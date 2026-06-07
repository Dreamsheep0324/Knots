package com.tang.prm.ui.profile

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.NavController
import com.tang.prm.feature.profile.ProfileScreen
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

/**
 * ProfileScreen 冒烟测试
 *
 * 验证个人页基本渲染：头像区、设置项、关于区、备份区。
 */
class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val navController: NavController = mockk(relaxed = true)

    @Test
    fun profileScreen_displaysProfileHeader() {
        composeTestRule.setContent {
            MaterialTheme {
                ProfileScreen(navController = navController)
            }
        }
        composeTestRule.waitForIdle()
        // 验证页面标题
        composeTestRule.onNodeWithText("我的").assertIsDisplayed()
        // 验证编辑资料按钮
        composeTestRule.onNodeWithText("编辑资料").assertIsDisplayed()
    }

    @Test
    fun profileScreen_displaysSettingsItems() {
        composeTestRule.setContent {
            MaterialTheme {
                ProfileScreen(navController = navController)
            }
        }
        composeTestRule.waitForIdle()
        // 验证数据管理设置项
        composeTestRule.onNodeWithText("数据备份").assertIsDisplayed()
        composeTestRule.onNodeWithText("数据恢复").assertIsDisplayed()
        // 验证提醒设置项
        composeTestRule.onNodeWithText("提醒通知").assertIsDisplayed()
    }

    @Test
    fun profileScreen_displaysAboutSection() {
        composeTestRule.setContent {
            MaterialTheme {
                ProfileScreen(navController = navController)
            }
        }
        composeTestRule.waitForIdle()
        // 验证关于我们入口
        composeTestRule.onNodeWithText("关于我们").assertIsDisplayed()
    }

    @Test
    fun profileScreen_displaysBackupSection() {
        composeTestRule.setContent {
            MaterialTheme {
                ProfileScreen(navController = navController)
            }
        }
        composeTestRule.waitForIdle()
        // 验证数据管理区标题
        composeTestRule.onNodeWithText("数据管理").assertIsDisplayed()
        // 验证备份和恢复入口
        composeTestRule.onNodeWithText("数据备份").assertIsDisplayed()
        composeTestRule.onNodeWithText("数据恢复").assertIsDisplayed()
        composeTestRule.onNodeWithText("导出数据").assertIsDisplayed()
    }
}
