package com.tang.prm.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * 应用统一形状定义。
 *
 * 与 [Dimens.cornerSmall]/[cornerMedium]/[cornerLarge]/[cornerXl]/[cornerXxl] 对应，
 * 但 [Shapes] 作用于 [androidx.compose.material3.MaterialTheme] 的默认形状，
 * 适合 Surface/Card/Dialog 等组件隐式使用。
 */
val TangShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)
