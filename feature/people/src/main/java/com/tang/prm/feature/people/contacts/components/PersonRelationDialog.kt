package com.tang.prm.feature.people.contacts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CustomType
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.components.AddTypeDialog
import com.tang.prm.ui.components.ContactAvatar
import com.tang.prm.ui.theme.DialogDefaults
import com.tang.prm.ui.theme.toComposeColor

/**
 * 添加/编辑人物关系 Dialog。
 *
 * 关键交互：
 * - SegmentedToggle 切换"联系人 / 外部人物"
 * - 联系人模式：姓名栏为"选择联系人"按钮，点击打开 [ContactPickerDialog]
 * - 外部人物模式：姓名栏为输入框 + 头像选择（可选）
 * - 关系类型 chip 单选 + 自定义关系输入框（customLabel 优先）
 * - 备注输入框（可选）
 *
 * 视觉规范：[DialogDefaults.containerColor] 背景，标题用 titleLarge + SemiBold。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PersonRelationDialog(
    onDismiss: () -> Unit,
    onConfirm: (PersonRelationDraft) -> Unit,
    relationTypes: List<CustomType>,
    availableContacts: List<Contact>,
    onAddType: (String, String?, String?) -> Unit,
    initial: PersonRelationDraft? = null,
    ownerId: Long = 0L
) {
    var isExternal by remember { mutableStateOf(initial?.isExternal ?: false) }
    var selectedContact by remember { mutableStateOf(initial?.selectedContact) }
    var externalName by remember { mutableStateOf(initial?.externalName ?: "") }
    var externalAvatar by remember { mutableStateOf(initial?.externalAvatar ?: "") }
    var selectedTypeId by remember { mutableStateOf(initial?.selectedTypeId) }
    var customLabel by remember { mutableStateOf(initial?.customLabel ?: "") }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var showContactPicker by remember { mutableStateOf(false) }
    var showAddTypeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isExternal) {
        // 切换模式时清空对方模式的状态
        if (isExternal) {
            selectedContact = null
        } else {
            externalName = ""
            externalAvatar = ""
        }
    }

    if (showAddTypeDialog) {
        AddTypeDialog(
            title = "新增关系类型",
            onDismiss = { showAddTypeDialog = false },
            onConfirm = { name, color, icon ->
                onAddType(name, color, icon)
                showAddTypeDialog = false
            },
            showIconPicker = true
        )
    }

    if (showContactPicker) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showContactPicker = false },
            containerColor = DialogDefaults.containerColor,
            shape = RoundedCornerShape(20.dp),
            title = { Text("选择联系人", fontWeight = FontWeight.Bold) },
            text = {
                if (availableContacts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier.size(56.dp).background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = AnimationTokens.Alpha.faint),
                                    CircleShape
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.People,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "暂无联系人",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(availableContacts, key = { it.id }) { contact ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        selectedContact = contact
                                        showContactPicker = false
                                    },
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ContactAvatar(avatar = contact.avatar, name = contact.name, size = 42)
                                    Spacer(Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            contact.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        contact.nickname?.let {
                                            Text(
                                                it,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showContactPicker = false }) {
                    Text("取消", color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    val canSave = if (isExternal) {
        externalName.isNotBlank()
    } else {
        selectedContact != null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DialogDefaults.containerColor,
        shape = RoundedCornerShape(20.dp),
        title = {
            Column {
                Text(
                    if (initial == null) "添加人物关系" else "编辑人物关系",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "记录 TA 与其他人物之间的关系",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 对方是谁：SegmentedToggle
                Text(
                    "对方是谁",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
                SegmentedToggle(
                    isExternal = isExternal,
                    onChange = { isExternal = it }
                )

                Spacer(Modifier.height(16.dp))

                // 姓名/联系人选择
                Text(
                    if (isExternal) "姓名" else "选择联系人",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(6.dp))
                if (isExternal) {
                    OutlinedTextField(
                        value = externalName,
                        onValueChange = { externalName = it },
                        placeholder = { Text("请输入对方姓名") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = outlinedTextFieldColors()
                    )
                } else {
                    ContactPickerField(
                        contact = selectedContact,
                        onClick = { showContactPicker = true }
                    )
                }

                // 关系类型
                Spacer(Modifier.height(16.dp))
                Text(
                    "关系类型",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
                RelationTypeChips(
                    types = relationTypes,
                    selectedTypeId = selectedTypeId,
                    onTypeSelected = { typeId ->
                        selectedTypeId = typeId
                        // 选 chip 时清空 customLabel，避免歧义
                        customLabel = ""
                    },
                    onAddType = { showAddTypeDialog = true }
                )

                Spacer(Modifier.height(8.dp))
                Text(
                    "或填入自由描述：",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = customLabel,
                    onValueChange = { newLabel ->
                        customLabel = newLabel
                        // 填入 customLabel 时取消 chip 选择
                        if (newLabel.isNotBlank()) selectedTypeId = null
                    },
                    placeholder = { Text("如：朋友的妻子") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = outlinedTextFieldColors()
                )

                // 备注
                Spacer(Modifier.height(16.dp))
                Text(
                    "备注（可选）",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("补充信息") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = outlinedTextFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val draft = PersonRelationDraft(
                        ownerId = ownerId,
                        isExternal = isExternal,
                        selectedContact = selectedContact,
                        externalName = externalName.ifBlank { null },
                        externalAvatar = externalAvatar.ifBlank { null },
                        selectedTypeId = selectedTypeId,
                        customLabel = customLabel.ifBlank { null },
                        note = note.ifBlank { null }
                    )
                    onConfirm(draft)
                },
                enabled = canSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("保存关系", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

/** Dialog 的中间状态：用户尚未确认时的草稿。 */
data class PersonRelationDraft(
    val ownerId: Long,
    val isExternal: Boolean,
    val selectedContact: Contact?,
    val externalName: String?,
    val externalAvatar: String?,
    val selectedTypeId: Long?,
    val customLabel: String?,
    val note: String?
)

@Composable
private fun SegmentedToggle(
    isExternal: Boolean,
    onChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(modifier = Modifier.padding(3.dp)) {
            SegmentOption(
                text = "联系人",
                isSelected = !isExternal,
                onClick = { onChange(false) },
                modifier = Modifier.weight(1f)
            )
            SegmentOption(
                text = "外部人物",
                isSelected = isExternal,
                onClick = { onChange(true) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SegmentOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(7.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(7.dp),
        color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ContactPickerField(
    contact: Contact?,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half)
        )
    ) {
        if (contact == null) {
            Box(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    "点击选择联系人",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ContactAvatar(avatar = contact.avatar, name = contact.name, size = 36)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        contact.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    contact.nickname?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RelationTypeChips(
    types: List<CustomType>,
    selectedTypeId: Long?,
    onTypeSelected: (Long) -> Unit,
    onAddType: () -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        types.forEach { type ->
            val isSelected = type.id == selectedTypeId
            val typeColor = type.color?.toComposeColor(MaterialTheme.colorScheme.primary)
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onTypeSelected(type.id) },
                shape = RoundedCornerShape(6.dp),
                color = if (isSelected) {
                    typeColor?.copy(alpha = AnimationTokens.Alpha.subtle) ?: MaterialTheme.colorScheme.primary.copy(alpha = AnimationTokens.Alpha.subtle)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                },
                border = BorderStroke(
                    1.dp,
                    if (isSelected) typeColor ?: MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (typeColor != null) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(typeColor, CircleShape)
                        )
                    }
                    Text(
                        type.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) typeColor ?: MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
        // "+ 新增" chip：允许用户创建自定义关系类型
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable { onAddType() },
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = AnimationTokens.Alpha.faint),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = AnimationTokens.Alpha.half)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "+",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "新增",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = AnimationTokens.Alpha.visible),
    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half),
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
)
