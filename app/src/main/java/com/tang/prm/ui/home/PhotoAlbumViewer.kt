package com.tang.prm.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.tang.prm.ui.components.ContactAvatar
import com.tang.prm.util.DateUtils
import com.tang.prm.ui.theme.Dimens

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun SwipeablePhotoViewerDialog(
    photos: List<AlbumPhoto>,
    initialIndex: Int,
    onDismiss: () -> Unit,
    onToggleFavorite: (AlbumPhoto) -> Unit,
    favoritePhotoIds: Set<Long>
) {
    val pagerState = rememberPagerState(initialPage = initialIndex) { photos.size }
    val dateFormat: (Long) -> String = { DateUtils.formatYearMonthDayChineseFull(it) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
        ) {
            HorizontalPager(
                state = pagerState
            ) { page ->
                val photo = photos[page]
                var scale by remember(photo.id) { mutableFloatStateOf(1f) }
                var offsetX by remember(photo.id) { mutableFloatStateOf(0f) }
                var offsetY by remember(photo.id) { mutableFloatStateOf(0f) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (scale > 1.01f) {
                                Modifier.pointerInput(photo.id) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        val newScale = (scale * zoom).coerceIn(1f, 4f)
                                        scale = newScale
                                        if (newScale > 1.01f) {
                                            offsetX += pan.x
                                            offsetY += pan.y
                                        } else {
                                            offsetX = 0f
                                            offsetY = 0f
                                        }
                                    }
                                }
                            } else {
                                Modifier.pointerInput(photo.id) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent()
                                            val changes = event.changes
                                            if (changes.size >= 2) {
                                                var totalZoom = 1f
                                                for (i in 0 until changes.size - 1) {
                                                    for (j in i + 1 until changes.size) {
                                                        val currDist = (changes[i].position - changes[j].position).getDistance()
                                                        val prevDist = (changes[i].previousPosition - changes[j].previousPosition).getDistance()
                                                        if (prevDist > 0f) totalZoom *= currDist / prevDist
                                                    }
                                                }
                                                val newScale = (scale * totalZoom).coerceIn(1f, 4f)
                                                scale = newScale
                                                if (newScale <= 1.01f) {
                                                    offsetX = 0f
                                                    offsetY = 0f
                                                }
                                                changes.forEach { it.consume() }
                                            }
                                        }
                                    }
                                }
                            }
                        )
                ) {
                    AsyncImage(
                        model = photo.uri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offsetX,
                                translationY = offsetY
                            ),
                        contentScale = ContentScale.Fit
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                    ) {
                        if (photo.contactName != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ContactAvatar(
                                    avatar = photo.contactAvatar,
                                    name = photo.contactName,
                                    size = 32
                                )
                                Text(
                                    text = photo.contactName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Text(
                            text = photo.sourceTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                                Text(
                                    text = dateFormat(photo.date),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                            if (photo.location != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Place, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                                    Text(
                                        text = photo.location,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${pagerState.currentPage + 1} / ${photos.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(Dimens.paddingCard),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val currentPhoto = photos[pagerState.currentPage]
                val currentIsFavorite = favoritePhotoIds.contains(currentPhoto.stableId)
                IconButton(
                    onClick = { onToggleFavorite(currentPhoto) },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        if (currentIsFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "收藏",
                        tint = if (currentIsFavorite) Color(0xFFFFB300) else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "关闭", tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}
