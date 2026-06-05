package com.tang.prm.ui.theme

import android.content.Context
import android.util.DisplayMetrics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

object DensityFix {

    fun calculateUnifiedDensity(context: Context): Float {
        val metrics = context.resources.displayMetrics
        val xdpi = metrics.xdpi
        val ydpi = metrics.ydpi

        if (!xdpi.isFinite() || xdpi <= 0f || !ydpi.isFinite() || ydpi <= 0f) {
            return metrics.density.coerceIn(1.0f, 4.0f)
        }

        val physicalDpi = (xdpi + ydpi) / 2f
        val calculatedDensity = physicalDpi / 160f

        if (!calculatedDensity.isFinite() || calculatedDensity <= 0f) {
            return metrics.density.coerceIn(1.0f, 4.0f)
        }

        return calculatedDensity.coerceIn(1.5f, 3.5f)
    }

    fun shouldFixDensity(context: Context, threshold: Float = 0.15f): Boolean {
        val metrics = context.resources.displayMetrics
        val systemDensity = metrics.density
        if (systemDensity <= 0f || !systemDensity.isFinite()) return false
        val unifiedDensity = calculateUnifiedDensity(context)
        return kotlin.math.abs(systemDensity - unifiedDensity) > threshold
    }

    fun createFixedDensity(context: Context): Density {
        val metrics = context.resources.displayMetrics
        val systemDensity = metrics.density

        if (systemDensity <= 0f || !systemDensity.isFinite()) {
            return Density(density = 2.75f, fontScale = 1.0f)
        }

        val unifiedDensity = calculateUnifiedDensity(context)
        val fontScale = if (systemDensity > 0f) {
            (metrics.scaledDensity / systemDensity).coerceIn(0.85f, 1.3f)
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


