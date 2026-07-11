package com.tang.prm.feature.profile

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.CloudBackupVersion
import com.tang.prm.domain.model.ConnectionTestResult
import com.tang.prm.domain.model.SyncResult
import com.tang.prm.domain.model.WebDavConfig
import com.tang.prm.domain.usecase.WebDavSyncUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class WebDavSyncViewModelTest {

    @MockK
    private lateinit var webDavSyncUseCase: WebDavSyncUseCase

    private lateinit var viewModel: WebDavSyncViewModel

    private val defaultConfig = WebDavConfig(
        serverUrl = "https://dav.example.com",
        username = "user",
        password = "pass"
    )

    @BeforeEach
    fun setUp() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        // 默认返回空配置，避免 init 中的自动测试连接
        every { webDavSyncUseCase.getConfig() } returns flowOf(WebDavConfig())
        coEvery { webDavSyncUseCase.testConnection() } returns ConnectionTestResult.Success
        coEvery { webDavSyncUseCase.listRemoteBackups() } returns emptyList()

        viewModel = WebDavSyncViewModel(webDavSyncUseCase)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    @DisplayName("初始状态")
    inner class InitTest {

        @Test
        fun syncState_isIdle() {
            assertThat(viewModel.uiState.value.dialog.syncState).isInstanceOf(SyncState.Idle::class.java)
        }

        @Test
        fun connectionState_isIdle_whenConfigEmpty() {
            // 空配置不触发自动测试连接
            assertThat(viewModel.uiState.value.dialog.connectionState).isInstanceOf(ConnectionState.Idle::class.java)
        }

        @Test
        fun cloudVersions_isEmptyInitially() {
            assertThat(viewModel.uiState.value.data.cloudVersions).isEmpty()
        }

        @Test
        fun cleanResult_isNullInitially() {
            assertThat(viewModel.uiState.value.dialog.cleanResult).isNull()
        }
    }

    @Nested
    @DisplayName("testConnection")
    inner class TestConnectionTest {

        @Test
        fun success_setsConnectionStateToSuccess() = runTest {
            backgroundScope.launch { viewModel.uiState.collect { } }
            coEvery { webDavSyncUseCase.testConnection() } returns ConnectionTestResult.Success
            viewModel.testConnection()
            advanceUntilIdle()
            assertThat(viewModel.uiState.value.dialog.connectionState).isInstanceOf(ConnectionState.Success::class.java)
        }

        @Test
        fun error_setsConnectionStateToError() = runTest {
            backgroundScope.launch { viewModel.uiState.collect { } }
            coEvery { webDavSyncUseCase.testConnection() } returns ConnectionTestResult.Error("网络错误")
            viewModel.testConnection()
            advanceUntilIdle()
            assertThat(viewModel.uiState.value.dialog.connectionState).isInstanceOf(ConnectionState.Error::class.java)
            assertThat((viewModel.uiState.value.dialog.connectionState as ConnectionState.Error).message).isEqualTo("网络错误")
        }

        @Test
        fun setsTestingStateBeforeResult() = runTest {
            backgroundScope.launch { viewModel.uiState.collect { } }
            coEvery { webDavSyncUseCase.testConnection() } returns ConnectionTestResult.Success
            viewModel.testConnection()
            advanceUntilIdle()
            // After completion, should be Success (Testing is transient)
            assertThat(viewModel.uiState.value.dialog.connectionState).isInstanceOf(ConnectionState.Success::class.java)
        }
    }

    @Nested
    @DisplayName("resetConnectionState")
    inner class ResetConnectionTest {

        @Test
        fun resetsToIdle() = runTest {
            backgroundScope.launch { viewModel.uiState.collect { } }
            viewModel.resetConnectionState()
            assertThat(viewModel.uiState.value.dialog.connectionState).isInstanceOf(ConnectionState.Idle::class.java)
        }
    }

    @Nested
    @DisplayName("uploadBackup")
    inner class UploadBackupTest {

        @Test
        fun success_setsUploadSuccessState() = runTest {
            backgroundScope.launch { viewModel.uiState.collect { } }
            val results = listOf<SyncResult>(
                SyncResult.UploadProgress("上传中", 5, 10, "正在上传"),
                SyncResult.UploadSuccess("backup.zip", 5, 2)
            )
            coEvery { webDavSyncUseCase.uploadBackup() } returns flowOf(*results.toTypedArray())
            coEvery { webDavSyncUseCase.listRemoteBackups() } returns emptyList()

            viewModel.uploadBackup()

            // 等待 flow collection 完成
            advanceUntilIdle()
            val state = viewModel.uiState.value.dialog.syncState
            assertThat(state).isInstanceOf(SyncState.UploadSuccess::class.java)
            val success = state as SyncState.UploadSuccess
            assertThat(success.fileName).isEqualTo("backup.zip")
            assertThat(success.uploadedImages).isEqualTo(5)
            assertThat(success.skippedImages).isEqualTo(2)
        }

        @Test
        fun error_setsErrorState() = runTest {
            backgroundScope.launch { viewModel.uiState.collect { } }
            coEvery { webDavSyncUseCase.uploadBackup() } returns flowOf(SyncResult.Error("上传失败"))
            viewModel.uploadBackup()

            advanceUntilIdle()
            val state = viewModel.uiState.value.dialog.syncState
            assertThat(state).isInstanceOf(SyncState.Error::class.java)
            assertThat((state as SyncState.Error).message).isEqualTo("上传失败")
        }
    }

    @Nested
    @DisplayName("downloadBackup")
    inner class DownloadBackupTest {

        @Test
        fun success_setsDownloadSuccessState() = runTest {
            backgroundScope.launch { viewModel.uiState.collect { } }
            val results = listOf<SyncResult>(
                SyncResult.DownloadProgress("下载中", 3, 5, "正在下载"),
                SyncResult.DownloadSuccess("backup.zip", 3, 1)
            )
            coEvery { webDavSyncUseCase.downloadBackup("backup.zip") } returns flowOf(*results.toTypedArray())

            viewModel.downloadBackup("backup.zip")

            advanceUntilIdle()
            val state = viewModel.uiState.value.dialog.syncState
            assertThat(state).isInstanceOf(SyncState.DownloadSuccess::class.java)
            val success = state as SyncState.DownloadSuccess
            assertThat(success.fileName).isEqualTo("backup.zip")
            assertThat(success.downloadedImages).isEqualTo(3)
        }

        @Test
        fun partialSuccess_setsPartialSuccessState() = runTest {
            backgroundScope.launch { viewModel.uiState.collect { } }
            val results = listOf<SyncResult>(
                SyncResult.PartialSuccess("backup.zip", 3, 1, 1)
            )
            coEvery { webDavSyncUseCase.downloadBackup("backup.zip") } returns flowOf(*results.toTypedArray())

            viewModel.downloadBackup("backup.zip")

            advanceUntilIdle()
            val state = viewModel.uiState.value.dialog.syncState
            assertThat(state).isInstanceOf(SyncState.PartialSuccess::class.java)
            val partial = state as SyncState.PartialSuccess
            assertThat(partial.succeeded).isEqualTo(3)
            assertThat(partial.failed).isEqualTo(1)
        }
    }

    @Nested
    @DisplayName("deleteRemoteBackup")
    inner class DeleteRemoteBackupTest {

        @Test
        fun success_removesFromCloudVersions() = runTest {
            backgroundScope.launch { viewModel.uiState.collect { } }
            val versions = listOf(
                CloudBackupVersion(fileName = "backup1.zip", fileSize = 1024, lastModified = "2024-01-01", displayName = "备份1"),
                CloudBackupVersion(fileName = "backup2.zip", fileSize = 2048, lastModified = "2024-01-02", displayName = "备份2")
            )
            coEvery { webDavSyncUseCase.listRemoteBackups() } returns versions
            viewModel.refreshCloudVersions()

            advanceUntilIdle()

            coEvery { webDavSyncUseCase.deleteRemoteBackup("backup1.zip") } returns true
            viewModel.deleteRemoteBackup("backup1.zip")

            advanceUntilIdle()
            assertThat(viewModel.uiState.value.data.cloudVersions).hasSize(1)
            assertThat(viewModel.uiState.value.data.cloudVersions[0].fileName).isEqualTo("backup2.zip")
        }

        @Test
        fun failure_keepsCloudVersionsUnchanged() = runTest {
            backgroundScope.launch { viewModel.uiState.collect { } }
            val versions = listOf(
                CloudBackupVersion(fileName = "backup1.zip", fileSize = 1024, lastModified = "2024-01-01", displayName = "备份1")
            )
            coEvery { webDavSyncUseCase.listRemoteBackups() } returns versions
            viewModel.refreshCloudVersions()

            advanceUntilIdle()

            coEvery { webDavSyncUseCase.deleteRemoteBackup("backup1.zip") } returns false
            viewModel.deleteRemoteBackup("backup1.zip")

            advanceUntilIdle()
            assertThat(viewModel.uiState.value.data.cloudVersions).hasSize(1)
        }
    }

    @Nested
    @DisplayName("refreshCloudVersions")
    inner class RefreshCloudVersionsTest {

        @Test
        fun loadsVersionsFromUseCase() = runTest {
            backgroundScope.launch { viewModel.uiState.collect { } }
            val versions = listOf(
                CloudBackupVersion(fileName = "backup.zip", fileSize = 1024, lastModified = "2024-01-01", displayName = "备份")
            )
            coEvery { webDavSyncUseCase.listRemoteBackups() } returns versions
            viewModel.refreshCloudVersions()

            advanceUntilIdle()
            assertThat(viewModel.uiState.value.data.cloudVersions).hasSize(1)
            assertThat(viewModel.uiState.value.data.cloudVersions[0].fileName).isEqualTo("backup.zip")
        }
    }

    @Nested
    @DisplayName("resetSyncState")
    inner class ResetSyncStateTest {

        @Test
        fun resetsToIdle() = runTest {
            backgroundScope.launch { viewModel.uiState.collect { } }
            viewModel.resetSyncState()
            assertThat(viewModel.uiState.value.dialog.syncState).isInstanceOf(SyncState.Idle::class.java)
        }
    }

    @Nested
    @DisplayName("cleanOrphanedImages")
    inner class CleanOrphanedImagesTest {

        @Test
        fun setsCleaningThenDone() = runTest {
            backgroundScope.launch { viewModel.uiState.collect { } }
            coEvery { webDavSyncUseCase.cleanOrphanedImages() } returns 5
            viewModel.cleanOrphanedImages()

            advanceUntilIdle()
            val result = viewModel.uiState.value.dialog.cleanResult
            assertThat(result).isInstanceOf(CleanResult.Done::class.java)
            assertThat((result as CleanResult.Done).count).isEqualTo(5)
        }

        @Test
        fun resetCleanResult_setsNull() = runTest {
            backgroundScope.launch { viewModel.uiState.collect { } }
            viewModel.resetCleanResult()
            assertThat(viewModel.uiState.value.dialog.cleanResult).isNull()
        }
    }

    @Nested
    @DisplayName("updateConfig")
    inner class UpdateConfigTest {

        @Test
        fun callsSaveConfig() = runTest {
            coEvery { webDavSyncUseCase.saveConfig(any()) } returns Unit
            viewModel.updateConfig(defaultConfig)
            coVerify { webDavSyncUseCase.saveConfig(defaultConfig) }
        }
    }

    @Nested
    @DisplayName("SyncState.percent 计算")
    inner class PercentTest {

        @Test
        fun uploadingPercent_calculatesCorrectly() {
            val state = SyncState.Uploading("phase", 5, 10, "detail")
            assertThat(state.percent).isEqualTo(50)
        }

        @Test
        fun uploadingPercent_zeroTotal_returnsZero() {
            val state = SyncState.Uploading("phase", 5, 0, "detail")
            assertThat(state.percent).isEqualTo(0)
        }

        @Test
        fun downloadingPercent_calculatesCorrectly() {
            val state = SyncState.Downloading("phase", 3, 4, "detail")
            assertThat(state.percent).isEqualTo(75)
        }

        @Test
        fun percent_clampedTo100() {
            val state = SyncState.Uploading("phase", 15, 10, "detail")
            assertThat(state.percent).isEqualTo(100)
        }
    }
}
