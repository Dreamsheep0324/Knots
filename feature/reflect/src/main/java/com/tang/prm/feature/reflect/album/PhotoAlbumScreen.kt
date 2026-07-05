@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.reflect.album

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tang.prm.domain.model.AlbumPhoto
import com.tang.prm.ui.components.EmptyState
import com.tang.prm.ui.theme.*

private val AlbumAccent = SignalPurple

@Composable
fun PhotoAlbumScreen(
    navController: NavController,
    viewModel: PhotoAlbumViewModel = hiltViewModel(),
    initialPhotoId: Long? = null,
    isTabletLayout: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var viewMode by remember { mutableStateOf("daily") }
    var selectedPhotoIndex by remember { mutableIntStateOf(-1) }
    var allPhotos by remember { mutableStateOf<List<AlbumPhoto>>(emptyList()) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var initialPhotoHandled by remember { mutableStateOf(false) }

    val groupedPhotos = remember(uiState.photos, viewMode) {
        when (viewMode) {
            "daily" -> groupPhotosByDate(uiState.photos)
            "event" -> groupPhotosBySource(uiState.photos)
            "grid" -> emptyList()
            else -> groupPhotosByDate(uiState.photos)
        }
    }

    val isWaitingForPhoto = initialPhotoId != null && !initialPhotoHandled

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SignalSky)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "往来相册",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (!isWaitingForPhoto) {
                        IconButton(onClick = { showFilterDialog = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "筛选")
                        }
                        if (uiState.selectedContactId != null || uiState.filterSourceType != null) {
                            TextButton(onClick = { viewModel.clearFilters() }) {
                                Text("清除")
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        if (isWaitingForPhoto && uiState.photos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SignalPurple)
            }
        } else if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SignalPurple)
            }
        } else if (uiState.totalPhotoCount == 0) {
            EmptyState(
                icon = Icons.Default.PhotoLibrary,
                title = "还没有照片",
                description = "在事件、对话或礼物中添加照片后会自动显示在这里",
                modifier = Modifier.padding(padding)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.surface)
                    .then(if (isTabletLayout) Modifier.padding(horizontal = 48.dp) else Modifier)
            ) {
                PhotoStatsSection(
                    photoCount = uiState.totalPhotoCount,
                    contactCount = uiState.totalContactCount,
                    locationCount = uiState.totalLocationCount,
                    isTabletLayout = isTabletLayout
                )

                FilterTabsSection(
                    viewMode = viewMode,
                    onViewModeChange = { viewMode = it }
                )

                if (uiState.photos.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.PhotoLibrary,
                        title = "没有符合条件的照片"
                    )
                } else if (viewMode == "grid") {
                    PhotoGridView(
                        photos = uiState.photos,
                        onPhotoClick = { index ->
                            allPhotos = uiState.photos
                            selectedPhotoIndex = index
                        },
                        isTabletLayout = isTabletLayout
                    )
                } else {
                    when (viewMode) {
                        "daily" -> DailyPhotoView(
                            groups = groupedPhotos,
                            onPhotoClick = { photos, index ->
                                allPhotos = photos
                                selectedPhotoIndex = index
                            },
                            isTabletLayout = isTabletLayout
                        )
                        "event" -> EventPhotoView(
                            groups = groupedPhotos,
                            onPhotoClick = { photos, index ->
                                allPhotos = photos
                                selectedPhotoIndex = index
                            },
                            isTabletLayout = isTabletLayout
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(uiState.photos.isNotEmpty(), initialPhotoId) {
        if (initialPhotoId != null && !initialPhotoHandled && uiState.photos.isNotEmpty()) {
            val index = uiState.photos.indexOfFirst { it.stableId == initialPhotoId }
            if (index >= 0) {
                allPhotos = uiState.photos
                selectedPhotoIndex = index
            }
            initialPhotoHandled = true
        }
    }

    if (showFilterDialog) {
        PhotoFilterDialog(
            contacts = uiState.allContacts,
            selectedContactId = uiState.selectedContactId,
            filterSourceType = uiState.filterSourceType,
            onContactSelect = { viewModel.filterByContact(it) },
            onSourceTypeSelect = { viewModel.filterBySourceType(it) },
            onDismiss = { showFilterDialog = false }
        )
    }

    if (selectedPhotoIndex >= 0) {
        SwipeablePhotoViewerDialog(
            photos = allPhotos,
            initialIndex = selectedPhotoIndex,
            onDismiss = { selectedPhotoIndex = -1 },
            onToggleFavorite = { photo -> viewModel.toggleFavorite(photo) },
            favoritePhotoIds = uiState.favoritePhotoIds
        )
    }
}

data class PhotoGroup(
    val groupType: String,
    val groupKey: String,
    val groupTitle: String,
    val subtitle: String?,
    val contactName: String?,
    val contactAvatar: String?,
    val contacts: List<Pair<String?, String?>>,
    val date: Long,
    val location: String?,
    val photos: List<AlbumPhoto>
)
