package com.tang.prm.ui.theme

import androidx.compose.ui.graphics.Color
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Color 工具函数")
class ColorTest {

    @Nested
    @DisplayName("parseHexColorOrNull")
    inner class ParseHexColorOrNullTest {

        @Test
        fun `#RRGGBB 格式正确解析`() {
            val result = parseHexColorOrNull("#FF5733")
            assertThat(result).isEqualTo(Color(0xFFFF5733))
        }

        @Test
        fun `#AARRGGBB 格式正确解析`() {
            val result = parseHexColorOrNull("#80FF5733")
            assertThat(result).isEqualTo(Color(0x80FF5733))
        }

        @Test
        fun `无前缀 RRGGBB 格式正确解析`() {
            val result = parseHexColorOrNull("FF5733")
            assertThat(result).isEqualTo(Color(0xFFFF5733))
        }

        @Test
        fun `无前缀 AARRGGBB 格式正确解析`() {
            val result = parseHexColorOrNull("80FF5733")
            assertThat(result).isEqualTo(Color(0x80FF5733))
        }

        @Test
        fun `非法字符返回 null`() {
            assertThat(parseHexColorOrNull("#GGGGGG")).isNull()
        }

        @Test
        fun `长度不合法返回 null`() {
            assertThat(parseHexColorOrNull("#FF")).isNull()
            assertThat(parseHexColorOrNull("#FF5733AA99")).isNull()
        }

        @Test
        fun `空字符串返回 null`() {
            assertThat(parseHexColorOrNull("")).isNull()
        }
    }

    @Nested
    @DisplayName("toComposeColor")
    inner class ToComposeColorTest {

        private val fallback = Color(0xFF000000)

        @Test
        fun `null 返回 fallback`() {
            assertThat(null.toComposeColor(fallback)).isEqualTo(fallback)
        }

        @Test
        fun `#RRGGBB 返回对应颜色`() {
            assertThat("#FF5733".toComposeColor(fallback)).isEqualTo(Color(0xFFFF5733))
        }

        @Test
        fun `#AARRGGBB 返回对应颜色`() {
            assertThat("#80FF5733".toComposeColor(fallback)).isEqualTo(Color(0x80FF5733))
        }

        @Test
        fun `无前缀格式返回对应颜色`() {
            assertThat("FF5733".toComposeColor(fallback)).isEqualTo(Color(0xFFFF5733))
        }

        @Test
        fun `非法字符串返回 fallback`() {
            assertThat("not-a-color".toComposeColor(fallback)).isEqualTo(fallback)
            assertThat("##FF5733".toComposeColor(fallback)).isEqualTo(fallback)
        }
    }
}
