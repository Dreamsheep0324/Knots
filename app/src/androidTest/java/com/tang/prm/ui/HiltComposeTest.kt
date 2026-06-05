package com.tang.prm.ui

import androidx.compose.ui.test.junit4.createComposeRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule

/**
 * Hilt + Compose 测试基类
 *
 * 结合 Hilt 依赖注入和 Compose 测试规则，
 * 适用于需要注入真实依赖的 UI 集成测试。
 */
@HiltAndroidTest
abstract class HiltComposeTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Before
    fun init() {
        hiltRule.inject()
    }
}
