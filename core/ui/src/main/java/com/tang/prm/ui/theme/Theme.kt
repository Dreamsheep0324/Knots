package com.tang.prm.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private const val TAG = "TangTheme"

/**
 * 递归解包 [ContextWrapper]，从任意包装上下文（如 Popup / Dialog / ContextThemeWrapper）
 * 中取出最底层的 [Activity]。返回 null 表示当前上下文不依附于任何 Activity，
 * 此时不应操作窗口属性。
 */
private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = Color(0xFFEEEEEE),
    onPrimaryContainer = Color(0xFF212121),
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = Color(0xFFEEEEEE),
    onSecondaryContainer = Color(0xFF212121),
    background = Background,
    onBackground = Color(0xFF212121),
    surface = Surface,
    onSurface = Color(0xFF212121),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFD8DEE6),
    outlineVariant = OutlineVariantLight,
    error = Error,
    onError = OnPrimary
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = DarkOnSurface,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = DarkOnSurface,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = DarkError,
    onError = DarkOnPrimary
)

@Composable
fun TangTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity()
            if (activity == null) {
                Log.w(TAG, "当前上下文不依附于 Activity，跳过状态栏配置")
                return@SideEffect
            }
            val window = activity.window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    val intimacyColors = if (darkTheme) DarkIntimacyColors else LightIntimacyColors

    CompositionLocalProvider(LocalIntimacyColors provides intimacyColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = TangShapes,
            content = content
        )
    }
}

object DialogDefaults {
    val containerColor: Color
        @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surface
    val shape = RoundedCornerShape(20.dp)
}
