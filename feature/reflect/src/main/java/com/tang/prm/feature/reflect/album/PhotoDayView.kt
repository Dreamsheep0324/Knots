package com.tang.prm.feature.reflect.album

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.model.AlbumPhoto
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.TextGray

@Composable
internal fun DailyPhotoView(
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
            DailyPhotoCard(
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
private fun DailyPhotoCard(
    group: PhotoGroup,
    photos: List<AlbumPhoto>,
    onPhotoClick: (Int) -> Unit,
    showTimeline: Boolean = false,
    isLast: Boolean = false,
    isTabletLayout: Boolean = false
) {
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
                        .background(SignalPurple, CircleShape)
                )
                if (!isLast) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(60.dp)
                            .background(SignalPurple.copy(alpha = 0.3f))
                    )
                }
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = group.groupTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    )
                    if (group.contacts.isNotEmpty()) {
                        Text(
                            text = group.contacts.mapNotNull { it.second }.joinToString("、"),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SignalPurple.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "${photos.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = SignalPurple,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }

            // C-1 修复：DailyPhotoGrid 替换为统一的 PhotoGridLayout
            PhotoGridLayout(
                photos = photos,
                onPhotoClick = onPhotoClick,
                isTabletLayout = isTabletLayout,
                heights = PhotoGridHeights.daily(isTabletLayout),
                overflowTextSize = 16.sp
            )
        }
    }
}
