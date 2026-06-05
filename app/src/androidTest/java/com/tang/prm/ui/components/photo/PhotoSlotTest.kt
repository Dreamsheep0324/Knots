package com.tang.prm.ui.components.photo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.tang.prm.ui.components.photo.PhotoAddSlot
import com.tang.prm.ui.components.photo.PhotoSelectionArea
import com.tang.prm.ui.components.photo.PhotoSlot
import com.tang.prm.ui.components.photo.PhotoSlotMode
import org.junit.Rule
import org.junit.Test

/**
 * PhotoSlot / PhotoAddSlot / PhotoSelectionArea 组件测试
 *
 * 验证三种展示模式（AVATAR / THUMBNAIL / POLAROID）的渲染行为。
 */
class PhotoSlotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // --- PhotoSlot ---

    @Test
    fun photoSlot_avatarMode_displaysImage() {
        composeTestRule.setContent {
            PhotoSlot(
                mode = PhotoSlotMode.AVATAR,
                photoUri = "file:///test_avatar.jpg",
                onRemove = null,
                onClick = null
            )
        }
        composeTestRule.onNodeWithContentDescription("头像").assertIsDisplayed()
    }

    @Test
    fun photoSlot_thumbnailMode_displaysImage() {
        composeTestRule.setContent {
            PhotoSlot(
                mode = PhotoSlotMode.THUMBNAIL,
                photoUri = "file:///test_thumb.jpg",
                onRemove = null,
                onClick = null
            )
        }
        composeTestRule.onNodeWithContentDescription("照片").assertIsDisplayed()
    }

    @Test
    fun photoSlot_polaroidMode_displaysImage() {
        composeTestRule.setContent {
            PhotoSlot(
                mode = PhotoSlotMode.POLAROID,
                photoUri = "file:///test_polaroid.jpg",
                onRemove = null,
                onClick = null
            )
        }
        composeTestRule.onNodeWithContentDescription("照片").assertIsDisplayed()
    }

    @Test
    fun photoSlot_thumbnailMode_withRemove_showsDeleteButton() {
        composeTestRule.setContent {
            PhotoSlot(
                mode = PhotoSlotMode.THUMBNAIL,
                photoUri = "file:///test.jpg",
                onRemove = {},
                onClick = null
            )
        }
        composeTestRule.onNodeWithContentDescription("删除").assertIsDisplayed()
    }

    // --- PhotoAddSlot ---

    @Test
    fun photoAddSlot_avatarMode_displaysAddIcon() {
        composeTestRule.setContent {
            PhotoAddSlot(
                mode = PhotoSlotMode.AVATAR,
                label = "点击上传头像",
                onClick = {}
            )
        }
        composeTestRule.onNodeWithContentDescription("点击上传头像").assertIsDisplayed()
    }

    @Test
    fun photoAddSlot_thumbnailMode_displaysAddIcon() {
        composeTestRule.setContent {
            PhotoAddSlot(
                mode = PhotoSlotMode.THUMBNAIL,
                label = "添加照片",
                onClick = {}
            )
        }
        composeTestRule.onNodeWithContentDescription("添加照片").assertIsDisplayed()
    }

    @Test
    fun photoAddSlot_polaroidMode_displaysAddIcon() {
        composeTestRule.setContent {
            PhotoAddSlot(
                mode = PhotoSlotMode.POLAROID,
                label = "添加照片",
                onClick = {}
            )
        }
        composeTestRule.onNodeWithContentDescription("添加照片").assertIsDisplayed()
    }

    // --- PhotoSelectionArea ---

    @Test
    fun photoSelectionArea_empty_showsAddButton() {
        composeTestRule.setContent {
            PhotoSelectionArea(
                photos = emptyList(),
                mode = PhotoSlotMode.THUMBNAIL,
                maxCount = 5,
                onAdd = {},
                onRemove = {}
            )
        }
        // 空列表时应显示添加按钮
        composeTestRule.onNodeWithContentDescription("添加照片").assertIsDisplayed()
    }

    @Test
    fun photoSelectionArea_withPhotos_showsImages() {
        composeTestRule.setContent {
            PhotoSelectionArea(
                photos = listOf("file:///1.jpg", "file:///2.jpg"),
                mode = PhotoSlotMode.THUMBNAIL,
                maxCount = 5,
                onAdd = {},
                onRemove = {}
            )
        }
        // 验证照片显示
        composeTestRule.onNodeWithContentDescription("照片").assertIsDisplayed()
    }
}
