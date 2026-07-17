@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.tang.prm.feature.reflect.thoughts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.Thought
import com.tang.prm.domain.model.ThoughtType
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.components.AppDatePicker
import com.tang.prm.ui.components.ContactAvatar
import com.tang.prm.ui.components.ContactPickerDialog
import com.tang.prm.ui.components.FormSectionLabel
import com.tang.prm.ui.theme.DialogDefaults
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.domain.util.DateUtils

@Composable
internal fun ThoughtDialog(
    thought: Thought?,
    dialogType: ThoughtType,
    contacts: List<Contact>,
    onDismiss: () -> Unit,
    onConfirm: (content: String, type: ThoughtType, contactId: Long?, isPrivate: Boolean, isTodo: Boolean, dueDate: Long?) -> Unit
) {
    var content by remember { mutableStateOf(thought?.content ?: "") }
    var selectedType by remember { mutableStateOf(thought?.type ?: dialogType) }
    var selectedContactId by remember { mutableStateOf(thought?.contactId) }
    var isPrivate by remember { mutableStateOf(thought?.isPrivate ?: false) }
    var isTodo by remember { mutableStateOf(thought?.isTodo ?: false) }
    var dueDate by remember { mutableStateOf(thought?.dueDate) }
    var showContactPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val selectedContact = contacts.find { it.id == selectedContactId }

    if (showContactPicker) {
        ContactPickerDialog(
            contacts = contacts,
            title = "关联人物",
            onContactSelected = { selectedContactId = it.id; showContactPicker = false },
            onDismiss = { showContactPicker = false }
        )
    }

    if (showDatePicker) {
        AppDatePicker(
            show = showDatePicker,
            onDismiss = { showDatePicker = false },
            onDateSelected = { dueDate = it },
            initialDate = dueDate
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DialogDefaults.containerColor,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val selectedStyle = selectedType.style
                Surface(
                    shape = CircleShape,
                    color = selectedStyle.bg,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            selectedStyle.icon,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = selectedStyle.color
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(if (thought == null) "新想法" else "编辑想法", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("写下你的想法...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(12.dp)
                )

                val typeSectionStyle = selectedType.style
                FormSectionLabel(
                    icon = typeSectionStyle.icon,
                    label = "类型",
                    color = typeSectionStyle.color
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ThoughtType.entries.forEach { type ->
                        val typeStyle = type.style
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(typeStyle.label, fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    typeStyle.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = typeStyle.color.copy(alpha = AnimationTokens.Alpha.subtle),
                                selectedLabelColor = typeStyle.color,
                                selectedLeadingIconColor = typeStyle.color
                            )
                        )
                    }
                }

                FormSectionLabel(
                    icon = Icons.Default.Person,
                    label = "关联人物",
                    color = SignalAmber
                )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showContactPicker = true },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedContact != null) {
                            ContactAvatar(avatar = selectedContact.avatar, name = selectedContact.name, size = 36)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "关联",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    selectedContact.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = { selectedContactId = null }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "移除", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(SignalAmber.copy(alpha = AnimationTokens.Alpha.faint), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = SignalAmber, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "选择人物",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SignalAmber,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TogglePill(
                        selected = isPrivate,
                        onToggle = { isPrivate = !isPrivate },
                        icon = Icons.Default.Lock,
                        selectedLabel = "私密",
                        unselectedLabel = "公开",
                        selectedColor = SignalAmber,
                        modifier = Modifier.weight(1f)
                    )
                    TogglePill(
                        selected = isTodo,
                        onToggle = {
                            isTodo = !isTodo
                            // B-4 修复：关闭 isTodo 时清空 dueDate，避免脏数据入库
                            if (!isTodo) dueDate = null
                        },
                        icon = if (isTodo) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                        selectedLabel = "待办",
                        unselectedLabel = "非待办",
                        selectedColor = SignalGreen,
                        modifier = Modifier.weight(1f)
                    )
                }

                // B-5 修复：移除外层 if，由 AnimatedVisibility 自身控制可见性和动画
                AnimatedVisibility(visible = isTodo) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showDatePicker = true },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = SignalGreen
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                dueDate?.let { "截止 ${DateUtils.formatMonthDayChinese(it)}" } ?: "设置截止日期",
                                fontSize = 12.sp,
                                color = if (dueDate != null) SignalGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (dueDate != null) {
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(onClick = { dueDate = null }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "清除", modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (content.isNotBlank()) {
                        onConfirm(content.trim(), selectedType, selectedContactId, isPrivate, isTodo, dueDate)
                    }
                },
                enabled = content.isNotBlank()
            ) {
                Text(if (thought == null) "添加" else "保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * C-4 提取：通用的切换胶囊组件，消除 isPrivate 和 isTodo 两处重复的 Surface 结构。
 * 差异通过参数注入：icon/selectedLabel/unselectedLabel/selectedColor。
 */
@Composable
private fun TogglePill(
    selected: Boolean,
    onToggle: () -> Unit,
    icon: ImageVector,
    selectedLabel: String,
    unselectedLabel: String,
    selectedColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onToggle() },
        shape = RoundedCornerShape(12.dp),
        color = if (selected) selectedColor.copy(alpha = AnimationTokens.Alpha.faint) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
            1.dp,
            if (selected) selectedColor.copy(alpha = AnimationTokens.Alpha.subtle) else MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (selected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                if (selected) selectedLabel else unselectedLabel,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
