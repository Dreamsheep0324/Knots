package com.tang.prm.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Baseline Profile 生成器
 *
 * 覆盖应用冷启动这一关键用户旅程，生成 AOT 编译配置以优化启动性能。
 * 启动路径是 Baseline Profile 最核心的场景，避免复杂 UI 交互以保证稳定性。
 *
 * 运行方式：
 *   ./gradlew :baselineprofile:connectedBenchmarkAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.enabledRules=BaselineProfile
 *
 * 生成完成后，baseline-prof.txt 会自动写入 app/src/main/baseline-prof.txt
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
            includeInStartupProfile = true
        ) {
            // 冷启动并等待首帧绘制完成
            startActivityAndWait()
            // 等待 UI 完全稳定
            device.waitForIdle()
        }
    }

    companion object {
        private const val PACKAGE_NAME = "com.tang.prm"
    }
}
