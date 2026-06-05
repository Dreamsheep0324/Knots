@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.SmartToy
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tang.prm.domain.model.ThemeMode
import com.tang.prm.ui.navigation.ThemeSettingsRoute
import com.tang.prm.ui.navigation.AiConfigRoute
import com.tang.prm.ui.navigation.BackupRestoreRoute
import com.tang.prm.ui.navigation.AboutRoute
import com.tang.prm.ui.theme.Dimens

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

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
            item { SectionLabel("外观") }
            item {
                SettingsCard {
                    ThemeEntryItem(
                        currentMode = themeMode,
                        onClick = { navController.navigate(ThemeSettingsRoute) }
                    )
                }
            }
            item { SectionLabel("AI配置") }
            item {
                SettingsCard {
                    SettingsEntryItem(
                        icon = Icons.Default.SmartToy,
                        iconTint = Color(0xFF4CAF50),
                        title = "AI配置",
                        subtitle = "API密钥、地址与模型设置",
                        onClick = { navController.navigate(AiConfigRoute) }
                    )
                }
            }
            item { SectionLabel("数据") }
            item {
                SettingsCard {
                    SettingsEntryItem(
                        icon = Icons.Default.CloudUpload,
                        iconTint = Color(0xFF2196F3),
                        title = "数据备份与恢复",
                        subtitle = "导出或恢复应用数据",
                        onClick = { navController.navigate(BackupRestoreRoute) }
                    )
                }
            }
            item { SectionLabel("其他") }
            item {
                SettingsCard {
                    SettingsEntryItem(
                        icon = Icons.Default.Info,
                        iconTint = Color(0xFF9C27B0),
                        title = "关于",
                        subtitle = "版本信息与应用说明",
                        onClick = { navController.navigate(AboutRoute) }
                    )
                }
            }
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
