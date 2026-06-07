package com.tang.prm.feature.people.contacts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tang.prm.domain.model.Thought
import com.tang.prm.domain.model.ThoughtType
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.DialogDefaults
import com.tang.prm.ui.theme.Primary
import com.tang.prm.ui.theme.SceneOrange
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalCoral
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.SignalSky
import com.tang.prm.domain.util.DateUtils

internal val TabIcons = listOf(
    Icons.Default.Person, Icons.Default.Event, Icons.Default.Cake,
    Icons.Default.CardGiftcard, Icons.Default.Lightbulb, Icons.AutoMirrored.Filled.Chat
)

internal val TabColors = listOf(
    Color(0xFF42A5F5), Color(0xFF66BB6A), Color(0xFFF43F5E),
    SceneOrange, Color(0xFFEAB308), Color(0xFF9575CD)
)

internal val ThoughtTypeColor = mapOf(
    ThoughtType.FRIEND to SignalAmber,
    ThoughtType.PLAN to SignalSky,
    ThoughtType.MURMUR to SignalPurple
)

internal val ThoughtTypeBg = mapOf(
    ThoughtType.FRIEND to SignalAmber.copy(alpha = AnimationTokens.Alpha.faint),
    ThoughtType.PLAN to SignalSky.copy(alpha = AnimationTokens.Alpha.faint),
    ThoughtType.MURMUR to SignalPurple.copy(alpha = AnimationTokens.Alpha.faint)
)

internal val ThoughtTypeIcon = mapOf(
    ThoughtType.FRIEND to Icons.Default.Group,
    ThoughtType.PLAN to Icons.Default.TaskAlt,
    ThoughtType.MURMUR to Icons.Default.Lightbulb
)

internal val ThoughtTypeLabel = mapOf(
    ThoughtType.FRIEND to "伙伴",
    ThoughtType.PLAN to "计划",
    ThoughtType.MURMUR to "碎碎念"
)

@Composable
internal fun ContactThoughtDetailDialog(
    thought: Thought,
    contactName: String?,
    contactAvatar: String?,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleTodo: () -> Unit,
    onDelete: () -> Unit
) {
    val typeColor = ThoughtTypeColor[thought.type] ?: SignalAmber
    val typeBg = ThoughtTypeBg[thought.type] ?: SignalAmber.copy(alpha = AnimationTokens.Alpha.faint)
    val typeIcon = ThoughtTypeIcon[thought.type] ?: Icons.Default.Lightbulb
    val typeLabel = ThoughtTypeLabel[thought.type] ?: "想法"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DialogDefaults.containerColor,
            tonalElevation = 0.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = typeBg,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                typeIcon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = typeColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            typeLabel,
                            fontSize = 14.sp,
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
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "私密",
                                    modifier = Modifier.size(10.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half)
                                )
                            }
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "关闭",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        thought.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (thought.isTodo && thought.isDone) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        lineHeight = 22.sp
                    )

                    if (thought.isTodo) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (thought.isDone) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (thought.isDone) SignalGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (thought.isDone) "已完成" else "待办中",
                                fontSize = 12.sp,
                                color = if (thought.isDone) SignalGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (thought.dueDate != null) {
                                val dueDate = thought.dueDate!!
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "· 截止 ${DateUtils.formatMonthDayChinese(dueDate)}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = SignalGreen
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 12.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(36.dp)) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "取消收藏" else "收藏",
                            tint = if (isFavorite) SignalCoral else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (thought.isTodo) {
                        IconButton(onClick = onToggleTodo, modifier = Modifier.size(36.dp)) {
                            Icon(
                                if (thought.isDone) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                contentDescription = if (thought.isDone) "已完成" else "待办",
                                tint = if (thought.isDone) SignalGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (contactName != null) {
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half))
                        ) {
                            Row(
                                modifier = Modifier.padding(start = 6.dp, end = 10.dp, top = 5.dp, bottom = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                com.tang.prm.ui.components.ContactAvatar(avatar = contactAvatar, name = contactName, size = 20)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    contactName,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp), tint = SignalCoral)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("删除", color = SignalCoral, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
internal fun TabSection(selectedTab: Int, onTabSelected: (Int) -> Unit, tabs: List<String>) {
    val scrollState = rememberLazyListState()
    LaunchedEffect(selectedTab) {
        scrollState.animateScrollToItem((selectedTab - 1).coerceAtLeast(0))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.paddingPage)
    ) {
        LazyRow(
            state = scrollState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(tabs, key = { _, title -> title }) { index, title ->
                val isSelected = selectedTab == index
                val tabIcon = TabIcons.getOrNull(index)
                val selectedColor = TabColors.getOrElse(index) { Primary }

                Surface(
                    onClick = { onTabSelected(index) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) selectedColor.copy(alpha = 0.1f) else Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        if (tabIcon != null) {
                            Icon(
                                tabIcon,
                                contentDescription = null,
                                tint = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
