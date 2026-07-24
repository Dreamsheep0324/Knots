package com.tang.prm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.theme.DialogDefaults
import com.tang.prm.ui.theme.Dimens

/**
 * ContactPickerDialog 的配置参数，封装标题、副标题、空态文案、多选开关与新建联系人回调。
 *
 * @param title 对话框标题
 * @param subtitle 副标题，null 时不渲染
 * @param emptyText 联系人为空时的提示文案
 * @param multiSelect 是否开启多选；false 时 selectedContacts 无意义
 * @param onCreateContact 空态点击"新建联系人"的回调，null 时不显示按钮
 */
data class ContactPickerConfig(
    val title: String = "选择联系人",
    val subtitle: String? = null,
    val emptyText: String = "暂无联系人",
    val multiSelect: Boolean = false,
    val onCreateContact: (() -> Unit)? = null
)

@Composable
fun ContactPickerDialog(
    contacts: List<com.tang.prm.domain.model.Contact>,
    onDismiss: () -> Unit,
    config: ContactPickerConfig = ContactPickerConfig(),
    onContactSelected: (com.tang.prm.domain.model.Contact) -> Unit,
    selectedContacts: List<com.tang.prm.domain.model.Contact> = emptyList()
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DialogDefaults.containerColor,
        shape = RoundedCornerShape(Dimens.cornerXl),
        title = {
            Column {
                Text(config.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                if (config.subtitle != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(config.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            if (contacts.isEmpty()) {
                ContactPickerEmptyState(
                    emptyText = config.emptyText,
                    onCreateContact = config.onCreateContact?.let { create -> { onDismiss(); create() } }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(contacts, key = { it.id }) { contact ->
                        val isSelected = selectedContacts.any { it.id == contact.id }
                        ContactPickerItem(
                            contact = contact,
                            isSelected = isSelected,
                            multiSelect = config.multiSelect,
                            onClick = { onContactSelected(contact) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(if (config.multiSelect) "完成" else "取消", color = MaterialTheme.colorScheme.primary) }
        }
    )
}

@Composable
private fun ContactPickerEmptyState(
    emptyText: String,
    onCreateContact: (() -> Unit)?
) {
    Box(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = AnimationTokens.Alpha.faint), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.People, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(emptyText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (onCreateContact != null) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { onCreateContact() },
                    shape = RoundedCornerShape(Dimens.cornerMedium)
                ) {
                    Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("新建联系人")
                }
            }
        }
    }
}

@Composable
private fun ContactPickerItem(
    contact: com.tang.prm.domain.model.Contact,
    isSelected: Boolean,
    multiSelect: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).then(
            if (multiSelect && isSelected) Modifier
            else Modifier.clickable { onClick() }
        ),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ContactAvatar(avatar = contact.avatar, name = contact.name, size = 42.dp)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(contact.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                contact.nickname?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
        }
    }
}
