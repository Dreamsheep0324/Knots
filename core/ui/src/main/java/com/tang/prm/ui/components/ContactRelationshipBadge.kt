package com.tang.prm.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun ContactRelationshipBadge(
    relationship: String?,
    bracketed: Boolean = false,
    color: Color,
    fontSize: TextUnit = if (bracketed) 11.sp else 9.sp,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    if (relationship != null) {
        Text(
            text = if (bracketed) "[$relationship]" else relationship,
            fontFamily = FontFamily.Monospace,
            fontSize = fontSize,
            color = color,
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = overflow
        )
    }
}
