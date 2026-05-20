package com.tang.prm.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tang.prm.domain.model.ThemeMode
import com.tang.prm.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_SIGNATURE = stringPreferencesKey("user_signature")
        val KEY_AI_API_KEY = stringPreferencesKey("ai_api_key")
        val KEY_AI_BASE_URL = stringPreferencesKey("ai_base_url")
        val KEY_AI_MODEL = stringPreferencesKey("ai_model")
        val KEY_AI_GENDER = stringPreferencesKey("ai_gender")
        val KEY_AI_BIRTH_DATE = stringPreferencesKey("ai_birth_date")
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

    override val aiApiKey: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_AI_API_KEY] ?: ""
    }

    override suspend fun setAiApiKey(key: String) {
        dataStore.edit { prefs ->
            prefs[KEY_AI_API_KEY] = key
        }
    }

    override val aiBaseUrl: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_AI_BASE_URL] ?: "https://api.deepseek.com"
    }

    override suspend fun setAiBaseUrl(url: String) {
        dataStore.edit { prefs ->
            prefs[KEY_AI_BASE_URL] = url
        }
    }

    override val aiModel: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_AI_MODEL] ?: "deepseek-v4-flash"
    }

    override suspend fun setAiModel(model: String) {
        dataStore.edit { prefs ->
            prefs[KEY_AI_MODEL] = model
        }
    }

    override suspend fun getAiApiKey(): String = aiApiKey.first()

    override suspend fun getAiBaseUrl(): String = aiBaseUrl.first()

    override suspend fun getAiModel(): String = aiModel.first()

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

    override suspend fun getAiGender(): String = aiGender.first()

    override suspend fun getAiBirthDate(): String = aiBirthDate.first()
}
