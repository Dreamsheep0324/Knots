@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.ui.chat

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.Event
import com.tang.prm.ui.components.AppCard
import com.tang.prm.ui.components.ContactAvatar
import com.tang.prm.ui.components.DeleteConfirmDialog
import com.tang.prm.ui.navigation.Screen
import com.tang.prm.ui.theme.*
import com.tang.prm.util.DateUtils
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.domain.model.AppStrings

private val MyBubbleColor = Primary
private val MyBubbleBg: Color
    @Composable get() = if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF1A3A4A) else Color(0xFFE3F2FD)

@Composable
fun ChatDetailScreen(
    eventId: Long,
    navController: NavController,
    viewModel: ChatDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(eventId) { viewModel.loadEvent(eventId) }

    val contact = uiState.event?.participants?.firstOrNull()
    val intimacyColor = contact?.let { getIntimacyColor(it.intimacyScore) } ?: Primary
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        DeleteConfirmDialog(
            title = "删除对话",
            message = "确定要删除这条对话记录吗？此操作不可撤销。",
            onConfirm = { viewModel.deleteEvent(); navController.popBackStack() },
            onDismiss = { showDeleteDialog = false }
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
                    IconButton(onClick = { navController.navigate(Screen.EditChat.createRoute(eventId)) }) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            if (uiState.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "收藏",
                            tint = if (uiState.isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        uiState.event?.let { event ->
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

                if (event.remarks != null) {
                    RemarksCard(remarks = event.remarks, accentColor = intimacyColor)
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
}

private data class DialogueLine(
    val speaker: String,
    val content: String,
    val isMe: Boolean,
    val imageUri: String? = null
)

private fun extractImageTag(raw: String): Pair<String, String?> {
    val regex = Regex("""\[img:(.+?)]""")
    val match = regex.find(raw)
    return if (match != null) {
        val uri = match.groupValues[1]
        val text = raw.replace(match.value, "").trim()
        text to uri
    } else {
        raw to null
    }
}

private fun parseDialogue(event: Event, contactName: String?): List<DialogueLine> {
    val source = event.description ?: return emptyList()
    val lines = source.split("\n").filter { it.isNotBlank() }
    if (lines.isEmpty()) return emptyList()

    val hasSpeakerPrefix = lines.any { line ->
        line.trimStart().startsWith("我：") ||
        line.trimStart().startsWith("我:") ||
        (contactName != null && line.trimStart().startsWith("$contactName：")) ||
        (contactName != null && line.trimStart().startsWith("$contactName:")) ||
        line.trimStart().startsWith("对方：") ||
        line.trimStart().startsWith("对方:")
    }

    if (!hasSpeakerPrefix) return emptyList()

    return lines.mapNotNull { line ->
        val trimmed = line.trimStart()
        when {
            trimmed.startsWith("我：") || trimmed.startsWith("我:") -> {
                val sepIdx = trimmed.indexOfFirst { it == '：' || it == ':' }
                if (sepIdx >= 0) {
                    val (text, uri) = extractImageTag(trimmed.substring(sepIdx + 1).trim())
                    DialogueLine(speaker = "我", content = text, isMe = true, imageUri = uri)
                } else null
            }
            contactName != null && (trimmed.startsWith("$contactName：") || trimmed.startsWith("$contactName:")) -> {
                val sepIdx = trimmed.indexOfFirst { it == '：' || it == ':' }
                if (sepIdx >= 0) {
                    val (text, uri) = extractImageTag(trimmed.substring(sepIdx + 1).trim())
                    DialogueLine(speaker = contactName, content = text, isMe = false, imageUri = uri)
                } else null
            }
            trimmed.startsWith("对方：") || trimmed.startsWith("对方:") -> {
                val sepIdx = trimmed.indexOfFirst { it == '：' || it == ':' }
                if (sepIdx >= 0) {
                    val (text, uri) = extractImageTag(trimmed.substring(sepIdx + 1).trim())
                    DialogueLine(speaker = contactName ?: "对方", content = text, isMe = false, imageUri = uri)
                } else null
            }
            else -> null
        }
    }
}

@Composable
private fun ScriptHeader(event: Event, contact: Contact?, intimacyColor: Color) {
    AppCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.paddingCard),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ContactAvatar(avatar = contact?.avatar, name = contact?.name ?: "对话", size = 48)

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact?.name ?: AppStrings.EventType.CONVERSATION,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (contact?.relationship != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = intimacyColor.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = contact.relationship,
                                style = MaterialTheme.typography.labelSmall,
                                color = intimacyColor,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = DateUtils.formatMonthDayTimeChineseFull(event.time),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    if (!event.weather.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = event.weather,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (!event.emotion.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                Icons.Default.EmojiEmotions,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = event.emotion,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogueScriptCard(
    dialogues: List<DialogueLine>,
    contactAvatar: String?,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(accentColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = accentColor, modifier = Modifier.size(15.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "对话内容",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${dialogues.size}条",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.surfaceVariant)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(dialogues.size, key = { it }) { index ->
                    val line = dialogues[index]
                    if (line.isMe) {
                        MyDialogueLine(
                            content = line.content,
                            imageUri = line.imageUri
                        )
                    } else {
                        TheirDialogueLine(
                            name = line.speaker,
                            avatar = contactAvatar,
                            content = line.content,
                            imageUri = line.imageUri,
                            accentColor = accentColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TheirDialogueLine(
    name: String,
    avatar: String?,
    content: String,
    imageUri: String?,
    accentColor: Color
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ContactAvatar(avatar = avatar, name = name, size = 36)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .heightIn(max = 180.dp)
                                .padding(start = 8.dp, top = 8.dp, end = 8.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    if (content.isNotBlank()) {
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    } else if (imageUri != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MyDialogueLine(content: String, imageUri: String?) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
            Text(
                text = "我",
                style = MaterialTheme.typography.labelSmall,
                color = MyBubbleColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                color = MyBubbleBg
            ) {
                Column {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .heightIn(max = 180.dp)
                                .padding(start = 8.dp, top = 8.dp, end = 8.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    if (content.isNotBlank()) {
                        Text(
                        text = content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    } else if (imageUri != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MyBubbleColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = MyBubbleColor, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun RemarksCard(remarks: String, accentColor: Color) {
    AppCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Dimens.paddingCard)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(accentColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.EditNote, contentDescription = null, tint = accentColor, modifier = Modifier.size(15.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "备注",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = remarks,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )
        }
    }
}

private fun getIntimacyColor(score: Int): Color = when {
    score >= 81 -> Color(0xFFF43F5E)
    score >= 61 -> Color(0xFFF97316)
    score >= 41 -> Color(0xFF6366F1)
    score >= 21 -> Color(0xFF64748B)
    else -> Color(0xFF94A3B8)
}
