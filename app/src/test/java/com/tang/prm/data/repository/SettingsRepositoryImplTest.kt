package com.tang.prm.data.repository

import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.ThemeMode
import org.junit.jupiter.api.Test

class SettingsRepositoryImplTest {

    @Test
    fun keyThemeMode_hasCorrectName() {
        assertThat(SettingsRepositoryImpl.KEY_THEME_MODE.name).isEqualTo("theme_mode")
    }

    @Test
    fun keyUserName_hasCorrectName() {
        assertThat(SettingsRepositoryImpl.KEY_USER_NAME.name).isEqualTo("user_name")
    }

    @Test
    fun keyUserSignature_hasCorrectName() {
        assertThat(SettingsRepositoryImpl.KEY_USER_SIGNATURE.name).isEqualTo("user_signature")
    }

    @Test
    fun keyAiApiKey_hasCorrectName() {
        assertThat(SettingsRepositoryImpl.KEY_AI_API_KEY.name).isEqualTo("ai_api_key")
    }

    @Test
    fun keyAiBaseUrl_hasCorrectName() {
        assertThat(SettingsRepositoryImpl.KEY_AI_BASE_URL.name).isEqualTo("ai_base_url")
    }

    @Test
    fun keyAiModel_hasCorrectName() {
        assertThat(SettingsRepositoryImpl.KEY_AI_MODEL.name).isEqualTo("ai_model")
    }

    @Test
    fun companionObject_keysAreStringPreferencesKey() {
        assertThat(SettingsRepositoryImpl.KEY_THEME_MODE).isInstanceOf(stringPreferencesKey("test")::class.java)
    }

    @Test
    fun themeMode_enumHasExpectedValues() {
        assertThat(ThemeMode.entries).containsExactly(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM).inOrder()
    }
}
