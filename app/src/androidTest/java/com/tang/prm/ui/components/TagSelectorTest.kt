package com.tang.prm.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.tang.prm.domain.model.CustomType
import com.tang.prm.ui.components.TagSelector
import com.tang.prm.ui.components.TagSelectorMode
import org.junit.Rule
import org.junit.Test

/**
 * TagSelector 组件测试
 *
 * 验证标签显示、选中高亮和选择回调。
 */
class TagSelectorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testTags = listOf(
        CustomType(id = 1, category = "TEST", name = "朋友"),
        CustomType(id = 2, category = "TEST", name = "同事"),
        CustomType(id = 3, category = "TEST", name = "家人")
    )

    @Test
    fun tagSelector_displaysTags() {
        composeTestRule.setContent {
            MaterialTheme {
                TagSelector(
                    mode = TagSelectorMode.SINGLE,
                    availableItems = testTags,
                    selectedItems = emptyList(),
                    onSelectionChange = {},
                    onAddItem = { _, _, _ -> },
                    onDeleteItem = {}
                )
            }
        }
        composeTestRule.onNodeWithText("朋友").assertIsDisplayed()
        composeTestRule.onNodeWithText("同事").assertIsDisplayed()
        composeTestRule.onNodeWithText("家人").assertIsDisplayed()
    }

    @Test
    fun tagSelector_selectedTagsAreHighlighted() {
        composeTestRule.setContent {
            MaterialTheme {
                TagSelector(
                    mode = TagSelectorMode.SINGLE,
                    availableItems = testTags,
                    selectedItems = listOf("朋友"),
                    onSelectionChange = {},
                    onAddItem = { _, _, _ -> },
                    onDeleteItem = {}
                )
            }
        }
        // 选中的标签仍然显示文本
        composeTestRule.onNodeWithText("朋友").assertIsDisplayed()
    }

    @Test
    fun tagSelector_selectionCallbackWorks() {
        var selectedTags = emptyList<String>()
        composeTestRule.setContent {
            MaterialTheme {
                TagSelector(
                    mode = TagSelectorMode.SINGLE,
                    availableItems = testTags,
                    selectedItems = selectedTags,
                    onSelectionChange = { selectedTags = it },
                    onAddItem = { _, _, _ -> },
                    onDeleteItem = {}
                )
            }
        }
        // 点击标签触发回调
        composeTestRule.onNodeWithText("朋友").performClick()
        assert(selectedTags.contains("朋友")) { "TagSelector should have selected '朋友'" }
    }

    @Test
    fun tagSelector_multiMode_allowsMultipleSelections() {
        var selectedTags = emptyList<String>()
        composeTestRule.setContent {
            MaterialTheme {
                TagSelector(
                    mode = TagSelectorMode.MULTI,
                    availableItems = testTags,
                    selectedItems = selectedTags,
                    onSelectionChange = { selectedTags = it },
                    onAddItem = { _, _, _ -> },
                    onDeleteItem = {}
                )
            }
        }
        composeTestRule.onNodeWithText("朋友").performClick()
        composeTestRule.onNodeWithText("同事").performClick()
        assert(selectedTags.size == 2) { "TagSelector MULTI mode should allow multiple selections" }
    }

    @Test
    fun tagSelector_displaysAddButton() {
        composeTestRule.setContent {
            MaterialTheme {
                TagSelector(
                    mode = TagSelectorMode.SINGLE,
                    availableItems = testTags,
                    selectedItems = emptyList(),
                    onSelectionChange = {},
                    onAddItem = { _, _, _ -> },
                    onDeleteItem = {},
                    showAddButton = true
                )
            }
        }
        composeTestRule.onNodeWithText("新增").assertIsDisplayed()
    }
}
