package com.tang.prm.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.model.Favorite
import com.tang.prm.util.DateUtils

@Composable
internal fun TermDetailPanel(
    favorite: Favorite?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    if (favorite == null) {
        Box(
            modifier = modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 24.dp)
            ) {
                Text(
                    "▸",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 20.sp,
                    color = TermComment.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "选择一项收藏查看详情",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = TermComment
                )
            }
        }
        return
    }

    val type = FavoriteType.fromCode(favorite.sourceType)
    val relativeTime = getRelativeTime(favorite.createdAt)
    val fullDate = DateUtils.formatDateTimeHyphen(favorite.createdAt)

    val panelInteractionSource = remember { MutableInteractionSource() }
    val panelPressed by panelInteractionSource.collectIsPressedAsState()
    val panelScale by animateFloatAsState(
        targetValue = if (panelPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "panelScale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .scale(panelScale)
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .border(BorderStroke(1.dp, TermDetailBorder), RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = panelInteractionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "▸ ",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TermText
            )
            Text(
                favorite.title,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TermText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "#${favorite.sourceId}",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                color = TermDim
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .background(TermTagBg, RoundedCornerShape(2.dp))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        type.label,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text(type.source, fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TermMuted)
            }
            Text(relativeTime, fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TermMuted)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("date:", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TermComment)
            Text(fullDate, fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TermText)
        }

        favorite.description?.let { desc ->
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(TermTableBorder.copy(alpha = 0.3f))
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("↳ ", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TermComment)
                Text(
                    desc,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = TermHighlight,
                    lineHeight = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(TermTableBorder.copy(alpha = 0.3f))
        )
        Spacer(modifier = Modifier.height(3.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "→ 查看原始${type.label}",
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                color = TermMuted
            )
            Text(
                "${type.code} · ${type.size}",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                color = TermComment
            )
        }
    }
}
