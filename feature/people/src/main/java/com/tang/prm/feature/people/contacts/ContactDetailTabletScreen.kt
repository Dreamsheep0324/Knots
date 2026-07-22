@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.people.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.tang.prm.domain.model.IntimacyTier
import com.tang.prm.feature.people.contacts.tablet.GalleryBody
import com.tang.prm.feature.people.contacts.tablet.GalleryClicks
import com.tang.prm.feature.people.contacts.tablet.GalleryHero
import com.tang.prm.feature.people.contacts.tablet.GalleryTopBar
import com.tang.prm.ui.components.DeleteConfirmDialog
import com.tang.prm.ui.navigation.AnniversaryDetailRoute
import com.tang.prm.ui.navigation.ChatDetailRoute
import com.tang.prm.ui.navigation.ContactDetailRoute
import com.tang.prm.ui.navigation.EditContactRoute
import com.tang.prm.ui.navigation.EventDetailRoute
import com.tang.prm.ui.navigation.GiftDetailRoute

// ═══════════════════════════════════════════════════════════════
// 平板人物详情 — 方案A 经典画廊精装
// ═══════════════════════════════════════════════════════════════

@Composable
fun ContactDetailTabletScreen(
    contactId: Long,
    navController: NavController,
    viewModel: ContactDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val contact = uiState.data.contact
    var detailThoughtId by remember { mutableStateOf<Long?>(null) }
    val detailThought = detailThoughtId?.let { id -> uiState.data.thoughts.find { it.id == id } }

    LaunchedEffect(contactId) {
        viewModel.setContactId(contactId)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ContactDetailEvent.ContactDeleted -> navController.popBackStack()
            }
        }
    }

    if (uiState.dialog.showDeleteDialog) {
        DeleteConfirmDialog(
            title = "删除人物",
            message = "确定要删除 \"${contact?.name ?: ""}\" 吗？此操作不可撤销。",
            onConfirm = { viewModel.deleteContact() },
            onDismiss = { viewModel.hideDeleteDialog() }
        )
    }

    detailThought?.let { thought ->
        ContactThoughtDetailDialog(
            thought = thought,
            contactName = contact?.name,
            contactAvatar = contact?.avatar,
            isFavorite = thought.id in uiState.data.favoriteIds,
            onDismiss = { detailThoughtId = null },
            onToggleFavorite = { viewModel.toggleFavorite(thought.id, thought.content) },
            onToggleTodo = { viewModel.toggleTodoDone(thought) },
            onDelete = { viewModel.deleteThought(thought.id); detailThoughtId = null }
        )
    }

    if (contact == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.data.isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 3.dp)
            } else {
                // 加载失败或人物已被删除：显示错误提示 + 返回按钮
                ContactNotFoundState(
                    onBack = { navController.popBackStack() }
                )
            }
        }
        return
    }

    val tier = IntimacyTier.of(contact.intimacyScore)
    val tierColor = Color(tier.colorValue)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .systemBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // ── 顶部栏 ──
            item {
                GalleryTopBar(
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(EditContactRoute(contactId)) },
                    onDelete = { viewModel.showDeleteDialog() }
                )
            }

            // ── Hero 区 ──
            item {
                GalleryHero(
                    contact = contact,
                    tier = tier,
                    tierColor = tierColor,
                    relationshipTypes = uiState.data.relationshipTypes
                )
            }

            // ── 四栏内容区 ──
            item {
                GalleryBody(
                    contact = contact,
                    tierColor = tierColor,
                    events = uiState.data.events,
                    conversations = uiState.data.conversations,
                    anniversaries = uiState.data.anniversaries,
                    gifts = uiState.data.gifts,
                    thoughts = uiState.data.thoughts,
                    eventTypes = uiState.data.eventTypes,
                    personRelations = uiState.data.personRelations,
                    personRelationTypes = uiState.data.personRelationTypes,
                    clicks = GalleryClicks(
                        onEventClick = { id -> navController.navigate(EventDetailRoute(id)) },
                        onAnniversaryClick = { id -> navController.navigate(AnniversaryDetailRoute(id)) },
                        onGiftClick = { id -> navController.navigate(GiftDetailRoute(id)) },
                        onThoughtClick = { id -> detailThoughtId = id },
                        onConversationClick = { id -> navController.navigate(ChatDetailRoute(id)) },
                        onPersonRelationClick = { id -> navController.navigate(ContactDetailRoute(id)) }
                    )
                )
            }
        }
    }
}
