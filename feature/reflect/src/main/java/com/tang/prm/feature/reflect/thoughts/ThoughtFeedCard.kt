package com.tang.prm.feature.reflect.thoughts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.model.Thought
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.components.AppCard
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalCoral
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.domain.util.DateUtils

@Composable
internal fun ThoughtFeedCard(
    thought: Thought,
    contactName: String?,
    contactAvatar: String?,
    isFavorite: Boolean,
    exp: Int,
    onToggleTodo: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCardClick: () -> Unit = {}
) {
    val style = thought.style
    val typeColor = style.color
    val typeBg = style.bg
    val typeIcon = style.icon
    val typeLabel = style.label

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = typeBg,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            typeIcon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = typeColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        typeLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 1.dp)
                    ) {
                        Text(
                            DateUtils.formatRelativeTime(thought.createdAt),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (thought.isPrivate) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(3.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.outline)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "私密",
                                modifier = Modifier.size(10.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half)
                            )
                        }
                        if (thought.isTodo) {
                            thought.dueDate?.let { dueDate ->
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(3.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.outline)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "截止 ${DateUtils.formatMonthDayChinese(dueDate)}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = SignalGreen
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "编辑",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        modifier = Modifier.size(16.dp),
                        tint = SignalCoral.copy(alpha = AnimationTokens.Alpha.half)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Q-5 修复：用 TextLayoutResult 检测实际截断，替代基于字符数的误判
            var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
            Text(
                thought.content,
                style = MaterialTheme.typography.bodyMedium,
                color = if (thought.isTodo && thought.isDone) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half)
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                lineHeight = 24.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { layoutResult = it }
            )

            if (layoutResult?.hasVisualOverflow == true) {
                Text(
                    "查看详情",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = SignalAmber,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (contactName != null) {
                    ContactNameChip(
                        name = contactName,
                        avatar = contactAvatar,
                        avatarSize = 18
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "取消收藏" else "收藏",
                        tint = if (isFavorite) SignalCoral else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                if (thought.isTodo) {
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onToggleTodo,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (thought.isDone) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                            contentDescription = if (thought.isDone) "已完成" else "待办",
                            tint = if (thought.isDone) SignalGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SignalAmber.copy(alpha = AnimationTokens.Alpha.faint)
                ) {
                    Text(
                        "+$exp EXP",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SignalAmber,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
