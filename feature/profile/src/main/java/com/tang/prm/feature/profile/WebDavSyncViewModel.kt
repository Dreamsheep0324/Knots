package com.tang.prm.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.CloudBackupVersion
import com.tang.prm.domain.model.ConnectionTestResult
import com.tang.prm.domain.model.SyncResult
import com.tang.prm.domain.model.WebDavConfig
import com.tang.prm.domain.usecase.WebDavSyncUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WebDavSyncViewModel @Inject constructor(
    private val webDavSyncUseCase: WebDavSyncUseCase
) : ViewModel() {

    val config: StateFlow<WebDavConfig> = webDavSyncUseCase.getConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WebDavConfig())

    private val _cloudVersions = MutableStateFlow<List<CloudBackupVersion>>(emptyList())
    val cloudVersions: StateFlow<List<CloudBackupVersion>> = _cloudVersions.asStateFlow()

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    fun updateConfig(config: WebDavConfig) {
        viewModelScope.launch {
            webDavSyncUseCase.saveConfig(config)
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            _connectionState.value = ConnectionState.Testing
            val result = webDavSyncUseCase.testConnection()
            _connectionState.value = when (result) {
                is ConnectionTestResult.Success -> ConnectionState.Success
                is ConnectionTestResult.Error -> ConnectionState.Error(result.message)
            }
        }
    }

    fun uploadBackup() {
        viewModelScope.launch {
            _syncState.value = SyncState.Uploading("准备中", 0, 0, "准备上传...")
            webDavSyncUseCase.uploadBackup().collect { result ->
                _syncState.value = when (result) {
                    is SyncResult.UploadProgress -> SyncState.Uploading(result.phase, result.current, result.total, result.detail)
                    is SyncResult.UploadSuccess -> {
                        refreshCloudVersions()
                        SyncState.UploadSuccess(result.fileName, result.uploadedImages, result.skippedImages)
                    }
                    is SyncResult.DownloadProgress -> SyncState.Idle
                    is SyncResult.DownloadSuccess -> SyncState.Idle
                    is SyncResult.PartialSuccess -> SyncState.Idle
                    is SyncResult.Error -> SyncState.Error(result.message)
                }
            }
        }
    }

    fun downloadBackup(fileName: String) {
        viewModelScope.launch {
            _syncState.value = SyncState.Downloading("准备中", 0, 0, "准备下载...")
            webDavSyncUseCase.downloadBackup(fileName).collect { result ->
                _syncState.value = when (result) {
                    is SyncResult.UploadProgress -> SyncState.Idle
                    is SyncResult.DownloadProgress -> SyncState.Downloading(result.phase, result.current, result.total, result.detail)
                    is SyncResult.DownloadSuccess -> SyncState.DownloadSuccess(result.fileName, result.downloadedImages, result.skippedImages)
                    is SyncResult.PartialSuccess -> SyncState.PartialSuccess(result.fileName, result.succeeded, result.failed, result.skipped)
                    is SyncResult.UploadSuccess -> SyncState.Idle
                    is SyncResult.Error -> SyncState.Error(result.message)
                }
            }
        }
    }

    fun deleteRemoteBackup(fileName: String) {
        viewModelScope.launch {
            val success = webDavSyncUseCase.deleteRemoteBackup(fileName)
            if (success) {
                _cloudVersions.value = _cloudVersions.value.filter { it.fileName != fileName }
            }
        }
    }

    fun refreshCloudVersions() {
        viewModelScope.launch {
            val versions = webDavSyncUseCase.listRemoteBackups()
            _cloudVersions.value = versions
        }
    }

    fun resetSyncState() {
        _syncState.value = SyncState.Idle
    }

    fun resetConnectionState() {
        _connectionState.value = ConnectionState.Idle
    }

    private val _cleanResult = MutableStateFlow<CleanResult?>(null)
    val cleanResult: StateFlow<CleanResult?> = _cleanResult.asStateFlow()

    fun cleanOrphanedImages() {
        viewModelScope.launch {
            _cleanResult.value = CleanResult.Cleaning
            val count = webDavSyncUseCase.cleanOrphanedImages()
            _cleanResult.value = CleanResult.Done(count)
        }
    }

    fun resetCleanResult() {
        _cleanResult.value = null
    }

    init {
        viewModelScope.launch {
            val savedConfig = webDavSyncUseCase.getConfig()
            savedConfig.collect { config ->
                if (config.serverUrl.isNotBlank() && config.username.isNotBlank()) {
                    _connectionState.value = ConnectionState.Testing
                    val result = webDavSyncUseCase.testConnection()
                    _connectionState.value = when (result) {
                        is ConnectionTestResult.Success -> ConnectionState.Success
                        is ConnectionTestResult.Error -> ConnectionState.Error(result.message)
                    }
                    if (result is ConnectionTestResult.Success) {
                        val versions = webDavSyncUseCase.listRemoteBackups()
                        _cloudVersions.value = versions
                    }
                }
                return@collect
            }
        }
    }
}

sealed class SyncState {
    data object Idle : SyncState()
    data class Uploading(val phase: String, val current: Int, val total: Int, val detail: String) : SyncState() {
        val percent: Int get() = if (total > 0) (current * 100 / total).coerceIn(0, 100) else 0
    }
    data class Downloading(val phase: String, val current: Int, val total: Int, val detail: String) : SyncState() {
        val percent: Int get() = if (total > 0) (current * 100 / total).coerceIn(0, 100) else 0
    }
    data class UploadSuccess(val fileName: String, val uploadedImages: Int = 0, val skippedImages: Int = 0) : SyncState()
    data class DownloadSuccess(val fileName: String, val downloadedImages: Int = 0, val skippedImages: Int = 0) : SyncState()
    /** 部分成功：核心数据已完成，但部分图片同步失败 */
    data class PartialSuccess(val fileName: String, val succeeded: Int, val failed: Int, val skipped: Int) : SyncState()
    data class Error(val message: String) : SyncState()
}

sealed class ConnectionState {
    data object Idle : ConnectionState()
    data object Testing : ConnectionState()
    data object Success : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

sealed class CleanResult {
    data object Cleaning : CleanResult()
    data class Done(val count: Int) : CleanResult()
}
