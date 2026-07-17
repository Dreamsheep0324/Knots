package com.tang.prm.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.CloudBackupVersion
import com.tang.prm.domain.model.ConnectionTestResult
import com.tang.prm.domain.model.SyncResult
import com.tang.prm.domain.model.WebDavConfig
import com.tang.prm.domain.repository.BackupRepositoryInterface
import com.tang.prm.domain.repository.WebDavRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.util.Log
import javax.inject.Inject

data class WebDavDataState(
    val config: WebDavConfig = WebDavConfig(),
    val cloudVersions: List<CloudBackupVersion> = emptyList()
)
data class WebDavDialogState(
    val connectionState: ConnectionState = ConnectionState.Idle,
    val syncState: SyncState = SyncState.Idle,
    val cleanResult: CleanResult? = null
)
data class WebDavUiState(
    val data: WebDavDataState = WebDavDataState(),
    val dialog: WebDavDialogState = WebDavDialogState()
)

@HiltViewModel
class WebDavSyncViewModel @Inject constructor(
    private val webDavRepository: WebDavRepository,
    private val backupRepository: BackupRepositoryInterface
) : ViewModel() {

    private val _cloudVersions = MutableStateFlow<List<CloudBackupVersion>>(emptyList())

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)

    private val _cleanResult = MutableStateFlow<CleanResult?>(null)

    val uiState: StateFlow<WebDavUiState> = combine(
        webDavRepository.getConfig(),
        _cloudVersions,
        combine(_syncState, _connectionState, _cleanResult) { sync, conn, clean ->
            Triple(sync, conn, clean)
        }
    ) { config, cloudVersions, dialogTriple ->
        val (syncState, connectionState, cleanResult) = dialogTriple
        WebDavUiState(
            data = WebDavDataState(config = config, cloudVersions = cloudVersions),
            dialog = WebDavDialogState(
                connectionState = connectionState,
                syncState = syncState,
                cleanResult = cleanResult
            )
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WebDavUiState())

    fun updateConfig(config: WebDavConfig) {
        viewModelScope.launch {
            webDavRepository.saveConfig(config)
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            _connectionState.value = ConnectionState.Testing
            try {
                val result = webDavRepository.testConnection()
                _connectionState.value = when (result) {
                    is ConnectionTestResult.Success -> ConnectionState.Success
                    is ConnectionTestResult.Error -> ConnectionState.Error(result.message)
                }
            } catch (e: Exception) {
                Log.e("WebDavSyncVM", "连接测试失败", e)
                _connectionState.value = ConnectionState.Error("连接失败：${e.message}")
            }
        }
    }

    fun uploadBackup() {
        viewModelScope.launch {
            _syncState.value = SyncState.Uploading("准备中", 0, 0, "准备上传...")
            try {
                webDavRepository.uploadBackup().collect { result ->
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
            } catch (e: Exception) {
                Log.e("WebDavSyncVM", "上传失败", e)
                _syncState.value = SyncState.Error("上传失败：${e.message}")
            }
        }
    }

    fun downloadBackup(fileName: String) {
        viewModelScope.launch {
            _syncState.value = SyncState.Downloading("准备中", 0, 0, "准备下载...")
            try {
                webDavRepository.downloadBackup(fileName).collect { result ->
                    _syncState.value = when (result) {
                        is SyncResult.UploadProgress -> SyncState.Idle
                        is SyncResult.DownloadProgress -> SyncState.Downloading(result.phase, result.current, result.total, result.detail)
                        is SyncResult.DownloadSuccess -> SyncState.DownloadSuccess(result.fileName, result.downloadedImages, result.skippedImages)
                        is SyncResult.PartialSuccess -> SyncState.PartialSuccess(result.fileName, result.succeeded, result.failed, result.skipped)
                        is SyncResult.UploadSuccess -> SyncState.Idle
                        is SyncResult.Error -> SyncState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                Log.e("WebDavSyncVM", "下载失败", e)
                _syncState.value = SyncState.Error("下载失败：${e.message}")
            }
        }
    }

    fun deleteRemoteBackup(fileName: String) {
        viewModelScope.launch {
            val success = webDavRepository.deleteRemoteBackup(fileName)
            if (success) {
                _cloudVersions.value = _cloudVersions.value.filter { it.fileName != fileName }
            }
        }
    }

    fun refreshCloudVersions() {
        viewModelScope.launch {
            val versions = webDavRepository.listRemoteBackups()
            _cloudVersions.value = versions
        }
    }

    fun resetSyncState() {
        _syncState.value = SyncState.Idle
    }

    fun resetConnectionState() {
        _connectionState.value = ConnectionState.Idle
    }

    fun cleanOrphanedImages() {
        viewModelScope.launch {
            _cleanResult.value = CleanResult.Cleaning
            val count = backupRepository.cleanOrphanedImages()
            _cleanResult.value = CleanResult.Done(count)
        }
    }

    fun resetCleanResult() {
        _cleanResult.value = null
    }

    init {
        viewModelScope.launch {
            // 只取首个配置做一次连接测试，不持续监听配置变化
            val savedConfig = webDavRepository.getConfig().first()
            if (savedConfig.serverUrl.isNotBlank() && savedConfig.username.isNotBlank()) {
                _connectionState.value = ConnectionState.Testing
                val result = webDavRepository.testConnection()
                _connectionState.value = when (result) {
                    is ConnectionTestResult.Success -> ConnectionState.Success
                    is ConnectionTestResult.Error -> ConnectionState.Error(result.message)
                }
                if (result is ConnectionTestResult.Success) {
                    _cloudVersions.value = webDavRepository.listRemoteBackups()
                }
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
