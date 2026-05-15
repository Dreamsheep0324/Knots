@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.tang.prm.domain.model.GiftType
import com.tang.prm.ui.components.ContactAvatar
import com.tang.prm.ui.components.DeleteConfirmDialog
import com.tang.prm.ui.theme.*
import com.tang.prm.util.DateUtils
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.animation.primitives.rememberContinuousRotation
import kotlin.math.roundToInt
import com.tang.prm.ui.theme.GiftTypeStyle
import com.tang.prm.ui.theme.toStyle
import com.tang.prm.ui.theme.Dimens

// ═══════════════════════════════════════════════════════════════
//  CASSETTE TAPE DETAIL — 简洁现代化详情页
//  与列表页风格统一：浅色卡片 + 卷轴 + 清晰信息层级
// ═══════════════════════════════════════════════════════════════

// ── 与列表页统一的配色 ──


// ── 礼物分类（与GiftsScreen统一）──
private val GiftTypeEntries = GiftType.entries

@Composable
fun GiftDetailScreen(
    navController: NavController,
    giftId: Long,
    viewModel: GiftsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val gift by viewModel.getGiftFlow(giftId).collectAsState(initial = null)
    val giftRecord = remember(gift, uiState.availableContacts) {
        gift?.let { g ->
            val contact = uiState.availableContacts.find { it.id == g.contactId }
            GiftRecord(gift = g, contactName = contact?.name ?: "未知人物", contactAvatar = contact?.avatar)
        }
    }
    var selectedPhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val giftTypeData = remember(giftRecord) {
        giftRecord?.let { GiftTypeEntries.find { type -> type.name == it.giftType } ?: GiftType.OTHER }
    }

    if (showDeleteDialog) {
        DeleteConfirmDialog(
            title = "删除礼物",
            message = "确定要删除这个礼物吗？此操作不可撤销。",
            onConfirm = { giftRecord?.let { viewModel.deleteGift(it.id); navController.popBackStack() } },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("磁带详情", fontWeight = FontWeight.Medium, fontSize = 17.sp, color = OnSurface) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = OnSurface)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(com.tang.prm.ui.navigation.Screen.EditGift.createRoute(giftId))
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑", tint = OnSurface, modifier = Modifier.size(22.dp))
                    }
                    val isFavorite = uiState.favoriteGiftIds.contains(giftId)
                    IconButton(onClick = { giftRecord?.let { viewModel.toggleFavorite(it.id, it.giftName, it.contactName) } }) {
                        Icon(
                            if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "收藏",
                            tint = if (isFavorite) FavoriteGold else TextGray,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除", tint = SignalCoral, modifier = Modifier.size(22.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        if (giftRecord == null || giftTypeData == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TextGray)
            }
        } else {
            ModernDetailContent(
                gift = giftRecord,
                giftTypeData = giftTypeData,
                modifier = Modifier.padding(padding),
                onPhotoClick = { selectedPhotoUri = it }
            )
        }
    }

    if (selectedPhotoUri != null) {
        PhotoViewerDialog(photoUri = selectedPhotoUri!!, onDismiss = { selectedPhotoUri = null })
    }
}

// ═══════════════════════════════════════════════════════════════
//  MODERN DETAIL CONTENT — 简洁现代化内容
// ═══════════════════════════════════════════════════════════════
@Composable
private fun ModernDetailContent(
    gift: GiftRecord,
    giftTypeData: GiftType,
    modifier: Modifier = Modifier,
    onPhotoClick: (android.net.Uri) -> Unit
) {
    val dateFormat: (Long) -> String = { DateUtils.formatYearMonthDayDot(it) }
    val scrollState = rememberScrollState()

    val reelRotation by rememberContinuousRotation(cycleDuration = 10000)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // ═══════════════════════════════════════════════════════
        //  1. 主卡片（卷轴 + 名称 + 标签）
        // ═══════════════════════════════════════════════════════
        MainInfoCard(
            gift = gift,
            giftTypeData = giftTypeData,
            reelRotation = reelRotation
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ═══════════════════════════════════════════════════════
        //  2. 详细信息卡片
        // ═══════════════════════════════════════════════════════
        DetailInfoCard(gift = gift, giftTypeData = giftTypeData, dateFormat = dateFormat)

        Spacer(modifier = Modifier.height(16.dp))

        // ═══════════════════════════════════════════════════════
        //  3. 备注卡片
        // ═══════════════════════════════════════════════════════
        gift.description?.let { desc ->
            MemoCard(description = desc)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ═══════════════════════════════════════════════════════
        //  4. 照片卡片
        // ═══════════════════════════════════════════════════════
        if (gift.photos.isNotEmpty()) {
            PhotosCard(photos = gift.photos, onPhotoClick = onPhotoClick)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ── 主信息卡片（卷轴 + 名称）──
@Composable
private fun MainInfoCard(
    gift: GiftRecord,
    giftTypeData: GiftType,
    reelRotation: Float
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ── 卷轴区 ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.paddingCard)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.5.dp, TapeWindow, RoundedCornerShape(10.dp))
                    .padding(vertical = 20.dp, horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 左卷轴
                    DetailReel(color = giftTypeData.toStyle().color, rotation = reelRotation, size = 72.dp)

                    // 中间磁头
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(56.dp)
                                .height(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(TapeWindow)
                        .border(1.dp, TapeGearColor, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (gift.isSent) "REC" else "PLAY",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (gift.isSent) SignalCoral else SignalGreen
                            )
                        }
                    }

                    // 右卷轴
                    DetailReel(color = giftTypeData.toStyle().color, rotation = reelRotation + 180f, size = 72.dp)
                }
            }

            // ── 名称 + 标签 ──
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                // 标签行
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (gift.isSent) SignalAmber.copy(alpha = AnimationTokens.Alpha.subtle) else SignalGreen.copy(alpha = AnimationTokens.Alpha.subtle))
                            .border(0.5.dp, if (gift.isSent) SignalAmber.copy(alpha = 0.4f) else SignalGreen.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            if (gift.isSent) "A面 · 送出" else "B面 · 收到",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (gift.isSent) SignalAmber else SignalGreen
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        giftTypeData.displayName,
                        fontSize = 13.sp,
                        color = giftTypeData.toStyle().color,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 礼物名称
                Text(
                    text = gift.giftName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 32.sp
                )
            }
        }
    }
}

// ── 详细信息卡片 ──
@Composable
private fun DetailInfoCard(
    gift: GiftRecord,
    giftTypeData: GiftType,
    dateFormat: (Long) -> String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(Dimens.paddingCard)) {
            Text(
                "磁带信息",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextGray,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 信息行
            DetailRow(label = "关联人物", value = gift.contactName)
            DetailRow(label = "记录日期", value = dateFormat(gift.date))
            DetailRow(label = "礼物分类", value = giftTypeData.displayName)
            gift.occasion?.let { DetailRow(label = "赠送场合", value = it) }
            gift.location?.let { DetailRow(label = "赠送地点", value = it) }
            gift.amount?.let { DetailRow(label = "礼物金额", value = "¥${it.roundToInt()}") }
        }
    }
}

// ── 备注卡片 ──
@Composable
private fun MemoCard(description: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(Dimens.paddingCard)) {
            Text(
                "磁带备注",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = description,
                fontSize = 15.sp,
                color = OnSurface,
                lineHeight = 24.sp
            )
        }
    }
}

// ── 照片卡片 ──
@Composable
private fun PhotosCard(
    photos: List<android.net.Uri>,
    onPhotoClick: (android.net.Uri) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(Dimens.paddingCard)) {
            Text(
                "影像记录 (${photos.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextGray,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(photos) { photoUri ->
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "礼物照片",
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onPhotoClick(photoUri) },
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

// ── 信息行（标签: 值）──
@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextGray,
            modifier = Modifier.width(72.dp)
        )
        Text(
            text = ":",
            fontSize = 13.sp,
            color = TextGray,
            modifier = Modifier.width(12.dp)
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = OnSurface
        )
    }
}

// ── 详情页卷轴（与列表页统一风格）──
@Composable
private fun DetailReel(
    color: Color,
    rotation: Float,
    size: androidx.compose.ui.unit.Dp
) {
    val tapeGearColor = TapeGearColor
    val tapeGearDarkColor = TapeGearDarkColor
    val tapeWindow = TapeWindow
    Box(
        modifier = Modifier
            .size(size)
            .drawBehind {
                val s = this.size
                val cx = s.width / 2
                val cy = s.height / 2
                val r = s.width / 2 - 1

                // 外圈
                drawCircle(
                    color = tapeGearColor.copy(alpha = 0.4f),
                    radius = r,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.5f)
                )

                // 磁带卷（旋转）
                rotate(rotation, pivot = Offset(cx, cy)) {
                    for (i in 0 until 6) {
                        val angle = (i * 60f) * (Math.PI / 180f)
                        val innerR = r * 0.3f
                        val outerR = r * 0.85f
                        val sx = cx + kotlin.math.cos(angle).toFloat() * innerR
                        val sy = cy + kotlin.math.sin(angle).toFloat() * innerR
                        val ex = cx + kotlin.math.cos(angle).toFloat() * outerR
                        val ey = cy + kotlin.math.sin(angle).toFloat() * outerR
                        drawLine(
                            color = color.copy(alpha = AnimationTokens.Alpha.half),
                            start = Offset(sx, sy),
                            end = Offset(ex, ey),
                            strokeWidth = 2.5f
                        )
                    }

                    // 内圈磁带
                    drawCircle(
                        color = color.copy(alpha = 0.2f),
                        radius = r * 0.55f,
                        center = Offset(cx, cy),
                        style = Stroke(width = 5f)
                    )
                }

                // 中心轴
                drawCircle(
                    color = tapeGearDarkColor,
                    radius = r * 0.18f,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = tapeWindow,
                    radius = r * 0.08f,
                    center = Offset(cx, cy)
                )
            }
    )
}

// ═══════════════════════════════════════════════════════════════
//  PHOTO VIEWER DIALOG — 照片查看弹窗
// ═══════════════════════════════════════════════════════════════
@Composable
private fun PhotoViewerDialog(
    photoUri: android.net.Uri,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 4f)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                }
        ) {
            AsyncImage(
                model = photoUri,
                contentDescription = "放大查看照片",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    ),
                contentScale = ContentScale.Fit
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(Dimens.paddingCard)
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = AnimationTokens.Alpha.half), CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "关闭",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        IconButton(onClick = { scale = (scale - 0.5f).coerceAtLeast(0.5f); offsetX = 0f; offsetY = 0f }) {
                            Icon(Icons.Default.ZoomOut, contentDescription = "缩小", tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        IconButton(onClick = { scale = (scale + 0.5f).coerceAtMost(4f); offsetX = 0f; offsetY = 0f }) {
                            Icon(Icons.Default.ZoomIn, contentDescription = "放大", tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}
