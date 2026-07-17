@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.events

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.util.DateUtils
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.components.DeleteConfirmDialog
import com.tang.prm.ui.navigation.ContactDetailRoute
import com.tang.prm.ui.navigation.EditEventRoute
import com.tang.prm.ui.theme.*
import com.tang.prm.ui.theme.getEmotionIcon
import com.tang.prm.ui.theme.getWeatherIcon

// ═══════════════════════════════════════════════════════════════
// 平板事件详情 — 方案A 杂志式
// ═══════════════════════════════════════════════════════════════

@Composable
fun EventDetailTabletScreen(
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

    val event = uiState.data.event
    if (event == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.data.isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 3.dp)
            } else {
                EventNotFoundState(onBack = { navController.popBackStack() })
            }
        }
        return
    }

    selectedPhotoIndex?.let { index ->
        PhotoPreviewDialog(
            photos = event.photos,
            initialIndex = index,
            onDismiss = { selectedPhotoIndex = null }
        )
    }

    Row(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        // ── 左侧 Hero 区 (56%) ──
        TabletHeroLeft(
            event = event,
            onBack = { navController.popBackStack() },
            onEdit = { navController.navigate(EditEventRoute(eventId)) },
            onDelete = { showDeleteDialog = true },
            onPhotoClick = { selectedPhotoIndex = it },
            modifier = Modifier.weight(0.56f).fillMaxHeight()
        )

        // ── 右侧信息侧栏 (44%) ──
        TabletInfoRight(
            event = event,
            isFavorite = uiState.data.isFavorite,
            onRemarkEdit = { showRemarkDialog = true },
            onShareClick = { shareEvent(context, event) },
            onFavoriteClick = { viewModel.toggleFavorite() },
            onParticipantClick = { contact -> navController.navigate(ContactDetailRoute(contact.id)) },
            modifier = Modifier.weight(0.44f).fillMaxHeight()
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// 左侧 Hero 区
// ═══════════════════════════════════════════════════════════════

@Composable
private fun TabletHeroLeft(
    event: Event,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPhotoClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = resolveEventAccentColor(event)
    val photos = event.photos
    val pagerState = rememberPagerState(pageCount = { photos.size })
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = modifier.background(MaterialTheme.colorScheme.surface)) {
        HeroBackground(
            photos = photos,
            accentColor = accentColor,
            pagerState = pagerState,
            onPhotoClick = onPhotoClick
        )
        HeroTopActionBar(
            onBack = onBack,
            onEdit = onEdit,
            onDelete = onDelete
        )
        HeroBottomContent(
            event = event,
            accentColor = accentColor,
            photos = photos,
            pagerState = pagerState,
            coroutineScope = coroutineScope,
            modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(32.dp)
        )
    }
}

/**
 * Hero 区背景：有照片时为可滑动图片 + 渐变遮罩；无照片时为强调色渐变兜底。
 */
@Composable
private fun HeroBackground(
    photos: List<String>,
    accentColor: Color,
    pagerState: PagerState,
    onPhotoClick: (Int) -> Unit
) {
    if (photos.isNotEmpty()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            AsyncImage(
                model = photos[page],
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onPhotoClick(page) },
                contentScale = ContentScale.Crop
            )
        }
        // 底部渐变遮罩，确保白色文字可读
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )
    } else {
        // 无照片时使用渐变兜底
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(accentColor, accentColor.copy(alpha = 0.7f))
                    )
                )
        )
    }
}

/**
 * Hero 区顶部操作栏：返回 / 编辑 / 删除。
 */
@Composable
private fun HeroTopActionBar(
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeroIconButton(icon = Icons.AutoMirrored.Filled.ArrowBack, onClick = onBack)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HeroIconButton(icon = Icons.Default.Edit, onClick = onEdit)
            HeroIconButton(icon = Icons.Default.Delete, onClick = onDelete, tint = Color.White)
        }
    }
}

/**
 * Hero 区底部内容：标签、标题、描述、照片缩略图、时间信息。
 */
@Composable
private fun HeroBottomContent(
    event: Event,
    accentColor: Color,
    photos: List<String>,
    pagerState: PagerState,
    coroutineScope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // 标签行
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeroTypeBadge(event = event, accentColor = accentColor)
            event.emotion?.let { emotion ->
                if (emotion.isNotBlank()) {
                    HeroEmotionBadge(emotion = emotion)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 标题
        Text(
            text = event.title,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            lineHeight = 38.sp
        )

        // 描述
        event.description?.let { desc ->
            if (desc.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = desc,
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.88f),
                    lineHeight = 26.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 照片缩略图行 + 页码
        if (photos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            HeroPhotoThumbnails(
                photos = photos,
                pagerState = pagerState,
                coroutineScope = coroutineScope
            )
        }

        // 时间信息条
        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeroTimeItem(icon = Icons.Default.CalendarToday, text = DateUtils.formatMonthDayWeekday(event.time))
            HeroTimeItem(icon = Icons.Default.Schedule, text = DateUtils.formatTime(event.time))
            event.location?.let { loc ->
                if (loc.isNotBlank()) {
                    HeroTimeItem(icon = Icons.Default.Place, text = loc)
                }
            }
        }
    }
}

/**
 * Hero 区底部照片缩略图行 + 页码指示器。
 */
@Composable
private fun HeroPhotoThumbnails(
    photos: List<String>,
    pagerState: PagerState,
    coroutineScope: CoroutineScope
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(photos.size, key = { photos[it] }) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                ) {
                    AsyncImage(
                        model = photos[index],
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White.copy(alpha = 0.15f))
                        )
                    }
                }
            }
        }
        if (photos.size > 1) {
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "${pagerState.currentPage + 1}/${photos.size}",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun HeroIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    tint: Color = Color.White
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun HeroTypeBadge(event: Event, accentColor: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(accentColor, CircleShape)
        )
        Text(
            text = event.typeDisplayName,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun HeroEmotionBadge(emotion: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val eIcon = getEmotionIcon(emotion)
        if (eIcon != null) {
            Icon(eIcon, contentDescription = null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(14.dp))
        }
        Text(
            text = emotion,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 13.sp
        )
    }
}

@Composable
private fun HeroTimeItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
        }
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// 右侧信息侧栏
// ═══════════════════════════════════════════════════════════════

@Composable
private fun TabletInfoRight(
    event: Event,
    isFavorite: Boolean,
    onRemarkEdit: () -> Unit,
    onShareClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onParticipantClick: (Contact) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        InfoSidebarHeader(event = event)
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
            modifier = Modifier.padding(horizontal = 28.dp)
        )
        InfoSidebarScrollContent(
            event = event,
            onRemarkEdit = onRemarkEdit,
            onParticipantClick = onParticipantClick,
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 24.dp)
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        InfoSidebarBottomBar(
            isFavorite = isFavorite,
            onRemarkEdit = onRemarkEdit,
            onShareClick = onShareClick,
            onFavoriteClick = onFavoriteClick
        )
    }
}

/**
 * 侧栏顶部标题区：事件详情 + 创建时间。
 */
@Composable
private fun InfoSidebarHeader(event: Event) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 20.dp)
    ) {
        Text(
            text = "事件详情",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${DateUtils.formatRelativeTime(event.createdAt)}创建",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 侧栏可滚动内容区：时间、地点与天气、参与者、个人感悟。
 */
@Composable
private fun InfoSidebarScrollContent(
    event: Event,
    onRemarkEdit: () -> Unit,
    onParticipantClick: (Contact) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        // 时间区
        InfoSection(label = "时间", barColor = SignalGreen) {
            TimeCard(event = event)
        }

        // 地点与天气
        val hasLocation = !event.location.isNullOrBlank()
        val hasWeather = !event.weather.isNullOrBlank()
        if (hasLocation || hasWeather) {
            InfoSection(label = "地点与天气", barColor = SignalSky) {
                LocationWeatherRow(
                    event = event,
                    hasLocation = hasLocation,
                    hasWeather = hasWeather
                )
            }
        }

        // 参与者
        if (event.participants.isNotEmpty()) {
            InfoSection(
                label = "参与者",
                barColor = SignalPurple,
                count = "${event.participants.size}人"
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    event.participants.take(8).forEach { participant ->
                        ParticipantAvatarItem(
                            participant = participant,
                            onClick = { onParticipantClick(participant) }
                        )
                    }
                }
            }
        }

        // 个人感悟
        val remarks = event.remarks
        if (!remarks.isNullOrBlank()) {
            InfoSection(label = "个人感悟", barColor = SignalAmber) {
                RemarkCard(remarks = remarks, onEdit = onRemarkEdit)
            }
        } else {
            InfoSection(label = "个人感悟", barColor = SignalAmber) {
                AddRemarkCard(onAdd = onRemarkEdit)
            }
        }
    }
}

/**
 * 地点与天气行：根据事件数据展示地点和天气网格项。
 */
@Composable
private fun LocationWeatherRow(
    event: Event,
    hasLocation: Boolean,
    hasWeather: Boolean
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        val location = event.location
        if (hasLocation && location != null) {
            InfoGridItem(
                icon = Icons.Default.LocationOn,
                iconBg = SignalSky.copy(alpha = 0.1f),
                text = location,
                subText = "地点",
                iconTint = SignalSky,
                modifier = Modifier.weight(1f)
            )
        }
        val weather = event.weather
        if (hasWeather && weather != null) {
            val wColor = resolveWeatherColor(weather)
            val wIcon = getWeatherIcon(weather) ?: Icons.Default.WbSunny
            InfoGridItem(
                icon = wIcon,
                iconBg = wColor.copy(alpha = 0.1f),
                text = weather,
                subText = "天气",
                iconTint = wColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 侧栏底部交互栏：评论 / 转发 / 收藏 / 分享。
 */
@Composable
private fun InfoSidebarBottomBar(
    isFavorite: Boolean,
    onRemarkEdit: () -> Unit,
    onShareClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        InteractionButtonItem(
            icon = Icons.Default.ChatBubbleOutline,
            label = "评论",
            onClick = onRemarkEdit
        )
        InteractionButtonItem(
            icon = Icons.Default.Repeat,
            label = "转发",
            onClick = onShareClick
        )
        InteractionButtonItem(
            icon = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
            label = if (isFavorite) "已收藏" else "收藏",
            onClick = onFavoriteClick,
            tint = if (isFavorite) FavoriteGold else MaterialTheme.colorScheme.onSurfaceVariant
        )
        InteractionButtonItem(
            icon = Icons.Default.Share,
            label = "分享",
            onClick = onShareClick
        )
    }
}

@Composable
private fun InfoSection(
    label: String,
    barColor: Color,
    count: String? = null,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(barColor)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.5.sp
            )
            if (count != null) {
                Text(
                    text = count,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.visible),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        content()
    }
}

@Composable
private fun TimeCard(event: Event) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp, 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SignalGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Column {
            Text(
                text = DateUtils.formatDateTime(event.time),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = DateUtils.formatRelativeTime(event.time),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                event.endTime?.let { endTime ->
                    if (endTime > event.time) {
                        Dot()
                        Text(
                            text = "持续 ${formatDuration(event.time, endTime)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Dot() {
    Box(
        modifier = Modifier
            .size(3.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
    )
}

@Composable
private fun InfoGridItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    text: String,
    subText: String,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(14.dp, 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
        }
        Column {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subText,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ParticipantAvatarItem(participant: Contact, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(52.dp).clickable(onClick = onClick)
    ) {
        AvatarOrInitial(
            avatarUrl = participant.avatar,
            name = participant.name,
            modifier = Modifier.size(46.dp),
            bgColor = SignalPurple.copy(alpha = AnimationTokens.Alpha.faint),
            initialColor = SignalPurple,
            initialFontSize = 17.sp
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = participant.name,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RemarkCard(remarks: String, onEdit: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(20.dp, 22.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SignalAmber, modifier = Modifier.size(14.dp))
                Text(
                    text = "感悟",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.5.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = remarks,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 26.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.clickable(onClick = onEdit),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                Text(
                    text = "编辑感悟",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AddRemarkCard(onAdd: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onAdd)
            .padding(20.dp, 22.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            Text(
                text = "添加感悟",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InteractionButtonItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = tint,
            fontWeight = if (tint == FavoriteGold) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// 辅助函数
// ═══════════════════════════════════════════════════════════════

private fun formatDuration(start: Long, end: Long): String {
    val diff = end - start
    val hours = diff / (60 * 60 * 1000)
    val minutes = (diff % (60 * 60 * 1000)) / (60 * 1000)
    return when {
        hours > 0 && minutes > 0 -> "${hours}小时${minutes}分钟"
        hours > 0 -> "${hours}小时"
        minutes > 0 -> "${minutes}分钟"
        else -> "片刻"
    }
}
