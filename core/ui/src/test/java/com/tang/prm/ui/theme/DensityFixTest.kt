package com.tang.prm.ui.theme

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("DensityFix 密度计算")
class DensityFixTest {

    private fun createContext(
        xdpi: Float,
        ydpi: Float,
        density: Float,
        scaledDensity: Float = density
    ): Context {
        val metrics = DisplayMetrics().apply {
            this.xdpi = xdpi
            this.ydpi = ydpi
            this.density = density
            this.scaledDensity = scaledDensity
        }
        val resources = mockk<Resources>()
        every { resources.displayMetrics } returns metrics
        val context = mockk<Context>()
        every { context.resources } returns resources
        return context
    }

    @Nested
    @DisplayName("calculateUnifiedDensity")
    inner class CalculateUnifiedDensityTest {

        @Test
        fun `正常 xdpi ydpi 计算正确密度`() {
            // xdpi=480, ydpi=480 → physicalDpi=480 → density=480/160=3.0
            val context = createContext(xdpi = 480f, ydpi = 480f, density = 2.75f)
            val result = DensityFix.calculateUnifiedDensity(context)
            assertThat(result).isEqualTo(3.0f)
        }

        @Test
        fun `xdpi 为 0 时回退到系统 density`() {
            val context = createContext(xdpi = 0f, ydpi = 480f, density = 2.75f)
            val result = DensityFix.calculateUnifiedDensity(context)
            assertThat(result).isEqualTo(2.75f)
        }

        @Test
        fun `xdpi 为负数时回退到系统 density`() {
            val context = createContext(xdpi = -1f, ydpi = 480f, density = 2.75f)
            val result = DensityFix.calculateUnifiedDensity(context)
            assertThat(result).isEqualTo(2.75f)
        }

        @Test
        fun `xdpi 为 NaN 时回退到系统 density`() {
            val context = createContext(xdpi = Float.NaN, ydpi = 480f, density = 2.75f)
            val result = DensityFix.calculateUnifiedDensity(context)
            assertThat(result).isEqualTo(2.75f)
        }

        @Test
        fun `计算密度低于 1_5 时被钳制到 1_5`() {
            // xdpi=160 → density=1.0 → coerceIn(1.5, 3.5) → 1.5
            val context = createContext(xdpi = 160f, ydpi = 160f, density = 1.0f)
            val result = DensityFix.calculateUnifiedDensity(context)
            assertThat(result).isEqualTo(1.5f)
        }

        @Test
        fun `计算密度高于 3_5 时被钳制到 3_5`() {
            // xdpi=720 → density=4.5 → coerceIn(1.5, 3.5) → 3.5
            val context = createContext(xdpi = 720f, ydpi = 720f, density = 4.5f)
            val result = DensityFix.calculateUnifiedDensity(context)
            assertThat(result).isEqualTo(3.5f)
        }

        @Test
        fun `系统 density 超出范围时也被钳制`() {
            // xdpi=0 (invalid) → fallback to density=5.0 → coerceIn(1.0, 4.0) → 4.0
            val context = createContext(xdpi = 0f, ydpi = 0f, density = 5.0f)
            val result = DensityFix.calculateUnifiedDensity(context)
            assertThat(result).isEqualTo(4.0f)
        }

        @Test
        fun `系统 density 为负数时被钳制到 1_0`() {
            val context = createContext(xdpi = 0f, ydpi = 0f, density = -1f)
            val result = DensityFix.calculateUnifiedDensity(context)
            assertThat(result).isEqualTo(1.0f)
        }
    }

    @Nested
    @DisplayName("createFixedDensity")
    inner class CreateFixedDensityTest {

        @Test
        fun `正常参数返回包含正确 density 和 fontScale 的 Density`() {
            val context = createContext(
                xdpi = 480f, ydpi = 480f, density = 2.75f, scaledDensity = 2.75f
            )
            val result = context.run { DensityFix.createFixedDensity(this) }
            assertThat(result.density).isEqualTo(3.0f)
            // fontScale = scaledDensity / density = 2.75 / 2.75 = 1.0
            assertThat(result.fontScale).isEqualTo(1.0f)
        }

        @Test
        fun `系统 density 为 0 时返回默认 Density`() {
            val context = createContext(
                xdpi = 480f, ydpi = 480f, density = 0f, scaledDensity = 0f
            )
            val result = DensityFix.createFixedDensity(context)
            assertThat(result.density).isEqualTo(2.75f)
            assertThat(result.fontScale).isEqualTo(1.0f)
        }

        @Test
        fun `系统 density 为负数时返回默认 Density`() {
            val context = createContext(
                xdpi = 480f, ydpi = 480f, density = -1f, scaledDensity = 0f
            )
            val result = DensityFix.createFixedDensity(context)
            assertThat(result.density).isEqualTo(2.75f)
            assertThat(result.fontScale).isEqualTo(1.0f)
        }

        @Test
        fun `fontScale 被钳制在 0_85 到 2_0 之间`() {
            // scaledDensity/density = 0.5 → coerceIn(0.85, 2.0) → 0.85
            val context = createContext(
                xdpi = 480f, ydpi = 480f, density = 4.0f, scaledDensity = 2.0f
            )
            val result = DensityFix.createFixedDensity(context)
            assertThat(result.fontScale).isEqualTo(0.85f)
        }

        @Test
        fun `fontScale 上限被钳制到 2_0`() {
            // scaledDensity/density = 3.0 → coerceIn(0.85, 2.0) → 2.0
            val context = createContext(
                xdpi = 480f, ydpi = 480f, density = 1.0f, scaledDensity = 3.0f
            )
            val result = DensityFix.createFixedDensity(context)
            assertThat(result.fontScale).isEqualTo(2.0f)
        }
    }
}
