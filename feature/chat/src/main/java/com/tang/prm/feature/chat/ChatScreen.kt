@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.chat

import androidx.compose.foundation.background
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tang.prm.ui.components.ContactAvatar
import com.tang.prm.ui.components.SearchBar
import com.tang.prm.ui.navigation.AddChatRoute
import com.tang.prm.ui.navigation.ChatDetailRoute
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.animation.primitives.staggeredAppear
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.Primary
import com.tang.prm.ui.theme.TextGray

@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var isGrouped by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        TopAppBar(
            title = { Text("对话", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall) },
            actions = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (!isGrouped) Primary else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { isGrouped = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ViewList,
                            contentDescription = "列表",
                            tint = if (!isGrouped) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (isGrouped) Primary else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { isGrouped = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "分组",
                            tint = if (isGrouped) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                IconButton(onClick = { navController.navigate(AddChatRoute()) }) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "新建对话",
                            tint = Primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.paddingPage),
            placeholder = "搜索对话..."
        )

        Spacer(modifier = Modifier.height(6.dp))

        val filteredConversations = remember(uiState.conversations, searchQuery) {
            if (searchQuery.isNotBlank()) {
                uiState.conversations.filter {
                    it.contactName.contains(searchQuery, ignoreCase = true) ||
                    it.lastMessage.contains(searchQuery, ignoreCase = true)
                }
            } else {
                uiState.conversations
            }
        }

        if (filteredConversations.isEmpty() && !uiState.isLoading) {
            ChatEmptyState()
        } else if (isGrouped) {
            ChatGroupedList(
                grouped = filteredConversations.groupBy { it.contactName },
                onConversationClick = { navController.navigate(ChatDetailRoute(it)) }
            )
        } else {
            ChatFlatList(
                conversations = filteredConversations,
                onConversationClick = { navController.navigate(ChatDetailRoute(it)) }
            )
        }
    }
}

@Composable
private fun ChatEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Chat,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(60.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "暂无对话记录",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "开始和联系人聊天吧",
                style = MaterialTheme.typography.bodyMedium,
                color = TextGray
            )
        }
    }
}

@Composable
private fun ChatGroupedList(
    grouped: Map<String, List<ConversationUiModel>>,
    onConversationClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        grouped.forEach { (contactName, conversations) ->
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 6.dp)
                ) {
                    ContactAvatar(
                        avatar = conversations.firstOrNull()?.avatar,
                        name = contactName,
                        size = 28
                    )
                    Text(
                        text = contactName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Primary.copy(alpha = AnimationTokens.Alpha.faint)
                    ) {
                        Text(
                            text = "${conversations.size}条",
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                            fontSize = 10.sp
                        )
                    }
                }
            }
            itemsIndexed(conversations, key = { _, it -> it.eventId }) { index, conversation ->
                ConversationItem(
                    conversation = conversation,
                    showAvatar = false,
                    onClick = { onConversationClick(conversation.eventId) },
                    modifier = Modifier.staggeredAppear(index = minOf(index, 15))
                )
            }
        }
        item { Spacer(modifier = Modifier.height(100.dp).navigationBarsPadding()) }
    }
}

@Composable
private fun ChatFlatList(
    conversations: List<ConversationUiModel>,
    onConversationClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(conversations, key = { _, it -> it.eventId }) { index, conversation ->
            ConversationItem(
                conversation = conversation,
                showAvatar = true,
                onClick = { onConversationClick(conversation.eventId) },
                modifier = Modifier.staggeredAppear(index = minOf(index, 15))
            )
        }
        item { Spacer(modifier = Modifier.height(100.dp).navigationBarsPadding()) }
    }
}
