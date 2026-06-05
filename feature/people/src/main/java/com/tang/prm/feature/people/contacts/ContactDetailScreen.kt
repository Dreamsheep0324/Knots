package com.tang.prm.feature.people.contacts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.tang.prm.domain.model.AppStrings
import com.tang.prm.ui.components.DeleteConfirmDialog
import com.tang.prm.ui.navigation.EditContactRoute
import com.tang.prm.ui.navigation.EventDetailRoute
import com.tang.prm.ui.navigation.AnniversaryDetailRoute
import com.tang.prm.ui.navigation.GiftDetailRoute
import com.tang.prm.ui.navigation.ChatDetailRoute
import com.tang.prm.ui.theme.Primary

@Composable
fun ContactDetailScreen(
    contactId: Long,
    navController: NavController,
    viewModel: ContactDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val contact = uiState.data.contact
    val layoutDirection = LocalLayoutDirection.current
    var detailThoughtId by remember { mutableStateOf<Long?>(null) }
    val detailThought = detailThoughtId?.let { id -> uiState.data.thoughts.find { it.id == id } }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        contact?.let {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = padding.calculateStartPadding(layoutDirection),
                        end = padding.calculateEndPadding(layoutDirection),
                        bottom = padding.calculateBottomPadding()
                    ),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    ProfileHeader(
                        contact = it,
                        relationshipTypes = uiState.data.relationshipTypes,
                        onBack = { navController.popBackStack() },
                        onEdit = { navController.navigate(EditContactRoute(contactId)) },
                        onDelete = { viewModel.showDeleteDialog() }
                    )
                }
                item {
                    TabSection(
                        selectedTab = uiState.dialog.selectedTab,
                        onTabSelected = viewModel::onTabSelected,
                        tabs = listOf(AppStrings.ContactDetail.PROFILE, AppStrings.ContactDetail.EVENTS, AppStrings.ContactDetail.ANNIVERSARY, AppStrings.ContactDetail.GIFTS, AppStrings.ContactDetail.THOUGHTS, AppStrings.ContactDetail.CHATS)
                    )
                }
                item {
                    when (uiState.dialog.selectedTab) {
                        0 -> ProfileContent(contact = it, uiState = uiState)
                        1 -> EventsContent(events = uiState.data.events, eventTypes = uiState.data.eventTypes, onEventClick = { id -> navController.navigate(EventDetailRoute(id)) })
                        2 -> AnniversariesContent(anniversaries = uiState.data.anniversaries, onAnniversaryClick = { id -> navController.navigate(AnniversaryDetailRoute(id)) })
                        3 -> GiftsContent(gifts = uiState.data.gifts, onGiftClick = { id -> navController.navigate(GiftDetailRoute(id)) })
                        4 -> ThoughtsContent(thoughts = uiState.data.thoughts, onThoughtClick = { id -> detailThoughtId = id })
                        5 -> ChatContent(conversations = uiState.data.conversations, onConversationClick = { id -> navController.navigate(ChatDetailRoute(id)) })
                    }
                }
            }
        } ?: run {
            if (uiState.data.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary, strokeWidth = 3.dp)
                }
            }
        }
    }

    if (uiState.dialog.showDeleteDialog) {
        DeleteConfirmDialog(
            title = "删除人物",
            message = "确定要删除 \"${contact?.name ?: ""}\" 吗？此操作不可撤销。",
            onConfirm = { viewModel.deleteContact { navController.popBackStack() } },
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
}
