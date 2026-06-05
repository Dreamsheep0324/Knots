package com.tang.prm.data.repository

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
    fun keyAiGender_hasCorrectName() {
        assertThat(SettingsRepositoryImpl.KEY_AI_GENDER.name).isEqualTo("ai_gender")
    }

    @Test
    fun keyAiBirthDate_hasCorrectName() {
        assertThat(SettingsRepositoryImpl.KEY_AI_BIRTH_DATE.name).isEqualTo("ai_birth_date")
    }

    @Test
    fun themeMode_enumHasExpectedValues() {
        assertThat(ThemeMode.entries).containsExactly(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM).inOrder()
    }
}
