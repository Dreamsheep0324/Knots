package com.tang.prm.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class ThemeModeTest {

    @Test
    fun `themeMode values has 3 entries`() {
        assertThat(ThemeMode.entries).hasSize(3)
    }

    @Test
    fun `themeMode light name is LIGHT`() {
        assertThat(ThemeMode.LIGHT.name).isEqualTo("LIGHT")
    }

    @Test
    fun `themeMode dark name is DARK`() {
        assertThat(ThemeMode.DARK.name).isEqualTo("DARK")
    }

    @Test
    fun `themeMode system name is SYSTEM`() {
        assertThat(ThemeMode.SYSTEM.name).isEqualTo("SYSTEM")
    }

    @Test
    fun `themeMode light label is 浅色模式`() {
        assertThat(ThemeMode.LIGHT.label).isEqualTo("浅色模式")
    }

    @Test
    fun `themeMode dark label is 深色模式`() {
        assertThat(ThemeMode.DARK.label).isEqualTo("深色模式")
    }

    @Test
    fun `themeMode system label is 跟随系统`() {
        assertThat(ThemeMode.SYSTEM.label).isEqualTo("跟随系统")
    }
}
