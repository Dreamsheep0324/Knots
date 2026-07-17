package com.tang.prm.feature.reflect.album

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tang.prm.domain.model.AlbumPhoto

/**
 * C-1 修复：统一照片网格布局，消除 DailyPhotoGrid / EventPhotoGrid 85% 重复代码。
 *
 * 差异维度仅 heights（单图/2-3图/4+图高度）与 overflowTextSize（"+N" 遮罩文字大小），
 * 由调用方通过 [PhotoGridHeights] 与 [overflowTextSize] 参数注入。
 *
 * 死代码清理：EventPhotoGrid 原被 `photos.take(4)` 调用，导致 "+N" 遮罩永远不触发。
 * 统一组件仅在 `photos.size > 4` 时显示遮罩，调用方传入 take(4) 后自然无遮罩。
 */
internal data class PhotoGridHeights(
    val singleHeight: Dp,
    val multiHeight: Dp,
    val gridHeight: Dp
) {
    companion object {
        /** 每日视图高度配置（单图更大，强调当日主打照片） */
        fun daily(isTabletLayout: Boolean): PhotoGridHeights = if (isTabletLayout) {
            PhotoGridHeights(singleHeight = 200.dp, multiHeight = 140.dp, gridHeight = 110.dp)
        } else {
            PhotoGridHeights(singleHeight = 140.dp, multiHeight = 100.dp, gridHeight = 80.dp)
        }

        /** 事件视图高度配置（整体更紧凑，配合事件信息卡片） */
        fun event(isTabletLayout: Boolean): PhotoGridHeights = if (isTabletLayout) {
            PhotoGridHeights(singleHeight = 180.dp, multiHeight = 130.dp, gridHeight = 100.dp)
        } else {
            PhotoGridHeights(singleHeight = 120.dp, multiHeight = 90.dp, gridHeight = 70.dp)
        }
    }
}

@Composable
internal fun PhotoGridLayout(
    photos: List<AlbumPhoto>,
    onPhotoClick: (Int) -> Unit,
    isTabletLayout: Boolean,
    heights: PhotoGridHeights,
    overflowTextSize: TextUnit = 14.sp
) {
    val spacing = if (isTabletLayout) 8.dp else 4.dp
    val cornerRadius = if (isTabletLayout) 10.dp else 8.dp
    val remainingCount = (photos.size - 4).coerceAtLeast(0)

    when {
        photos.size == 1 -> PhotoCell(
            photo = photos[0],
            height = heights.singleHeight,
            cornerRadius = cornerRadius,
            modifier = Modifier.fillMaxWidth(),
            onPhotoClick = { onPhotoClick(0) }
        )
        photos.size in 2..3 -> Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            photos.forEachIndexed { index, photo ->
                PhotoCell(
                    photo = photo,
                    height = heights.multiHeight,
                    cornerRadius = cornerRadius,
                    modifier = Modifier.weight(1f),
                    onPhotoClick = { onPhotoClick(index) }
                )
            }
        }
        else -> Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                photos.subList(0, 2).forEachIndexed { index, photo ->
                    PhotoCell(
                        photo = photo,
                        height = heights.gridHeight,
                        cornerRadius = cornerRadius,
                        modifier = Modifier.weight(1f),
                        onPhotoClick = { onPhotoClick(index) }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                photos.subList(2, 4).forEachIndexed { index, photo ->
                    val globalIndex = index + 2
                    val showOverflow = index == 1 && remainingCount > 0
                    PhotoCell(
                        photo = photo,
                        height = heights.gridHeight,
                        cornerRadius = cornerRadius,
                        modifier = Modifier.weight(1f),
                        onPhotoClick = { onPhotoClick(globalIndex) }
                    ) {
                        if (showOverflow) {
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
                                    fontSize = overflowTextSize
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoCell(
    photo: AlbumPhoto,
    height: Dp,
    cornerRadius: Dp,
    modifier: Modifier = Modifier,
    onPhotoClick: () -> Unit,
    content: @Composable () -> Unit = {}
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onPhotoClick() }
    ) {
        AsyncImage(
            model = photo.uri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        content()
    }
}
