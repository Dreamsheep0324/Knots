package com.tang.prm.ui

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule

/**
 * Compose UI 测试基类
 *
 * 提供通用的等待动画和异步数据加载方法，
 * 子类继承后可直接使用 composeTestRule。
 */
abstract class BaseComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /** 等待动画完成 */
    protected fun waitForAnimation() {
        composeTestRule.mainClock.advanceTimeBy(500)
    }

    /**
     * 等待异步数据加载完成，直到 [condition] 返回 true 或超时。
     *
     * 示例：`waitForData { composeTestRule.onNodeWithText("加载中").assertDoesNotExist() }`
     */
    protected fun waitForData(timeoutMs: Long = 3000, condition: () -> Boolean) {
        composeTestRule.waitUntil(timeoutMs, condition)
    }
}
