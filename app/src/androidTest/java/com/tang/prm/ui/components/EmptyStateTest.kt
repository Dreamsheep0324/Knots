package com.tang.prm.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.tang.prm.ui.components.EmptyState
import org.junit.Rule
import org.junit.Test

/**
 * EmptyState 组件测试
 *
 * 验证空状态组件的标题、描述和操作按钮渲染。
 */
class EmptyStateTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyState_displaysTitle() {
        composeTestRule.setContent {
            EmptyState(
                icon = Icons.Default.Inbox,
                title = "暂无数据"
            )
        }
        composeTestRule.onNodeWithText("暂无数据").assertIsDisplayed()
    }

    @Test
    fun emptyState_displaysDescription() {
        composeTestRule.setContent {
            EmptyState(
                icon = Icons.Default.Inbox,
                title = "暂无数据",
                description = "点击下方按钮添加"
            )
        }
        composeTestRule.onNodeWithText("暂无数据").assertIsDisplayed()
        composeTestRule.onNodeWithText("点击下方按钮添加").assertIsDisplayed()
    }

    @Test
    fun emptyState_displaysActionButton() {
        composeTestRule.setContent {
            EmptyState(
                icon = Icons.Default.Inbox,
                title = "暂无数据",
                actionLabel = "添加",
                onAction = {}
            )
        }
        composeTestRule.onNodeWithText("添加").assertIsDisplayed()
    }

    @Test
    fun emptyState_withoutAction_hidesActionButton() {
        composeTestRule.setContent {
            EmptyState(
                icon = Icons.Default.Inbox,
                title = "暂无数据"
            )
        }
        composeTestRule.onNodeWithText("添加").assertDoesNotExist()
    }
}
