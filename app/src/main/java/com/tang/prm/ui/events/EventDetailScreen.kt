@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.ui.events

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.tang.prm.domain.model.Event
import com.tang.prm.ui.navigation.Screen
import com.tang.prm.ui.theme.Primary
import com.tang.prm.ui.theme.SignalSky
import com.tang.prm.util.DateUtils
import com.tang.prm.ui.components.DeleteConfirmDialog
import com.tang.prm.ui.theme.toComposeColor
import com.tang.prm.ui.theme.getEmotionColor
import com.tang.prm.ui.theme.getEmotionIcon
import com.tang.prm.ui.theme.getWeatherColor
import com.tang.prm.ui.theme.getWeatherIcon
import java.io.File
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.theme.DialogDefaults

@Composable
fun EventDetailScreen(
    eventId: Long,
    navController: NavController,
    viewModel: EventDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedPhotoIndex by remember { mutableStateOf<Int?>(null) }
    var showRemarkDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(eventId) {
        viewModel.loadEvent(eventId)
    }

    LaunchedEffect(uiState.isRemarkSaved) {
        if (uiState.isRemarkSaved) {
            showRemarkDialog = false
            viewModel.consumeRemarkSaved()
        }
    }

    if (showRemarkDialog) {
        RemarkInputDialog(
            existingRemark = uiState.event?.remarks ?: "",
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
                    IconButton(onClick = { navController.navigate(Screen.EditEvent.createRoute(eventId)) }) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color(0xFFEF4444))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        uiState.event?.let { event ->
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
                        isFavorite = uiState.isFavorite,
                        onCommentClick = { showRemarkDialog = true },
                        onShareClick = { shareEvent(context, event) },
                        onFavoriteClick = { viewModel.toggleFavorite() }
                    )
                }

                if (event.remarks != null && event.remarks.isNotBlank()) {
                    item { HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp) }

                    item {
                        RemarkSection(
                            remarks = event.remarks,
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

@Composable
private fun RemarkInputDialog(
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
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(20.dp))
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
private fun RemarkSection(remarks: String, onEdit: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFFEC4899).copy(alpha = AnimationTokens.Alpha.faint)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(14.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("个人感悟", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onEdit, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.width(2.dp))
                Text("编辑", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(remarks, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 22.sp)
    }
}

@Composable
private fun EventHeader(event: Event) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(Primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = "我", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = DateUtils.formatRelativeTime(event.time), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "·", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.Public, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), modifier = Modifier.size(12.dp))
            }
        }

        if (event.type.isNotBlank()) {
            Surface(color = Primary.copy(alpha = AnimationTokens.Alpha.faint), shape = RoundedCornerShape(12.dp)) {
                Box(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Text(text = event.type, style = MaterialTheme.typography.labelSmall, color = Primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun EventMainContent(event: Event) {
    if (event.description != null && event.description.isNotBlank()) {
        Text(text = event.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, lineHeight = 22.sp)
    }
}

@Composable
private fun PolaroidPhotosRow(
    photos: List<String>,
    onPhotoClick: (Int) -> Unit
) {
    when (photos.size) {
        1 -> {
            PolaroidPhoto(
                photoUri = photos[0],
                rotation = 0f,
                photoSize = PhotoSize.LARGE,
                onClick = { onPhotoClick(0) }
            )
        }
        2 -> {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                photos.forEachIndexed { index, photo ->
                    PolaroidPhoto(
                        photoUri = photo,
                        rotation = if (index == 0) -1f else 1f,
                        photoSize = PhotoSize.MEDIUM,
                        modifier = Modifier.weight(1f),
                        onClick = { onPhotoClick(index) }
                    )
                }
            }
        }
        else -> {
            val rows = photos.chunked(3)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rows.forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEachIndexed { indexInRow, photo ->
                            val globalIndex = photos.indexOf(photo)
                            PolaroidPhoto(
                                photoUri = photo,
                                rotation = if (indexInRow == 0) -1f else if (indexInRow == 2) 1f else 0f,
                                photoSize = PhotoSize.SMALL,
                                modifier = Modifier.weight(1f),
                                onClick = { onPhotoClick(globalIndex) }
                            )
                        }
                        if (row.size < 3) {
                            repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
    }
}

private enum class PhotoSize(val imageHeight: Dp, val paddingValues: PaddingValues, val cornerRadius: Dp) {
    LARGE(200.dp, PaddingValues(6.dp, 6.dp, 6.dp, 16.dp), 4.dp),
    MEDIUM(160.dp, PaddingValues(5.dp, 5.dp, 5.dp, 14.dp), 4.dp),
    SMALL(120.dp, PaddingValues(4.dp, 4.dp, 4.dp, 12.dp), 3.dp)
}

@Composable
private fun PolaroidPhoto(
    photoUri: String,
    rotation: Float,
    photoSize: PhotoSize,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.rotate(rotation).clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(photoSize.paddingValues)) {
            AsyncImage(
                model = photoUri,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(photoSize.imageHeight).clip(RoundedCornerShape(photoSize.cornerRadius)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoPreviewDialog(
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

@Composable
private fun ParticipantsSection(
    participants: List<com.tang.prm.domain.model.Contact>,
    onParticipantClick: (com.tang.prm.domain.model.Contact) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.People, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = participants.joinToString("、") { it.name } + " 等${participants.size}人参与",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(participants.take(9), key = { it.id }) { participant ->
                ParticipantAvatar(participant = participant, onClick = { onParticipantClick(participant) })
            }
        }
    }
}

@Composable
private fun ParticipantAvatar(
    participant: com.tang.prm.domain.model.Contact,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(48.dp)) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(Primary.copy(alpha = AnimationTokens.Alpha.faint)).clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            val avatarUrl = participant.avatar
            if (!avatarUrl.isNullOrBlank()) {
                AsyncImage(model = avatarUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Text(text = participant.name.firstOrNull()?.toString() ?: "?", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Primary)
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = participant.name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StatsRow(event: Event, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = DateUtils.formatDateTime(event.time), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            event.location?.let { location ->
                if (location.isNotBlank()) {
                    InfoTag(icon = Icons.Default.LocationOn, text = location, bgColor = SignalSky.copy(alpha = AnimationTokens.Alpha.faint), textColor = SignalSky)
                }
            }

            event.weather?.let { weather ->
                if (weather.isNotBlank()) {
                    val wColor = getWeatherColor(weather)?.let { it.toComposeColor(Color(0xFFF59E0B)) } ?: Color(0xFFF59E0B)
                    val wIcon = getWeatherIcon(weather)
                    WeatherTag(text = weather, icon = wIcon, color = wColor)
                }
            }

            event.emotion?.let { emotion ->
                if (emotion.isNotBlank()) {
                    val eColor = getEmotionColor(emotion)?.let { it.toComposeColor(Color(0xFF8B5CF6)) } ?: Color(0xFF8B5CF6)
                    val eIcon = getEmotionIcon(emotion)
                    EmotionTag(text = emotion, icon = eIcon, color = eColor)
                }
            }
        }
    }
}

@Composable
private fun InfoTag(icon: ImageVector, text: String, bgColor: Color, textColor: Color) {
    Surface(color = bgColor, shape = RoundedCornerShape(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = text, style = MaterialTheme.typography.labelSmall, color = textColor, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun WeatherTag(text: String, icon: ImageVector?, color: Color) {
    Surface(color = color.copy(alpha = AnimationTokens.Alpha.faint), shape = RoundedCornerShape(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            if (icon != null) Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = text, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun EmotionTag(text: String, icon: ImageVector?, color: Color) {
    Surface(color = color.copy(alpha = AnimationTokens.Alpha.faint), shape = RoundedCornerShape(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            if (icon != null) Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = text, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun InteractionRow(
    isFavorite: Boolean = false,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onFavoriteClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        InteractionButton(icon = Icons.Default.ChatBubbleOutline, text = "评论", onClick = onCommentClick)
        InteractionButton(icon = Icons.Default.Repeat, text = "转发", onClick = onShareClick)
        InteractionButton(icon = Icons.Default.FavoriteBorder, text = "喜欢", onClick = {})
        InteractionButton(
            icon = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
            text = "收藏",
            onClick = onFavoriteClick,
            tint = if (isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        InteractionButton(icon = Icons.Default.Share, text = "分享", onClick = onShareClick)
    }
}

@Composable
private fun InteractionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
) {
    Column(
        modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = tint, fontSize = 10.sp)
    }
}

private fun shareEvent(context: android.content.Context, event: Event) {
    val sb = StringBuilder()

    event.title.let { if (it.isNotBlank()) sb.appendLine(it) }

    event.description?.let { if (it.isNotBlank()) sb.appendLine(it) }

    sb.appendLine()

    if (event.type.isNotBlank()) sb.append("🏷️ ${event.type}  ")
    event.location?.let { if (it.isNotBlank()) sb.append("📍 $it  ") }
    event.weather?.let { if (it.isNotBlank()) sb.append("🌤️ $it  ") }
    event.emotion?.let { if (it.isNotBlank()) sb.append("💭 $it  ") }

    sb.appendLine()
    sb.append("📅 ${DateUtils.formatDateTime(event.time)}")

    if (event.participants.isNotEmpty()) {
        sb.appendLine()
        sb.append("👥 ${event.participants.joinToString("、") { it.name }}")
    }

    event.remarks?.let { if (it.isNotBlank()) sb.appendLine().appendLine().append("✨ $it") }

    sb.appendLine().append("— 来自 YU")

    val shareIntent = android.content.Intent().apply {
        action = android.content.Intent.ACTION_SEND
        putExtra(android.content.Intent.EXTRA_TEXT, sb.toString().trim())
        type = "text/plain"

        if (event.photos.isNotEmpty()) {
            try {
                val uri = event.photos.first().toUri()
                if (uri.scheme == "content") {
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    type = "image/*"
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            } catch (_: Exception) {}
        }
    }

    context.startActivity(android.content.Intent.createChooser(shareIntent, "分享事件"))
}
