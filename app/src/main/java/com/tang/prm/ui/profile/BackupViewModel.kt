package com.tang.prm.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.usecase.BackupRestoreUseCase
import com.tang.prm.domain.model.BackupInfo
import com.tang.prm.domain.model.BackupResult
import com.tang.prm.domain.model.ClearDataResult
import com.tang.prm.domain.model.RestoreResult
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
}

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupRestoreUseCase: BackupRestoreUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<BackupState>(BackupState.Idle)
    val state: StateFlow<BackupState> = _state.asStateFlow()

    fun createBackup(uri: android.net.Uri) {
        viewModelScope.launch {
            _state.value = BackupState.BackingUp
            backupRestoreUseCase.backupToUri(uri.toString()).collect { result ->
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
            backupRestoreUseCase.restoreFromUri(uri.toString()).collect { result ->
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
            when (val result = backupRestoreUseCase.clearAllData()) {
                is ClearDataResult.Success -> _state.value = BackupState.ClearSuccess
                is ClearDataResult.Error -> _state.value = BackupState.ClearError(result.message)
            }
        }
    }

    fun resetState() {
        _state.value = BackupState.Idle
    }

    fun generateBackupFileName(): String = backupRestoreUseCase.generateBackupFileName()
}
