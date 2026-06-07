@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.tang.prm.feature.events

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tang.prm.ui.theme.DialogDefaults
import com.tang.prm.ui.theme.InsightPink
import com.tang.prm.ui.theme.Primary

@Composable
internal fun RemarkInputDialog(
    existingRemark: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(existingRemark) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DialogDefaults.containerColor,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = InsightPink, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("个人感悟", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                placeholder = { Text("写下你的感受...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, lineHeight = 22.sp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) {
                Text("保存", color = Primary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
internal fun PhotoPreviewDialog(
    photos: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { photos.size })

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                AsyncImage(
                    model = photos[page],
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            TopAppBar(
                title = { Text(text = "${pagerState.currentPage + 1} / ${photos.size}", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    }
}
