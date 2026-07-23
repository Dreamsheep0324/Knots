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
import com.tang.prm.domain.repository.BackupRepositoryInterface
import com.tang.prm.domain.repository.SettingsRepository
import com.tang.prm.domain.repository.WebDavRepository
import com.tang.prm.domain.model.SyncResult
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
    lateinit var backupRepository: BackupRepositoryInterface

    @Inject
    lateinit var webDavRepository: WebDavRepository

    /** 应用级协程作用域，随进程生命周期存在，避免绑定 Activity 导致泄漏 */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var lastFingerprint = ""

    companion object {
        /** 自动云同步最小间隔（30 分钟），避免频繁回到前台触发过多网络请求 */
        private const val AUTO_SYNC_MIN_INTERVAL_MS = 30L * 60 * 1000
    }

    override fun onCreate() {
        super.onCreate()

        // 监听应用前后台切换，在回到前台时检查自动备份与自动云同步
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                checkAutoBackup()
                checkAutoCloudSync()
            }
        })

        // 初始化指纹
        appScope.launch {
            try {
                val enabled = settingsRepository.autoBackupEnabled.first()
                if (enabled) {
                    lastFingerprint = backupRepository.computeDataFingerprint()
                }
            } catch (e: Exception) {
                Log.e("TangApplication", "初始化数据指纹失败，首次回到前台将触发全量备份", e)
            }
        }
    }

    private fun checkAutoBackup() {
        appScope.launch {
            try {
                val enabled = settingsRepository.autoBackupEnabled.first()
                if (!enabled) return@launch
                if (!backupRepository.hasBackupDir()) return@launch

                val currentFingerprint = backupRepository.computeDataFingerprint()
                if (currentFingerprint != lastFingerprint && currentFingerprint.isNotEmpty()) {
                    lastFingerprint = currentFingerprint
                    backupRepository.backupToDir()
                }
            } catch (e: Exception) {
                Log.w("TangApplication", "自动备份检查失败", e)
            }
        }
    }

    /**
     * B-14 修复：消费 autoSyncOnLaunch 配置。
     *
     * 应用回到前台时，若用户开启了"启动时自动同步"且 WebDAV 已配置，
     * 触发云端增量上传同步。B-3 防护已落地（新设备空库禁止覆盖上传），
     * B-7 防护已落地（仓库层 Mutex 串行化），此处静默执行无需 UI 反馈。
     *
     * 节流：距上次同步超过 [AUTO_SYNC_MIN_INTERVAL_MS] 才触发，
     * 避免频繁回到前台导致过多网络请求。
     */
    private fun checkAutoCloudSync() {
        appScope.launch {
            try {
                val config = webDavRepository.getConfig().first()
                if (!config.autoSyncOnLaunch) return@launch
                if (config.serverUrl.isBlank() || config.username.isBlank()) return@launch
                // 节流：距上次同步不足 30 分钟则跳过
                val elapsed = System.currentTimeMillis() - config.lastSyncTime
                if (elapsed < AUTO_SYNC_MIN_INTERVAL_MS) return@launch

                webDavRepository.uploadBackup().collect { result ->
                    // 静默收集，仅在错误时记日志
                    if (result is SyncResult.Error) {
                        Log.w("TangApplication", "自动云同步失败：${result.message}")
                    }
                }
            } catch (e: Exception) {
                Log.w("TangApplication", "自动云同步检查失败", e)
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
