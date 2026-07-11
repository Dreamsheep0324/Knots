@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tang.prm.ui.components.DeleteConfirmDialog
import com.tang.prm.ui.navigation.EditChatRoute
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tang.prm.ui.theme.FavoriteGold

/**
 * 平板双栏模式：对话详情内容（无 Scaffold/TopBar）。
 * 供 ChatScreen 的右栏嵌入。
 */
@Composable
fun ChatDetailContent(
    eventId: Long,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val contact = uiState.data.event?.participants?.firstOrNull()
    val intimacyColor = contact?.let { getIntimacyColor(it.intimacyScore) } ?: MaterialTheme.colorScheme.primary

    LaunchedEffect(eventId) {
        viewModel.setEventId(eventId)
    }

    if (uiState.dialog.showDeleteConfirm) {
        DeleteConfirmDialog(
            title = "删除对话",
            message = "确定要删除这条对话记录吗？此操作不可撤销。",
            onConfirm = { viewModel.deleteEvent(); viewModel.hideDeleteConfirm(); onDelete() },
            onDismiss = { viewModel.hideDeleteConfirm() }
        )
    }

    uiState.data.event?.let { event ->
        val dialogues = parseDialogue(event, contact?.name)

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "编辑", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { viewModel.toggleFavorite() }) {
                    Icon(
                        if (uiState.data.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "收藏",
                        tint = if (uiState.data.isFavorite) FavoriteGold else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { viewModel.showDeleteConfirm() }) {
                    Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
                }
            }

            ScriptHeader(event = event, contact = contact, intimacyColor = intimacyColor)

            if (dialogues.isNotEmpty()) {
                DialogueScriptCard(
                    dialogues = dialogues,
                    contactAvatar = contact?.avatar,
                    accentColor = intimacyColor,
                    modifier = Modifier.weight(1f)
                )
            }

            event.remarks?.let { remarks ->
                RemarksCard(remarks = remarks, accentColor = intimacyColor)
            }
        }
    } ?: run {
        if (uiState.data.isLoading) {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 3.dp)
            }
        }
    }
}

@Composable
fun ChatDetailScreen(
    eventId: Long,
    navController: NavController,
    viewModel: ChatDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val contact = uiState.data.event?.participants?.firstOrNull()
    val intimacyColor = contact?.let { getIntimacyColor(it.intimacyScore) } ?: MaterialTheme.colorScheme.primary

    if (uiState.dialog.showDeleteConfirm) {
        DeleteConfirmDialog(
            title = "删除对话",
            message = "确定要删除这条对话记录吗？此操作不可撤销。",
            onConfirm = { viewModel.deleteEvent(); viewModel.hideDeleteConfirm(); navController.popBackStack() },
            onDismiss = { viewModel.hideDeleteConfirm() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        contact?.name ?: "对话详情",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(EditChatRoute(eventId)) }) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            if (uiState.data.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "收藏",
                            tint = if (uiState.data.isFavorite) FavoriteGold else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { viewModel.showDeleteConfirm() }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        uiState.data.event?.let { event ->
            val dialogues = parseDialogue(event, contact?.name)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ScriptHeader(event = event, contact = contact, intimacyColor = intimacyColor)

                if (dialogues.isNotEmpty()) {
                    DialogueScriptCard(
                        dialogues = dialogues,
                        contactAvatar = contact?.avatar,
                        accentColor = intimacyColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                event.remarks?.let { remarks ->
                    RemarksCard(remarks = remarks, accentColor = intimacyColor)
                }
            }
        } ?: run {
            if (uiState.data.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 3.dp)
                }
            }
        }
    }
}
