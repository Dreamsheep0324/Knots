package com.tang.prm.ui.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tang.prm.domain.model.Thought
import com.tang.prm.domain.model.ThoughtType
import com.tang.prm.ui.components.DeleteConfirmDialog
import com.tang.prm.ui.navigation.Screen
import com.tang.prm.domain.model.AppStrings
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.theme.*
import com.tang.prm.util.DateUtils

private val TabIcons = listOf(
    Icons.Default.Person, Icons.Default.Event, Icons.Default.Cake,
    Icons.Default.CardGiftcard, Icons.Default.Lightbulb, Icons.AutoMirrored.Filled.Chat
)

private val TabColors = listOf(
    Color(0xFF42A5F5), Color(0xFF66BB6A), Color(0xFFF43F5E),
    Color(0xFFF97316), Color(0xFFEAB308), Color(0xFF9575CD)
)

private val ThoughtTypeColor = mapOf(
    ThoughtType.FRIEND to SignalAmber,
    ThoughtType.PLAN to SignalSky,
    ThoughtType.MURMUR to SignalPurple
)

private val ThoughtTypeBg = mapOf(
    ThoughtType.FRIEND to SignalAmber.copy(alpha = AnimationTokens.Alpha.faint),
    ThoughtType.PLAN to SignalSky.copy(alpha = AnimationTokens.Alpha.faint),
    ThoughtType.MURMUR to SignalPurple.copy(alpha = AnimationTokens.Alpha.faint)
)

private val ThoughtTypeIcon = mapOf(
    ThoughtType.FRIEND to Icons.Default.Group,
    ThoughtType.PLAN to Icons.Default.TaskAlt,
    ThoughtType.MURMUR to Icons.Default.Lightbulb
)

private val ThoughtTypeLabel = mapOf(
    ThoughtType.FRIEND to "伙伴",
    ThoughtType.PLAN to "计划",
    ThoughtType.MURMUR to "碎碎念"
)

@Composable
fun ContactDetailScreen(
    contactId: Long,
    navController: NavController,
    viewModel: ContactDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val contact = uiState.contact
    val layoutDirection = LocalLayoutDirection.current
    var detailThoughtId by remember { mutableStateOf<Long?>(null) }
    val detailThought = detailThoughtId?.let { id -> uiState.thoughts.find { it.id == id } }

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
                        onBack = { navController.popBackStack() },
                        onEdit = { navController.navigate(Screen.EditContact.createRoute(contactId)) },
                        onDelete = { viewModel.showDeleteDialog() }
                    )
                }
                item {
                    TabSection(
                        selectedTab = uiState.selectedTab,
                        onTabSelected = viewModel::onTabSelected,
                        tabs = listOf(AppStrings.ContactDetail.PROFILE, AppStrings.ContactDetail.EVENTS, AppStrings.ContactDetail.ANNIVERSARY, AppStrings.ContactDetail.GIFTS, AppStrings.ContactDetail.THOUGHTS, AppStrings.ContactDetail.CHATS)
                    )
                }
                item {
                    when (uiState.selectedTab) {
                        0 -> ProfileContent(contact = it, uiState = uiState)
                        1 -> EventsContent(events = uiState.events, eventTypes = uiState.eventTypes, onEventClick = { id -> navController.navigate(Screen.EventDetail.createRoute(id)) })
                        2 -> AnniversariesContent(anniversaries = uiState.anniversaries, onAnniversaryClick = { id -> navController.navigate(Screen.AnniversaryDetail.createRoute(id)) })
                        3 -> GiftsContent(gifts = uiState.gifts, onGiftClick = { id -> navController.navigate(Screen.GiftDetail.createRoute(id)) })
                        4 -> ThoughtsContent(thoughts = uiState.thoughts, onThoughtClick = { id -> detailThoughtId = id })
                        5 -> ChatContent(conversations = uiState.conversations, onConversationClick = { id -> navController.navigate(Screen.ChatDetail.createRoute(id)) })
                    }
                }
            }
        } ?: run {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary, strokeWidth = 3.dp)
                }
            }
        }
    }

    if (uiState.showDeleteDialog) {
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
            isFavorite = thought.id in uiState.favoriteIds,
            onDismiss = { detailThoughtId = null },
            onToggleFavorite = { viewModel.toggleFavorite(thought.id, thought.content) },
            onToggleTodo = { viewModel.toggleTodoDone(thought) },
            onDelete = { viewModel.deleteThought(thought.id); detailThoughtId = null }
        )
    }
}

@Composable
private fun ContactThoughtDetailDialog(
    thought: Thought,
    contactName: String?,
    contactAvatar: String?,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleTodo: () -> Unit,
    onDelete: () -> Unit
) {
    val typeColor = ThoughtTypeColor[thought.type] ?: SignalAmber
    val typeBg = ThoughtTypeBg[thought.type] ?: SignalAmber.copy(alpha = AnimationTokens.Alpha.faint)
    val typeIcon = ThoughtTypeIcon[thought.type] ?: Icons.Default.Lightbulb
    val typeLabel = ThoughtTypeLabel[thought.type] ?: "想法"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DialogDefaults.containerColor,
            tonalElevation = 0.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = typeBg,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                typeIcon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = typeColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            typeLabel,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 1.dp)
                        ) {
                            Text(
                                DateUtils.formatRelativeTime(thought.createdAt),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (thought.isPrivate) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "私密",
                                    modifier = Modifier.size(10.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half)
                                )
                            }
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "关闭",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        thought.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (thought.isTodo && thought.isDone) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        lineHeight = 22.sp
                    )

                    if (thought.isTodo) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (thought.isDone) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (thought.isDone) SignalGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (thought.isDone) "已完成" else "待办中",
                                fontSize = 12.sp,
                                color = if (thought.isDone) SignalGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (thought.dueDate != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "· 截止 ${DateUtils.formatMonthDayChinese(thought.dueDate)}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = SignalGreen
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 12.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(36.dp)) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "取消收藏" else "收藏",
                            tint = if (isFavorite) SignalCoral else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (thought.isTodo) {
                        IconButton(onClick = onToggleTodo, modifier = Modifier.size(36.dp)) {
                            Icon(
                                if (thought.isDone) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                contentDescription = if (thought.isDone) "已完成" else "待办",
                                tint = if (thought.isDone) SignalGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (contactName != null) {
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half))
                        ) {
                            Row(
                                modifier = Modifier.padding(start = 6.dp, end = 10.dp, top = 5.dp, bottom = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                com.tang.prm.ui.components.ContactAvatar(avatar = contactAvatar, name = contactName, size = 20)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    contactName,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp), tint = SignalCoral)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("删除", color = SignalCoral, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TabSection(selectedTab: Int, onTabSelected: (Int) -> Unit, tabs: List<String>) {
    val scrollState = rememberLazyListState()
    LaunchedEffect(selectedTab) {
        scrollState.animateScrollToItem((selectedTab - 1).coerceAtLeast(0))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.paddingPage)
    ) {
        LazyRow(
            state = scrollState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(tabs) { index, title ->
                val isSelected = selectedTab == index
                val tabIcon = TabIcons.getOrNull(index)
                val selectedColor = TabColors.getOrElse(index) { Primary }

                Surface(
                    onClick = { onTabSelected(index) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) selectedColor.copy(alpha = 0.1f) else Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        if (tabIcon != null) {
                            Icon(
                                tabIcon,
                                contentDescription = null,
                                tint = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
