package com.tang.prm.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class ThemeModeTest {

    @Test
    fun themeMode_values_has3Entries() {
        assertThat(ThemeMode.entries).hasSize(3)
    }

    @Test
    fun themeMode_light_nameIsLIGHT() {
        assertThat(ThemeMode.LIGHT.name).isEqualTo("LIGHT")
    }

    @Test
    fun themeMode_dark_nameIsDARK() {
        assertThat(ThemeMode.DARK.name).isEqualTo("DARK")
    }

    @Test
    fun themeMode_system_nameIsSYSTEM() {
        assertThat(ThemeMode.SYSTEM.name).isEqualTo("SYSTEM")
    }

    @Test
    fun themeMode_light_labelIs浅色模式() {
        assertThat(ThemeMode.LIGHT.label).isEqualTo("浅色模式")
    }

    @Test
    fun themeMode_dark_labelIs深色模式() {
        assertThat(ThemeMode.DARK.label).isEqualTo("深色模式")
    }

    @Test
    fun themeMode_system_labelIs跟随系统() {
        assertThat(ThemeMode.SYSTEM.label).isEqualTo("跟随系统")
    }
}
