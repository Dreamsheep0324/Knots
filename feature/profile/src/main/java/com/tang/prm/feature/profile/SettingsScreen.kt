@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.TabletAndroid
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tang.prm.domain.model.ThemeMode
import com.tang.prm.ui.navigation.ThemeSettingsRoute
import com.tang.prm.ui.navigation.AiConfigRoute
import com.tang.prm.ui.navigation.BackupRestoreRoute
import com.tang.prm.ui.navigation.WebDavSyncRoute
import com.tang.prm.ui.navigation.AboutRoute
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.Success
import com.tang.prm.ui.components.SimpleSectionLabel
import com.tang.prm.ui.theme.Warning

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val tabletModeEnabled by viewModel.tabletModeEnabled.collectAsStateWithLifecycle()
    val encryptionDegraded by viewModel.encryptionDegraded.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置", fontWeight = FontWeight.Bold) },
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
            if (encryptionDegraded) {
                item { EncryptionDegradedWarning() }
            }
            item { SimpleSectionLabel("外观") }
            item {
                SettingsCard {
                    ThemeEntryItem(
                        currentMode = themeMode,
                        onClick = { navController.navigate(ThemeSettingsRoute) }
                    )
                    SettingsToggleItem(
                        icon = Icons.Default.TabletAndroid,
                        iconTint = SignalPurple,
                        title = "平板模式",
                        subtitle = "在大屏设备上启用侧边栏与双栏布局",
                        checked = tabletModeEnabled,
                        onCheckedChange = { viewModel.setTabletModeEnabled(it) }
                    )
                }
            }
            item { SimpleSectionLabel("AI配置") }
            item {
                SettingsCard {
                    SettingsEntryItem(
                        icon = Icons.Default.SmartToy,
                        iconTint = Success,
                        title = "AI配置",
                        subtitle = "API密钥、地址与模型设置",
                        onClick = { navController.navigate(AiConfigRoute) }
                    )
                }
            }
            item { SimpleSectionLabel("数据") }
            item {
                SettingsCard {
                    SettingsEntryItem(
                        icon = Icons.Default.CloudUpload,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "数据备份与恢复",
                        subtitle = "导出或恢复应用数据",
                        onClick = { navController.navigate(BackupRestoreRoute) }
                    )
                    SettingsEntryItem(
                        icon = Icons.Default.CloudSync,
                        iconTint = SignalGreen,
                        title = "WebDAV 同步",
                        subtitle = "通过 WebDAV 同步数据到云端",
                        onClick = { navController.navigate(WebDavSyncRoute) }
                    )
                }
            }
            item { SimpleSectionLabel("其他") }
            item {
                SettingsCard {
                    SettingsEntryItem(
                        icon = Icons.Default.Info,
                        iconTint = SignalPurple,
                        title = "关于",
                        subtitle = "版本信息与应用说明",
                        onClick = { navController.navigate(AboutRoute) }
                    )
                }
            }
        }
    }
}

/**
 * 加密存储降级警告卡片。
 *
 * 当 EncryptedSharedPreferences 初始化失败（如系统不支持或密钥被破坏）时，
 * 敏感数据会以明文形式存储，此卡片提示用户该风险。
 */
@Composable
private fun EncryptionDegradedWarning() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Warning.copy(alpha = 0.12f))
            .border(1.dp, Warning.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Warning.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = Warning,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                "加密存储不可用",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "系统加密模块初始化失败，API 密钥等敏感数据暂以明文存储，请注意风险",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun ThemeScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("主题", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimens.paddingCard)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            ) {
                ThemeModeOption(
                    icon = Icons.Default.LightMode,
                    title = "浅色模式",
                    description = "始终使用浅色主题",
                    mode = ThemeMode.LIGHT,
                    currentMode = themeMode,
                    onModeSelected = { viewModel.setThemeMode(it) }
                )
                ThemeModeDivider()
                ThemeModeOption(
                    icon = Icons.Default.DarkMode,
                    title = "深色模式",
                    description = "始终使用深色主题",
                    mode = ThemeMode.DARK,
                    currentMode = themeMode,
                    onModeSelected = { viewModel.setThemeMode(it) }
                )
                ThemeModeDivider()
                ThemeModeOption(
                    icon = Icons.Default.SettingsBrightness,
                    title = "跟随系统",
                    description = "根据系统设置自动切换",
                    mode = ThemeMode.SYSTEM,
                    currentMode = themeMode,
                    onModeSelected = { viewModel.setThemeMode(it) }
                )
            }
        }
    }
}
