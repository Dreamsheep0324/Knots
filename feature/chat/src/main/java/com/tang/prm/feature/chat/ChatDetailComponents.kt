@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.Event
import com.tang.prm.ui.components.AppCard
import com.tang.prm.ui.components.ContactAvatar
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.SceneOrange
import com.tang.prm.ui.theme.SemanticBlueBg
import com.tang.prm.domain.util.DateUtils

internal data class DialogueLine(
    val speaker: String,
    val content: String,
    val isMe: Boolean,
    val imageUri: String? = null
)

internal fun extractImageTag(raw: String): Pair<String, String?> {
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

internal fun parseDialogue(event: Event, contactName: String?): List<DialogueLine> {
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

internal fun getIntimacyColor(score: Int): Color =
    Color(com.tang.prm.domain.model.IntimacyTier.of(score).colorValue)

internal val MyBubbleColor: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary
internal val MyBubbleBg: Color
    @Composable @ReadOnlyComposable get() = SemanticBlueBg

@Composable
internal fun ScriptHeader(event: Event, contact: Contact?, intimacyColor: Color) {
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
                    text = contact?.name ?: com.tang.prm.domain.model.EventType.CONVERSATION.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    contact?.relationship?.let { relationship ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = intimacyColor.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = relationship,
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

                    event.weather?.takeIf { it.isNotBlank() }?.let { weather ->
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
                                text = weather,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    event.emotion?.takeIf { it.isNotBlank() }?.let { emotion ->
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
                                text = emotion,
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
internal fun DialogueScriptCard(
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
                items(dialogues.size, key = { index -> "$index" }) { index ->
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
internal fun TheirDialogueLine(
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
internal fun MyDialogueLine(content: String, imageUri: String?) {
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
internal fun RemarksCard(remarks: String, accentColor: Color) {
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
