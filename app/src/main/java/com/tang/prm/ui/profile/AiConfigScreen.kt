@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tang.prm.ui.theme.Dimens

@Composable
fun AiConfigScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val apiKey by viewModel.aiApiKey.collectAsStateWithLifecycle()
    val baseUrl by viewModel.aiBaseUrl.collectAsStateWithLifecycle()
    val aiModel by viewModel.aiModel.collectAsStateWithLifecycle()
    val testState by viewModel.testState.collectAsStateWithLifecycle()

    var apiKeyInput by remember { mutableStateOf(apiKey) }
    var baseUrlInput by remember { mutableStateOf(baseUrl) }
    var showApiKey by remember { mutableStateOf(false) }

    LaunchedEffect(apiKey) { apiKeyInput = apiKey }
    LaunchedEffect(baseUrl) { baseUrlInput = baseUrl }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI配置", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
        ) {
            ApiKeySection(
                apiKeyInput = apiKeyInput,
                showApiKey = showApiKey,
                onApiKeyInputChange = {
                    apiKeyInput = it
                    viewModel.resetTestState()
                },
                onToggleShowApiKey = { showApiKey = !showApiKey },
                onSave = { viewModel.setAiApiKey(apiKeyInput) },
                hasChanges = apiKeyInput != apiKey
            )

            Spacer(modifier = Modifier.height(12.dp))

            BaseUrlSection(
                baseUrlInput = baseUrlInput,
                onBaseUrlInputChange = {
                    baseUrlInput = it
                    viewModel.resetTestState()
                },
                onSave = { viewModel.setAiBaseUrl(baseUrlInput.trimEnd('/')) },
                hasChanges = baseUrlInput.trimEnd('/') != baseUrl
            )

            Spacer(modifier = Modifier.height(12.dp))

            ModelSelectionSection(
                aiModel = aiModel,
                onModelSelected = { viewModel.setAiModel(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            TestConnectionSection(
                testState = testState,
                apiKey = apiKey,
                onTest = { viewModel.testConnection() }
            )
        }
    }
}
