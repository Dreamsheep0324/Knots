package com.tang.prm.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.repository.AiRepository
import com.tang.prm.domain.repository.SettingsRepository
import com.tang.prm.domain.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TestConnectionState {
    object Idle : TestConnectionState()
    object Testing : TestConnectionState()
    data class Success(val message: String) : TestConnectionState()
    data class Error(val message: String) : TestConnectionState()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val aiRepository: AiRepository
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    val aiApiKey: StateFlow<String> = settingsRepository.aiApiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val aiBaseUrl: StateFlow<String> = settingsRepository.aiBaseUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "https://api.deepseek.com")

    val aiModel: StateFlow<String> = settingsRepository.aiModel
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "deepseek-v4-flash")

    fun setAiApiKey(key: String) {
        viewModelScope.launch { settingsRepository.setAiApiKey(key) }
    }

    fun setAiBaseUrl(url: String) {
        viewModelScope.launch { settingsRepository.setAiBaseUrl(url) }
    }

    fun setAiModel(model: String) {
        viewModelScope.launch { settingsRepository.setAiModel(model) }
    }

    private val _testState = MutableStateFlow<TestConnectionState>(TestConnectionState.Idle)
    val testState: StateFlow<TestConnectionState> = _testState.asStateFlow()

    fun testConnection() {
        viewModelScope.launch(Dispatchers.IO) {
            _testState.value = TestConnectionState.Testing
            val result = aiRepository.testConnection()
            _testState.value = result.fold(
                onSuccess = { TestConnectionState.Success(it) },
                onFailure = { TestConnectionState.Error(it.message ?: "未知错误") }
            )
        }
    }

    fun resetTestState() {
        _testState.value = TestConnectionState.Idle
    }
}
