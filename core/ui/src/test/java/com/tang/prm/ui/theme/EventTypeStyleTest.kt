package com.tang.prm.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("EventTypeStyle 图标解析")
class EventTypeStyleTest {

    @Nested
    @DisplayName("resolveOtherTypeIconByKeywords")
    inner class ResolveOtherTypeIconByKeywordsTest {

        @Test
        fun `餐食关键词匹配 Restaurant`() {
            assertThat(resolveOtherTypeIconByKeywords("聚餐")).isEqualTo(Icons.Default.Restaurant)
            assertThat(resolveOtherTypeIconByKeywords("吃饭")).isEqualTo(Icons.Default.Restaurant)
            assertThat(resolveOtherTypeIconByKeywords("晚饭")).isEqualTo(Icons.Default.Restaurant)
            assertThat(resolveOtherTypeIconByKeywords("dining")).isEqualTo(Icons.Default.Restaurant)
            assertThat(resolveOtherTypeIconByKeywords("lunch")).isEqualTo(Icons.Default.Restaurant)
        }

        @Test
        fun `见面关键词匹配 People`() {
            assertThat(resolveOtherTypeIconByKeywords("见面")).isEqualTo(Icons.Default.People)
            assertThat(resolveOtherTypeIconByKeywords("聚会")).isEqualTo(Icons.Default.People)
            assertThat(resolveOtherTypeIconByKeywords("约见")).isEqualTo(Icons.Default.People)
            assertThat(resolveOtherTypeIconByKeywords("meetup")).isEqualTo(Icons.Default.People)
        }

        @Test
        fun `旅行关键词匹配 Flight`() {
            assertThat(resolveOtherTypeIconByKeywords("旅行")).isEqualTo(Icons.Default.Flight)
            assertThat(resolveOtherTypeIconByKeywords("飞行")).isEqualTo(Icons.Default.Flight)
            assertThat(resolveOtherTypeIconByKeywords("出游")).isEqualTo(Icons.Default.Flight)
            assertThat(resolveOtherTypeIconByKeywords("travel")).isEqualTo(Icons.Default.Flight)
            assertThat(resolveOtherTypeIconByKeywords("trip")).isEqualTo(Icons.Default.Flight)
        }

        @Test
        fun `通话关键词匹配 Phone`() {
            assertThat(resolveOtherTypeIconByKeywords("电话")).isEqualTo(Icons.Default.Phone)
            assertThat(resolveOtherTypeIconByKeywords("通话")).isEqualTo(Icons.Default.Phone)
            assertThat(resolveOtherTypeIconByKeywords("call")).isEqualTo(Icons.Default.Phone)
        }

        @Test
        fun `礼物关键词匹配 CardGiftcard`() {
            assertThat(resolveOtherTypeIconByKeywords("送礼物")).isEqualTo(Icons.Default.CardGiftcard)
            assertThat(resolveOtherTypeIconByKeywords("收礼")).isEqualTo(Icons.Default.CardGiftcard)
            assertThat(resolveOtherTypeIconByKeywords("gift")).isEqualTo(Icons.Default.CardGiftcard)
        }

        @Test
        fun `聊天关键词匹配 Chat`() {
            assertThat(resolveOtherTypeIconByKeywords("聊天")).isEqualTo(Icons.AutoMirrored.Filled.Chat)
            assertThat(resolveOtherTypeIconByKeywords("对话")).isEqualTo(Icons.AutoMirrored.Filled.Chat)
            assertThat(resolveOtherTypeIconByKeywords("talk")).isEqualTo(Icons.AutoMirrored.Filled.Chat)
        }

        @Test
        fun `无匹配关键词返回通用 Event 图标`() {
            assertThat(resolveOtherTypeIconByKeywords("随机事件")).isEqualTo(Icons.Default.Event)
            assertThat(resolveOtherTypeIconByKeywords("")).isEqualTo(Icons.Default.Event)
            assertThat(resolveOtherTypeIconByKeywords("random")).isEqualTo(Icons.Default.Event)
        }

        @Test
        fun `餐食优先级高于见面`() {
            // "约饭" 同时命中 "约"(见面) 和 "饭"(餐食)，应返回 Restaurant
            assertThat(resolveOtherTypeIconByKeywords("约饭")).isEqualTo(Icons.Default.Restaurant)
        }

        @Test
        fun `大小写不敏感`() {
            assertThat(resolveOtherTypeIconByKeywords("DINING")).isEqualTo(Icons.Default.Restaurant)
            assertThat(resolveOtherTypeIconByKeywords("TRAVEL")).isEqualTo(Icons.Default.Flight)
        }
    }

    @Nested
    @DisplayName("getGenericIcon")
    inner class GetGenericIconTest {

        @Test
        fun `null 返回 null`() {
            assertThat(getGenericIcon(null)).isNull()
        }

        @Test
        fun `已知 key 返回对应图标`() {
            assertThat(getGenericIcon("People")).isEqualTo(Icons.Default.People)
            assertThat(getGenericIcon("Restaurant")).isEqualTo(Icons.Default.Restaurant)
            assertThat(getGenericIcon("Flight")).isEqualTo(Icons.Default.Flight)
            assertThat(getGenericIcon("Phone")).isEqualTo(Icons.Default.Phone)
            assertThat(getGenericIcon("Cake")).isEqualTo(Icons.Default.Cake)
        }

        @Test
        fun `未知 key 返回 null`() {
            assertThat(getGenericIcon("NonExistentIcon")).isNull()
            assertThat(getGenericIcon("")).isNull()
        }
    }
}
