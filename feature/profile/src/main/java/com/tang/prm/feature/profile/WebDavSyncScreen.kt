@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tang.prm.domain.model.CloudBackupVersion
import com.tang.prm.domain.model.WebDavConfig
import com.tang.prm.domain.util.DateUtils
import com.tang.prm.ui.components.AppConfirmDialog
import com.tang.prm.ui.theme.AnniversaryHoliday
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.Error
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.SignalSky

@Composable
fun WebDavSyncScreen(
    onBack: () -> Unit,
    viewModel: WebDavSyncViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val config = uiState.data.config
    val cloudVersions = uiState.data.cloudVersions
    val syncState = uiState.dialog.syncState
    val connectionState = uiState.dialog.connectionState
    val cleanResult = uiState.dialog.cleanResult

    var serverUrl by remember(config.serverUrl) { mutableStateOf(config.serverUrl) }
    var username by remember(config.username) { mutableStateOf(config.username) }
    var password by remember(config.password) { mutableStateOf(config.password) }
    var remotePath by remember(config.remotePath) { mutableStateOf(config.remotePath) }
    var autoSyncOnLaunch by remember(config.autoSyncOnLaunch) { mutableStateOf(config.autoSyncOnLaunch) }
    var trustAllCertificates by remember(config.trustAllCertificates) { mutableStateOf(config.trustAllCertificates) }
    var passwordVisible by remember { mutableStateOf(false) }
    var configExpanded by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    val isConnected = connectionState is ConnectionState.Success

    LaunchedEffect(serverUrl, username, password, remotePath, autoSyncOnLaunch, trustAllCertificates) {
        delay(500)
        viewModel.updateConfig(
            WebDavConfig(
                serverUrl = serverUrl, username = username, password = password,
                remotePath = remotePath, autoSyncOnLaunch = autoSyncOnLaunch,
                lastSyncTime = config.lastSyncTime, lastSyncDirection = config.lastSyncDirection,
                trustAllCertificates = trustAllCertificates
            )
        )
    }

    LaunchedEffect(syncState) {
        if (syncState is SyncState.UploadSuccess || syncState is SyncState.DownloadSuccess || syncState is SyncState.PartialSuccess || syncState is SyncState.Error) {
            kotlinx.coroutines.delay(3000)
            viewModel.resetSyncState()
        }
    }

    showRestoreDialog?.let { fileName ->
        AppConfirmDialog(
            title = "确认恢复", text = "恢复数据将覆盖当前所有数据，此操作不可撤销。\n\n恢复后应用将自动重启。",
            confirmLabel = "确认恢复", isDestructive = true,
            onConfirm = { viewModel.downloadBackup(fileName); showRestoreDialog = null },
            onDismiss = { showRestoreDialog = null }
        )
    }

    showDeleteDialog?.let { fileName ->
        AppConfirmDialog(
            title = "确认删除", text = "确定删除云端备份？此操作不可撤销。",
            confirmLabel = "确认删除", isDestructive = true,
            onConfirm = { viewModel.deleteRemoteBackup(fileName); showDeleteDialog = null },
            onDismiss = { showDeleteDialog = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WebDAV 同步", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ConnectionHeroSection(connectionState, config) }

            item {
                WebDavConfigPanel(
                    expanded = configExpanded, onToggle = { configExpanded = !configExpanded },
                    serverUrl = serverUrl, onServerUrlChange = { serverUrl = it },
                    username = username, onUsernameChange = { username = it },
                    password = password, onPasswordChange = { password = it },
                    remotePath = remotePath, onRemotePathChange = { remotePath = it },
                    trustAllCertificates = trustAllCertificates, onTrustAllCertificatesChange = { trustAllCertificates = it },
                    passwordVisible = passwordVisible, onPasswordToggle = { passwordVisible = !passwordVisible },
                    onTestConnection = { viewModel.testConnection() },
                    isTesting = connectionState is ConnectionState.Testing, isConnected = isConnected
                )
            }

            item {
                SyncActionButtons(
                    onUpload = { viewModel.uploadBackup() },
                    onDownload = {
                        val latest = cloudVersions.firstOrNull()
                        if (latest != null) showRestoreDialog = latest.fileName
                    },
                    uploadState = Triple(
                        syncState is SyncState.Uploading,
                        syncState is SyncState.UploadSuccess,
                        syncState is SyncState.Error
                    ),
                    downloadState = Triple(
                        syncState is SyncState.Downloading,
                        syncState is SyncState.DownloadSuccess,
                        false
                    )
                )
            }

            // 上传进度条
            if (syncState is SyncState.Uploading) {
                item { SyncProgressBar((syncState as SyncState.Uploading).phase, (syncState as SyncState.Uploading).current, (syncState as SyncState.Uploading).total, (syncState as SyncState.Uploading).detail, SignalSky) }
            }

            // 下载进度条
            if (syncState is SyncState.Downloading) {
                item { SyncProgressBar((syncState as SyncState.Downloading).phase, (syncState as SyncState.Downloading).current, (syncState as SyncState.Downloading).total, (syncState as SyncState.Downloading).detail, SignalGreen) }
            }

            if (syncState is SyncState.UploadSuccess) {
                val s = syncState as SyncState.UploadSuccess
                item { SyncResultBanner("上传成功 · 新增 ${s.uploadedImages} 张 · 跳过 ${s.skippedImages} 张", isSuccess = true) }
            }
            if (syncState is SyncState.DownloadSuccess) {
                val s = syncState as SyncState.DownloadSuccess
                item { SyncResultBanner("恢复成功 · 下载 ${s.downloadedImages} 张 · 跳过 ${s.skippedImages} 张", isSuccess = true) }
            }
            if (syncState is SyncState.PartialSuccess) {
                val s = syncState as SyncState.PartialSuccess
                item { SyncResultBanner("部分成功 · 成功 ${s.succeeded} 张 · 失败 ${s.failed} 张 · 跳过 ${s.skipped} 张", isSuccess = true) }
            }
            if (syncState is SyncState.Error) {
                item { SyncResultBanner((syncState as SyncState.Error).message, isSuccess = false) }
            }

            item {
                AutoSyncToggle(
                    autoSyncOnLaunch = autoSyncOnLaunch, onToggle = { autoSyncOnLaunch = it },
                    lastSyncTime = config.lastSyncTime, lastSyncDirection = config.lastSyncDirection
                )
            }

            item {
                CleanOrphanedImagesButton(
                    onClean = { viewModel.cleanOrphanedImages() },
                    isCleaning = cleanResult is CleanResult.Cleaning,
                    cleanCount = if (cleanResult is CleanResult.Done) (cleanResult as CleanResult.Done).count else null,
                    onDismiss = { viewModel.resetCleanResult() }
                )
            }

            item { CloudBackupHeader(onRefresh = { viewModel.refreshCloudVersions() }) }

            if (cloudVersions.isEmpty()) {
                item { CloudEmptyState() }
            } else {
                items(cloudVersions, key = { it.fileName }) { version ->
                    CloudBackupItem(
                        version = version,
                        onRestore = { showRestoreDialog = version.fileName },
                        onDelete = { showDeleteDialog = version.fileName },
                        isRestoring = syncState is SyncState.Downloading
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// ===== 连接状态 Hero =====

@Composable
private fun ConnectionHeroSection(state: ConnectionState, config: WebDavConfig) {
    val (icon, label, tint, bgGradient) = when (state) {
        is ConnectionState.Success -> Tuple4(
            Icons.Default.CloudDone, "已连接", SignalGreen,
            listOf(SignalGreen.copy(alpha = 0.15f), SignalGreen.copy(alpha = 0.05f))
        )
        is ConnectionState.Testing -> Tuple4(
            Icons.Default.CloudSync, "正在连接...", MaterialTheme.colorScheme.primary,
            listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), MaterialTheme.colorScheme.primary.copy(alpha = 0.03f))
        )
        is ConnectionState.Error -> Tuple4(
            Icons.Default.CloudOff, "连接失败", Error,
            listOf(Error.copy(alpha = 0.12f), Error.copy(alpha = 0.03f))
        )
        else -> Tuple4(
            Icons.Default.Cloud,
            if (config.serverUrl.isBlank()) "未配置" else "未连接",
            MaterialTheme.colorScheme.onSurfaceVariant,
            listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(bgGradient), RoundedCornerShape(20.dp))
            .border(1.dp, tint.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(tint.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (state is ConnectionState.Testing) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp, color = tint)
                } else {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp), tint = tint)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = tint)
                if (state is ConnectionState.Success && config.serverUrl.isNotBlank()) {
                    Text(
                        config.serverUrl.removeSuffix("/").takeLast(30),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
                if (state is ConnectionState.Error) {
                    Text(
                        state.message,
                        style = MaterialTheme.typography.labelSmall,
                        color = Error.copy(alpha = 0.8f),
                        maxLines = 2, overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ===== 服务器配置面板（可折叠） =====

@Composable
private fun WebDavConfigPanel(
    expanded: Boolean,
    onToggle: () -> Unit,
    serverUrl: String,
    onServerUrlChange: (String) -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    remotePath: String,
    onRemotePathChange: (String) -> Unit,
    trustAllCertificates: Boolean,
    onTrustAllCertificatesChange: (Boolean) -> Unit,
    passwordVisible: Boolean,
    onPasswordToggle: () -> Unit,
    onTestConnection: () -> Unit,
    isTesting: Boolean,
    isConnected: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.cornerCard))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(Dimens.cornerCard))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "服务器配置",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            if (!expanded && serverUrl.isNotBlank()) {
                Text(
                    serverUrl.removePrefix("https://").removePrefix("http://").take(20),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "收起" else "展开",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CompactConfigField(serverUrl, onServerUrlChange, "服务器地址", "https://dav.jianguoyun.com/dav/", Icons.Default.Link)
                CompactConfigField(username, onUsernameChange, "用户名", "", Icons.Default.Person)
                CompactConfigField(
                    password, onPasswordChange, "密码", "", Icons.Default.Key,
                    isPassword = true, passwordVisible = passwordVisible, onPasswordToggle = onPasswordToggle
                )
                CompactConfigField(remotePath, onRemotePathChange, "远程目录", "/knots_backup/", Icons.Default.Folder)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("信任所有证书", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text("自建服务器证书不受信任时开启", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    }
                    Switch(
                        checked = trustAllCertificates,
                        onCheckedChange = onTrustAllCertificatesChange,
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            checkedThumbColor = Color.White,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Button(
                    onClick = onTestConnection,
                    enabled = !isTesting && serverUrl.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isConnected) SignalGreen else MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                    )
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("正在连接...")
                    } else if (isConnected) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("已连接 · 重新测试")
                    } else {
                        Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("测试连接")
                    }
                }
            }
        }
    }
}

// ===== 同步操作按钮 =====

@Composable
private fun SyncActionButtons(
    onUpload: () -> Unit,
    onDownload: () -> Unit,
    uploadState: Triple<Boolean, Boolean, Boolean>,
    downloadState: Triple<Boolean, Boolean, Boolean>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SyncActionButton(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.CloudUpload,
            label = "上传",
            description = "备份到云端",
            tint = SignalSky,
            isLoading = uploadState.first,
            isSuccess = uploadState.second,
            onClick = onUpload
        )
        SyncActionButton(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.CloudSync,
            label = "恢复",
            description = "从云端下载",
            tint = SignalGreen,
            isLoading = downloadState.first,
            isSuccess = downloadState.second,
            onClick = onDownload
        )
    }
}

@Composable
private fun SyncActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    description: String,
    tint: Color,
    isLoading: Boolean,
    isSuccess: Boolean,
    onClick: () -> Unit
) {
    val bgColor = when {
        isSuccess -> SignalGreen.copy(alpha = 0.12f)
        isLoading -> tint.copy(alpha = 0.08f)
        else -> MaterialTheme.colorScheme.surface
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, tint.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .clickable(enabled = !isLoading) { onClick() }
            .padding(vertical = 20.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(tint.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp, color = tint)
            } else if (isSuccess) {
                Icon(Icons.Default.Check, contentDescription = null, tint = SignalGreen, modifier = Modifier.size(24.dp))
            } else {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold,
            color = if (isSuccess) SignalGreen else MaterialTheme.colorScheme.onSurface)
        Text(
            if (isLoading) "进行中..." else description,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp
        )
    }
}

// ===== 同步结果横幅 =====

@Composable
private fun SyncResultBanner(message: String, isSuccess: Boolean) {
    val tint = if (isSuccess) SignalGreen else MaterialTheme.colorScheme.error
    val bgColor = if (isSuccess) SignalGreen.copy(alpha = 0.1f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)

    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = bgColor) {
        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (isSuccess) Icons.Default.Check else Icons.Default.CloudOff, contentDescription = null, modifier = Modifier.size(16.dp), tint = tint)
            Spacer(modifier = Modifier.width(8.dp))
            Text(message, color = tint, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ===== 清理孤立图片 =====

@Composable
private fun CleanOrphanedImagesButton(
    onClean: () -> Unit,
    isCleaning: Boolean,
    cleanCount: Int?,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.cornerCard))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(Dimens.cornerCard))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isCleaning) { onClean() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (cleanCount != null && cleanCount > 0) SignalGreen.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCleaning) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp, color = MaterialTheme.colorScheme.primary)
                } else {
                    Icon(
                        Icons.Default.CleaningServices,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = if (cleanCount != null && cleanCount > 0) SignalGreen else MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("清理无用图片", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    if (isCleaning) "正在清理..."
                    else if (cleanCount != null) "已清理 $cleanCount 张孤立图片"
                    else "删除裁剪残留等未被引用的图片文件",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp
                )
            }
            if (cleanCount != null && !isCleaning) {
                TextButton(onClick = onDismiss) {
                    Text("关闭", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
        }
    }
}

// ===== 自动同步开关 =====

@Composable
private fun AutoSyncToggle(
    autoSyncOnLaunch: Boolean,
    onToggle: (Boolean) -> Unit,
    lastSyncTime: Long,
    lastSyncDirection: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.cornerCard))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(Dimens.cornerCard))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle(!autoSyncOnLaunch) }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (autoSyncOnLaunch) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CloudSync,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = if (autoSyncOnLaunch) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("启动时自动同步", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "打开应用时自动下载云端最新数据",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp
                )
            }
            Switch(
                checked = autoSyncOnLaunch,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    checkedThumbColor = Color.White,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        if (lastSyncTime > 0) {
            val direction = if (lastSyncDirection == "upload") "上传" else "下载"
            val timeStr = DateUtils.formatDateTimeHyphen(lastSyncTime)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Cloud, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(6.dp))
                Text("上次同步：$timeStr ($direction)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ===== 云端备份列表 =====

@Composable
private fun CloudBackupHeader(onRefresh: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("云端备份", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
        IconButton(onClick = onRefresh, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Refresh, contentDescription = "刷新", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CloudEmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Cloud, contentDescription = null, modifier = Modifier.size(44.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f))
        Spacer(modifier = Modifier.height(8.dp))
        Text("暂无云端备份", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        Text("点击上方「上传」创建第一份备份", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f), fontSize = 12.sp)
    }
}

@Composable
private fun CloudBackupItem(
    version: CloudBackupVersion,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    isRestoring: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(10.dp).background(AnniversaryHoliday.copy(alpha = 0.6f), CircleShape))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(version.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (version.isIncremental) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(SignalSky.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text("增量", style = MaterialTheme.typography.labelSmall, color = SignalSky, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            val info = buildString {
                append(formatFileSize(version.fileSize))
                append(" · ")
                append(version.lastModified)
                if (version.imageCount > 0) {
                    append(" · ")
                    append("${version.imageCount} 张图片")
                }
            }
            Text(info, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
            IconButton(onClick = onRestore, enabled = !isRestoring, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Restore, contentDescription = "恢复", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete, enabled = !isRestoring, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "删除", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
            }
        }
    }
}

// ===== 紧凑配置输入框 =====

@Composable
private fun CompactConfigField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        placeholder = { Text(placeholder, fontSize = 12.sp) },
        leadingIcon = {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        trailingIcon = if (isPassword && onPasswordToggle != null) {
            {
                IconButton(onClick = onPasswordToggle, modifier = Modifier.size(32.dp)) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null, modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            focusedContainerColor = Color.Transparent
        )
    )
}

// ===== 同步进度条 =====

@Composable
private fun SyncProgressBar(phase: String, current: Int, total: Int, detail: String, tint: Color) {
    val percent = if (total > 0) (current * 100 / total).coerceIn(0, 100) else 0
    val animatedProgress by animateFloatAsState(
        targetValue = percent / 100f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "sync_progress"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, tint.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "$phase $percent%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = tint
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = tint,
            trackColor = tint.copy(alpha = 0.15f),
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            detail,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}

// ===== 工具 =====

private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

private fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "${"%.1f".format(size / 1024.0)} KB"
        size < 1024 * 1024 * 1024 -> "${"%.1f".format(size / (1024.0 * 1024.0))} MB"
        else -> "${"%.1f".format(size / (1024.0 * 1024.0 * 1024.0))} GB"
    }
}
