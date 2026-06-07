package com.tang.prm.feature.chat.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.model.Contact
import com.tang.prm.ui.components.AppCard
import com.tang.prm.ui.components.ContactAvatar
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.Primary

@Composable
internal fun ContactSelectionCard(
    selectedContact: Contact?,
    onSelectClick: () -> Unit,
    onChangeClick: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Dimens.paddingCard)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(3.dp).height(16.dp).background(Primary, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.Person, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("对话人物", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedContact != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ContactAvatar(avatar = selectedContact.avatar, name = selectedContact.name, size = 44)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(selectedContact.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        selectedContact.relationship?.takeIf { it.isNotBlank() }?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    TextButton(onClick = onChangeClick) {
                        Text("更换", color = Primary, fontSize = 13.sp)
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onSelectClick),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Primary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("选择人物", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
