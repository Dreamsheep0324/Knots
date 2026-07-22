package com.tang.prm.feature.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.repository.AiRepository
import com.tang.prm.domain.repository.EncryptionStatusProvider
import com.tang.prm.domain.repository.HomeOrbitalMode
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
    private val aiRepository: AiRepository,
    // A-2 修复：注入 EncryptionStatusProvider 替代 domain 层全局单例
    encryptionStatusProvider: EncryptionStatusProvider
) : ViewModel() {

    /**
     * 加密存储是否处于降级模式（EncryptedSharedPreferences 初始化失败时回退到明文）。
     * UI 应在为 true 时提示用户敏感数据未加密。
     *
     * A-2 修复：改为订阅 [EncryptionStatusProvider.isDegraded] StateFlow，响应式获取状态变化。
     */
    val encryptionDegraded: StateFlow<Boolean> = encryptionStatusProvider.isDegraded

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    private fun launchWithErrorHandling(block: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { block() }.onFailure { Log.e(TAG, "操作失败", it) }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        launchWithErrorHandling { settingsRepository.setThemeMode(mode) }
    }

    val tabletModeEnabled: StateFlow<Boolean> = settingsRepository.tabletModeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setTabletModeEnabled(enabled: Boolean) {
        launchWithErrorHandling { settingsRepository.setTabletModeEnabled(enabled) }
    }

    /** 首页轨道模块显示模式：轨道罗盘 / 力导向图 */
    val homeOrbitalMode: StateFlow<HomeOrbitalMode> = settingsRepository.homeOrbitalMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeOrbitalMode.ORBITAL)

    fun setHomeOrbitalMode(mode: HomeOrbitalMode) {
        launchWithErrorHandling { settingsRepository.setHomeOrbitalMode(mode) }
    }

    val aiApiKey: StateFlow<String> = settingsRepository.aiApiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val aiBaseUrl: StateFlow<String> = settingsRepository.aiBaseUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "https://api.deepseek.com")

    val aiModel: StateFlow<String> = settingsRepository.aiModel
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "deepseek-v4-flash")

    fun setAiApiKey(key: String) {
        launchWithErrorHandling { settingsRepository.setAiApiKey(key) }
    }

    fun setAiBaseUrl(url: String) {
        launchWithErrorHandling { settingsRepository.setAiBaseUrl(url) }
    }

    fun setAiModel(model: String) {
        launchWithErrorHandling { settingsRepository.setAiModel(model) }
    }

    private val _testState = MutableStateFlow<TestConnectionState>(TestConnectionState.Idle)
    val testState: StateFlow<TestConnectionState> = _testState.asStateFlow()

    fun testConnection() {
        viewModelScope.launch {
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

    private companion object {
        const val TAG = "SettingsViewModel"
    }
}
