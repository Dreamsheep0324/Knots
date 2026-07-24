package com.tang.prm.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoodBad
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolunteerActivism
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("EmotionIcons 查表函数")
class EmotionIconsTest {

    @Nested
    @DisplayName("getEmotionIcon")
    inner class GetEmotionIconTest {

        @Test
        fun `已知 key 开心 返回 SentimentVerySatisfied 图标`() {
            assertThat(getEmotionIcon("开心")).isEqualTo(Icons.Default.SentimentVerySatisfied)
        }

        @Test
        fun `已知 key 平静 返回 SelfImprovement 图标`() {
            assertThat(getEmotionIcon("平静")).isEqualTo(Icons.Default.SelfImprovement)
        }

        @Test
        fun `已知 key 兴奋 返回 EmojiEmotions 图标`() {
            assertThat(getEmotionIcon("兴奋")).isEqualTo(Icons.Default.EmojiEmotions)
        }

        @Test
        fun `已知 key 感动 返回 Favorite 图标`() {
            assertThat(getEmotionIcon("感动")).isEqualTo(Icons.Default.Favorite)
        }

        @Test
        fun `已知 key 焦虑 返回 Psychology 图标`() {
            assertThat(getEmotionIcon("焦虑")).isEqualTo(Icons.Default.Psychology)
        }

        @Test
        fun `已知 key 难过 返回 SentimentVeryDissatisfied 图标`() {
            assertThat(getEmotionIcon("难过")).isEqualTo(Icons.Default.SentimentVeryDissatisfied)
        }

        @Test
        fun `已知 key 愤怒 返回 MoodBad 图标`() {
            assertThat(getEmotionIcon("愤怒")).isEqualTo(Icons.Default.MoodBad)
        }

        @Test
        fun `已知 key 疲惫 返回 Bedtime 图标`() {
            assertThat(getEmotionIcon("疲惫")).isEqualTo(Icons.Default.Bedtime)
        }

        @Test
        fun `已知 key 惊喜 返回 AutoAwesome 图标`() {
            assertThat(getEmotionIcon("惊喜")).isEqualTo(Icons.Default.AutoAwesome)
        }

        @Test
        fun `已知 key 感恩 返回 VolunteerActivism 图标`() {
            assertThat(getEmotionIcon("感恩")).isEqualTo(Icons.Default.VolunteerActivism)
        }

        @Test
        fun `已知 key 期待 返回 Star 图标`() {
            assertThat(getEmotionIcon("期待")).isEqualTo(Icons.Default.Star)
        }

        @Test
        fun `已知 key 思念 返回 FavoriteBorder 图标`() {
            assertThat(getEmotionIcon("思念")).isEqualTo(Icons.Default.FavoriteBorder)
        }

        @Test
        fun `未知 key 返回 null`() {
            assertThat(getEmotionIcon("未知情绪")).isNull()
        }

        @Test
        fun `空字符串返回 null`() {
            assertThat(getEmotionIcon("")).isNull()
        }

        @Test
        fun `所有 emotionIconDefs 的 name 都能命中对应图标`() {
            emotionIconDefs.forEach { def ->
                assertThat(getEmotionIcon(def.name)).isEqualTo(def.icon)
            }
        }
    }

    @Nested
    @DisplayName("getEmotionColor")
    inner class GetEmotionColorTest {

        @Test
        fun `已知 key 开心 返回对应颜色字符串`() {
            assertThat(getEmotionColor("开心")).isEqualTo("#F59E0B")
        }

        @Test
        fun `已知 key 平静 返回对应颜色字符串`() {
            assertThat(getEmotionColor("平静")).isEqualTo("#14B8A6")
        }

        @Test
        fun `已知 key 感动 返回对应颜色字符串`() {
            assertThat(getEmotionColor("感动")).isEqualTo("#EC4899")
        }

        @Test
        fun `未知 key 返回 null`() {
            assertThat(getEmotionColor("未知情绪")).isNull()
        }

        @Test
        fun `空字符串返回 null`() {
            assertThat(getEmotionColor("")).isNull()
        }

        @Test
        fun `所有颜色字符串符合 RRGGBB 格式`() {
            val hexPattern = Regex("^#[0-9A-Fa-f]{6}$")
            emotionIconDefs.forEach { def ->
                val color = getEmotionColor(def.name)
                assertThat(color).isNotNull()
                assertThat(hexPattern.matches(color!!)).isTrue()
            }
        }

        @Test
        fun `所有 emotionIconDefs 的 name 都能命中对应颜色`() {
            emotionIconDefs.forEach { def ->
                assertThat(getEmotionColor(def.name)).isEqualTo(def.color)
            }
        }
    }
}
