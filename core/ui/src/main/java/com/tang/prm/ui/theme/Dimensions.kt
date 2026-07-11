package com.tang.prm.ui.theme

import androidx.compose.ui.unit.dp

/**
 * 应用统一尺寸常量。
 *
 * 命名约定：
 * - `padding*` / `spacing*`：内边距与间距
 * - `corner*`：圆角半径（与 [TangShapes] 对应）
 * - `icon*` / `avatar*`：图标与头像尺寸
 * - `elevation*`：阴影高度
 * - `*Weight`：布局权重
 *
 * 散落在各 Composable 中的魔法 dp/sp 值应优先使用本对象中的常量。
 */
object Dimens {
    // ── 内边距 ──────────────────────────────────────────────
    val paddingPage = 16.dp
    val paddingCard = 16.dp
    val paddingSmall = 8.dp
    val paddingXs = 4.dp
    val paddingLg = 20.dp
    val paddingXl = 24.dp

    // ── 间距 ────────────────────────────────────────────────
    val spacingXs = 4.dp
    val spacingSm = 8.dp
    val spacingItem = 10.dp
    val spacingMd = 12.dp
    val spacingLg = 16.dp
    val spacingXl = 20.dp
    val spacingXxl = 24.dp

    // ── 圆角半径（与 TangShapes 对应） ─────────────────────
    val cornerXs = 4.dp
    val cornerSmall = 8.dp
    val cornerMedium = 12.dp
    val cornerLarge = 16.dp
    val cornerCard = 16.dp
    val cornerXl = 20.dp
    val cornerXxl = 24.dp
    val cornerNav = 28.dp

    // ── 图标尺寸 ───────────────────────────────────────────
    val iconXs = 12.dp
    val iconSmall = 16.dp
    val iconMedium = 24.dp
    val iconLarge = 32.dp
    val iconXl = 36.dp

    // ── 头像尺寸 ───────────────────────────────────────────
    val avatarSmall = 32.dp
    val avatarMedium = 48.dp
    val avatarLarge = 64.dp

    // ── 阴影 ───────────────────────────────────────────────
    val elevationCard = 3.dp

    // ── 平板布局 ───────────────────────────────────────────
    val railWidth = 205.dp

    val listPaneWeight = 0.4f
    val homeLeftWeight = 0.38f
}
