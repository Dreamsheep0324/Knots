@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.chat

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.tang.prm.domain.model.IntimacyTier
import com.tang.prm.domain.usecase.ConversationItem
import com.tang.prm.domain.util.DateUtils
import com.tang.prm.ui.components.ContactAvatar
import com.tang.prm.ui.components.DeleteConfirmDialog
import com.tang.prm.ui.navigation.AddChatRoute
import com.tang.prm.ui.navigation.EditChatRoute
import com.tang.prm.ui.theme.FavoriteGold

// ═══════════════════════════════════════════════════════════════
// 平板对话主界面 — 方案A 剧本剧场 Script Theatre
// ═══════════════════════════════════════════════════════════════
// 设计语言：
// - 三栏布局：左对话列表(380dp) + 中央剧本舞台 + 右角色信息(400dp)
// - 衬线大字标题 + eyebrow 小标签 + 暖色调气泡
// - 复用 parseDialogue / getIntimacyColor / IntimacyTier
// ═══════════════════════════════════════════════════════════════

@Composable
fun ChatTabletScreen(
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedId by remember { mutableStateOf<Long?>(null) }

    val filteredConversations = remember(uiState.data.conversations, searchQuery) {
        if (searchQuery.isNotBlank()) {
            uiState.data.conversations.filter {
                it.contactName.contains(searchQuery, ignoreCase = true) ||
                it.lastMessage.contains(searchQuery, ignoreCase = true)
            }
        } else {
            uiState.data.conversations
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // ── 左栏：对话列表 ──
        ConversationListPane(
            conversations = filteredConversations,
            totalCount = uiState.data.conversations.size,
            contactCount = uiState.data.conversations.map { it.contactName }.distinct().size,
            searchQuery = searchQuery,
            onQueryChange = { searchQuery = it },
            selectedId = selectedId,
            onConversationClick = { selectedId = it },
            onAddClick = { navController.navigate(AddChatRoute()) },
            modifier = Modifier.width(380.dp).fillMaxHeight()
        )

        // ── 中央 + 右栏 ──
        // 使用 AnimatedContent 复用 NavHost 级别动效（fadeIn + scaleIn(0.96f)），
        // 让对话列表→详情的切换与人物界面跳转保持一致的视觉过渡
        val currentId = selectedId
        AnimatedContent(
            targetState = currentId,
            transitionSpec = {
                // 复用 navTransitions 的详情页动效参数
                (fadeIn(tween(450, delayMillis = 50, easing = FastOutSlowInEasing)) +
                    scaleIn(
                        animationSpec = tween(450, delayMillis = 50, easing = FastOutSlowInEasing),
                        initialScale = 0.96f
                    )) togetherWith
                    (fadeOut(tween(200)) +
                        scaleOut(
                            animationSpec = tween(200),
                            targetScale = 0.96f
                        ))
            },
            modifier = Modifier.weight(1f).fillMaxHeight(),
            label = "chatDetailSwitch"
        ) { id ->
            if (id != null) {
                val detailViewModel: ChatDetailViewModel = hiltViewModel(key = "chatDetail_$id")
                val detailState by detailViewModel.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(id) { detailViewModel.setEventId(id) }

                var showDeleteDialog by remember { mutableStateOf(false) }
                if (showDeleteDialog) {
                    DeleteConfirmDialog(
                        title = "删除对话",
                        message = "确定要删除这条对话记录吗？此操作不可撤销。",
                        onConfirm = {
                            detailViewModel.deleteEvent()
                            selectedId = null
                            showDeleteDialog = false
                        },
                        onDismiss = { showDeleteDialog = false }
                    )
                }

                Row(modifier = Modifier.fillMaxSize()) {
                    // 中央：剧本舞台
                    ScriptStagePane(
                        uiState = detailState,
                        onEdit = { navController.navigate(EditChatRoute(id)) },
                        onToggleFavorite = { detailViewModel.toggleFavorite() },
                        onDelete = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )

                    // 右栏：角色信息
                    CharacterPane(
                        uiState = detailState,
                        conversations = uiState.data.conversations,
                        modifier = Modifier.width(400.dp).fillMaxHeight()
                    )
                }
            } else {
                // 空状态
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                        Spacer(Modifier.height(24.dp))
                        Text(
                            "选择对话查看剧本",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "从左侧列表选择一段对话",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 左栏：对话列表
// ═══════════════════════════════════════════════════════════════

@Composable
private fun ConversationListPane(
    conversations: List<ConversationItem>,
    totalCount: Int,
    contactCount: Int,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    selectedId: Long?,
    onConversationClick: (Long) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(28.dp)) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "对话",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "共 $totalCount 段 · $contactCount 位联系人",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                // 添加按钮
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                        .clickable(onClick = onAddClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "新建对话",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // 搜索框
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                androidx.compose.foundation.text.BasicTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                "搜索人物、对话...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        innerTextField()
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            // 对话列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(conversations, key = { it.eventId }) { conversation ->
                    ConversationListItem(
                        conversation = conversation,
                        isSelected = conversation.eventId == selectedId,
                        onClick = { onConversationClick(conversation.eventId) }
                    )
                }
                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
private fun ConversationListItem(
    conversation: ConversationItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 圆形头像
        ContactAvatar(
            avatar = conversation.avatar,
            name = conversation.contactName,
            size = 44.dp
        )

        Spacer(Modifier.width(12.dp))

        // 信息区
        Column(modifier = Modifier.weight(1f)) {
            // 第一行：名字 + 时间
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conversation.contactName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Text(
                    text = conversation.lastMessageTime,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // 第二行：最后消息（或标题）
            val subText: String = if (conversation.title != null && conversation.title != "与${conversation.contactName}的对话") {
                conversation.title ?: ""
            } else {
                conversation.lastMessage.ifEmpty { "暂无消息" }
            }
            Text(
                text = subText,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 中央：剧本舞台
// ═══════════════════════════════════════════════════════════════

@Composable
private fun ScriptStagePane(
    uiState: ChatDetailUiState,
    onEdit: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background
    ) {
        val event = uiState.data.event
        if (event != null) {
            val contact = event.participants?.firstOrNull()
            val intimacyColor = contact?.let { getIntimacyColor(it.intimacyScore) } ?: MaterialTheme.colorScheme.onSurfaceVariant
            // 使用 DialogueLineManager 解析（支持 JSON 新格式 + 旧格式）
            val dialogueManager = remember { DialogueLineManager() }
            val dialogueInputs = remember(event.id, event.description) {
                dialogueManager.parseDescriptionToLines(event.description, contact?.name)
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(40.dp)
            ) {
                // ── 顶部：剧本头 ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        // Eyebrow
                        Text(
                            text = "CONVERSATION · 对话剧本",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 4.sp
                        )
                        // 衬线大标题
                        Text(
                            text = event.title ?: "与${contact?.name ?: "对方"}的对话",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Serif,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                        // 元信息
                        Row(
                            modifier = Modifier.padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MetaTag(
                                icon = Icons.Default.CalendarToday,
                                text = DateUtils.formatMonthDayTimeChineseFull(event.time)
                            )
                            event.weather?.takeIf { it.isNotBlank() }?.let {
                                MetaTag(icon = Icons.Default.WbSunny, text = it)
                            }
                            event.emotion?.takeIf { it.isNotBlank() }?.let {
                                MetaTag(icon = Icons.Default.EmojiEmotions, text = it)
                            }
                        }
                    }
                    // 操作按钮
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionButton(
                            icon = Icons.Default.Edit,
                            contentDescription = "编辑",
                            onClick = onEdit
                        )
                        ActionButton(
                            icon = if (uiState.data.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "收藏",
                            onClick = onToggleFavorite,
                            tint = if (uiState.data.isFavorite) FavoriteGold else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        ActionButton(
                            icon = Icons.Default.Delete,
                            contentDescription = "删除",
                            onClick = onDelete,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(top = 24.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // ── 中间：剧本对话区 ──
                if (dialogueInputs.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.weight(1f).padding(top = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(dialogueInputs.size, key = { it }) { index ->
                            val line = dialogueInputs[index]
                            if (line.isMe) {
                                MeLine(
                                    content = line.content,
                                    imageUri = line.imageUri
                                )
                            } else {
                                ThemLine(
                                    speaker = contact?.name ?: "对方",
                                    avatar = contact?.avatar,
                                    content = line.content,
                                    imageUri = line.imageUri,
                                    accentColor = intimacyColor
                                )
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                // ── 底部：备注 ──
                event.remarks?.takeIf { it.isNotBlank() }?.let { remarks ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        tonalElevation = 0.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp, 12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 3.dp, height = 14.dp)
                                        .background(intimacyColor)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "备注",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 2.sp
                                )
                            }
                            Text(
                                text = remarks,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 22.sp,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }
            }
        } else if (uiState.data.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurfaceVariant, strokeWidth = 3.dp)
            }
        }
    }
}

@Composable
private fun MetaTag(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = text,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(18.dp))
    }
}

// ── 对话气泡 ──

@Composable
private fun ThemLine(
    speaker: String,
    avatar: String?,
    content: String,
    imageUri: String?,
    accentColor: Color
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ContactAvatar(avatar = avatar, name = speaker, size = 36.dp)
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
                text = speaker,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = accentColor
            )
            Spacer(Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(180.dp)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    if (content.isNotBlank()) {
                        Text(
                            text = content,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    } else if (imageUri != null) {
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MeLine(content: String, imageUri: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
            Text(
                text = "我",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(topStart = 14.dp, topEnd = 4.dp, bottomStart = 14.dp, bottomEnd = 14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(180.dp)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    if (content.isNotBlank()) {
                        Text(
                            text = content,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    } else if (imageUri != null) {
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 右栏：角色信息
// ═══════════════════════════════════════════════════════════════

@Composable
private fun CharacterPane(
    uiState: ChatDetailUiState,
    conversations: List<ConversationItem>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface
    ) {
        val event = uiState.data.event
        val contact = event?.participants?.firstOrNull()
        val intimacyColor = contact?.let { getIntimacyColor(it.intimacyScore) } ?: MaterialTheme.colorScheme.onSurfaceVariant
        val intimacyTier = contact?.let { IntimacyTier.of(it.intimacyScore) }

        Column(modifier = Modifier.fillMaxSize().padding(28.dp)) {
            if (contact == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "无角色信息",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                return@Surface
            }

            // ── 顶部：双层画框头像 ──
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 双层画框：外层 intimacyColor 描边 + 内层白色
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(intimacyColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(108.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, intimacyColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (contact.avatar != null) {
                            AsyncImage(
                                model = contact.avatar,
                                contentDescription = contact.name,
                                modifier = Modifier.fillMaxSize().padding(4.dp),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = contact.name.firstOrNull()?.toString() ?: "?",
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Serif,
                                color = intimacyColor
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 衬线大字名字
                Text(
                    text = contact.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // 关系副标题
                contact.relationship?.takeIf { it.isNotBlank() }?.let { rel ->
                    Text(
                        text = rel,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // 亲密度星级 + 徽章
                if (intimacyTier != null) {
                    Spacer(Modifier.height(12.dp))
                    // 星级
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(5) { i ->
                            val filled = i < intimacyTier.stars
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (filled) intimacyColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    // 徽章
                    Row(
                        modifier = Modifier
                            .background(intimacyColor.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(intimacyColor, CircleShape)
                        )
                        Text(
                            text = "${intimacyTier.cardRarity} · ${intimacyTier.label}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = intimacyColor
                        )
                    }
                }
            }

            // ── 章节标题色条 ──
            SectionDivider("CONVERSATION STATS · 对话统计")

            // ── 统计项 ──
            val contactConversations = remember(conversations, contact.name) {
                conversations.filter { it.contactName == contact.name }
            }
            CharacterStatRow(
                label = "对话总数",
                value = "${contactConversations.size}",
                unit = "段"
            )
            CharacterStatRow(
                label = "最近对话",
                value = event?.let { DateUtils.formatShortDate(it.time) } ?: "—",
                unit = ""
            )
            CharacterStatRow(
                label = "关系亲密度",
                value = "${contact.intimacyScore}",
                unit = "/ 100"
            )
            CharacterStatRow(
                label = "收藏状态",
                value = if (uiState.data.isFavorite) "已收藏" else "未收藏",
                valueColor = if (uiState.data.isFavorite) FavoriteGold else MaterialTheme.colorScheme.onSurfaceVariant,
                unit = ""
            )

            // ── 其他信息 ──
            if (contact.phone?.isNotBlank() == true ||
                contact.email?.isNotBlank() == true ||
                contact.address?.isNotBlank() == true
            ) {
                SectionDivider("CONTACT · 联系方式")
                contact.phone?.takeIf { it.isNotBlank() }?.let {
                    ContactInfoRow(icon = Icons.Default.Phone, label = "电话", value = it)
                }
                contact.email?.takeIf { it.isNotBlank() }?.let {
                    ContactInfoRow(icon = Icons.Default.Email, label = "邮箱", value = it)
                }
                contact.address?.takeIf { it.isNotBlank() }?.let {
                    ContactInfoRow(icon = Icons.Default.LocationOn, label = "地址", value = it)
                }
            }
        }
    }
}

@Composable
private fun SectionDivider(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 12.dp)
                .background(MaterialTheme.colorScheme.primary)
        )
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 2.sp
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
private fun CharacterStatRow(
    label: String,
    value: String,
    unit: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = valueColor
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = " $unit",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 2.dp, bottom = 1.dp)
                )
            }
        }
    }
}

@Composable
private fun ContactInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 1.dp)
            )
        }
    }
}
