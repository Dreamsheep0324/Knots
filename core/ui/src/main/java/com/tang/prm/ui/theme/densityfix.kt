package com.tang.prm.ui.theme

import android.content.Context
import android.util.DisplayMetrics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

object DensityFix {

    private const val DPI_BASELINE = 160f
    private const val DENSITY_FALLBACK = 2.75f
    private const val DENSITY_MIN = 1.5f
    private const val DENSITY_MAX = 3.5f
    private const val DENSITY_SAFE_MIN = 1.0f
    private const val DENSITY_SAFE_MAX = 4.0f
    private const val FONT_SCALE_MIN = 0.85f
    private const val FONT_SCALE_MAX = 2.0f

    fun calculateUnifiedDensity(context: Context): Float {
        val metrics = context.resources.displayMetrics
        val xdpi = metrics.xdpi
        val ydpi = metrics.ydpi

        if (!xdpi.isFinite() || xdpi <= 0f || !ydpi.isFinite() || ydpi <= 0f) {
            return metrics.density.coerceIn(DENSITY_SAFE_MIN, DENSITY_SAFE_MAX)
        }

        val physicalDpi = (xdpi + ydpi) / 2f
        val calculatedDensity = physicalDpi / DPI_BASELINE

        if (!calculatedDensity.isFinite() || calculatedDensity <= 0f) {
            return metrics.density.coerceIn(DENSITY_SAFE_MIN, DENSITY_SAFE_MAX)
        }

        return calculatedDensity.coerceIn(DENSITY_MIN, DENSITY_MAX)
    }

    fun createFixedDensity(context: Context): Density {
        val metrics = context.resources.displayMetrics
        val systemDensity = metrics.density

        if (systemDensity <= 0f || !systemDensity.isFinite()) {
            return Density(density = DENSITY_FALLBACK, fontScale = 1.0f)
        }

        val unifiedDensity = calculateUnifiedDensity(context)
        val fontScale = if (systemDensity > 0f) {
            (metrics.scaledDensity / systemDensity).coerceIn(FONT_SCALE_MIN, FONT_SCALE_MAX)
        } else {
            1.0f
        }

        return Density(
            density = unifiedDensity,
            fontScale = fontScale
        )
    }
}

@Composable
fun FixedDensityProvider(
    context: Context,
    content: @Composable () -> Unit
) {
    val fixedDensity = remember(context) {
        runCatching { DensityFix.createFixedDensity(context) }
            .getOrElse { Density(density = context.resources.displayMetrics.density.coerceIn(1.0f, 4.0f), fontScale = 1.0f) }
    }

    CompositionLocalProvider(
        LocalDensity provides fixedDensity,
        content = content
    )
}


