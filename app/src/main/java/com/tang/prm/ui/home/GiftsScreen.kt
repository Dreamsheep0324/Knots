@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.ui.home

import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.Gift
import com.tang.prm.domain.model.GiftType
import com.tang.prm.ui.components.ContactAvatar
import com.tang.prm.ui.navigation.Screen
import com.tang.prm.ui.theme.*
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.animation.primitives.rememberBreathingPulse
import com.tang.prm.ui.theme.GiftTypeStyle
import com.tang.prm.ui.theme.toStyle
import com.tang.prm.ui.theme.DialogDefaults
import com.tang.prm.domain.model.AppStrings

// ═══════════════════════════════════════════════════════════════
//  RETRO CASSETTE TAPE LIBRARY — 复古录音磁带库
//  浅色模式 · 真实复古录音磁带外观
// ═══════════════════════════════════════════════════════════════

// ── 软件整体配色系统（与HomeScreen/Theme统一）──

// ── 礼物分类 ──

// ═══════════════════════════════════════════════════════════════
//  MAIN SCREEN
// ═══════════════════════════════════════════════════════════════
@Composable
fun GiftsScreen(
    navController: NavController,
    viewModel: GiftsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedTypeFilter by remember { mutableStateOf<GiftType?>(null) }

    val filteredGifts = remember(uiState.gifts, uiState.filterType, uiState.selectedContactId, selectedTypeFilter) {
        uiState.gifts
            .filter { gift ->
                when (uiState.filterType) {
                    "sent" -> gift.isSent
                    "received" -> !gift.isSent
                    else -> true
                }
            }
            .filter { gift -> uiState.selectedContactId?.let { it == gift.contactId } ?: true }
            .filter { gift -> selectedTypeFilter?.let { it.name == gift.giftType } ?: true }
    }

    val sentCount = uiState.gifts.count { it.isSent }
    val receivedCount = uiState.gifts.count { !it.isSent }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SignalElectric)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "磁带收藏室",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = OnSurface, modifier = Modifier.size(22.dp))
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.AddGift.route) }) {
                        Icon(Icons.Default.Add, contentDescription = "添加磁带", tint = OnSurface, modifier = Modifier.size(22.dp))
                    }
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "筛选", tint = OnSurface, modifier = Modifier.size(22.dp))
                    }
                    if (uiState.selectedContactId != null) {
                        TextButton(onClick = { viewModel.clearContactFilter() }) {
                            Text("清除", color = SignalElectric, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = { },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.surface)
        ) {
            RetroControlPanel(
                totalCount = uiState.gifts.size,
                sentCount = sentCount,
                receivedCount = receivedCount,
                typeBreakdown = uiState.gifts.groupBy { it.giftType }.mapValues { it.value.size }
            )

            TypeFilterStrip(
                gifts = uiState.gifts,
                selectedType = selectedTypeFilter,
                onTypeSelect = { selectedTypeFilter = it }
            )

            if (filteredGifts.isEmpty()) {
                EmptyCassetteState(onAddClick = { navController.navigate(Screen.AddGift.route) })
            } else {
                CassetteTapeRack(
                    gifts = filteredGifts,
                    onTapeClick = { gift ->
                        navController.navigate(Screen.GiftDetail.createRoute(gift.id))
                    }
                )
            }
        }
    }

    if (showFilterDialog) {
        GiftFilterDialog(
            contacts = uiState.availableContacts,
            selectedContactId = uiState.selectedContactId,
            onContactSelect = { viewModel.filterByContact(it) },
            onDismiss = { showFilterDialog = false }
        )
    }
}

// ═══════════════════════════════════════════════════════════════
//  RETRO CONTROL PANEL — 复古控制面板
// ═══════════════════════════════════════════════════════════════
@Composable
private fun RetroControlPanel(
    totalCount: Int,
    sentCount: Int,
    receivedCount: Int,
    typeBreakdown: Map<String, Int>
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SignalGreen))
                    Text("播放机就绪", fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SignalGreen)
                }
                Text(
                    "CASSETTE DECK",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.sp,
                    color = TextGray.copy(alpha = AnimationTokens.Alpha.visible),
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RetroMeter(modifier = Modifier.weight(1f), label = "A面 · 送出", count = sentCount, total = totalCount.coerceAtLeast(1), color = SignalAmber)
                RetroMeter(modifier = Modifier.weight(1f), label = "B面 · 收到", count = receivedCount, total = totalCount.coerceAtLeast(1), color = SignalGreen)
            }

            Spacer(modifier = Modifier.height(8.dp))
            RetroSpectrum(typeBreakdown = typeBreakdown)
        }
    }
}

@Composable
private fun RetroMeter(
    modifier: Modifier = Modifier,
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    val progress = (count.toFloat() / total).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(AnimationTokens.Duration.dramatic), label = "meter")

    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TextGray)
            Text(String.format("%02d", count), fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(2.dp)).background(DividerLight)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color.copy(alpha = AnimationTokens.Alpha.strong))
            )
        }
    }
}

@Composable
private fun RetroSpectrum(typeBreakdown: Map<String, Int>) {
    if (typeBreakdown.isEmpty()) return
    val maxCount = typeBreakdown.values.maxOrNull() ?: 1

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.Bottom) {
        GiftType.entries.forEach { type ->
            val count = typeBreakdown[type.name] ?: 0
            val heightFraction = (count.toFloat() / maxCount).coerceIn(0.05f, 1f)
            val animatedHeight by animateFloatAsState(targetValue = heightFraction, animationSpec = tween(800), label = "spec")
            val style = type.toStyle()

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text(if (count > 0) "$count" else "", fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = style.color, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(1.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((animatedHeight * 20).dp)
                        .clip(RoundedCornerShape(topStart = 1.dp, topEnd = 1.dp))
                        .background(style.color.copy(alpha = if (count > 0) 0.6f else 0.1f))
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(type.displayName.first().toString(), fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = if (count > 0) style.color else TextGray.copy(alpha = 0.3f))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  TYPE FILTER STRIP
// ═══════════════════════════════════════════════════════════════
@Composable
private fun TypeFilterStrip(
    gifts: List<GiftRecord>,
    selectedType: GiftType?,
    onTypeSelect: (GiftType?) -> Unit
) {
    val usedTypes = remember(gifts) {
        val used = gifts.map { it.giftType }.distinct()
        listOf(null) + GiftType.entries.filter { it.name in used }
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(usedTypes, key = { it?.name ?: "all" }) { type ->
            val isSelected = selectedType == type
            val typeColor = type?.toStyle()?.color ?: SignalElectric
            val count = if (type == null) gifts.size else gifts.count { it.giftType == type.name }

            Surface(
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onTypeSelect(if (isSelected) null else type) },
                color = if (isSelected) typeColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
                border = if (isSelected) BorderStroke(1.dp, typeColor.copy(alpha = AnimationTokens.Alpha.half)) else BorderStroke(0.5.dp, CardBorder.copy(alpha = AnimationTokens.Alpha.half))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (type != null) {
                        Icon(type.toStyle().icon, contentDescription = null, tint = if (isSelected) typeColor else TextGray, modifier = Modifier.size(12.dp))
                    } else {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = if (isSelected) SignalElectric else TextGray, modifier = Modifier.size(12.dp))
                    }
                    Text(
                        text = if (type != null) type.displayName else AppStrings.Tabs.ALL,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) typeColor else TextGray
                    )
                    Text(
                        text = String.format("%02d", count),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = if (isSelected) typeColor else TextGray.copy(alpha = AnimationTokens.Alpha.visible)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  CASSETTE TAPE RACK — 磁带架
// ═══════════════════════════════════════════════════════════════
@Composable
private fun CassetteTapeRack(
    gifts: List<GiftRecord>,
    onTapeClick: (GiftRecord) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(gifts, key = { it.id }) { gift ->
            CassetteTapeCard(
                gift = gift,
                index = gifts.indexOf(gift) + 1,
                onClick = { onTapeClick(gift) }
            )
        }
        item { Spacer(modifier = Modifier.height(96.dp)) }
    }
}

// ═══════════════════════════════════════════════════════════════
//  EMPTY CASSETTE STATE — 空磁带状态
// ═══════════════════════════════════════════════════════════════
@Composable
private fun EmptyCassetteState(onAddClick: () -> Unit) {
    val blinkAlpha by rememberBreathingPulse(
        minAlpha = 0.3f, maxAlpha = 1f,
        cycleDuration = 1500
    )
    val cardBorderColor = CardBorder.copy(alpha = AnimationTokens.Alpha.visible)
    val dividerColor = Divider.copy(alpha = AnimationTokens.Alpha.half)
    val tapeWindow = TapeWindow
    val tapeGearColor = TapeGearColor

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(110.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = AnimationTokens.Alpha.half))
                    .border(1.5.dp, cardBorderColor, RoundedCornerShape(8.dp))
                    .drawBehind {
                        val w = size.width
                        val h = size.height

                        drawRoundRect(
                            color = dividerColor,
                            topLeft = Offset(w * 0.08f, h * 0.08f),
                            size = Size(w * 0.84f, h * 0.35f),
                            cornerRadius = CornerRadius(3.dp.toPx()),
                            style = Stroke(width = 1f)
                        )

                        drawRoundRect(
                            color = tapeWindow.copy(alpha = 0.4f),
                            topLeft = Offset(w * 0.15f, h * 0.5f),
                            size = Size(w * 0.7f, h * 0.3f),
                            cornerRadius = CornerRadius(3.dp.toPx()),
                            style = Stroke(width = 1f)
                        )

                        drawCircle(
                            color = tapeGearColor.copy(alpha = 0.3f),
                            radius = h * 0.12f,
                            center = Offset(w * 0.3f, h * 0.65f),
                            style = Stroke(width = 1f)
                        )
                        drawCircle(
                            color = tapeGearColor.copy(alpha = 0.3f),
                            radius = h * 0.12f,
                            center = Offset(w * 0.7f, h * 0.65f),
                            style = Stroke(width = 1f)
                        )

                        var x = w * 0.15f
                        while (x < w * 0.4f) {
                            drawCircle(
                                color = tapeGearColor.copy(alpha = 0.2f),
                                radius = 2f,
                                center = Offset(x, h * 0.9f)
                            )
                            x += 10f
                        }
                        x = w * 0.6f
                        while (x < w * 0.85f) {
                            drawCircle(
                                color = tapeGearColor.copy(alpha = 0.2f),
                                radius = 2f,
                                center = Offset(x, h * 0.9f)
                            )
                            x += 10f
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "EMPTY",
                    fontFamily = PixelFontFamily,
                    fontSize = 14.sp,
                    color = SignalCoral.copy(alpha = blinkAlpha)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "磁带库为空",
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = TextGray.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "放入第一盘磁带",
                fontSize = 12.sp,
                color = TextGray.copy(alpha = AnimationTokens.Alpha.half)
            )

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("添加磁带", color = Color.White, fontSize = 13.sp)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  FILTER & DIALOGS
// ═══════════════════════════════════════════════════════════════
@Composable
private fun GiftFilterDialog(
    contacts: List<Contact>,
    selectedContactId: Long?,
    onContactSelect: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DialogDefaults.containerColor,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        title = { Text("筛选磁带", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) },
        text = {
            Column {
                Text(text = "按人物筛选", style = MaterialTheme.typography.labelMedium, color = TextGray, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { onContactSelect(null) },
                    color = if (selectedContactId == null) SignalElectric.copy(alpha = 0.1f) else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = if (selectedContactId == null) SignalElectric else TextGray, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "全部磁带", color = if (selectedContactId == null) SignalElectric else MaterialTheme.colorScheme.onSurface)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(contacts, key = { it.id }) { contact ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).clip(RoundedCornerShape(8.dp)).clickable { onContactSelect(contact.id) },
                            color = if (selectedContactId == contact.id) SignalElectric.copy(alpha = 0.1f) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                ContactAvatar(avatar = contact.avatar, name = contact.name, size = 24)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = contact.name, color = if (selectedContactId == contact.id) SignalElectric else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("完成", color = SignalElectric) }
        }
    )
}
