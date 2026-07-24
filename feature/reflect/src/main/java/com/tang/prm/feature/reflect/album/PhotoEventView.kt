package com.tang.prm.feature.reflect.album

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.model.AlbumPhoto
import com.tang.prm.ui.components.ContactAvatar
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.SignalSky
import com.tang.prm.ui.theme.TextGray
import com.tang.prm.domain.util.DateUtils

@Composable
internal fun EventPhotoView(
    groups: List<PhotoGroup>,
    onPhotoClick: (List<AlbumPhoto>, Int) -> Unit,
    isTabletLayout: Boolean = false
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        items(groups.size, key = { groups[it].groupKey }) { index ->
            val group = groups[index]
            val isLast = index == groups.size - 1
            EventPhotoCard(
                group = group,
                photos = group.photos,
                onPhotoClick = { photoIndex -> onPhotoClick(group.photos, photoIndex) },
                showTimeline = true,
                isLast = isLast,
                isTabletLayout = isTabletLayout
            )
            if (!isLast) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun EventPhotoCard(
    group: PhotoGroup,
    photos: List<AlbumPhoto>,
    onPhotoClick: (Int) -> Unit,
    showTimeline: Boolean = false,
    isLast: Boolean = false,
    isTabletLayout: Boolean = false
) {
    val dateFormat: (Long) -> String = { DateUtils.formatMonthDayChineseFull(it) }
    val sourceColor = when (group.subtitle) {
        "事件" -> SignalGreen
        "对话" -> SignalSky
        "礼物" -> SignalAmber
        else -> SignalPurple
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (showTimeline) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(sourceColor, CircleShape)
                )
                if (!isLast) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(60.dp)
                            .background(sourceColor.copy(alpha = 0.3f))
                    )
                }
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = group.groupTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = TextGray,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = dateFormat(group.date),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray,
                            fontSize = 12.sp
                        )
                        group.location?.let {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Place,
                                contentDescription = null,
                                tint = TextGray.copy(alpha = 0.6f),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGray,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = sourceColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${photos.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = sourceColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // C-1 修复：EventPhotoGrid 替换为统一的 PhotoGridLayout
            // 死代码清理：原 EventPhotoGrid 的 "+N" 遮罩因 take(4) 永不触发，统一组件在 photos.size <= 4 时自然无遮罩
            PhotoGridLayout(
                photos = photos.take(4),
                onPhotoClick = onPhotoClick,
                isTabletLayout = isTabletLayout,
                heights = PhotoGridHeights.event(isTabletLayout),
                overflowTextSize = 14.sp
            )

            if (photos.size > 4) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SignalPurple.copy(alpha = AnimationTokens.Alpha.faint))
                        .clickable { onPhotoClick(4) }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "查看全部 ${photos.size} 张",
                        style = MaterialTheme.typography.bodySmall,
                        color = SignalPurple,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }

            if (group.contacts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    group.contacts.forEach { (avatar, name) ->
                        if (name != null) {
                            ContactAvatar(
                                avatar = avatar,
                                name = name,
                                size = 18.dp
                            )
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
