package com.tang.prm

import android.app.Application
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.tang.prm.domain.model.BackupImageQuality
import com.tang.prm.domain.repository.SettingsRepository
import com.tang.prm.domain.usecase.BackupRestoreUseCase
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import java.io.File
import javax.inject.Inject

@HiltAndroidApp
class TangApplication : Application(), ImageLoaderFactory {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var backupRestoreUseCase: BackupRestoreUseCase

    /** 应用级协程作用域，随进程生命周期存在，避免绑定 Activity 导致泄漏 */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var lastFingerprint = ""

    override fun onCreate() {
        super.onCreate()

        // 监听应用前后台切换，在回到前台时检查自动备份
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                checkAutoBackup()
            }
        })

        // 初始化指纹
        appScope.launch {
            val enabled = settingsRepository.getAutoBackupEnabled().first()
            if (enabled) {
                lastFingerprint = backupRestoreUseCase.computeDataFingerprint()
            }
        }
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
                    backupRestoreUseCase.backupToDir(BackupImageQuality.ORIGINAL)
                } catch (e: Exception) {
                    Log.w("TangApplication", "自动备份失败", e)
                }
            }
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .crossfade(true)
            .crossfade(300)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.15)
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .diskCache {
                val cacheDir = File(cacheDir, "image_cache")
                DiskCache.Builder()
                    .directory(cacheDir)
                    .maxSizePercent(0.02)
                    .build()
            }
            .respectCacheHeaders(false)
            .build()
    }
}
