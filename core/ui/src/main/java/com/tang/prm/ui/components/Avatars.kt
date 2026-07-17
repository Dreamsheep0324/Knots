package com.tang.prm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

/**
 * 统一联系人头像组件。
 *
 * - 有 avatar：加载远程/本地图片，加载中显示 primary 色块，加载失败回退到首字母色块。
 * - 无 avatar：直接显示 primary 色块 + 首字母。
 */
@Composable
fun ContactAvatar(
    avatar: String?,
    name: String,
    size: Int = 48,
    modifier: Modifier = Modifier
) {
    val initial = name.firstOrNull()?.toString() ?: "?"
    val fallbackBg = MaterialTheme.colorScheme.primary
    val textStyle = MaterialTheme.typography.titleMedium

    if (avatar != null) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(avatar)
                .crossfade(true)
                .build(),
            contentDescription = name,
            modifier = modifier
                .size(size.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize().background(fallbackBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        style = textStyle,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            error = {
                Box(
                    modifier = Modifier.fillMaxSize().background(fallbackBg),
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
        )
    } else {
        Box(
            modifier = modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(fallbackBg),
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
}
