@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tang.prm.ui.theme.Primary
import com.tang.prm.ui.components.AppConfirmDialog

@Composable
fun BackupScreen(
    navController: NavController,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showRestoreConfirm by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        uri?.let { viewModel.createBackup(it) }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.restoreBackup(it) }
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

    if (showRestoreConfirm) {
        AppConfirmDialog(
            title = "确认恢复",
            text = "恢复数据将覆盖当前所有数据，此操作不可撤销。建议先备份当前数据再恢复。\n\n恢复后应用将自动重启。",
            confirmLabel = "确认恢复",
            isDestructive = true,
            onConfirm = {
                showRestoreConfirm = false
                restoreLauncher.launch(arrayOf("application/zip", "application/octet-stream"))
            },
            onDismiss = { showRestoreConfirm = false }
        )
    }

    if (showClearConfirm) {
        AppConfirmDialog(
            title = "确认清空",
            text = "清空数据将删除所有联系人、事件、纪念日、礼物、想法等全部数据，此操作不可撤销。\n\n强烈建议先备份当前数据再清空。清空后应用将自动重启。",
            confirmLabel = "确认清空",
            isDestructive = true,
            onConfirm = {
                showClearConfirm = false
                viewModel.clearAllData()
            },
            onDismiss = { showClearConfirm = false }
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { BackupSectionLabel("备份") }

            item {
                BackupSettingsCard {
                    BackupEntryItem(
                        icon = Icons.Default.CloudUpload,
                        iconTint = Color(0xFF2196F3),
                        title = "备份数据",
                        subtitle = if (state is BackupState.BackingUp) "正在备份..." else "将全部数据导出为备份文件",
                        trailing = {
                            if (state is BackupState.BackingUp) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Primary
                                )
                            } else if (state is BackupState.BackupSuccess) {
                                Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                            } else {
                                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
                            }
                        },
                        onClick = {
                            val fileName = viewModel.generateBackupFileName()
                            backupLauncher.launch(fileName)
                        }
                    )
                }
            }

            if (state is BackupState.BackupSuccess) {
                item { SuccessTip("备份成功") }
            }
            if (state is BackupState.BackupError) {
                item { ErrorTip((state as BackupState.BackupError).message) }
            }

            item { BackupSectionLabel("恢复") }

            item {
                BackupSettingsCard {
                    BackupEntryItem(
                        icon = Icons.Default.CloudDownload,
                        iconTint = Color(0xFF4CAF50),
                        title = "恢复数据",
                        subtitle = if (state is BackupState.Restoring) "正在恢复..." else "从备份文件恢复数据",
                        trailing = {
                            if (state is BackupState.Restoring) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Primary
                                )
                            } else if (state is BackupState.RestoreSuccess) {
                                Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                            } else {
                                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
                            }
                        },
                        onClick = { showRestoreConfirm = true }
                    )
                }
            }

            if (state is BackupState.RestoreSuccess) {
                item { SuccessTip("恢复成功，请重启应用") }
            }
            if (state is BackupState.RestoreError) {
                item { ErrorTip((state as BackupState.RestoreError).message) }
            }

            item { BackupSectionLabel("危险操作") }

            item {
                BackupSettingsCard {
                    BackupEntryItem(
                        icon = Icons.Default.DeleteForever,
                        iconTint = Color(0xFFE53935),
                        title = "清空数据",
                        subtitle = if (state is BackupState.Clearing) "正在清空..." else "删除所有数据，恢复初始状态",
                        trailing = {
                            if (state is BackupState.Clearing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFFE53935)
                                )
                            } else if (state is BackupState.ClearSuccess) {
                                Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                            } else {
                                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
                            }
                        },
                        onClick = { showClearConfirm = true }
                    )
                }
            }

            if (state is BackupState.ClearSuccess) {
                item { SuccessTip("数据已清空，请重启应用") }
            }
            if (state is BackupState.ClearError) {
                item { ErrorTip((state as BackupState.ClearError).message) }
            }
        }
    }
}
