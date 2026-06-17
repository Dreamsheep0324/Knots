@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.gifts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tang.prm.domain.model.GiftType
import com.tang.prm.ui.components.DeleteConfirmDialog
import com.tang.prm.ui.theme.*

// ═══════════════════════════════════════════════════════════════
//  CASSETTE TAPE DETAIL — 简洁现代化详情页
//  与列表页风格统一：浅色卡片 + 卷轴 + 清晰信息层级
// ═══════════════════════════════════════════════════════════════

@Composable
fun GiftDetailScreen(
    navController: NavController,
    giftId: Long,
    viewModel: GiftsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val gift by viewModel.getGiftFlow(giftId).collectAsStateWithLifecycle(initialValue = null)
    val giftRecord = remember(gift, uiState.data.availableContacts) {
        gift?.let { g ->
            val contact = uiState.data.availableContacts.find { it.id == g.contactId }
            GiftRecord(gift = g, contactName = contact?.name ?: "未知人物", contactAvatar = contact?.avatar)
        }
    }
    var selectedPhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val giftTypeData: GiftType = remember(giftRecord) {
        giftRecord?.giftType ?: GiftType.OTHER
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
                        navController.navigate(com.tang.prm.ui.navigation.EditGiftRoute(giftId))
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑", tint = OnSurface, modifier = Modifier.size(22.dp))
                    }
                    val isFavorite = uiState.data.favoriteGiftIds.contains(giftId)
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

    selectedPhotoUri?.let { uri ->
        PhotoViewerDialog(photoUri = uri, onDismiss = { selectedPhotoUri = null })
    }
}
