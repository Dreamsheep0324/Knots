@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.tang.prm.ui.components.DeleteConfirmDialog
import com.tang.prm.ui.navigation.EditEventRoute
import com.tang.prm.ui.theme.Error

@Composable
fun EventDetailScreen(
    eventId: Long,
    navController: NavController,
    viewModel: EventDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedPhotoIndex by remember { mutableStateOf<Int?>(null) }
    var showRemarkDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(uiState.dialog.isRemarkSaved) {
        if (uiState.dialog.isRemarkSaved) {
            showRemarkDialog = false
            viewModel.consumeRemarkSaved()
        }
    }

    if (showRemarkDialog) {
        RemarkInputDialog(
            existingRemark = uiState.data.event?.remarks ?: "",
            onDismiss = { showRemarkDialog = false },
            onConfirm = { viewModel.updateRemarks(it) }
        )
    }

    if (showDeleteDialog) {
        DeleteConfirmDialog(
            title = "删除事件",
            message = "确定要删除这个事件吗？此操作不可撤销。",
            onConfirm = { viewModel.deleteEvent(); navController.popBackStack() },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("事件详情", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(EditEventRoute(eventId)) }) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除", tint = Error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        uiState.data.event?.let { event ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        EventHeader(event = event)

                        Spacer(modifier = Modifier.height(14.dp))

                        EventMainContent(event = event)

                        if (event.photos.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            PolaroidPhotosRow(photos = event.photos, onPhotoClick = { index -> selectedPhotoIndex = index })
                        }

                        if (event.participants.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            ParticipantsSection(participants = event.participants, onParticipantClick = {})
                        }
                    }
                }

                item { HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp) }

                item {
                    StatsRow(event = event, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
                }

                item { HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp) }

                item {
                    InteractionRow(
                        isFavorite = uiState.data.isFavorite,
                        onCommentClick = { showRemarkDialog = true },
                        onShareClick = { shareEvent(context, event) },
                        onFavoriteClick = { viewModel.toggleFavorite() }
                    )
                }

                val remarks = event.remarks
                if (remarks != null && remarks.isNotBlank()) {
                    item { HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp) }

                    item {
                        RemarkSection(
                            remarks = remarks,
                            onEdit = { showRemarkDialog = true }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }

            if (selectedPhotoIndex != null) {
                PhotoPreviewDialog(
                    photos = event.photos,
                    initialIndex = selectedPhotoIndex!!,
                    onDismiss = { selectedPhotoIndex = null }
                )
            }
        }
    }
}
