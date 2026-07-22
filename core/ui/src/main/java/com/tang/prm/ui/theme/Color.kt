package com.tang.prm.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ══════════════════════════════════════════════════════════════════
// 颜色定义规范（三种风格，按用途选择）：
//
// 1. 顶层 val — 品牌色/语义色，明暗模式通用（如 SignalCoral、SignalSky）
// 2. @Composable getter — 需要深色模式适配的颜色（如 TextGray、Success）
// 3. CompositionLocal — 主题切换的色组（如 IntimacyColors）
//
// 新增颜色时按用途选择风格，避免在深色模式下显示异常。
// ══════════════════════════════════════════════════════════════════

// ── 基础色板（仅用于 Theme.kt 的 ColorScheme 定义） ──────────────
val Primary = Color(0xFF2196F3)
val Secondary = Color(0xFF03A9F4)
val Background = Color(0xFFFFFFFF)
val Surface = Color(0xFFFFFFFF)
val OnPrimary = Color(0xFFFFFFFF)
val OnSecondary = Color(0xFFFFFFFF)

val DarkPrimary = Color(0xFF58A6FF)
val DarkPrimaryVariant = Color(0xFF79C0FF)
val DarkSecondary = Color(0xFF79C0FF)
val DarkBackground = Color(0xFF0D1117)
val DarkSurface = Color(0xFF161B22)
val DarkSurfaceVariant = Color(0xFF1C2129)
val DarkOnPrimary = Color(0xFF0D1117)
val DarkOnSecondary = Color(0xFF0D1117)
val DarkOnBackground = Color(0xFFE6EDF3)
val DarkOnSurface = Color(0xFFE6EDF3)
val DarkOnSurfaceVariant = Color(0xFF8B949E)
val DarkOutline = Color(0xFF30363D)
val DarkOutlineVariant = Color(0xFF21262D)
val DarkTextGray = Color(0xFF8B949E)
val DarkDivider = Color(0xFF21262D)
val DarkError = Color(0xFFFF7B72)

// ── 文本/背景语义色（深色模式自适应） ────────────────────────────
val OnBackground: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkOnBackground else Color(0xFF212121)

val OnSurface: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkOnSurface else Color(0xFF212121)

val TextGray: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkTextGray else Color(0xFF757575)

val LightGray: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkOutline else Color(0xFFE0E0E0)

val Divider: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkDivider else Color(0xFFEEEEEE)

val CardBorder: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkOutline else Color(0xFFD8DEE6)
val GridLine: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkOutline else Color(0xFFE5E5E7)

val DividerLight: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkDivider else Color(0xFFF0F0F0)
val BorderSlate: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkOutline else Color(0xFFE2E8F0)
val TextSlate: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkOnSurfaceVariant else Color(0xFF64748B)
val TextDarkSlate: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkOnSurface else Color(0xFF1E293B)

// ── 状态色（深色模式自适应） ──────────────────────────────────────
val Success: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) Color(0xFF3FB950) else Color(0xFF4CAF50)
val Warning: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) Color(0xFFD29922) else Color(0xFFFFC107)
val Error = Color(0xFFEF4444)

// ── 事件/标签背景色（深色模式自适应） ────────────────────────────
val EventLightGreen: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) Color(0xFF1A3A2E) else Color(0xFFD1FAE5)
val EventLightAmber: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) Color(0xFF3D2E1A) else Color(0xFFFFECD2)
val EventLightBlue: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) Color(0xFF1A2E4A) else Color(0xFFDBEAFE)
val EventLightPurple: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) Color(0xFF2A1E4A) else Color(0xFFEDE9FE)
val EventLightRed: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) Color(0xFF3A1A1A) else Color(0xFFFEE2E2)
val EventLightIndigo: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) Color(0xFF1A2340) else Color(0xFFEEF2FF)

val SemanticAmberBg: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) Color(0xFF3D2E1A) else Color(0xFFFEF3C7)
val SemanticAmberText = Color(0xFFD97706)
val SemanticBlueBg: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) Color(0xFF1A2E4A) else Color(0xFFDBEAFE)
val SemanticBlueText = Color(0xFF2563EB)
val SemanticPurpleBg: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) Color(0xFF2A1E4A) else Color(0xFFEDE9FE)
val SemanticPurpleText = Color(0xFF7C3AED)
val SemanticCoralBg: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) Color(0xFF3A1A1A) else Color(0xFFFEE2E2)
val SemanticCoralText = Color(0xFFDC2626)
val SemanticGreenBg: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) Color(0xFF1A3A2E) else Color(0xFFD1FAE5)

// ── 品牌信号色（明暗通用，无需深色适配） ─────────────────────────
val SignalCoral = Color(0xFFEF4444)
val SignalSky = Color(0xFF3B82F6)
val SignalAmber = Color(0xFFF59E0B)
val SignalGreen = Color(0xFF10B981)
val SignalPurple = Color(0xFF6366F1)
val SignalGold = Color(0xFFFBBF24)
val SignalElectric = Color(0xFF2196F3)
val SignalCyan = Color(0xFF4DD0E1)
val EventMoneyTeal = Color(0xFF14B8A6)
val FavoriteGold = Color(0xFFFFB300)
val AnniversaryBirthday = Color(0xFFF97316)
val AnniversaryDate = Color(0xFFE91E63)
val AnniversaryHoliday = Color(0xFF0EA5E9)
val OutlineVariantLight = Color(0xFFCBD5E1)

// ── 图表调色板（订阅统计圆环图等，明暗通用） ─────────────────────
val SubscriptionChartPalette = listOf(
    Color(0xFF5B8FF9), // 柔蓝
    Color(0xFF5AD8A6), // 薄荷绿
    Color(0xFFF6BD16), // 暖金
    Color(0xFFE86452), // 珊瑚红
    Color(0xFF6DC8EC), // 天青
    Color(0xFF945FB9), // 紫藤
    Color(0xFFFF9845), // 蜜橙
    Color(0xFF1E9493), // 青碧
)

// ── 深色模式标签色（用于 Tag 组件） ──────────────────────────────
val DarkTagBlueBg = Color(0x1A58A6FF)
val DarkTagBlueText = Color(0xFF79C0FF)
val DarkTagAmberBg = Color(0x1AD29922)
val DarkTagAmberText = Color(0xFFD29922)
val DarkTagPurpleBg = Color(0x1ABC8CFF)
val DarkTagPurpleText = Color(0xFFBC8CFF)
val DarkTagGreenBg = Color(0x1A3FB950)
val DarkTagGreenText = Color(0xFF3FB950)
val DarkTagRedBg = Color(0x1AFF7B72)
val DarkTagRedText = Color(0xFFFF7B72)

// ── 亲密度等级颜色 ────────────────────────────────────────────────
// 色值统一由 com.tang.prm.domain.model.IntimacyTier 派生（SSOT），
// 禁止在此处定义独立色板常量。浅色用 colorValue，深色用 darkColorValue。

/** 亲密度等级颜色方案，支持深色模式覆盖 */
data class IntimacyColors(
    val new: Color,
    val acquaintance: Color,
    val friend: Color,
    val close: Color,
    val family: Color
) {
    /** 根据 IntimacyTier 获取对应颜色 */
    fun forTier(tier: com.tang.prm.domain.model.IntimacyTier): Color = when (tier) {
        com.tang.prm.domain.model.IntimacyTier.NEW -> new
        com.tang.prm.domain.model.IntimacyTier.ACQUAINTANCE -> acquaintance
        com.tang.prm.domain.model.IntimacyTier.FRIEND -> friend
        com.tang.prm.domain.model.IntimacyTier.CLOSE -> close
        com.tang.prm.domain.model.IntimacyTier.FAMILY -> family
    }

    /** 根据亲密度分数获取对应颜色 */
    fun forScore(score: Int): Color = forTier(com.tang.prm.domain.model.IntimacyTier.of(score))
}

/** 浅色模式亲密度色板：派生自 [com.tang.prm.domain.model.IntimacyTier.colorValue] */
val LightIntimacyColors = IntimacyColors(
    new = Color(com.tang.prm.domain.model.IntimacyTier.NEW.colorValue),
    acquaintance = Color(com.tang.prm.domain.model.IntimacyTier.ACQUAINTANCE.colorValue),
    friend = Color(com.tang.prm.domain.model.IntimacyTier.FRIEND.colorValue),
    close = Color(com.tang.prm.domain.model.IntimacyTier.CLOSE.colorValue),
    family = Color(com.tang.prm.domain.model.IntimacyTier.FAMILY.colorValue)
)

/** 深色模式亲密度色板：派生自 [com.tang.prm.domain.model.IntimacyTier.darkColorValue] */
val DarkIntimacyColors = IntimacyColors(
    new = Color(com.tang.prm.domain.model.IntimacyTier.NEW.darkColorValue),
    acquaintance = Color(com.tang.prm.domain.model.IntimacyTier.ACQUAINTANCE.darkColorValue),
    friend = Color(com.tang.prm.domain.model.IntimacyTier.FRIEND.darkColorValue),
    close = Color(com.tang.prm.domain.model.IntimacyTier.CLOSE.darkColorValue),
    family = Color(com.tang.prm.domain.model.IntimacyTier.FAMILY.darkColorValue)
)

val LocalIntimacyColors = staticCompositionLocalOf { LightIntimacyColors }

// ── 磁带/卡带 UI 专用色（深色模式自适应） ────────────────────────
val TapeWindow: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) Color(0xFF30363D) else Color(0xFFD1D5DB)
val TapeGearColor: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) Color(0xFF484F58) else Color(0xFF9CA3AF)
val TapeGearDarkColor: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) Color(0xFF6E7681) else Color(0xFF6B7280)
val TapePlastic: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) Color(0xFF161B22) else Color(0xFFE8ECF1)

// ── 功能图标色（明暗通用） ────────────────────────────────────────
val InsightPink = Color(0xFFEC4899)
val DeleteRed = Color(0xFFFF6B6B)
val SceneOrange = Color(0xFFF97316)

fun String?.toComposeColor(fallback: Color): Color {
    if (this == null) return fallback
    return try { Color(android.graphics.Color.parseColor(this)) } catch (e: Exception) { fallback }
}
