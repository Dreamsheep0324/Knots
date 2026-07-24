package com.tang.prm.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.ui.graphics.Color
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("AnniversaryIcons 查表函数")
class AnniversaryIconsTest {

    @Nested
    @DisplayName("getAnniversaryIcon")
    inner class GetAnniversaryIconTest {

        @Test
        fun `null 返回默认 Cake 图标`() {
            assertThat(getAnniversaryIcon(null)).isEqualTo(Icons.Default.Cake)
        }

        @Test
        fun `已知 key Cake 返回对应图标`() {
            assertThat(getAnniversaryIcon("Cake")).isEqualTo(Icons.Default.Cake)
        }

        @Test
        fun `已知 key Favorite 返回对应图标`() {
            assertThat(getAnniversaryIcon("Favorite")).isEqualTo(Icons.Default.Favorite)
        }

        @Test
        fun `未知 key 返回默认 Cake 图标`() {
            assertThat(getAnniversaryIcon("Unknown")).isEqualTo(Icons.Default.Cake)
        }

        @Test
        fun `空字符串返回默认 Cake 图标`() {
            assertThat(getAnniversaryIcon("")).isEqualTo(Icons.Default.Cake)
        }

        @Test
        fun `key 大小写敏感 小写 cake 不命中 Cake`() {
            // commonAnniversaryIcons 中 key 为 "Cake"，"cake" 不应命中
            assertThat(getAnniversaryIcon("cake")).isEqualTo(Icons.Default.Cake)
        }

        @Test
        fun `所有 commonAnniversaryIcons 的 key 都能命中对应图标`() {
            commonAnniversaryIcons.forEach { def ->
                assertThat(getAnniversaryIcon(def.key)).isEqualTo(def.icon)
            }
        }
    }

    @Nested
    @DisplayName("getAnniversaryIconBackground")
    inner class GetAnniversaryIconBackgroundTest {

        @Test
        fun `null 返回默认背景色`() {
            assertThat(getAnniversaryIconBackground(null)).isEqualTo(Color(0xFFFFF3E0))
        }

        @Test
        fun `已知 key Cake 返回对应背景色`() {
            assertThat(getAnniversaryIconBackground("Cake")).isEqualTo(Color(0xFFFFF3E0))
        }

        @Test
        fun `已知 key Favorite 返回对应背景色`() {
            assertThat(getAnniversaryIconBackground("Favorite")).isEqualTo(Color(0xFFFCE4EC))
        }

        @Test
        fun `未知 key 返回默认背景色`() {
            assertThat(getAnniversaryIconBackground("Unknown")).isEqualTo(Color(0xFFFFF3E0))
        }

        @Test
        fun `空字符串返回默认背景色`() {
            assertThat(getAnniversaryIconBackground("")).isEqualTo(Color(0xFFFFF3E0))
        }

        @Test
        fun `key 大小写敏感 小写 favorite 不命中 Favorite`() {
            assertThat(getAnniversaryIconBackground("favorite")).isEqualTo(Color(0xFFFFF3E0))
        }

        @Test
        fun `所有 commonAnniversaryIcons 的 key 都能命中对应背景色`() {
            commonAnniversaryIcons.forEach { def ->
                assertThat(getAnniversaryIconBackground(def.key)).isEqualTo(def.backgroundColor)
            }
        }
    }

    @Nested
    @DisplayName("getAnniversaryIconTint")
    inner class GetAnniversaryIconTintTest {

        @Test
        fun `null 返回默认 tint 色`() {
            assertThat(getAnniversaryIconTint(null)).isEqualTo(Color(0xFFF57C00))
        }

        @Test
        fun `已知 key Cake 返回对应 tint 色`() {
            assertThat(getAnniversaryIconTint("Cake")).isEqualTo(Color(0xFFF57C00))
        }

        @Test
        fun `已知 key Favorite 返回对应 tint 色`() {
            assertThat(getAnniversaryIconTint("Favorite")).isEqualTo(Color(0xFFEC407A))
        }

        @Test
        fun `未知 key 返回默认 tint 色`() {
            assertThat(getAnniversaryIconTint("Unknown")).isEqualTo(Color(0xFFF57C00))
        }

        @Test
        fun `空字符串返回默认 tint 色`() {
            assertThat(getAnniversaryIconTint("")).isEqualTo(Color(0xFFF57C00))
        }

        @Test
        fun `key 大小写敏感 小写 cake 不命中 Cake`() {
            assertThat(getAnniversaryIconTint("cake")).isEqualTo(Color(0xFFF57C00))
        }

        @Test
        fun `所有 commonAnniversaryIcons 的 key 都能命中对应 tint 色`() {
            commonAnniversaryIcons.forEach { def ->
                assertThat(getAnniversaryIconTint(def.key)).isEqualTo(def.iconTint)
            }
        }
    }
}
