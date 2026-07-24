package com.tang.prm.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("WeatherIcons 查表函数")
class WeatherIconsTest {

    @Nested
    @DisplayName("getWeatherIcon")
    inner class GetWeatherIconTest {

        @Test
        fun `已知 key 晴天 返回 WbSunny 图标`() {
            assertThat(getWeatherIcon("晴天")).isEqualTo(Icons.Default.WbSunny)
        }

        @Test
        fun `已知 key 多云 返回 Cloud 图标`() {
            assertThat(getWeatherIcon("多云")).isEqualTo(Icons.Default.Cloud)
        }

        @Test
        fun `已知 key 小雨 返回 WaterDrop 图标`() {
            assertThat(getWeatherIcon("小雨")).isEqualTo(Icons.Default.WaterDrop)
        }

        @Test
        fun `已知 key 大雨 返回 Thunderstorm 图标`() {
            assertThat(getWeatherIcon("大雨")).isEqualTo(Icons.Default.Thunderstorm)
        }

        @Test
        fun `已知 key 雪 返回 AcUnit 图标`() {
            assertThat(getWeatherIcon("雪")).isEqualTo(Icons.Default.AcUnit)
        }

        @Test
        fun `已知 key 风 返回 Air 图标`() {
            assertThat(getWeatherIcon("风")).isEqualTo(Icons.Default.Air)
        }

        @Test
        fun `已知 key 雾 返回 CloudQueue 图标`() {
            assertThat(getWeatherIcon("雾")).isEqualTo(Icons.Default.CloudQueue)
        }

        @Test
        fun `未知 key 返回 null`() {
            assertThat(getWeatherIcon("未知天气")).isNull()
        }

        @Test
        fun `空字符串返回 null`() {
            assertThat(getWeatherIcon("")).isNull()
        }

        @Test
        fun `所有 weatherIconDefs 的 name 都能命中对应图标`() {
            weatherIconDefs.forEach { def ->
                assertThat(getWeatherIcon(def.name)).isEqualTo(def.icon)
            }
        }
    }

    @Nested
    @DisplayName("getWeatherColor")
    inner class GetWeatherColorTest {

        @Test
        fun `已知 key 晴天 返回对应颜色字符串`() {
            assertThat(getWeatherColor("晴天")).isEqualTo("#F59E0B")
        }

        @Test
        fun `已知 key 多云 返回对应颜色字符串`() {
            assertThat(getWeatherColor("多云")).isEqualTo("#94A3B8")
        }

        @Test
        fun `已知 key 小雨 返回对应颜色字符串`() {
            assertThat(getWeatherColor("小雨")).isEqualTo("#3B82F6")
        }

        @Test
        fun `未知 key 返回 null`() {
            assertThat(getWeatherColor("未知天气")).isNull()
        }

        @Test
        fun `空字符串返回 null`() {
            assertThat(getWeatherColor("")).isNull()
        }

        @Test
        fun `所有颜色字符串符合 RRGGBB 格式`() {
            val hexPattern = Regex("^#[0-9A-Fa-f]{6}$")
            weatherIconDefs.forEach { def ->
                val color = getWeatherColor(def.name)
                assertThat(color).isNotNull()
                assertThat(hexPattern.matches(color!!)).isTrue()
            }
        }

        @Test
        fun `所有 weatherIconDefs 的 name 都能命中对应颜色`() {
            weatherIconDefs.forEach { def ->
                assertThat(getWeatherColor(def.name)).isEqualTo(def.color)
            }
        }
    }
}
