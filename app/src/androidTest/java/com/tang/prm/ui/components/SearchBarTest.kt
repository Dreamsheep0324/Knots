package com.tang.prm.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.tang.prm.ui.components.SearchBar
import org.junit.Rule
import org.junit.Test

/**
 * SearchBar 组件测试
 *
 * 验证搜索栏的提示文本、输入、清除按钮和回调。
 */
class SearchBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun searchBar_displaysHintText() {
        composeTestRule.setContent {
            MaterialTheme {
                SearchBar(
                    query = "",
                    onQueryChange = {},
                    placeholder = "搜索内容"
                )
            }
        }
        composeTestRule.onNodeWithText("搜索内容").assertIsDisplayed()
    }

    @Test
    fun searchBar_canEnterInput() {
        var query by mutableStateOf("")
        composeTestRule.setContent {
            MaterialTheme {
                SearchBar(
                    query = query,
                    onQueryChange = { query = it },
                    placeholder = "搜索"
                )
            }
        }
        // 输入文本
        composeTestRule.onNodeWithText("搜索")
            .performTextInput("测试")
        // 验证回调被触发，query 已更新
        assert(query == "测试")
    }

    @Test
    fun searchBar_clearButtonAppears_whenTextEntered() {
        composeTestRule.setContent {
            MaterialTheme {
                SearchBar(
                    query = "已有文本",
                    onQueryChange = {},
                    placeholder = "搜索"
                )
            }
        }
        // 有文本时清除按钮应显示
        composeTestRule.onNodeWithContentDescription("清除").assertIsDisplayed()
    }

    @Test
    fun searchBar_clearButtonHides_whenTextEmpty() {
        composeTestRule.setContent {
            MaterialTheme {
                SearchBar(
                    query = "",
                    onQueryChange = {},
                    placeholder = "搜索"
                )
            }
        }
        // 无文本时清除按钮不应存在
        composeTestRule.onNodeWithContentDescription("清除").assertDoesNotExist()
    }

    @Test
    fun searchBar_clearButtonClearsInput() {
        var query by mutableStateOf("已有文本")
        composeTestRule.setContent {
            MaterialTheme {
                SearchBar(
                    query = query,
                    onQueryChange = { query = it },
                    placeholder = "搜索"
                )
            }
        }
        // 点击清除按钮
        composeTestRule.onNodeWithContentDescription("清除").performClick()
        // 验证 query 被清空
        assert(query.isEmpty())
    }

    @Test
    fun searchBar_callbackTriggered_onQueryChange() {
        var receivedQuery = ""
        composeTestRule.setContent {
            MaterialTheme {
                SearchBar(
                    query = "",
                    onQueryChange = { receivedQuery = it },
                    placeholder = "搜索"
                )
            }
        }
        composeTestRule.onNodeWithText("搜索")
            .performTextInput("hello")
        assert(receivedQuery == "hello")
    }
}
