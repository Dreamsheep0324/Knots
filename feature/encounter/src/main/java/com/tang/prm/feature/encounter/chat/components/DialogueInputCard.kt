package com.tang.prm.feature.encounter.chat.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tang.prm.feature.encounter.chat.DialogueLineInput
import com.tang.prm.feature.encounter.chat.MyBubbleColor
import com.tang.prm.ui.components.AppCard
import com.tang.prm.ui.components.ContactAvatar
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.Primary

@Composable
internal fun DialogueInputCard(
    dialogueLines: List<DialogueLineInput>,
    contactName: String,
    contactAvatar: String?,
    onAddMyLine: () -> Unit,
    onAddTheirLine: () -> Unit,
    onUpdateLine: (Long, String) -> Unit,
    onRemoveLineImage: (Long, String?) -> Unit,
    onRequestImage: (Long) -> Unit,
    onToggleSpeaker: (Long) -> Unit,
    onRemoveLine: (Long) -> Unit,
    onMoveLine: (Long, Int) -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Dimens.paddingCard)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(3.dp).height(16.dp).background(MyBubbleColor, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.TheaterComedy, contentDescription = null, tint = MyBubbleColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("对话剧本", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(6.dp))
                if (dialogueLines.isNotEmpty()) {
                    Text("${dialogueLines.size}条", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (dialogueLines.isEmpty()) {
                EmptyDialogueHint(contactName = contactName)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    dialogueLines.forEachIndexed { index, line ->
                        DialogueLineEditor(
                            line = line,
                            contactName = contactName,
                            contactAvatar = contactAvatar,
                            isFirst = index == 0,
                            isLast = index == dialogueLines.size - 1,
                            onUpdate = { onUpdateLine(line.id, it) },
                            onRemoveImage = { onRemoveLineImage(line.id, null) },
                            onRequestImage = { onRequestImage(line.id) },
                            onToggleSpeaker = { onToggleSpeaker(line.id) },
                            onRemove = { onRemoveLine(line.id) },
                            onMoveUp = { onMoveLine(line.id, -1) },
                            onMoveDown = { onMoveLine(line.id, 1) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onAddTheirLine,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${contactName}说了", fontSize = 13.sp)
                }

                Button(
                    onClick = onAddMyLine,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MyBubbleColor)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("我说了", fontSize = 13.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
internal fun EmptyDialogueHint(contactName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(36.dp))
        Spacer(modifier = Modifier.height(10.dp))
        Text("还没有对话内容", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text("点击下方按钮添加对话", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
    }
}

@Composable
internal fun DialogueLineEditor(
    line: DialogueLineInput,
    contactName: String,
    contactAvatar: String?,
    isFirst: Boolean,
    isLast: Boolean,
    onUpdate: (String) -> Unit,
    onRemoveImage: () -> Unit,
    onRequestImage: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val bubbleBg = if (line.isMe) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val accentColor = if (line.isMe) MyBubbleColor else Primary
    val speakerLabel = if (line.isMe) "我" else contactName

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (line.isMe) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            if (!line.isMe) {
                ContactAvatar(avatar = contactAvatar, name = contactName, size = 28)
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = if (line.isMe) Alignment.End else Alignment.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        speakerLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                    Icon(
                        Icons.Default.SwapHoriz,
                        contentDescription = "切换说话人",
                        modifier = Modifier
                            .size(14.dp)
                            .clickable(onClick = onToggleSpeaker),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    shape = if (line.isMe) {
                        RoundedCornerShape(topStart = 14.dp, topEnd = 4.dp, bottomStart = 14.dp, bottomEnd = 14.dp)
                    } else {
                        RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 14.dp)
                    },
                    color = bubbleBg
                ) {
                    Column {
                        if (line.imageUri != null) {
                            Box(modifier = Modifier.padding(8.dp)) {
                                AsyncImage(
                                    model = line.imageUri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth(0.7f)
                                        .heightIn(max = 200.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(22.dp)
                                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                                        .clickable { onRemoveImage() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "删除图片", tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                        OutlinedTextField(
                            value = line.content,
                            onValueChange = onUpdate,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp, vertical = 2.dp),
                            placeholder = {
                                Text(
                                    if (line.isMe) "我说了什么..." else "${contactName}说了什么...",
                                    color = if (line.isMe) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = AnimationTokens.Alpha.half) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half),
                                    fontSize = 14.sp
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            minLines = 1,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = accentColor.copy(alpha = 0.3f),
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = if (line.isMe) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                lineHeight = 20.sp
                            ),
                            trailingIcon = {
                                if (line.imageUri == null) {
                                    Box(
                                        modifier = Modifier
                                            .padding(end = 6.dp)
                                            .size(26.dp)
                                            .background(accentColor.copy(alpha = AnimationTokens.Alpha.faint), CircleShape)
                                            .clickable { onRequestImage() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Image,
                                            contentDescription = "添加图片",
                                            tint = accentColor.copy(alpha = AnimationTokens.Alpha.half),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }

            if (line.isMe) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(MyBubbleColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MyBubbleColor, modifier = Modifier.size(14.dp))
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = if (line.isMe) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val offsetModifier = if (!line.isMe) Modifier.padding(start = 36.dp) else Modifier.padding(end = 36.dp)

            Row(offsetModifier, horizontalArrangement = Arrangement.spacedBy(0.dp), verticalAlignment = Alignment.CenterVertically) {
                if (!isFirst) {
                    IconButton(onClick = onMoveUp, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "上移", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half), modifier = Modifier.size(16.dp))
                    }
                }
                if (!isLast) {
                    IconButton(onClick = onMoveDown, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "下移", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half), modifier = Modifier.size(16.dp))
                    }
                }
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "删除", tint = Color(0xFFFF6B6B).copy(alpha = AnimationTokens.Alpha.visible), modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}
