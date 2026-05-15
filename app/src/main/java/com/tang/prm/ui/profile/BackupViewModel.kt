package com.tang.prm.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.data.repository.BackupRepository
import com.tang.prm.data.repository.BackupResult
import com.tang.prm.data.repository.ClearDataResult
import com.tang.prm.data.repository.RestoreResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BackupState {
    data object Idle : BackupState()
    data object BackingUp : BackupState()
    data class BackupSuccess(val info: com.tang.prm.data.repository.BackupInfo) : BackupState()
    data class BackupError(val message: String) : BackupState()
    data object Restoring : BackupState()
    data object RestoreSuccess : BackupState()
    data class RestoreError(val message: String) : BackupState()
    data object Clearing : BackupState()
    data object ClearSuccess : BackupState()
    data class ClearError(val message: String) : BackupState()
}

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val _state = MutableStateFlow<BackupState>(BackupState.Idle)
    val state: StateFlow<BackupState> = _state.asStateFlow()

    fun createBackup(uri: android.net.Uri) {
        viewModelScope.launch {
            _state.value = BackupState.BackingUp
            backupRepository.backupToUri(uri).collect { result ->
                _state.value = when (result) {
                    is BackupResult.Success -> BackupState.BackupSuccess(result.info)
                    is BackupResult.Error -> BackupState.BackupError(result.message)
                }
            }
        }
    }

    fun restoreBackup(uri: android.net.Uri) {
        viewModelScope.launch {
            _state.value = BackupState.Restoring
            backupRepository.restoreFromUri(uri).collect { result ->
                _state.value = when (result) {
                    is RestoreResult.Success -> {
                        BackupState.RestoreSuccess
                    }
                    is RestoreResult.Error -> BackupState.RestoreError(result.message)
                }
            }
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

    fun generateBackupFileName(): String = backupRepository.generateBackupFileName()
}
