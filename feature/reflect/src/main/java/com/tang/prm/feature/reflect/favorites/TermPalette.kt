package com.tang.prm.feature.reflect.favorites

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 终端风格调色板。
 *
 * 设计目的：将 favorites 子模块的终端风格颜色集中管理，统一响应主题切换，
 * 替代原先散落在 FavoritesConstants 中的 18 个 isSystemInDarkTheme() 二选一属性。
 *
 * 不下沉到 core:ui：终端风格只服务 favorites 单一子模块，上沉会违反"core 层应通用"原则。
 */
internal data class TermColors(
    val border: Color,
    val text: Color,
    val muted: Color,
    val comment: Color,
    val highlight: Color,
    val dim: Color,
    val tableBorder: Color,
    val rowHover: Color,
    val detailBorder: Color,
    val activeTab: Color,
    val inactiveTab: Color,
    val tagBg: Color,
    val selectedTagBg: Color,
    val favoriteTypeBorder: Map<FavoriteType, Color>
)

internal val LocalTermColors = staticCompositionLocalOf<TermColors> {
    error("TermColors not provided. Wrap content with CompositionLocalProvider(LocalTermColors provides rememberTermColors()).")
}

internal val LightTermColors = TermColors(
    border = Color(0xFFE2E8F0),
    text = Color(0xFF1E293B),
    muted = Color(0xFF64748B),
    comment = Color(0xFF94A3B8),
    highlight = Color(0xFF334155),
    dim = Color(0xFFCBD5E1),
    tableBorder = Color(0xFFE2E8F0),
    rowHover = Color(0xFFF1F5F9),
    detailBorder = Color(0xFFE2E8F0),
    activeTab = Color(0xFF1E293B),
    inactiveTab = Color(0xFF64748B),
    tagBg = Color(0xFF1E293B),
    selectedTagBg = Color(0xFF1E293B),
    favoriteTypeBorder = mapOf(
        FavoriteType.EVENT to Color(0xFF1E293B),
        FavoriteType.DIALOG to Color(0xFF475569),
        FavoriteType.THOUGHT to Color(0xFF94A3B8),
        FavoriteType.GIFT to Color(0xFF334155),
        FavoriteType.PHOTO to Color(0xFFCBD5E1)
    )
)

internal val DarkTermColors = TermColors(
    border = Color(0xFF30363D),
    text = Color(0xFFE6EDF3),
    muted = Color(0xFF8B949E),
    comment = Color(0xFF6E7681),
    highlight = Color(0xFFC9D1D9),
    dim = Color(0xFF484F58),
    tableBorder = Color(0xFF21262D),
    rowHover = Color(0xFF1C2129),
    detailBorder = Color(0xFF30363D),
    activeTab = Color(0xFFE6EDF3),
    inactiveTab = Color(0xFF6E7681),
    tagBg = Color(0xFF30363D),
    selectedTagBg = Color(0xFF484F58),
    favoriteTypeBorder = mapOf(
        FavoriteType.EVENT to Color(0xFF58A6FF),
        FavoriteType.DIALOG to Color(0xFF79C0FF),
        FavoriteType.THOUGHT to Color(0xFFBC8CFF),
        FavoriteType.GIFT to Color(0xFFD29922),
        FavoriteType.PHOTO to Color(0xFF3FB950)
    )
)

@Composable
internal fun rememberTermColors(): TermColors {
    val dark = isSystemInDarkTheme()
    return remember(dark) {
        if (dark) DarkTermColors else LightTermColors
    }
}

// 顶层属性：委托到 LocalTermColors，调用方无感知地通过 CompositionLocal 读取主题色
// 保持原 FavoritesConstants 顶层 val 名称不变，最小化对调用方的破坏

internal val TermBorder: Color
    @Composable @ReadOnlyComposable get() = LocalTermColors.current.border
internal val TermText: Color
    @Composable @ReadOnlyComposable get() = LocalTermColors.current.text
internal val TermMuted: Color
    @Composable @ReadOnlyComposable get() = LocalTermColors.current.muted
internal val TermComment: Color
    @Composable @ReadOnlyComposable get() = LocalTermColors.current.comment
internal val TermHighlight: Color
    @Composable @ReadOnlyComposable get() = LocalTermColors.current.highlight
internal val TermDim: Color
    @Composable @ReadOnlyComposable get() = LocalTermColors.current.dim
internal val TermTableBorder: Color
    @Composable @ReadOnlyComposable get() = LocalTermColors.current.tableBorder
internal val TermRowHover: Color
    @Composable @ReadOnlyComposable get() = LocalTermColors.current.rowHover
internal val TermDetailBorder: Color
    @Composable @ReadOnlyComposable get() = LocalTermColors.current.detailBorder
internal val TermActiveTab: Color
    @Composable @ReadOnlyComposable get() = LocalTermColors.current.activeTab
internal val TermInactiveTab: Color
    @Composable @ReadOnlyComposable get() = LocalTermColors.current.inactiveTab
internal val TermTagBg: Color
    @Composable @ReadOnlyComposable get() = LocalTermColors.current.tagBg
internal val TermSelectedTagBg: Color
    @Composable @ReadOnlyComposable get() = LocalTermColors.current.selectedTagBg

internal val FavoriteTypeBorderColorEvent: Color
    @Composable @ReadOnlyComposable get() = LocalTermColors.current.favoriteTypeBorder.getValue(FavoriteType.EVENT)
internal val FavoriteTypeBorderColorDialog: Color
    @Composable @ReadOnlyComposable get() = LocalTermColors.current.favoriteTypeBorder.getValue(FavoriteType.DIALOG)
internal val FavoriteTypeBorderColorThought: Color
    @Composable @ReadOnlyComposable get() = LocalTermColors.current.favoriteTypeBorder.getValue(FavoriteType.THOUGHT)
internal val FavoriteTypeBorderColorGift: Color
    @Composable @ReadOnlyComposable get() = LocalTermColors.current.favoriteTypeBorder.getValue(FavoriteType.GIFT)
internal val FavoriteTypeBorderColorPhoto: Color
    @Composable @ReadOnlyComposable get() = LocalTermColors.current.favoriteTypeBorder.getValue(FavoriteType.PHOTO)
