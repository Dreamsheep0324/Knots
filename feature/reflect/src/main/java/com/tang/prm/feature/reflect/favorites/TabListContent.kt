package com.tang.prm.feature.reflect.favorites

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.model.Favorite

@Composable
internal fun ColumnScope.TabListContent(
    favorites: List<Favorite>,
    viewModel: FavoritesViewModel,
    uiState: FavoritesUiState,
    dateFormat: (Long) -> String,
    selectedFavorite: Favorite?,
    onFavoriteClick: (Favorite) -> Unit,
    onFavoriteNavigate: (Favorite) -> Unit
) {
    TermPathBar(segments = listOf("~", "favorites", "all"))

    TermCommentLine("# 总计 ${favorites.size} 项收藏 [查询耗时 0.02s]")

    TermPromptLine("ls -la")

    TermSeparator()

    TermTableHeader()

    val maxDisplayCount = 7
    val displayFavorites = favorites.take(maxDisplayCount)
    val hasMore = favorites.size > maxDisplayCount

    displayFavorites.forEach { favorite ->
        TermTableRow(
            favorite = favorite,
            dateFormat = dateFormat,
            isSelected = favorite == selectedFavorite,
            onClick = { onFavoriteClick(favorite) }
        )
    }

    if (hasMore) {
        TermCommentLine("  ... 还有 ${favorites.size - maxDisplayCount} 项未显示")
    }

    TermSeparator()

    if (favorites.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "∅",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 28.sp,
                    color = TermComment
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "暂无收藏记录",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = TermComment
                )
            }
        }
    } else {
        TermDetailPanel(
            favorite = selectedFavorite,
            onClick = {
                selectedFavorite?.let { onFavoriteNavigate(it) }
            }
        )
    }
}
