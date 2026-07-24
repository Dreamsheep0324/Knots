package com.tang.prm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

/**
 * 统一联系人头像组件。
 *
 * - 有 avatar：加载远程/本地图片，加载中显示 primary 色块，加载失败回退到首字母色块。
 * - 无 avatar：直接显示 primary 色块 + 首字母。
 *
 * 性能优化：
 * - [ImageRequest] 通过 [remember] 缓存，避免每次重组都重建。
 * - 加载/错误/无头像三种回退场景共用 [FallbackAvatarBlock]，消除 10+ 行重复代码。
 *
 * 注：使用 [SubcomposeAsyncImage] 是因为加载/错误占位需要组合式（composable）slot，
 * [coil.compose.AsyncImage] 仅支持 Painter 占位，无法表达"文字+色块"的回退 UI。
 * 列表场景的主要开销已通过 [ImageRequest] 缓存缓解。
 */
@Composable
fun ContactAvatar(
    avatar: String?,
    name: String,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val initial = name.firstOrNull()?.toString() ?: "?"
    val fallbackBg = MaterialTheme.colorScheme.primary
    val textStyle = MaterialTheme.typography.titleMedium

    if (avatar != null) {
        val context = LocalContext.current
        val request = remember(avatar) {
            ImageRequest.Builder(context)
                .data(avatar)
                .crossfade(true)
                .build()
        }
        SubcomposeAsyncImage(
            model = request,
            contentDescription = name,
            modifier = modifier
                .size(size)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            loading = { FallbackAvatarBlock(initial, fallbackBg, textStyle) },
            error = { FallbackAvatarBlock(initial, fallbackBg, textStyle) }
        )
    } else {
        FallbackAvatarBlock(initial, fallbackBg, textStyle, modifier.size(size))
    }
}

/** 加载中/加载失败/无头像共用的首字母色块。 */
@Composable
private fun FallbackAvatarBlock(
    initial: String,
    bg: Color,
    textStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            style = textStyle,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}
