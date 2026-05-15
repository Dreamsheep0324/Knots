@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tang.prm.domain.model.Contact
import com.tang.prm.ui.components.ContactAvatar
import com.tang.prm.ui.theme.*
import com.tang.prm.util.DateUtils
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.theme.Dimens

private val AlbumAccent = SignalPurple

@Composable
fun PhotoAlbumScreen(
    navController: NavController,
    viewModel: PhotoAlbumViewModel = hiltViewModel(),
    initialPhotoId: Long? = null
) {
    val uiState by viewModel.uiState.collectAsState()
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
            EmptyPhotoState(modifier = Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                PhotoStatsSection(
                    photoCount = uiState.totalPhotoCount,
                    contactCount = uiState.totalContactCount,
                    locationCount = uiState.totalLocationCount
                )

                FilterTabsSection(
                    viewMode = viewMode,
                    onViewModeChange = { viewMode = it }
                )

                if (uiState.photos.isEmpty()) {
                    EmptyFilterState(message = "没有符合条件的照片")
                } else if (viewMode == "grid") {
                    PhotoGridView(
                        photos = uiState.photos,
                        onPhotoClick = { index ->
                            allPhotos = uiState.photos
                            selectedPhotoIndex = index
                        }
                    )
                } else {
                    when (viewMode) {
                        "daily" -> DailyPhotoView(
                            groups = groupedPhotos,
                            onPhotoClick = { photos, index ->
                                allPhotos = photos
                                selectedPhotoIndex = index
                            }
                        )
                        "event" -> EventPhotoView(
                            groups = groupedPhotos,
                            onPhotoClick = { photos, index ->
                                allPhotos = photos
                                selectedPhotoIndex = index
                            }
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
    val date: Long,
    val location: String?,
    val photos: List<AlbumPhoto>
)

fun groupPhotosByDate(photos: List<AlbumPhoto>): List<PhotoGroup> {
    val grouped = photos.groupBy { DateUtils.formatYearMonthDayChineseFull(it.date) }
    return grouped.map { (dateLabel, datePhotos) ->
        val firstPhoto = datePhotos.first()
        PhotoGroup(
            groupType = "daily",
            groupKey = dateLabel,
            groupTitle = dateLabel,
            subtitle = "${datePhotos.size}张照片",
            contactName = firstPhoto.contactName,
            contactAvatar = firstPhoto.contactAvatar,
            date = firstPhoto.date,
            location = firstPhoto.location,
            photos = datePhotos
        )
    }.sortedByDescending { it.date }
}

fun groupPhotosBySource(photos: List<AlbumPhoto>): List<PhotoGroup> {
    val grouped = photos.groupBy { "${it.sourceType}_${it.sourceId}" }
    return grouped.map { (key, groupPhotos) ->
        val firstPhoto = groupPhotos.first()
        val subtitle = when (firstPhoto.sourceType) {
            "event" -> "事件"
            "chat" -> "对话"
            "gift" -> "礼物"
            else -> "其他"
        }
        PhotoGroup(
            groupType = "event",
            groupKey = key,
            groupTitle = firstPhoto.sourceTitle,
            subtitle = subtitle,
            contactName = firstPhoto.contactName,
            contactAvatar = firstPhoto.contactAvatar,
            date = firstPhoto.date,
            location = firstPhoto.location,
            photos = groupPhotos
        )
    }.sortedByDescending { it.date }
}

@Composable
private fun FilterTabsSection(
    viewMode: String,
    onViewModeChange: (String) -> Unit
) {
    val tabs = listOf(
        "daily" to "每日",
        "event" to "事件",
        "grid" to "网格"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 8.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp)),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEach { (key, label) ->
            val selected = viewMode == key
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                color = if (selected) SignalPurple else Color.Transparent,
                onClick = { onViewModeChange(key) }
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (selected) Color.White else TextGray,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyPhotoState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(SignalPurple.copy(alpha = AnimationTokens.Alpha.faint), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint = SignalPurple,
                    modifier = Modifier.size(44.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "还没有照片",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "在事件、对话或礼物中添加照片后会自动显示在这里",
                style = MaterialTheme.typography.bodyMedium,
                color = TextGray
            )
        }
    }
}

@Composable
private fun EmptyFilterState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(TextGray.copy(alpha = AnimationTokens.Alpha.faint), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint = TextGray.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextGray
            )
        }
    }
}

@Composable
private fun PhotoStatsSection(
    photoCount: Int,
    contactCount: Int,
    locationCount: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 16.dp, 16.dp, 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(Dimens.paddingCard)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "照片统计",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "共 $photoCount 张",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PhotoStatCardItem(
                    modifier = Modifier.weight(1f),
                    title = "照片",
                    value = photoCount.toString(),
                    icon = Icons.Default.Image,
                    color = SignalPurple
                )
                PhotoStatCardItem(
                    modifier = Modifier.weight(1f),
                    title = "人物",
                    value = contactCount.toString(),
                    icon = Icons.Default.People,
                    color = SignalGreen
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            PhotoStatCardItem(
                modifier = Modifier.fillMaxWidth(),
                title = "地点",
                value = locationCount.toString(),
                icon = Icons.Default.Place,
                color = SignalAmber
            )
        }
    }
}

@Composable
private fun PhotoStatCardItem(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.06f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(color.copy(alpha = AnimationTokens.Alpha.subtle), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(17.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGray
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun PhotoFilterDialog(
    contacts: List<Contact>,
    selectedContactId: Long?,
    filterSourceType: String?,
    onContactSelect: (Long?) -> Unit,
    onSourceTypeSelect: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(onClick = onDismiss)
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction = 0.75f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(2.dp))
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.paddingPage),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "筛选照片",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = onDismiss) {
                            Text("完成")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "按来源类型",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextGray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = filterSourceType == null,
                                    onClick = { onSourceTypeSelect(null) },
                                    label = { Text("全部") },
                                    leadingIcon = {
                                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SignalPurple,
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = filterSourceType == "event",
                                    onClick = { onSourceTypeSelect("event") },
                                    label = { Text("事件") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(16.dp))
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SignalGreen,
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = filterSourceType == "chat",
                                    onClick = { onSourceTypeSelect("chat") },
                                    label = { Text("对话") },
                                    leadingIcon = {
                                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SignalSky,
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = filterSourceType == "gift",
                                    onClick = { onSourceTypeSelect("gift") },
                                    label = { Text("礼物") },
                                    leadingIcon = {
                                        Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(16.dp))
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SignalAmber,
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "按人物筛选",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextGray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onContactSelect(null) },
                                color = if (selectedContactId == null) SignalPurple.copy(alpha = 0.1f) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.AllInclusive,
                                        contentDescription = null,
                                        tint = if (selectedContactId == null) SignalPurple else TextGray
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "全部人物",
                                        color = if (selectedContactId == null) SignalPurple else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        items(contacts, key = { it.id }) { contact ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onContactSelect(contact.id) },
                                color = if (selectedContactId == contact.id) SignalPurple.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ContactAvatar(avatar = contact.avatar, name = contact.name, size = 32)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = contact.name,
                                        color = if (selectedContactId == contact.id) SignalPurple else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
