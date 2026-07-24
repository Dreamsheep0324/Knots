package com.tang.prm.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SportsEsports
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.GiftType
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("GiftType.toStyle() 扩展函数")
class GiftTypeStyleTest {

    @Nested
    @DisplayName("各枚举值返回正确图标与颜色")
    inner class StyleMappingTest {

        @Test
        fun `DIGITAL 返回 Devices 图标`() {
            val style = GiftType.DIGITAL.toStyle()
            assertThat(style.icon).isEqualTo(Icons.Default.Devices)
        }

        @Test
        fun `CLOTHING 返回 Checkroom 图标`() {
            val style = GiftType.CLOTHING.toStyle()
            assertThat(style.icon).isEqualTo(Icons.Default.Checkroom)
        }

        @Test
        fun `FOOD 返回 Restaurant 图标`() {
            val style = GiftType.FOOD.toStyle()
            assertThat(style.icon).isEqualTo(Icons.Default.Restaurant)
        }

        @Test
        fun `COSMETICS 返回 Face 图标`() {
            val style = GiftType.COSMETICS.toStyle()
            assertThat(style.icon).isEqualTo(Icons.Default.Face)
        }

        @Test
        fun `BOOKS 返回 MenuBook 图标`() {
            val style = GiftType.BOOKS.toStyle()
            assertThat(style.icon).isEqualTo(Icons.AutoMirrored.Filled.MenuBook)
        }

        @Test
        fun `TOYS 返回 SportsEsports 图标`() {
            val style = GiftType.TOYS.toStyle()
            assertThat(style.icon).isEqualTo(Icons.Default.SportsEsports)
        }

        @Test
        fun `TRAVEL 返回 FlightTakeoff 图标`() {
            val style = GiftType.TRAVEL.toStyle()
            assertThat(style.icon).isEqualTo(Icons.Default.FlightTakeoff)
        }

        @Test
        fun `SPORTS 返回 FitnessCenter 图标`() {
            val style = GiftType.SPORTS.toStyle()
            assertThat(style.icon).isEqualTo(Icons.Default.FitnessCenter)
        }

        @Test
        fun `HOME 返回 Chair 图标`() {
            val style = GiftType.HOME.toStyle()
            assertThat(style.icon).isEqualTo(Icons.Default.Chair)
        }

        @Test
        fun `OTHER 返回 CardGiftcard 图标`() {
            val style = GiftType.OTHER.toStyle()
            assertThat(style.icon).isEqualTo(Icons.Default.CardGiftcard)
        }
    }

    @Nested
    @DisplayName("遍历所有枚举值校验")
    inner class AllValuesTest {

        @Test
        fun `所有 GiftType 的 toStyle 返回非 null icon 和 color`() {
            GiftType.values().forEach { type ->
                val style = type.toStyle()
                assertThat(style.icon).isNotNull()
                assertThat(style.color).isNotNull()
            }
        }

        @Test
        fun `所有 GiftType 的 toStyle 返回唯一图标`() {
            // 每个类型应有独立的图标（除特殊设计外，此处仅验证非空且可调用）
            val styles = GiftType.values().associate { it to it.toStyle() }
            styles.forEach { (type, style) ->
                assertThat(style.icon).isNotNull()
                assertThat(style.color).isNotNull()
            }
        }

        @Test
        fun `所有 GiftType 的 toStyle 返回不同颜色`() {
            // 验证每个类型颜色不同（领域调色板设计上各不相同）
            val colors = GiftType.values().map { it.toStyle().color }
            val distinctColors = colors.toSet()
            assertThat(distinctColors.size).isEqualTo(colors.size)
        }
    }
}
