package com.tang.prm.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tang.prm.ui.components.ContactAvatar
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.SignalSky
import com.tang.prm.ui.theme.TextGray
import com.tang.prm.util.DateUtils

@Composable
internal fun EventPhotoView(
    groups: List<PhotoGroup>,
    onPhotoClick: (List<AlbumPhoto>, Int) -> Unit
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
                isLast = isLast
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
    isLast: Boolean = false
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
                            color = Color(0xFF111827),
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

            EventPhotoGrid(
                photos = photos.take(4),
                onPhotoClick = onPhotoClick
            )

            if (photos.size > 4) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SignalPurple.copy(alpha = AnimationTokens.Alpha.faint))
                        .clickable { }
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

            group.contactName?.let {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ContactAvatar(
                        avatar = group.contactAvatar,
                        name = it,
                        size = 18
                    )
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun EventPhotoGrid(
    photos: List<AlbumPhoto>,
    onPhotoClick: (Int) -> Unit
) {
    val spacing = 4.dp
    val cornerRadius = 8.dp

    when {
        photos.size == 1 -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(Color(0xFFF3F4F6))
                    .clickable { onPhotoClick(0) }
            ) {
                AsyncImage(
                    model = photos[0].uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        photos.size == 2 -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                photos.forEachIndexed { index, photo ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp)
                            .clip(RoundedCornerShape(cornerRadius))
                            .background(Color(0xFFF3F4F6))
                            .clickable { onPhotoClick(index) }
                    ) {
                        AsyncImage(
                            model = photo.uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
        photos.size == 3 -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                photos.forEachIndexed { index, photo ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp)
                            .clip(RoundedCornerShape(cornerRadius))
                            .background(Color(0xFFF3F4F6))
                            .clickable { onPhotoClick(index) }
                    ) {
                        AsyncImage(
                            model = photo.uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
        else -> {
            val displayCount = minOf(photos.size, 4)
            val remainingCount = photos.size - displayCount

            Column(
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    photos.subList(0, 2).forEachIndexed { index, photo ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(70.dp)
                                .clip(RoundedCornerShape(cornerRadius))
                                .background(Color(0xFFF3F4F6))
                                .clickable { onPhotoClick(index) }
                        ) {
                            AsyncImage(
                                model = photo.uri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    photos.subList(2, 4).forEachIndexed { index, photo ->
                        val globalIndex = index + 2
                        val isLast = index == 1 && remainingCount > 0

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(70.dp)
                                .clip(RoundedCornerShape(cornerRadius))
                                .background(Color(0xFFF3F4F6))
                                .clickable { onPhotoClick(globalIndex) }
                        ) {
                            AsyncImage(
                                model = photo.uri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            if (isLast) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+$remainingCount",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
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
