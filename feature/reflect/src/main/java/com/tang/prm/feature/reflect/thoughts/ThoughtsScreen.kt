@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.reflect.thoughts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tang.prm.domain.model.ThoughtType
import com.tang.prm.ui.components.EmptyState
import com.tang.prm.ui.components.IconGradientSectionHeader
import com.tang.prm.ui.theme.SignalAmber

@Composable
fun ThoughtsScreen(
    onBack: () -> Unit,
    isTabletLayout: Boolean = false,
    viewModel: ThoughtsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var detailThoughtId by remember { mutableStateOf<Long?>(null) }
    val detailThought = detailThoughtId?.let { id -> uiState.data.filteredThoughts.find { it.id == id } }
    // U-2 修复：删除前确认，避免误删不可恢复
    var pendingDeleteId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SignalAmber)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "想法",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showAddDialog(ThoughtType.MURMUR) }) {
                        Icon(Icons.Default.Add, contentDescription = "添加")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .then(if (isTabletLayout) Modifier.padding(horizontal = 48.dp) else Modifier)
        ) {
            ThoughtLevelBanner(uiState = uiState)

            if (uiState.data.contactThoughts.isNotEmpty()) {
                IconGradientSectionHeader(
                    icon = Icons.Default.Person,
                    iconColor = SignalAmber,
                    title = "关联人物",
                    action = null,
                    onActionClick = null,
                    modifier = Modifier.padding(start = 24.dp)
                )
                ContactStoriesRow(
                    contactThoughts = uiState.data.contactThoughts,
                    selectedContactId = uiState.data.selectedContactId,
                    onContactClick = { contactId ->
                        viewModel.onContactFilterSelected(contactId)
                    }
                )
            }

            FilterTabRow(
                selectedFilter = uiState.data.selectedFilter,
                onFilterSelected = { viewModel.onFilterSelected(it) },
                counts = mapOf(
                    "all" to (uiState.gamification?.totalCount ?: 0),
                    "friend" to (uiState.gamification?.friendCount ?: 0),
                    "plan" to (uiState.gamification?.planCount ?: 0),
                    "murmur" to (uiState.gamification?.murmurCount ?: 0),
                    "todo" to (uiState.gamification?.todoTotalCount ?: 0)
                )
            )

            if (uiState.data.filteredThoughts.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Lightbulb,
                    title = when (uiState.data.selectedFilter) {
                        "friend" -> "还没有关于伙伴的想法"
                        "plan" -> "还没有计划"
                        "murmur" -> "还没有碎碎念"
                        "todo" -> "没有待办事项"
                        else -> "还没有想法"
                    },
                    description = "点击 + 记录你的第一个想法"
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp
                    )
                ) {
                    items(uiState.data.filteredThoughts, key = { it.id }) { thought ->
                        ThoughtFeedCard(
                            thought = thought,
                            contactName = viewModel.getContactName(thought.contactId),
                            contactAvatar = viewModel.getContactAvatar(thought.contactId),
                            isFavorite = thought.id in uiState.data.favoriteIds,
                            exp = viewModel.thoughtExp(thought),
                            onToggleTodo = { viewModel.toggleTodoDone(thought) },
                            onEdit = { viewModel.showEditDialog(thought) },
                            onDelete = { pendingDeleteId = thought.id },
                            onToggleFavorite = { viewModel.toggleFavorite(thought.id, thought.content) },
                            onCardClick = { detailThoughtId = thought.id }
                        )
                    }
                }
            }
        }
    }

    detailThought?.let { thought ->
        ThoughtDetailDialog(
            thought = thought,
            contactName = viewModel.getContactName(thought.contactId),
            contactAvatar = viewModel.getContactAvatar(thought.contactId),
            isFavorite = thought.id in uiState.data.favoriteIds,
            onDismiss = { detailThoughtId = null },
            onToggleFavorite = { viewModel.toggleFavorite(thought.id, thought.content) },
            onToggleTodo = { viewModel.toggleTodoDone(thought) },
            onEdit = { viewModel.showEditDialog(thought); detailThoughtId = null },
            onDelete = { pendingDeleteId = thought.id; detailThoughtId = null }
        )
    }

    if (uiState.dialog.showDialog) {
        ThoughtDialog(
            thought = uiState.dialog.editingThought,
            dialogType = uiState.dialog.dialogType,
            contacts = uiState.data.contacts,
            onDismiss = { viewModel.dismissDialog() },
            onConfirm = { content, type, contactId, isPrivate, isTodo, dueDate ->
                val editing = uiState.dialog.editingThought
                if (editing == null) {
                    viewModel.insertThought(content, type, contactId, isPrivate, isTodo, dueDate)
                } else {
                    viewModel.updateThought(
                        editing.copy(
                            content = content,
                            type = type,
                            contactId = contactId,
                            isPrivate = isPrivate,
                            isTodo = isTodo,
                            dueDate = dueDate
                        )
                    )
                }
            }
        )
    }

    // U-2 修复：删除确认对话框
    pendingDeleteId?.let { deleteId ->
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text("删除想法") },
            text = { Text("确定要删除这个想法吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteThought(deleteId)
                    pendingDeleteId = null
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) {
                    Text("取消")
                }
            }
        )
    }
}


