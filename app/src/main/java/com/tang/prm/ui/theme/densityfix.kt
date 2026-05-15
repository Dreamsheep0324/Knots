package com.tang.prm.ui.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/**
 * 屏幕密度统一修复
 *
 * 问题：不同厂商对相同物理 DPI 的设备使用不同的 density 值（如 2.55 vs 3.0），
 * 导致同样的 dp 在不同手机上显示大小差异明显。
 *
 * 解决方案：基于物理 DPI 计算一个统一的 density，使所有设备的显示效果趋于一致。
 * 以物理 DPI ≈ 392 为基准，目标 density 约为 2.55（对应 424dp 宽度）。
 */
object DensityFix {

    /**
     * 计算基于物理 DPI 的统一 density
     *
     * 公式：density = xdpi / 160（160 是 Android 标准基准 DPI）
     * 然后限制在合理范围内（1.5 ~ 3.5），避免极端值
     */
    fun calculateUnifiedDensity(context: Context): Float {
        val metrics = context.resources.displayMetrics
        val xdpi = metrics.xdpi
        val ydpi = metrics.ydpi
        val physicalDpi = (xdpi + ydpi) / 2f

        // 基于物理 DPI 计算 density
        val calculatedDensity = physicalDpi / 160f

        // 限制在合理范围内，避免极端值导致显示异常
        return calculatedDensity.coerceIn(1.5f, 3.5f)
    }

    /**
     * 判断当前系统的 density 是否需要修正
     *
     * 当系统 density 与基于物理 DPI 计算的 density 差异超过阈值时，
     * 认为需要修正。
     */
    fun shouldFixDensity(context: Context, threshold: Float = 0.15f): Boolean {
        val metrics = context.resources.displayMetrics
        val systemDensity = metrics.density
        val unifiedDensity = calculateUnifiedDensity(context)
        return kotlin.math.abs(systemDensity - unifiedDensity) > threshold
    }

    /**
     * 创建修正后的 Density 对象
     */
    fun createFixedDensity(context: Context): Density {
        val metrics = context.resources.displayMetrics
        val unifiedDensity = calculateUnifiedDensity(context)
        val fontScale = metrics.scaledDensity / metrics.density

        return Density(
            density = unifiedDensity,
            fontScale = fontScale.coerceIn(0.85f, 1.3f)
        )
    }
}

/**
 * 提供统一 density 的 Compose 包装器
 *
 * 用法：在 App 最外层包裹，使整个应用使用统一的 density
 */
@Composable
fun FixedDensityProvider(
    context: Context,
    content: @Composable () -> Unit
) {
    val fixedDensity = remember(context) { DensityFix.createFixedDensity(context) }

    CompositionLocalProvider(
        LocalDensity provides fixedDensity,
        content = content
    )
}


