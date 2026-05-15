package com.tang.prm.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tang.prm.domain.model.Circle
import com.tang.prm.domain.model.Contact
import com.tang.prm.ui.components.ContactAvatar
import com.tang.prm.ui.components.ContactRelationshipBadge
import com.tang.prm.ui.theme.*
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.DialogDefaults

@Composable
internal fun TerminalCreateDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String?, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedWaveformIndex by remember { mutableStateOf(0) }

    val selectedWaveform = ContactListViewModel.WaveformTypes[selectedWaveformIndex].first
    val accentColor = MaterialTheme.colorScheme.onSurface

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(2.dp),
            color = DialogDefaults.containerColor,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "> 新建圈子",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(accentColor, CircleShape)
                    )
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(2.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    shape = RoundedCornerShape(2.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("波形", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TerminalTextDim, fontFamily = FontFamily.Monospace)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "[${ContactListViewModel.WaveformTypes[selectedWaveformIndex].second}]",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(ContactListViewModel.WaveformTypes.size, key = { it }) { index ->
                        val (waveformKey, waveformLabel) = ContactListViewModel.WaveformTypes[index]
                        val isSelected = index == selectedWaveformIndex
                        Surface(
                            onClick = { selectedWaveformIndex = index },
                            shape = RoundedCornerShape(2.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface.copy(alpha = AnimationTokens.Alpha.faint) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val waveFillColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                val waveStrokeColor = MaterialTheme.colorScheme.onSurface
                                val waveStrokeDimColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                Canvas(modifier = Modifier.size(36.dp, 14.dp)) {
                                    val w = size.width
                                    val h = size.height
                                    val cy = h / 2
                                    val path = Path()
                                    for (x in 0..w.toInt()) {
                                        val t = x / w
                                        val y = cy + computeWaveform(waveformKey, t, 0f) * (h * 0.38f)
                                        if (x == 0) path.moveTo(x.toFloat(), y)
                                        else path.lineTo(x.toFloat(), y)
                                    }
                                    if (isSelected) {
                                        drawPath(path, waveFillColor, style = Stroke(width = 4f))
                                    }
                                    drawPath(path, if (isSelected) waveStrokeColor else waveStrokeDimColor, style = Stroke(width = 1.2f))
                                }
                                Spacer(Modifier.height(1.dp))
                                Text(
                                    waveformLabel,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 7.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else TerminalTextMuted,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TerminalActionButton(label = "取消", onClick = onDismiss)
                        TerminalActionButton(label = "创建", onClick = {
                            if (name.isNotBlank()) onCreate(name, description.ifBlank { null }, "#1A1A2E", selectedWaveform)
                        })
                    }
                }
            }
        }
    }
}

@Composable
internal fun TerminalEditDialog(
    circle: Circle,
    onDismiss: () -> Unit,
    onSave: (String, String?, String, String) -> Unit
) {
    var name by remember { mutableStateOf(circle.name) }
    var description by remember { mutableStateOf(circle.description ?: "") }
    var selectedWaveformIndex by remember { mutableStateOf(ContactListViewModel.WaveformTypes.indexOfFirst { it.first == circle.waveform }.coerceAtLeast(0)) }

    val selectedWaveform = ContactListViewModel.WaveformTypes[selectedWaveformIndex].first
    val accentColor = MaterialTheme.colorScheme.onSurface

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(2.dp),
            color = DialogDefaults.containerColor,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "> 编辑圈子",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(accentColor, CircleShape)
                    )
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(2.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    shape = RoundedCornerShape(2.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("波形", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TerminalTextDim, fontFamily = FontFamily.Monospace)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "[${ContactListViewModel.WaveformTypes[selectedWaveformIndex].second}]",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(ContactListViewModel.WaveformTypes.size, key = { it }) { index ->
                        val (waveformKey, waveformLabel) = ContactListViewModel.WaveformTypes[index]
                        val isSelected = index == selectedWaveformIndex
                        Surface(
                            onClick = { selectedWaveformIndex = index },
                            shape = RoundedCornerShape(2.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface.copy(alpha = AnimationTokens.Alpha.faint) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val waveFillColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                val waveStrokeColor = MaterialTheme.colorScheme.onSurface
                                val waveStrokeDimColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                Canvas(modifier = Modifier.size(36.dp, 14.dp)) {
                                    val w = size.width
                                    val h = size.height
                                    val cy = h / 2
                                    val path = Path()
                                    for (x in 0..w.toInt()) {
                                        val t = x / w
                                        val y = cy + computeWaveform(waveformKey, t, 0f) * (h * 0.38f)
                                        if (x == 0) path.moveTo(x.toFloat(), y)
                                        else path.lineTo(x.toFloat(), y)
                                    }
                                    if (isSelected) {
                                        drawPath(path, waveFillColor, style = Stroke(width = 4f))
                                    }
                                    drawPath(path, if (isSelected) waveStrokeColor else waveStrokeDimColor, style = Stroke(width = 1.2f))
                                }
                                Spacer(Modifier.height(1.dp))
                                Text(
                                    waveformLabel,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 7.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else TerminalTextMuted,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TerminalActionButton(label = "取消", onClick = onDismiss)
                        TerminalActionButton(label = "保存", onClick = {
                            if (name.isNotBlank()) onSave(name, description.ifBlank { null }, "#1A1A2E", selectedWaveform)
                        })
                    }
                }
            }
        }
    }
}

@Composable
internal fun TerminalAddMemberDialog(
    availableContacts: List<Contact>,
    onDismiss: () -> Unit,
    onAdd: (Long) -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = if (search.isBlank()) availableContacts else availableContacts.filter { it.name.contains(search, ignoreCase = true) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.75f),
            shape = RoundedCornerShape(2.dp),
            color = DialogDefaults.containerColor,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(Dimens.paddingCard)) {
                Text(
                    "> 添加成员",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("搜索联系人...", fontFamily = FontFamily.Monospace, color = TerminalTextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(2.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TerminalTextDim) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SignalPurple,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                Spacer(Modifier.height(12.dp))
                if (filtered.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("[暂无数据]", color = TerminalTextMuted, fontFamily = FontFamily.Monospace)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(filtered, key = { it.id }) { contact ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAdd(contact.id) }
                                    .padding(horizontal = 8.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(2.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ContactAvatar(contact.avatar, contact.name, 32)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        contact.name,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    ContactRelationshipBadge(
                                        relationship = contact.relationship,
                                        color = TerminalTextDim,
                                        fontSize = 10.sp
                                    )
                                }
                                Text("[+]", fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = SignalPurple, fontWeight = FontWeight.Bold)
                            }
                            TerminalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun TerminalDeleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(2.dp),
            color = DialogDefaults.containerColor,
            border = BorderStroke(1.dp, Error.copy(alpha = AnimationTokens.Alpha.visible))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "> 确认删除?",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Error
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "此操作不可撤销。",
                    fontFamily = FontFamily.Monospace,
                    color = TerminalTextDim,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TerminalActionButton(label = "取消", onClick = onDismiss)
                    TerminalActionButton(label = "确认", onClick = onConfirm)
                }
            }
        }
    }
}
