package com.tang.prm.ui.components.photo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * 统一的图片展示槽位
 *
 * @param mode 展示模式
 * @param photoUri 图片 URI/路径
 * @param onRemove 删除回调，null 则不显示删除按钮
 * @param onClick 点击回调，null 则不可点击
 */
@Composable
fun PhotoSlot(
    mode: PhotoSlotMode,
    photoUri: String,
    onRemove: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    when (mode) {
        PhotoSlotMode.AVATAR -> AvatarPhotoSlot(photoUri, onClick)
        PhotoSlotMode.THUMBNAIL -> ThumbnailPhotoSlot(photoUri, onRemove, onClick)
        PhotoSlotMode.POLAROID -> PolaroidPhotoSlot(photoUri, onRemove, onClick)
    }
}

/**
 * 统一的图片添加按钮
 *
 * @param mode 展示模式
 * @param label 按钮文字
 * @param onClick 点击回调
 */
@Composable
fun PhotoAddSlot(
    mode: PhotoSlotMode,
    label: String = "添加照片",
    onClick: () -> Unit
) {
    when (mode) {
        PhotoSlotMode.AVATAR -> AvatarAddSlot(label, onClick)
        PhotoSlotMode.THUMBNAIL -> ThumbnailAddSlot(label, onClick)
        PhotoSlotMode.POLAROID -> PolaroidAddSlot(label, onClick)
    }
}

/**
 * 统一的图片选择区域
 *
 * 自动处理空状态/已有照片的展示，支持 LazyRow 横向滚动。
 *
 * @param photos 已选照片路径列表
 * @param mode 展示模式
 * @param maxCount 最大数量
 * @param onAdd 添加回调
 * @param onRemove 删除回调（参数为索引）
 * @param onPhotoClick 照片点击回调（参数为索引）
 */
@Composable
fun PhotoSelectionArea(
    photos: List<String>,
    mode: PhotoSlotMode,
    maxCount: Int = Int.MAX_VALUE,
    onAdd: () -> Unit,
    onRemove: (Int) -> Unit,
    onPhotoClick: ((Int) -> Unit)? = null
) {
    if (photos.isEmpty()) {
        PhotoAddSlot(
            mode = mode,
            label = when (mode) {
                PhotoSlotMode.AVATAR -> "点击上传头像"
                PhotoSlotMode.THUMBNAIL -> "添加照片"
                PhotoSlotMode.POLAROID -> "添加照片"
            },
            onClick = onAdd
        )
    } else {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(photos, key = { _, photo -> photo }) { index, photo ->
                PhotoSlot(
                    mode = mode,
                    photoUri = photo,
                    onRemove = { onRemove(index) },
                    onClick = { onPhotoClick?.invoke(index) }
                )
            }
            if (photos.size < maxCount) {
                item {
                    PhotoAddSlot(mode = mode, onClick = onAdd)
                }
            }
        }
    }
}

// --- 内部实现 ---

@Composable
private fun AvatarPhotoSlot(photoUri: String, onClick: (() -> Unit)?) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photoUri)
                .crossfade(true)
                .build(),
            contentDescription = "头像",
            modifier = Modifier.size(100.dp).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun AvatarAddSlot(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.AddAPhoto,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ThumbnailPhotoSlot(photoUri: String, onRemove: (() -> Unit)?, onClick: (() -> Unit)?) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photoUri)
                .crossfade(true)
                .build(),
            contentDescription = "照片",
            modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        if (onRemove != null) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .padding(2.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "删除",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ThumbnailAddSlot(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.AddPhotoAlternate,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PolaroidPhotoSlot(photoUri: String, onRemove: (() -> Unit)?, onClick: (() -> Unit)?) {
    val rotation = remember { (-3f..3f).let { (it.start + (it.endInclusive - it.start) * kotlin.random.Random.nextFloat()) } }

    Box(
        modifier = Modifier
            .width(80.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surface)
                .rotate(rotation),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photoUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "照片",
                modifier = Modifier
                    .size(72.dp)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(3.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        RoundedCornerShape(1.dp)
                    )
            )
        }

        if (onRemove != null) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "删除",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun PolaroidAddSlot(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.AddPhotoAlternate,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
