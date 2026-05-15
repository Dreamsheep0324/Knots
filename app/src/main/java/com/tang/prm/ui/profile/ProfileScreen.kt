@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import android.widget.Toast
import androidx.compose.material.icons.automirrored.filled.Help
import com.tang.prm.ui.theme.Primary
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.DialogDefaults

@Composable
fun ProfileScreen(navController: NavController, viewModel: ProfileViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val developingToast: () -> Unit = { Toast.makeText(context, "功能开发中", Toast.LENGTH_SHORT).show() }

    if (showEditDialog) {
        EditProfileDialog(
            currentName = uiState.userName,
            currentSignature = uiState.userSignature,
            onDismiss = { showEditDialog = false },
            onConfirm = { name, signature ->
                viewModel.updateProfile(name, signature)
                showEditDialog = false
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopAppBar(title = { Text("我的", fontWeight = FontWeight.Bold) })

        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { ProfileHeader(userName = uiState.userName, userSignature = uiState.userSignature, onEditClick = { showEditDialog = true }) }
            item { SettingsSection(title = "数据管理") {
                SettingsItem(icon = Icons.Default.CloudUpload, title = "数据备份", subtitle = "备份你的所有数据", onClick = developingToast)
                SettingsItem(icon = Icons.Default.CloudDownload, title = "数据恢复", subtitle = "从备份恢复数据", onClick = developingToast)
                SettingsItem(icon = Icons.Default.Share, title = "导出数据", subtitle = "导出为CSV或JSON", onClick = developingToast)
            }}
            item { SettingsSection(title = "提醒设置") {
                SettingsItem(icon = Icons.Default.Notifications, title = "提醒通知", subtitle = "配置提醒推送方式", onClick = developingToast)
                SettingsItem(icon = Icons.Default.Schedule, title = "默认提醒时间", subtitle = "设置提前提醒天数", onClick = developingToast)
            }}
            item { SettingsSection(title = "其他") {
                SettingsItem(icon = Icons.Default.Info, title = "关于我们", subtitle = "版本信息", onClick = developingToast)
                SettingsItem(icon = Icons.AutoMirrored.Filled.Help, title = "帮助与反馈", subtitle = "联系我们", onClick = developingToast)
            }}
        }
    }
}

@Composable
private fun ProfileHeader(userName: String, userSignature: String, onEditClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(80.dp).background(Primary, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(userName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(userSignature, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("编辑资料")
            }
        }
    }
}

@Composable
private fun EditProfileDialog(currentName: String, currentSignature: String, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf(currentName) }
    var signature by remember { mutableStateOf(currentSignature) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DialogDefaults.containerColor,
        title = { Text("编辑资料") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("昵称") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = signature, onValueChange = { signature = it }, label = { Text("个性签名") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, signature) }) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.titleSmall, color = Primary, fontWeight = FontWeight.Medium, modifier = Modifier.padding(vertical = 8.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Column(content = content) }
    }
}

@Composable
private fun SettingsItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(modifier = Modifier.fillMaxWidth().padding(Dimens.paddingCard), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}
