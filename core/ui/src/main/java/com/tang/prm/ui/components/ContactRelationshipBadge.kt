package com.tang.prm.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * 联系人关系徽章的文本样式参数集合。
 *
 * 使用 [ContactRelationshipBadge] 时，若传入 [Default] 则字号由 `bracketed` 决定
 * （保留历史行为）；显式构造实例时以实例字段为准。
 *
 * @param fontSize 字号
 * @param textAlign 对齐方式
 * @param maxLines 最大行数
 * @param overflow 溢出处理
 */
@Immutable
data class BadgeTextStyle(
    val fontSize: TextUnit = 9.sp,
    val textAlign: TextAlign? = null,
    val maxLines: Int = Int.MAX_VALUE,
    val overflow: TextOverflow = TextOverflow.Clip
) {
    companion object {
        /** 哨兵默认值：调用方未显式指定样式时使用，字号由 `bracketed` 动态决定。 */
        val Default = BadgeTextStyle()
    }
}

@Composable
fun ContactRelationshipBadge(
    relationship: String?,
    bracketed: Boolean = false,
    color: Color,
    style: BadgeTextStyle = BadgeTextStyle.Default
) {
    if (relationship != null) {
        // 保留原 bracketed 相关默认字号：仅在调用方使用 Default 哨兵时生效
        val fontSize = if (style === BadgeTextStyle.Default && bracketed) 11.sp else style.fontSize
        Text(
            text = if (bracketed) "[$relationship]" else relationship,
            fontFamily = FontFamily.Monospace,
            fontSize = fontSize,
            color = color,
            textAlign = style.textAlign,
            maxLines = style.maxLines,
            overflow = style.overflow
        )
    }
}
