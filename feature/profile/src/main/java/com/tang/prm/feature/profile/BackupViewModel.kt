package com.tang.prm.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.BackupFileInfo
import com.tang.prm.domain.model.BackupInfo
import com.tang.prm.domain.model.ClearDataResult
import com.tang.prm.domain.model.RestoreResult
import com.tang.prm.domain.repository.BackupRepositoryInterface
import com.tang.prm.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

data class BackupDataState(
    val backupFiles: List<BackupFileInfo> = emptyList(),
    val hasBackupDir: Boolean = false,
    val backupDirName: String = "",
    val autoBackupEnabled: Boolean = false
)
data class BackupDialogState(
    val operationState: BackupState = BackupState.Idle
)
data class BackupUiState(
    val data: BackupDataState = BackupDataState(),
    val dialog: BackupDialogState = BackupDialogState()
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupRepository: BackupRepositoryInterface,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow<BackupState>(BackupState.Idle)

    private val _backupFiles = MutableStateFlow<List<BackupFileInfo>>(emptyList())

    private val _hasBackupDir = MutableStateFlow(false)

    private val _backupDirName = MutableStateFlow("")

    private val _autoBackupEnabled = MutableStateFlow(false)

    val uiState: StateFlow<BackupUiState> = combine(
        _backupFiles,
        _hasBackupDir,
        _backupDirName,
        _autoBackupEnabled
    ) { backupFiles, hasBackupDir, backupDirName, autoBackupEnabled ->
        BackupDataState(
            backupFiles = backupFiles,
            hasBackupDir = hasBackupDir,
            backupDirName = backupDirName,
            autoBackupEnabled = autoBackupEnabled
        )
    }.combine(_state) { data, operationState ->
        BackupUiState(data = data, dialog = BackupDialogState(operationState = operationState))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BackupUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _hasBackupDir.value = backupRepository.hasBackupDir()
            _backupDirName.value = backupRepository.getBackupDirName()
        }
        viewModelScope.launch {
            settingsRepository.autoBackupEnabled.collect { _autoBackupEnabled.value = it }
        }
        viewModelScope.launch { loadBackupFiles() }
    }

    fun setBackupDir(uri: android.net.Uri) {
        backupRepository.setBackupDirUri(uri.toString())
        _hasBackupDir.value = true
        viewModelScope.launch(Dispatchers.IO) {
            _backupDirName.value = backupRepository.getBackupDirName()
        }
        viewModelScope.launch { loadBackupFiles() }
    }

    fun setAutoBackupEnabled(enabled: Boolean) {
        _autoBackupEnabled.value = enabled
        viewModelScope.launch {
            settingsRepository.setAutoBackupEnabled(enabled)
        }
    }

    fun createBackup() {
        viewModelScope.launch {
            _state.value = BackupState.BackingUp
            try {
                val info = backupRepository.backupToDir()
                loadBackupFiles()
                _state.value = BackupState.BackupSuccess(info)
            } catch (e: Exception) {
                _state.value = BackupState.BackupError(e.message ?: "备份失败")
            }
        }
    }

    fun restoreFromLocal(fileName: String) {
        viewModelScope.launch {
            _state.value = BackupState.Restoring
            backupRepository.restoreBackup(fileName).collect { result ->
                _state.value = when (result) {
                    is RestoreResult.Success -> BackupState.RestoreSuccess
                    is RestoreResult.Error -> BackupState.RestoreError(result.message)
                    is RestoreResult.FatalError -> BackupState.RestoreError(result.message)
                }
            }
        }
    }

    fun restoreFromUri(uri: android.net.Uri) {
        viewModelScope.launch {
            _state.value = BackupState.Restoring
            backupRepository.restoreFromUri(uri.toString()).collect { result ->
                _state.value = when (result) {
                    is RestoreResult.Success -> BackupState.RestoreSuccess
                    is RestoreResult.Error -> BackupState.RestoreError(result.message)
                    is RestoreResult.FatalError -> BackupState.RestoreError(result.message)
                }
            }
        }
    }

    fun deleteBackupFile(fileName: String) {
        viewModelScope.launch {
            _state.value = BackupState.DeletingBackup(fileName)
            backupRepository.deleteBackup(fileName)
            loadBackupFiles()
            _state.value = BackupState.Idle
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            _state.value = BackupState.Clearing
            when (val result = backupRepository.clearAllData()) {
                is ClearDataResult.Success -> _state.value = BackupState.ClearSuccess
                is ClearDataResult.Error -> _state.value = BackupState.ClearError(result.message)
            }
        }
    }

    fun resetState() {
        _state.value = BackupState.Idle
    }

    private suspend fun loadBackupFiles() {
        _backupFiles.value = backupRepository.listBackups()
    }

}
