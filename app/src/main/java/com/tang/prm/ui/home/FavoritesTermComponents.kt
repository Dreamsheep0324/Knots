package com.tang.prm.ui.home

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.model.Favorite
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.util.DateUtils
import com.tang.prm.ui.animation.primitives.rememberBreathingPulse
import com.tang.prm.ui.theme.Dimens

@Composable
internal fun TermStatusBar(totalItems: Int, isLive: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(BorderStroke(1.dp, TermBorder))
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (isLive) {
            val pulseAlpha by rememberBreathingPulse(minAlpha = 0.3f, maxAlpha = 1f, cycleDuration = 3000)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(SignalGreen.copy(alpha = pulseAlpha))
                )
                Text(
                    "LIVE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SignalGreen,
                    letterSpacing = 1.sp
                )
            }
        } else {
            Text(
                "UTF-8 · LF",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = TermMuted
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "${FavoriteType.entries.size} types",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = TermMuted
            )
            Text(
                "$totalItems items",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = TermMuted
            )
        }
    }
}

@Composable
internal fun TermPathBar(segments: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(BorderStroke(0.5.dp, TermBorder))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        segments.forEachIndexed { index, seg ->
            if (index > 0) {
                Text("/", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = TermDim)
            }
            Text(
                seg,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = if (index == segments.lastIndex) FontWeight.Bold else FontWeight.Normal,
                color = if (index == 0) TermMuted else TermText
            )
        }
    }
}

@Composable
internal fun TermFilterRow(
    filters: List<String>,
    selectedIndex: Int,
    onFilterClick: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(filters.size, key = { filters[it] }) { index ->
            val isSelected = index == selectedIndex
            val filterInteractionSource = remember { MutableInteractionSource() }
            val filterPressed by filterInteractionSource.collectIsPressedAsState()
            val filterScale by animateFloatAsState(
                targetValue = if (filterPressed) 0.95f else 1f,
                animationSpec = tween(100),
                label = "filterScale"
            )
            Box(
                modifier = Modifier
                    .scale(filterScale)
                    .background(
                        if (isSelected) TermTagBg else MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(8.dp)
                    )
                    .border(
                        BorderStroke(0.5.dp, if (isSelected) TermTagBg else TermBorder),
                        RoundedCornerShape(8.dp)
                    )
                    .clickable(
                        interactionSource = filterInteractionSource,
                        indication = null,
                        onClick = { onFilterClick(index) }
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    filters[index],
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else TermMuted
                )
            }
        }
    }
}

@Composable
internal fun TermCommentLine(text: String) {
    Text(
        text,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        color = TermComment
    )
}

@Composable
internal fun TermPromptLine(cmd: String) {
    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)) {
        Text("$ ", fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TermText)
        Text(cmd, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = TermText)
    }
}

@Composable
internal fun TermSeparator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .height(1.dp)
            .background(TermBorder)
    )
}

@Composable
internal fun TermThickSeparator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .height(2.dp)
            .background(TermBorder)
    )
}

@Composable
internal fun TermTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("类型", fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TermMuted)
        Spacer(modifier = Modifier.width(8.dp))
        Text("名称", modifier = Modifier.weight(1f), fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TermMuted)
        Text("日期", modifier = Modifier.width(44.dp), fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TermMuted)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.paddingPage)
            .height(0.5.dp)
            .background(TermTableBorder.copy(alpha = 0.6f))
    )
}

@Composable
internal fun TermTableRow(
    favorite: Favorite,
    dateFormat: (Long) -> String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val type = FavoriteType.fromCode(favorite.sourceType)
    val rowInteractionSource = remember { MutableInteractionSource() }
    val rowPressed by rowInteractionSource.collectIsPressedAsState()
    val rowScale by animateFloatAsState(
        targetValue = if (rowPressed) 0.98f else 1f,
        animationSpec = tween(80),
        label = "rowScale"
    )
    val rowBg by animateColorAsState(
        targetValue = when {
            isSelected -> TermRowHover
            rowPressed -> TermRowHover.copy(alpha = 0.7f)
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(200),
        label = "rowBg"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .scale(rowScale)
            .background(rowBg, RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = rowInteractionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .background(if (isSelected) TermSelectedTagBg else TermTagBg, RoundedCornerShape(2.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                type.label,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            favorite.title,
            modifier = Modifier.weight(1f),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) TermActiveTab else TermText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            dateFormat(favorite.createdAt),
            modifier = Modifier.width(44.dp),
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = if (isSelected) TermText else TermMuted
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.paddingPage)
            .height(0.5.dp)
            .background(TermTableBorder.copy(alpha = 0.4f))
    )
}

@Composable
internal fun TermKvRow(key: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(key, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TermComment)
        Text(value, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TermText, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
internal fun TermStatItem(key: String, value: String) {
    Row {
        Text(key, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TermComment)
        Text(value, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TermHighlight)
    }
}
