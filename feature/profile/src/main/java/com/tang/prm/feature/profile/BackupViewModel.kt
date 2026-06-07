package com.tang.prm.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.usecase.BackupRestoreUseCase
import com.tang.prm.domain.model.BackupFileInfo
import com.tang.prm.domain.model.BackupImageQuality
import com.tang.prm.domain.model.BackupInfo
import com.tang.prm.domain.model.ClearDataResult
import com.tang.prm.domain.model.RestoreResult
import com.tang.prm.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BackupState {
    data object Idle : BackupState()
    data object BackingUp : BackupState()
    data class BackupSuccess(val info: BackupInfo) : BackupState()
    data class BackupError(val message: String) : BackupState()
    data object Restoring : BackupState()
    data object RestoreSuccess : BackupState()
    data class RestoreError(val message: String) : BackupState()
    data object Clearing : BackupState()
    data object ClearSuccess : BackupState()
    data class ClearError(val message: String) : BackupState()
    data class DeletingBackup(val fileName: String) : BackupState()
}

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupRestoreUseCase: BackupRestoreUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow<BackupState>(BackupState.Idle)
    val state: StateFlow<BackupState> = _state.asStateFlow()

    private val _backupFiles = MutableStateFlow<List<BackupFileInfo>>(emptyList())
    val backupFiles: StateFlow<List<BackupFileInfo>> = _backupFiles.asStateFlow()

    private val _hasBackupDir = MutableStateFlow(false)
    val hasBackupDir: StateFlow<Boolean> = _hasBackupDir.asStateFlow()

    private val _backupDirName = MutableStateFlow("")
    val backupDirName: StateFlow<String> = _backupDirName.asStateFlow()

    // 自动备份
    private val _autoBackupEnabled = MutableStateFlow(false)
    val autoBackupEnabled: StateFlow<Boolean> = _autoBackupEnabled.asStateFlow()

    private var lastDataFingerprint: String = ""

    init {
        _hasBackupDir.value = backupRestoreUseCase.hasBackupDir()
        _backupDirName.value = backupRestoreUseCase.getBackupDirName()
        viewModelScope.launch {
            settingsRepository.getAutoBackupEnabled().collect { _autoBackupEnabled.value = it }
        }
        viewModelScope.launch { loadBackupFiles() }
    }

    fun setBackupDir(uri: android.net.Uri) {
        backupRestoreUseCase.setBackupDirUri(uri.toString())
        _hasBackupDir.value = true
        _backupDirName.value = backupRestoreUseCase.getBackupDirName()
        viewModelScope.launch { loadBackupFiles() }
    }

    fun setAutoBackupEnabled(enabled: Boolean) {
        _autoBackupEnabled.value = enabled
        viewModelScope.launch {
            settingsRepository.setAutoBackupEnabled(enabled)
            if (enabled) lastDataFingerprint = computeDataFingerprint()
        }
    }

    fun createBackup() {
        viewModelScope.launch {
            _state.value = BackupState.BackingUp
            try {
                val info = backupRestoreUseCase.backupToDir(BackupImageQuality.ORIGINAL)
                loadBackupFiles()
                lastDataFingerprint = computeDataFingerprint()
                _state.value = BackupState.BackupSuccess(info)
            } catch (e: Exception) {
                _state.value = BackupState.BackupError(e.message ?: "备份失败")
            }
        }
    }

    fun restoreFromLocal(fileName: String) {
        viewModelScope.launch {
            _state.value = BackupState.Restoring
            backupRestoreUseCase.restoreBackup(fileName).collect { result ->
                _state.value = when (result) {
                    is RestoreResult.Success -> BackupState.RestoreSuccess
                    is RestoreResult.Error -> BackupState.RestoreError(result.message)
                }
            }
        }
    }

    fun restoreFromUri(uri: android.net.Uri) {
        viewModelScope.launch {
            _state.value = BackupState.Restoring
            backupRestoreUseCase.restoreFromUri(uri.toString()).collect { result ->
                _state.value = when (result) {
                    is RestoreResult.Success -> BackupState.RestoreSuccess
                    is RestoreResult.Error -> BackupState.RestoreError(result.message)
                }
            }
        }
    }

    fun deleteBackupFile(fileName: String) {
        viewModelScope.launch {
            _state.value = BackupState.DeletingBackup(fileName)
            backupRestoreUseCase.deleteBackup(fileName)
            loadBackupFiles()
            _state.value = BackupState.Idle
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            _state.value = BackupState.Clearing
            when (val result = backupRestoreUseCase.clearAllData()) {
                is ClearDataResult.Success -> _state.value = BackupState.ClearSuccess
                is ClearDataResult.Error -> _state.value = BackupState.ClearError(result.message)
            }
        }
    }

    fun resetState() {
        _state.value = BackupState.Idle
    }

    private suspend fun loadBackupFiles() {
        _backupFiles.value = backupRestoreUseCase.listBackups()
    }

    private suspend fun computeDataFingerprint(): String {
        return try { backupRestoreUseCase.computeDataFingerprint() } catch (_: Exception) { "" }
    }

    fun checkAndAutoBackup() {
        if (!_autoBackupEnabled.value || !_hasBackupDir.value) return
        viewModelScope.launch {
            val currentFingerprint = computeDataFingerprint()
            if (currentFingerprint != lastDataFingerprint && currentFingerprint.isNotEmpty()) {
                lastDataFingerprint = currentFingerprint
                try {
                    backupRestoreUseCase.backupToDir(BackupImageQuality.ORIGINAL)
                    loadBackupFiles()
                } catch (_: Exception) {}
            }
        }
    }
}
