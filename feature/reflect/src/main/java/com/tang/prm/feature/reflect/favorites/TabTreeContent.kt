package com.tang.prm.feature.reflect.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.model.Favorite
import com.tang.prm.domain.util.DateUtils

@Composable
internal fun TabTreeContent(favorites: List<Favorite>) {
    TermPathBar(segments = listOf("~", "favorites", "stats"))

    TermCommentLine("# 磁盘使用 & 目录结构")

    TermPromptLine("du -h favorites/")

    TermThickSeparator()

    val typeCounts = remember(favorites) {
        FavoriteType.entries.associateWith { type ->
            favorites.count { it.sourceType == type.code }
        }
    }
    val maxCount = typeCounts.values.maxOrNull() ?: 1

    typeCounts.forEach { (type, count) ->
        val filled = if (maxCount > 0) (count.toFloat() / maxCount * 20).toInt() else 0
        val bar = "█".repeat(filled) + "░".repeat(20 - filled)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 1.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                type.shortCode,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = TermMuted,
                modifier = Modifier.width(32.dp)
            )
            Text(
                bar,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                color = TermHighlight,
                letterSpacing = (-1).sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "$count",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = TermComment
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .height(1.dp)
            .background(TermDim)
    )

    val newestDate = remember(favorites) {
        favorites.maxByOrNull { it.createdAt }?.let {
            DateUtils.formatShortDate(it.createdAt)
        } ?: "--"
    }
    val activeType = remember(typeCounts) {
        typeCounts.maxByOrNull { it.value }?.key?.shortCode ?: "--"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TermStatItem("total=", "${favorites.size}")
        TermStatItem("types=", "${typeCounts.count { it.value > 0 }}")
        TermStatItem("newest=", newestDate)
        TermStatItem("active=", activeType)
    }

    TermThickSeparator()

    TermPromptLine("tree favorites/")

    TermSeparator()

    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        Text("favorites/", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TermText)

        val typeEntries = typeCounts.entries.toList()
        typeEntries.forEachIndexed { index, (type, count) ->
            val isLast = index == typeEntries.size - 1
            val connector = if (isLast) "└──" else "├──"
            val prefix = if (isLast) "    " else "│   "

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(connector, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TermComment)
                Spacer(modifier = Modifier.width(4.dp))
                Text("${type.label}/", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TermText)
                Spacer(modifier = Modifier.width(4.dp))
                Text("($count)", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TermComment)
            }

            val typeFavorites = favorites.filter { it.sourceType == type.code }
            val showItems = typeFavorites.take(2)
            showItems.forEachIndexed { itemIndex, fav ->
                val isLastItem = itemIndex == showItems.size - 1 && count <= 2
                val itemConnector = if (isLastItem) "└──" else "├──"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("$prefix$itemConnector", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TermComment)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(fav.title, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TermHighlight, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            if (count > 2) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${prefix}└──", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TermComment)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("... +${count - 2}", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TermComment)
                }
            }
        }
    }
}
