@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tang.prm.ui.theme.Success
import com.tang.prm.ui.components.AppConfirmDialog
import com.tang.prm.ui.components.SimpleSectionLabel

@Composable
fun BackupScreen(
    navController: NavController,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val state = uiState.dialog.operationState
    val backupFiles = uiState.data.backupFiles
    val autoBackupEnabled = uiState.data.autoBackupEnabled
    val hasBackupDir = uiState.data.hasBackupDir
    val backupDirName = uiState.data.backupDirName

    var showRestoreConfirm by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showRestoreLocalConfirm by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<String?>(null) }

    // 选择备份目录
    val dirLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let { viewModel.setBackupDir(it) }
    }

    // 从外部文件恢复
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.restoreFromUri(it) }
    }

    LaunchedEffect(state) {
        if (state is BackupState.BackupSuccess || state is BackupState.RestoreSuccess ||
            state is BackupState.BackupError || state is BackupState.RestoreError ||
            state is BackupState.ClearSuccess || state is BackupState.ClearError
        ) {
            kotlinx.coroutines.delay(3000)
            viewModel.resetState()
        }
    }

    // 确认弹窗
    if (showRestoreConfirm) {
        AppConfirmDialog(
            title = "确认恢复", text = "恢复数据将覆盖当前所有数据，此操作不可撤销。建议先备份当前数据再恢复。\n\n恢复后应用将自动重启。",
            confirmLabel = "确认恢复", isDestructive = true,
            onConfirm = { showRestoreConfirm = false; restoreLauncher.launch(arrayOf("application/zip", "application/octet-stream")) },
            onDismiss = { showRestoreConfirm = false }
        )
    }
    showRestoreLocalConfirm?.let { fileName ->
        AppConfirmDialog(
            title = "确认恢复", text = "将从备份「$fileName」恢复数据，当前所有数据将被覆盖，此操作不可撤销。\n\n恢复后应用将自动重启。",
            confirmLabel = "确认恢复", isDestructive = true,
            onConfirm = { showRestoreLocalConfirm = null; viewModel.restoreFromLocal(fileName) },
            onDismiss = { showRestoreLocalConfirm = null }
        )
    }
    if (showClearConfirm) {
        AppConfirmDialog(
            title = "确认清空", text = "清空数据将删除所有联系人、事件、纪念日、礼物、想法等全部数据，此操作不可撤销。\n\n强烈建议先备份当前数据再清空。清空后应用将自动重启。",
            confirmLabel = "确认清空", isDestructive = true,
            onConfirm = { showClearConfirm = false; viewModel.clearAllData() },
            onDismiss = { showClearConfirm = false }
        )
    }
    showDeleteConfirm?.let { fileName ->
        AppConfirmDialog(
            title = "删除备份", text = "确定要删除备份「$fileName」吗？此操作不可撤销。",
            confirmLabel = "确认删除", isDestructive = true,
            onConfirm = { showDeleteConfirm = null; viewModel.deleteBackupFile(fileName) },
            onDismiss = { showDeleteConfirm = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("数据备份与恢复", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // === 备份目录 ===
            item { SimpleSectionLabel("备份目录") }

            item {
                OutlinedButton(
                    onClick = { dirLauncher.launch(null) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.FolderOpen, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (hasBackupDir) backupDirName else "选择备份文件夹")
                }
            }

            // === 备份区域 ===
            item { SimpleSectionLabel("备份") }

            // 自动备份开关
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("自动备份", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text("数据变更时自动备份", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = autoBackupEnabled,
                        onCheckedChange = { viewModel.setAutoBackupEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            checkedThumbColor = Color.White,
                            uncheckedTrackColor = MaterialTheme.colorScheme.outline,
                            uncheckedThumbColor = MaterialTheme.colorScheme.surfaceVariant,
                            uncheckedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }

            item {
                Button(
                    onClick = { viewModel.createBackup() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = hasBackupDir && state !is BackupState.BackingUp
                ) {
                    if (state is BackupState.BackingUp) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("正在备份...")
                    } else if (state is BackupState.BackupSuccess) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("备份成功")
                    } else {
                        Icon(Icons.Default.CloudUpload, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("备份数据")
                    }
                }
            }

            if (!hasBackupDir) {
                item {
                    Text("请先选择备份文件夹", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp))
                }
            }
            if (state is BackupState.BackupError) {
                item { ErrorTip((state as BackupState.BackupError).message) }
            }

            // === 恢复区域 ===
            item { SimpleSectionLabel("恢复") }

            item {
                OutlinedButton(
                    onClick = { showRestoreConfirm = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Success),
                    enabled = state !is BackupState.Restoring
                ) {
                    if (state is BackupState.Restoring) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Success)
                        Spacer(Modifier.width(8.dp))
                        Text("正在恢复...")
                    } else if (state is BackupState.RestoreSuccess) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("恢复成功")
                    } else {
                        Icon(Icons.Default.CloudDownload, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("从文件恢复")
                    }
                }
            }

            if (state is BackupState.RestoreSuccess) { item { SuccessTip("恢复成功，应用将自动重启") } }
            if (state is BackupState.RestoreError) { item { ErrorTip((state as BackupState.RestoreError).message) } }

            // === 危险操作 ===
            item { SimpleSectionLabel("危险操作") }

            item {
                OutlinedButton(
                    onClick = { showClearConfirm = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    enabled = state !is BackupState.Clearing
                ) {
                    if (state is BackupState.Clearing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text("正在清空...")
                    } else if (state is BackupState.ClearSuccess) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("已清空")
                    } else {
                        Icon(Icons.Default.DeleteForever, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("清空所有数据")
                    }
                }
            }

            if (state is BackupState.ClearSuccess) { item { SuccessTip("数据已清空，应用将自动重启") } }
            if (state is BackupState.ClearError) { item { ErrorTip((state as BackupState.ClearError).message) } }

            // === 备份列表 ===
            item { SimpleSectionLabel("备份列表") }

            if (backupFiles.isEmpty()) {
                item {
                    Text(
                        if (hasBackupDir) "备份文件夹中暂无备份" else "请先选择备份文件夹",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                    )
                }
            } else {
                items(count = backupFiles.size, key = { backupFiles[it].fileName }) { index ->
                    val fileInfo = backupFiles[index]
                    val isDeleting = state is BackupState.DeletingBackup && (state as BackupState.DeletingBackup).fileName == fileInfo.fileName
                    BackupFileItem(
                        fileInfo = fileInfo,
                        isDeleting = isDeleting,
                        onRestore = { showRestoreLocalConfirm = fileInfo.fileName },
                        onDelete = { showDeleteConfirm = fileInfo.fileName }
                    )
                }
            }
        }
    }
}
