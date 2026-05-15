package com.tang.prm.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tang.prm.domain.repository.SettingsRepository
import com.tang.prm.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
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
}
