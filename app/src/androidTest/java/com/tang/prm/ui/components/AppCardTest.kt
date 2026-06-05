package com.tang.prm.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.tang.prm.ui.components.AppCard
import org.junit.Rule
import org.junit.Test

/**
 * AppCard 组件测试
 *
 * 验证卡片标题、内容和点击回调。
 */
class AppCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun appCard_displaysTitle() {
        composeTestRule.setContent {
            MaterialTheme {
                AppCard {
                    Text("卡片标题")
                }
            }
        }
        composeTestRule.onNodeWithText("卡片标题").assertIsDisplayed()
    }

    @Test
    fun appCard_displaysContent() {
        composeTestRule.setContent {
            MaterialTheme {
                AppCard {
                    Column {
                        Text("标题行")
                        Text("内容描述")
                    }
                }
            }
        }
        composeTestRule.onNodeWithText("标题行").assertIsDisplayed()
        composeTestRule.onNodeWithText("内容描述").assertIsDisplayed()
    }

    @Test
    fun appCard_clickActionWorks() {
        var clicked = false
        composeTestRule.setContent {
            MaterialTheme {
                AppCard(
                    onClick = { clicked = true }
                ) {
                    Text("可点击卡片")
                }
            }
        }
        composeTestRule.onNodeWithText("可点击卡片").performClick()
        assert(clicked) { "AppCard onClick should have been triggered" }
    }
}
