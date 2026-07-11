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
val EventLightTeal: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) Color(0xFF1A3A3A) else Color(0xFFCCFBF1)
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

// ── 品牌信号色（明暗通用，无需深色适配） ─────────────────────────
val SignalCoral = Color(0xFFEF4444)
val SignalSky = Color(0xFF3B82F6)
val SignalAmber = Color(0xFFF59E0B)
val SignalGreen = Color(0xFF10B981)
val SignalPurple = Color(0xFF6366F1)
val SignalGold = Color(0xFFFBBF24)
val SignalElectric = Color(0xFF2196F3)
val EventMoneyTeal = Color(0xFF14B8A6)
val FavoriteGold = Color(0xFFFFB300)
val AnniversaryBirthday = Color(0xFFF97316)
val AnniversaryDate = Color(0xFFE91E63)
val AnniversaryHoliday = Color(0xFF0EA5E9)
val OutlineVariantLight = Color(0xFFCBD5E1)

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
// 顶层 val 为浅色模式色值；深色模式请使用 LocalIntimacyColors.current
/** 亲密度等级颜色（与 IntimacyTier.colorValue 保持一致） */
val IntimacyNew = Color(0xFF94A3B8)         // N 灰
val IntimacyAcquaintance = Color(0xFF3B82F6) // R 蓝
val IntimacyFriend = Color(0xFF8B5CF6)       // SR 紫
val IntimacyClose = Color(0xFFEF4444)        // SSR 红
val IntimacyFamily = Color(0xFFF59E0B)       // UR 金

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

val LightIntimacyColors = IntimacyColors(
    new = Color(0xFF94A3B8),
    acquaintance = Color(0xFF3B82F6),
    friend = Color(0xFF8B5CF6),
    close = Color(0xFFEF4444),
    family = Color(0xFFF59E0B)
)

val DarkIntimacyColors = IntimacyColors(
    new = Color(0xFFB0BEC5),
    acquaintance = Color(0xFF60A5FA),
    friend = Color(0xFFA78BFA),
    close = Color(0xFFF87171),
    family = Color(0xFFFBBF24)
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
