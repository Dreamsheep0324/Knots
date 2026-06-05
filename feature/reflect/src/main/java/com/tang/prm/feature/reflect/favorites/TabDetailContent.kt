package com.tang.prm.feature.reflect.favorites

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
import com.tang.prm.domain.util.DateUtils

@Composable
internal fun TabDetailContent(
    favorites: List<Favorite>,
    viewModel: FavoritesViewModel,
    uiState: FavoritesUiState,
    onFavoriteClick: (Favorite) -> Unit
) {
    TermPathBar(segments = listOf("~", "favorites", "all"))

    TermFilterRow(
        filters = viewModel.filterLabels,
        selectedIndex = uiState.selectedFilter,
        onFilterClick = { viewModel.setFilter(it) }
    )

    TermCommentLine("# 总计 ${favorites.size} 项收藏 · 逐项展开")

    TermPromptLine("cat favorites/*")

    TermThickSeparator()

    favorites.forEachIndexed { index, favorite ->
        val type = FavoriteType.fromCode(favorite.sourceType)
        val relativeTime = getRelativeTime(favorite.createdAt)
        val shortDate = DateUtils.formatMonthDayDotTime(favorite.createdAt)

        val cardInteractionSource = remember { MutableInteractionSource() }
        val cardPressed by cardInteractionSource.collectIsPressedAsState()
        val cardScale by animateFloatAsState(
            targetValue = if (cardPressed) 0.97f else 1f,
            animationSpec = tween(100),
            label = "cardScale"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .scale(cardScale)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .border(BorderStroke(0.5.dp, TermDetailBorder), RoundedCornerShape(12.dp))
                .clickable(
                    interactionSource = cardInteractionSource,
                    indication = null,
                    onClick = { onFavoriteClick(favorite) }
                )
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(28.dp)
                        .background(type.borderColor, RoundedCornerShape(1.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
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
                Box(
                    modifier = Modifier
                        .background(TermTagBg, RoundedCornerShape(2.dp))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        type.label,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(shortDate, fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TermMuted)
                Text(type.source, fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TermMuted)
                Text(relativeTime, fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TermMuted)
            }

            favorite.description?.let { desc ->
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 11.dp)
                ) {
                    Text(
                        "↳ ",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = TermComment
                    )
                    Text(
                        desc,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = TermComment,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (index < favorites.size - 1) {
            TermSeparator()
        }
    }
}
