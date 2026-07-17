package com.tang.prm.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import com.tang.prm.domain.model.WebDavConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * WebDAV 配置存储
 *
 * 从 WebDavRepositoryImpl 拆分出的配置管理职责：
 * - SharedPreferences 读写 WebDavConfig
 * - 密码加密存储与旧版 Base64 迁移
 * - configCache StateFlow 供 getConfig() 订阅
 * - lastSyncTime 持久化
 */
@Singleton
class WebDavConfigStore @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("webdav") private val encryptedPrefs: SharedPreferences
) {
    companion object {
        private const val TAG = "WebDavConfigStore"
        private const val PREFS_NAME = "webdav_config"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_REMOTE_PATH = "remote_path"
        private const val KEY_AUTO_SYNC_ON_LAUNCH = "auto_sync_on_launch"
        private const val KEY_LAST_SYNC_TIME = "last_sync_time"
        private const val KEY_LAST_SYNC_DIRECTION = "last_sync_direction"
        private const val KEY_TRUST_ALL_CERTS = "trust_all_certificates"
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    val configCache = MutableStateFlow(readConfig())

    fun readConfig(): WebDavConfig {
        val oldPassword = prefs.getString(KEY_PASSWORD, "") ?: ""
        val newPassword = encryptedPrefs.getString(KEY_PASSWORD, "") ?: ""

        val password = if (newPassword.isBlank() && oldPassword.isNotBlank()) {
            val decoded = decodePassword(oldPassword)
            savePassword(decoded)
            prefs.edit().remove(KEY_PASSWORD).apply()
            decoded
        } else {
            newPassword
        }

        return WebDavConfig(
            serverUrl = prefs.getString(KEY_SERVER_URL, "") ?: "",
            username = prefs.getString(KEY_USERNAME, "") ?: "",
            password = password,
            remotePath = prefs.getString(KEY_REMOTE_PATH, "/knots_backup/") ?: "/knots_backup/",
            autoSyncOnLaunch = prefs.getBoolean(KEY_AUTO_SYNC_ON_LAUNCH, false),
            lastSyncTime = prefs.getLong(KEY_LAST_SYNC_TIME, 0),
            lastSyncDirection = prefs.getString(KEY_LAST_SYNC_DIRECTION, "") ?: "",
            trustAllCertificates = prefs.getBoolean(KEY_TRUST_ALL_CERTS, false)
        )
    }

    fun saveConfig(config: WebDavConfig) {
        prefs.edit().apply {
            putString(KEY_SERVER_URL, config.serverUrl)
            putString(KEY_USERNAME, config.username)
            putString(KEY_REMOTE_PATH, config.remotePath)
            putBoolean(KEY_AUTO_SYNC_ON_LAUNCH, config.autoSyncOnLaunch)
            putBoolean(KEY_TRUST_ALL_CERTS, config.trustAllCertificates)
            putLong(KEY_LAST_SYNC_TIME, config.lastSyncTime)
            putString(KEY_LAST_SYNC_DIRECTION, config.lastSyncDirection)
            apply()
        }
        savePassword(config.password)
        configCache.value = config
    }

    fun saveLastSyncTime(time: Long, direction: String) {
        prefs.edit().apply {
            putLong(KEY_LAST_SYNC_TIME, time)
            putString(KEY_LAST_SYNC_DIRECTION, direction)
            commit()
        }
        configCache.value = readConfig()
    }

    private fun savePassword(password: String) {
        encryptedPrefs.edit().putString(KEY_PASSWORD, password).apply()
    }

    private fun decodePassword(encoded: String): String {
        if (encoded.isBlank()) return ""
        return try {
            String(Base64.decode(encoded, Base64.NO_WRAP), Charsets.UTF_8)
        } catch (e: Exception) {
            Log.w(TAG, "密码 Base64 解码失败，返回原始字符串", e)
            encoded
        }
    }
}
