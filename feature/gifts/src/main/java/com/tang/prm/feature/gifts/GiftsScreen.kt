@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.gifts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tang.prm.domain.model.GiftType
import com.tang.prm.ui.components.EmptyState
import com.tang.prm.ui.navigation.AddGiftRoute
import com.tang.prm.ui.navigation.GiftDetailRoute
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tang.prm.ui.theme.*

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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedTypeFilter by remember { mutableStateOf<GiftType?>(null) }

    val filteredGifts = remember(uiState.data.gifts, uiState.data.filterType, uiState.data.selectedContactId, selectedTypeFilter) {
        uiState.data.gifts
            .filter { gift ->
                when (uiState.data.filterType) {
                    "sent" -> gift.isSent
                    "received" -> !gift.isSent
                    else -> true
                }
            }
            .filter { gift -> uiState.data.selectedContactId?.let { it == gift.contactId } ?: true }
            .filter { gift -> selectedTypeFilter?.let { it == gift.giftType } ?: true }
    }

    val sentCount = uiState.data.gifts.count { it.isSent }
    val receivedCount = uiState.data.gifts.count { !it.isSent }

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
                    IconButton(onClick = { navController.navigate(AddGiftRoute) }) {
                        Icon(Icons.Default.Add, contentDescription = "添加磁带", tint = OnSurface, modifier = Modifier.size(22.dp))
                    }
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "筛选", tint = OnSurface, modifier = Modifier.size(22.dp))
                    }
                    if (uiState.data.selectedContactId != null) {
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
                totalCount = uiState.data.gifts.size,
                sentCount = sentCount,
                receivedCount = receivedCount,
                typeBreakdown = uiState.data.gifts.groupBy { it.giftType.name }.mapValues { it.value.size }
            )

            TypeFilterStrip(
                gifts = uiState.data.gifts,
                selectedType = selectedTypeFilter,
                onTypeSelect = { selectedTypeFilter = it }
            )

            if (filteredGifts.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.CardGiftcard,
                    title = "还没有礼物",
                    actionLabel = "添加礼物",
                    onAction = { navController.navigate(AddGiftRoute) }
                )
            } else {
                CassetteTapeRack(
                    gifts = filteredGifts,
                    onTapeClick = { gift ->
                        navController.navigate(GiftDetailRoute(gift.id))
                    }
                )
            }
        }
    }

    if (showFilterDialog) {
        GiftFilterDialog(
            contacts = uiState.data.availableContacts,
            selectedContactId = uiState.data.selectedContactId,
            onContactSelect = { viewModel.filterByContact(it) },
            onDismiss = { showFilterDialog = false }
        )
    }
}
