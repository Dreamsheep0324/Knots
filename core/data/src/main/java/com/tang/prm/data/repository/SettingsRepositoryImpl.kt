package com.tang.prm.data.repository

import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tang.prm.domain.model.BackupImageQuality
import com.tang.prm.domain.model.ThemeMode
import com.tang.prm.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val encryptedPrefs: SharedPreferences
) : SettingsRepository {

    companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_SIGNATURE = stringPreferencesKey("user_signature")
        val KEY_AI_GENDER = stringPreferencesKey("ai_gender")
        val KEY_AI_BIRTH_DATE = stringPreferencesKey("ai_birth_date")
        val KEY_BACKUP_IMAGE_QUALITY = stringPreferencesKey("backup_image_quality")
        val KEY_AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled")

        private const val ENC_KEY_API_KEY = "ai_api_key"
        private const val ENC_KEY_BASE_URL = "ai_base_url"
        private const val ENC_KEY_MODEL = "ai_model"
    }

    override val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        val name = prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name
        runCatching { ThemeMode.valueOf(name) }.getOrDefault(ThemeMode.SYSTEM)
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
    }

    override val userName: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_USER_NAME] ?: "用户"
    }

    override suspend fun setUserName(name: String) {
        dataStore.edit { prefs ->
            prefs[KEY_USER_NAME] = name
        }
    }

    override val userSignature: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_USER_SIGNATURE] ?: "用心管理每一段关系"
    }

    override suspend fun setUserSignature(signature: String) {
        dataStore.edit { prefs ->
            prefs[KEY_USER_SIGNATURE] = signature
        }
    }

    override val aiApiKey: Flow<String> = encryptedPrefsFlow(ENC_KEY_API_KEY, "")

    override suspend fun setAiApiKey(key: String) {
        withContext(Dispatchers.IO) {
            encryptedPrefs.edit().putString(ENC_KEY_API_KEY, key).commit()
        }
    }

    override val aiBaseUrl: Flow<String> = encryptedPrefsFlow(ENC_KEY_BASE_URL, "https://api.deepseek.com")

    override suspend fun setAiBaseUrl(url: String) {
        withContext(Dispatchers.IO) {
            encryptedPrefs.edit().putString(ENC_KEY_BASE_URL, url).commit()
        }
    }

    override val aiModel: Flow<String> = encryptedPrefsFlow(ENC_KEY_MODEL, "deepseek-v4-flash")

    override suspend fun setAiModel(model: String) {
        withContext(Dispatchers.IO) {
            encryptedPrefs.edit().putString(ENC_KEY_MODEL, model).commit()
        }
    }

    override val aiGender: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_AI_GENDER] ?: "男"
    }

    override suspend fun setAiGender(gender: String) {
        dataStore.edit { prefs ->
            prefs[KEY_AI_GENDER] = gender
        }
    }

    override val aiBirthDate: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_AI_BIRTH_DATE] ?: ""
    }

    override suspend fun setAiBirthDate(date: String) {
        dataStore.edit { prefs ->
            prefs[KEY_AI_BIRTH_DATE] = date
        }
    }

    override fun getBackupImageQuality(): Flow<BackupImageQuality> = dataStore.data.map { prefs ->
        val name = prefs[KEY_BACKUP_IMAGE_QUALITY] ?: BackupImageQuality.STANDARD.name
        runCatching { BackupImageQuality.valueOf(name) }.getOrDefault(BackupImageQuality.STANDARD)
    }

    override suspend fun setBackupImageQuality(quality: BackupImageQuality) {
        dataStore.edit { prefs ->
            prefs[KEY_BACKUP_IMAGE_QUALITY] = quality.name
        }
    }

    override fun getAutoBackupEnabled(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_AUTO_BACKUP_ENABLED] ?: false
    }

    override suspend fun setAutoBackupEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_AUTO_BACKUP_ENABLED] = enabled
        }
    }

    private fun encryptedPrefsFlow(key: String, defaultValue: String): Flow<String> = callbackFlow {
        trySend(encryptedPrefs.getString(key, defaultValue) ?: defaultValue)
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
            if (changedKey == key) {
                trySend(encryptedPrefs.getString(key, defaultValue) ?: defaultValue)
            }
        }
        encryptedPrefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { encryptedPrefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.flowOn(Dispatchers.IO)
}
