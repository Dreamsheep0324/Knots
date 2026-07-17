package com.tang.prm.feature.reflect.favorites

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.model.Favorite
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.animation.primitives.rememberBreathingPulse
import com.tang.prm.domain.util.DateUtils

@Composable
internal fun TabLogContent(favorites: List<Favorite>) {
    TermPathBar(segments = listOf("~", "var", "log", "favorites.log"))

    TermCommentLine("# 实时收藏操作日志")

    TermPromptLine("tail -f /var/log/favorites.log")

    TermThickSeparator()

    val pulseAlpha by rememberBreathingPulse(minAlpha = 0.3f, maxAlpha = 1f, cycleDuration = 3000)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(SignalGreen.copy(alpha = pulseAlpha))
        )
        // Q-3 修复：移除硬编码的假 "DEL: 0"，只显示真实的 ADD 计数
        Text("ADD: ", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TermText)
        Text("${favorites.size}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TermText)
    }

    TermSeparator()

    val groupedByDate = remember(favorites) {
        favorites
            .sortedByDescending { it.createdAt }
            .groupBy { DateUtils.formatDate(it.createdAt) }
            .mapValues { entry ->
                // Q-3 修复：简化 Triple→Pair，移除始终为 "ADD" 的死字段
                entry.value.map { fav ->
                    Pair(DateUtils.formatTime(fav.createdAt), fav)
                }
            }
    }

    groupedByDate.forEach { (date, entries) ->
        Text(
            "── $date ──",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = TermComment,
            letterSpacing = 1.sp
        )

        entries.forEach { (time, fav) ->
            val type = FavoriteType.fromCode(fav.sourceType)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        time,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = TermComment,
                        modifier = Modifier.width(32.dp)
                    )
                    // Q-3 修复：op 始终为 "ADD"，直接写死，移除死分支
                    Text(
                        "ADD",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SignalGreen
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .border(BorderStroke(1.dp, TermMuted), RoundedCornerShape(1.dp))
                            .padding(horizontal = 3.dp, vertical = 0.dp)
                    ) {
                        Text(
                            type.shortCode,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp,
                            color = TermMuted,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        fav.title,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = TermHighlight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "#${fav.sourceId}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        color = TermDim
                    )
                }
                Row(modifier = Modifier.padding(start = 32.dp)) {
                    Text(
                        "↳ 来自「${type.source}」",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = TermComment
                    )
                    fav.description?.let { desc ->
                        Text(
                            " · $desc",
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
        }
    }

    Text(
        "-- 共 ${favorites.size} 条记录 · ${groupedByDate.size} 个日期 --",
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        color = TermComment
    )
}
