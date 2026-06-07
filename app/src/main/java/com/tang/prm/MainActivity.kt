package com.tang.prm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.tang.prm.domain.repository.SettingsRepository
import com.tang.prm.domain.model.ThemeMode
import com.tang.prm.domain.usecase.BackupRestoreUseCase
import com.tang.prm.ui.navigation.TangNavHost
import com.tang.prm.ui.theme.FixedDensityProvider
import com.tang.prm.ui.theme.TangTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var backupRestoreUseCase: BackupRestoreUseCase

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var lastFingerprint = ""

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            // 应用进入后台时检查自动备份
            checkAutoBackup()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)

        // 初始化指纹
        appScope.launch {
            val enabled = settingsRepository.getAutoBackupEnabled().first()
            if (enabled) {
                lastFingerprint = backupRestoreUseCase.computeDataFingerprint()
            }
        }

        setContent {
            val themeMode by settingsRepository.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            FixedDensityProvider(context = this) {
                TangTheme(darkTheme = darkTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        TangNavHost()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(lifecycleObserver)
    }

    private fun checkAutoBackup() {
        appScope.launch {
            val enabled = settingsRepository.getAutoBackupEnabled().first()
            if (!enabled) return@launch
            if (!backupRestoreUseCase.hasBackupDir()) return@launch

            val currentFingerprint = backupRestoreUseCase.computeDataFingerprint()
            if (currentFingerprint != lastFingerprint && currentFingerprint.isNotEmpty()) {
                lastFingerprint = currentFingerprint
                try {
                    backupRestoreUseCase.backupToDir(
                        com.tang.prm.domain.model.BackupImageQuality.ORIGINAL
                    )
                } catch (_: Exception) {}
            }
        }
    }
}
