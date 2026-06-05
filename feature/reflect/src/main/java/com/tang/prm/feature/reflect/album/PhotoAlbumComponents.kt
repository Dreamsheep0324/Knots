package com.tang.prm.feature.reflect.album

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tang.prm.domain.model.AlbumPhoto
import com.tang.prm.domain.model.Contact
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.components.ContactAvatar
import com.tang.prm.ui.theme.*
import com.tang.prm.domain.util.DateUtils
import com.tang.prm.ui.theme.Dimens

internal fun groupPhotosByDate(photos: List<AlbumPhoto>): List<PhotoGroup> {
    val grouped = photos.groupBy { DateUtils.formatYearMonthDayChineseFull(it.date) }
    return grouped.map { (dateLabel, datePhotos) ->
        val firstPhoto = datePhotos.first()
        PhotoGroup(
            groupType = "daily",
            groupKey = dateLabel,
            groupTitle = dateLabel,
            subtitle = "${datePhotos.size}张照片",
            contactName = firstPhoto.contactName,
            contactAvatar = firstPhoto.contactAvatar,
            date = firstPhoto.date,
            location = firstPhoto.location,
            photos = datePhotos
        )
    }.sortedByDescending { it.date }
}

internal fun groupPhotosBySource(photos: List<AlbumPhoto>): List<PhotoGroup> {
    val grouped = photos.groupBy { "${it.sourceType}_${it.sourceId}" }
    return grouped.map { (key, groupPhotos) ->
        val firstPhoto = groupPhotos.first()
        val subtitle = when (firstPhoto.sourceType) {
            "event" -> "事件"
            "chat" -> "对话"
            "gift" -> "礼物"
            else -> "其他"
        }
        PhotoGroup(
            groupType = "event",
            groupKey = key,
            groupTitle = firstPhoto.sourceTitle,
            subtitle = subtitle,
            contactName = firstPhoto.contactName,
            contactAvatar = firstPhoto.contactAvatar,
            date = firstPhoto.date,
            location = firstPhoto.location,
            photos = groupPhotos
        )
    }.sortedByDescending { it.date }
}

@Composable
internal fun FilterTabsSection(
    viewMode: String,
    onViewModeChange: (String) -> Unit
) {
    val tabs = listOf(
        "daily" to "每日",
        "event" to "事件",
        "grid" to "网格"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 8.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp)),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEach { (key, label) ->
            val selected = viewMode == key
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                color = if (selected) SignalPurple else Color.Transparent,
                onClick = { onViewModeChange(key) }
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (selected) Color.White else TextGray,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
internal fun PhotoStatsSection(
    photoCount: Int,
    contactCount: Int,
    locationCount: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 16.dp, 16.dp, 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(Dimens.paddingCard)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "照片统计",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "共 $photoCount 张",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PhotoStatCardItem(
                    modifier = Modifier.weight(1f),
                    title = "照片",
                    value = photoCount.toString(),
                    icon = Icons.Default.Image,
                    color = SignalPurple
                )
                PhotoStatCardItem(
                    modifier = Modifier.weight(1f),
                    title = "人物",
                    value = contactCount.toString(),
                    icon = Icons.Default.People,
                    color = SignalGreen
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            PhotoStatCardItem(
                modifier = Modifier.fillMaxWidth(),
                title = "地点",
                value = locationCount.toString(),
                icon = Icons.Default.Place,
                color = SignalAmber
            )
        }
    }
}

@Composable
internal fun PhotoStatCardItem(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.06f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(color.copy(alpha = AnimationTokens.Alpha.subtle), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(17.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGray
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
internal fun PhotoFilterDialog(
    contacts: List<Contact>,
    selectedContactId: Long?,
    filterSourceType: String?,
    onContactSelect: (Long?) -> Unit,
    onSourceTypeSelect: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(onClick = onDismiss)
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction = 0.75f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(2.dp))
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.paddingPage),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "筛选照片",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = onDismiss) {
                            Text("完成")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "按来源类型",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextGray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = filterSourceType == null,
                                    onClick = { onSourceTypeSelect(null) },
                                    label = { Text("全部") },
                                    leadingIcon = {
                                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SignalPurple,
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = filterSourceType == "event",
                                    onClick = { onSourceTypeSelect("event") },
                                    label = { Text("事件") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(16.dp))
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SignalGreen,
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = filterSourceType == "chat",
                                    onClick = { onSourceTypeSelect("chat") },
                                    label = { Text("对话") },
                                    leadingIcon = {
                                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SignalSky,
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = filterSourceType == "gift",
                                    onClick = { onSourceTypeSelect("gift") },
                                    label = { Text("礼物") },
                                    leadingIcon = {
                                        Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(16.dp))
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SignalAmber,
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "按人物筛选",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextGray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onContactSelect(null) },
                                color = if (selectedContactId == null) SignalPurple.copy(alpha = 0.1f) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.AllInclusive,
                                        contentDescription = null,
                                        tint = if (selectedContactId == null) SignalPurple else TextGray
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "全部人物",
                                        color = if (selectedContactId == null) SignalPurple else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        items(contacts, key = { it.id }) { contact ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onContactSelect(contact.id) },
                                color = if (selectedContactId == contact.id) SignalPurple.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ContactAvatar(avatar = contact.avatar, name = contact.name, size = 32)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = contact.name,
                                        color = if (selectedContactId == contact.id) SignalPurple else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
