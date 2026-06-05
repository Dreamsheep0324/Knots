package com.tang.prm.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Baseline Profile 生成器
 *
 * 覆盖关键用户旅程，生成 AOT 编译配置以优化应用性能。
 * 运行方式：
 *   adb shell am instrument -w -e android.test.instrumentationRunnerArguments.class \
 *     com.tang.prm.baselineprofile.BaselineProfileGenerator \
 *     com.tang.prm/androidx.test.runner.AndroidJUnitRunner
 *
 * 生成完成后，将设备上的 baseline-prof.txt 内容复制到
 * app/src/main/baseline-prof.txt
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generateBaselineProfile() {
        baselineProfileRule.collect(
            packageName = PACKAGE_NAME,
            profileBlock = {
                // === 1. 应用冷启动 ===
                startActivityAndWait()

                // === 2. 首页浏览 ===
                device.waitForIdle()
                device.swipe(
                    device.displayWidth / 2,
                    device.displayHeight * 3 / 4,
                    device.displayWidth / 2,
                    device.displayHeight / 4,
                    5
                )
                device.waitForIdle()

                // === 3. 导航到事件页 ===
                navigateToTab("事件")
                device.waitForIdle()
                device.swipe(
                    device.displayWidth / 2,
                    device.displayHeight * 3 / 4,
                    device.displayWidth / 2,
                    device.displayHeight / 4,
                    5
                )
                device.waitForIdle()

                // === 4. 导航到纪念页 ===
                navigateToTab("纪念")
                device.waitForIdle()

                // === 5. 导航到对话页 ===
                navigateToTab("对话")
                device.waitForIdle()

                // === 6. 导航到人物页 ===
                navigateToTab("人物")
                device.waitForIdle()
                device.swipe(
                    device.displayWidth / 2,
                    device.displayHeight * 3 / 4,
                    device.displayWidth / 2,
                    device.displayHeight / 4,
                    5
                )
                device.waitForIdle()

                // === 7. 返回首页，进入占卜功能 ===
                navigateToTab("首页")
                device.waitForIdle()
                val divinationButton = device.findObject(By.desc("占卜"))
                    ?: device.findObject(By.textContains("占卜"))
                divinationButton?.click()
                device.wait(Until.hasObject(By.res(PACKAGE_NAME, "divination")), 3000)
                device.waitForIdle()
            }
        )
    }

    companion object {
        private const val PACKAGE_NAME = "com.tang.prm"
    }
}

/**
 * 通过底部导航栏切换页面
 */
private fun MacrobenchmarkScope.navigateToTab(tabTitle: String) {
    val tab = device.findObject(By.text(tabTitle))
        ?: device.findObject(By.desc(tabTitle))
    tab?.click()
    device.waitForIdle()
}
