package com.tang.prm.feature.reflect.album

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tang.prm.domain.model.AlbumPhoto
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.SignalSky

@Composable
internal fun PhotoGridView(
    photos: List<AlbumPhoto>,
    onPhotoClick: (Int) -> Unit,
    isTabletLayout: Boolean = false
) {
    val columns = if (isTabletLayout) 6 else 3
    val spacing = if (isTabletLayout) 6.dp else 4.dp
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 16.dp),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        itemsIndexed(photos, key = { _, photo -> photo.id }) { index, photo ->
            PhotoGridItem(
                photo = photo,
                onClick = { onPhotoClick(index) }
            )
        }
    }
}

@Composable
private fun PhotoGridItem(
    photo: AlbumPhoto,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = photo.uri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp),
            shape = RoundedCornerShape(6.dp),
            color = when (photo.sourceType) {
                "event" -> SignalGreen.copy(alpha = 0.85f)
                "chat" -> SignalSky.copy(alpha = 0.85f)
                "gift" -> SignalAmber.copy(alpha = 0.85f)
                else -> SignalPurple.copy(alpha = 0.85f)
            }
        ) {
            Icon(
                imageVector = when (photo.sourceType) {
                    "event" -> Icons.Default.Event
                    "chat" -> Icons.AutoMirrored.Filled.Chat
                    "gift" -> Icons.Default.CardGiftcard
                    else -> Icons.Default.Image
                },
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .padding(3.dp)
                    .size(12.dp)
            )
        }
    }
}
