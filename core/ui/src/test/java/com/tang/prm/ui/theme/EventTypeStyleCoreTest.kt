package com.tang.prm.ui.theme

import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.CustomCategories
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.EventType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import org.junit.jupiter.api.Test

class EventTypeStyleCoreTest {

    private val defaultIcons = mapOf(
        EventType.MEETUP to Icons.Default.People,
        EventType.DINING to Icons.Default.Restaurant,
        EventType.TRAVEL to Icons.Default.Flight,
        EventType.CALL to Icons.Default.Phone,
        EventType.GIFT_SENT to Icons.Default.CardGiftcard,
        EventType.GIFT_RECEIVED to Icons.Default.CardGiftcard,
        EventType.CONVERSATION to Icons.AutoMirrored.Filled.Chat,
        EventType.OTHER to Icons.Default.MoreHoriz
    )

    private val defaultColors = mapOf(
        EventType.MEETUP to SignalGreen,
        EventType.DINING to SignalAmber,
        EventType.TRAVEL to SignalSky,
        EventType.CALL to SignalPurple,
        EventType.GIFT_SENT to SignalCoral,
        EventType.GIFT_RECEIVED to SignalCoral,
        EventType.CONVERSATION to SignalPurple,
        EventType.OTHER to SignalElectric
    )

    private fun resolveIconByName(name: String): ImageVector? = when (name) {
        "People" -> Icons.Default.People
        "Restaurant" -> Icons.Default.Restaurant
        "Flight" -> Icons.Default.Flight
        "Phone" -> Icons.Default.Phone
        "CardGiftcard" -> Icons.Default.CardGiftcard
        "Event" -> Icons.Default.Event
        else -> null
    }

    // ===== resolveCustomType =====

    @Test
    fun `resolveCustomType 非 OTHER 按 key 匹配`() {
        val customType = CustomType(category = CustomCategories.EVENT_TYPE, name = "自定义", key = "MEETUP", color = null, icon = null, isDefault = false)
        val result = resolveCustomType(EventType.MEETUP, null, listOf(customType))
        assertThat(result).isEqualTo(customType)
    }

    @Test
    fun `resolveCustomType 非 OTHER key 不匹配时按 name 匹配`() {
        val customType = CustomType(category = CustomCategories.EVENT_TYPE, name = "MEETUP", key = "other_key", color = null, icon = null, isDefault = false)
        val result = resolveCustomType(EventType.MEETUP, null, listOf(customType))
        assertThat(result).isEqualTo(customType)
    }

    @Test
    fun `resolveCustomType OTHER 按 customTypeName 匹配`() {
        val customType = CustomType(category = CustomCategories.EVENT_TYPE, name = "聚餐", key = "other", color = null, icon = null, isDefault = false)
        val result = resolveCustomType(EventType.OTHER, "聚餐", listOf(customType))
        assertThat(result).isEqualTo(customType)
    }

    @Test
    fun `resolveCustomType 无匹配返回 null`() {
        val customType = CustomType(category = CustomCategories.EVENT_TYPE, name = "其他", key = "OTHER", color = null, icon = null, isDefault = false)
        val result = resolveCustomType(EventType.MEETUP, null, listOf(customType))
        assertThat(result).isNull()
    }

    @Test
    fun `resolveCustomType OTHER customTypeName 为 null 返回 null`() {
        val customType = CustomType(category = CustomCategories.EVENT_TYPE, name = "聚餐", key = "other", color = null, icon = null, isDefault = false)
        val result = resolveCustomType(EventType.OTHER, null, listOf(customType))
        assertThat(result).isNull()
    }

    // ===== resolveEventIconCore =====

    @Test
    fun `resolveEventIconCore 非 OTHER 无自定义类型 返回默认图标`() {
        val result = resolveEventIconCore(
            type = EventType.MEETUP,
            customTypeName = null,
            title = null,
            eventTypes = emptyList(),
            defaultIcon = defaultIcons.getValue(EventType.MEETUP),
            resolveIconByName = ::resolveIconByName
        )
        assertThat(result).isEqualTo(Icons.Default.People)
    }

    @Test
    fun `resolveEventIconCore 非 OTHER 有自定义类型 icon 字段 返回自定义图标`() {
        val customType = CustomType(category = CustomCategories.EVENT_TYPE, name = "MEETUP", key = "MEETUP", color = null, icon = "Restaurant", isDefault = false)
        val result = resolveEventIconCore(
            type = EventType.MEETUP,
            customTypeName = null,
            title = null,
            eventTypes = listOf(customType),
            defaultIcon = defaultIcons.getValue(EventType.MEETUP),
            resolveIconByName = ::resolveIconByName
        )
        assertThat(result).isEqualTo(Icons.Default.Restaurant)
    }

    @Test
    fun `resolveEventIconCore 非 OTHER 自定义类型 icon 字段为 null 返回默认图标`() {
        val customType = CustomType(category = CustomCategories.EVENT_TYPE, name = "MEETUP", key = "MEETUP", color = null, icon = null, isDefault = false)
        val result = resolveEventIconCore(
            type = EventType.MEETUP,
            customTypeName = null,
            title = null,
            eventTypes = listOf(customType),
            defaultIcon = defaultIcons.getValue(EventType.MEETUP),
            resolveIconByName = ::resolveIconByName
        )
        assertThat(result).isEqualTo(Icons.Default.People)
    }

    @Test
    fun `resolveEventIconCore OTHER 无自定义类型 关键词含餐 返回 Restaurant`() {
        val result = resolveEventIconCore(
            type = EventType.OTHER,
            customTypeName = "聚餐",
            title = "团队午餐",
            eventTypes = emptyList(),
            defaultIcon = defaultIcons.getValue(EventType.OTHER),
            resolveIconByName = ::resolveIconByName
        )
        assertThat(result).isEqualTo(Icons.Default.Restaurant)
    }

    @Test
    fun `resolveEventIconCore OTHER 无自定义类型 关键词含见 返回 People`() {
        val result = resolveEventIconCore(
            type = EventType.OTHER,
            customTypeName = "见面",
            title = null,
            eventTypes = emptyList(),
            defaultIcon = defaultIcons.getValue(EventType.OTHER),
            resolveIconByName = ::resolveIconByName
        )
        assertThat(result).isEqualTo(Icons.Default.People)
    }

    @Test
    fun `resolveEventIconCore OTHER 无自定义类型 关键词含对话 返回 Chat 而非 Phone`() {
        val result = resolveEventIconCore(
            type = EventType.OTHER,
            customTypeName = "对话",
            title = null,
            eventTypes = emptyList(),
            defaultIcon = defaultIcons.getValue(EventType.OTHER),
            resolveIconByName = ::resolveIconByName
        )
        assertThat(result).isEqualTo(Icons.AutoMirrored.Filled.Chat)
    }

    @Test
    fun `resolveEventIconCore OTHER 无自定义类型 无匹配关键词 返回 Event`() {
        val result = resolveEventIconCore(
            type = EventType.OTHER,
            customTypeName = "未知活动",
            title = "something random",
            eventTypes = emptyList(),
            defaultIcon = defaultIcons.getValue(EventType.OTHER),
            resolveIconByName = ::resolveIconByName
        )
        assertThat(result).isEqualTo(Icons.Default.Event)
    }

    @Test
    fun `resolveEventIconCore OTHER 有自定义类型 返回自定义图标`() {
        val customType = CustomType(category = CustomCategories.EVENT_TYPE, name = "自定义活动", key = "OTHER", color = null, icon = "Flight", isDefault = false)
        val result = resolveEventIconCore(
            type = EventType.OTHER,
            customTypeName = "自定义活动",
            title = null,
            eventTypes = listOf(customType),
            defaultIcon = defaultIcons.getValue(EventType.OTHER),
            resolveIconByName = ::resolveIconByName
        )
        assertThat(result).isEqualTo(Icons.Default.Flight)
    }

    // ===== resolveEventAccentColorCore =====

    @Test
    fun `resolveEventAccentColorCore 非 OTHER 无自定义类型 返回默认色`() {
        val result = resolveEventAccentColorCore(
            type = EventType.MEETUP,
            customTypeName = null,
            eventTypes = emptyList(),
            defaultColor = defaultColors.getValue(EventType.MEETUP),
            parseColor = { null }
        )
        assertThat(result).isEqualTo(SignalGreen)
    }

    @Test
    fun `resolveEventAccentColorCore 非 OTHER 有自定义类型 color 有效 返回自定义色`() {
        val customType = CustomType(category = CustomCategories.EVENT_TYPE, name = "MEETUP", key = "MEETUP", color = "#FF5733", icon = null, isDefault = false)
        val result = resolveEventAccentColorCore(
            type = EventType.MEETUP,
            customTypeName = null,
            eventTypes = listOf(customType),
            defaultColor = defaultColors.getValue(EventType.MEETUP),
            parseColor = { parseHexColorOrNull(it) }
        )
        assertThat(result).isEqualTo(Color(0xFFFF5733))
    }

    @Test
    fun `resolveEventAccentColorCore 非 OTHER 有自定义类型 color 无效 返回默认色`() {
        val customType = CustomType(category = CustomCategories.EVENT_TYPE, name = "MEETUP", key = "MEETUP", color = "invalid", icon = null, isDefault = false)
        val result = resolveEventAccentColorCore(
            type = EventType.MEETUP,
            customTypeName = null,
            eventTypes = listOf(customType),
            defaultColor = defaultColors.getValue(EventType.MEETUP),
            parseColor = { parseHexColorOrNull(it) }
        )
        assertThat(result).isEqualTo(SignalGreen)
    }

    @Test
    fun `resolveEventAccentColorCore 非 OTHER 有自定义类型 color 为 null 返回默认色`() {
        val customType = CustomType(category = CustomCategories.EVENT_TYPE, name = "MEETUP", key = "MEETUP", color = null, icon = null, isDefault = false)
        val result = resolveEventAccentColorCore(
            type = EventType.MEETUP,
            customTypeName = null,
            eventTypes = listOf(customType),
            defaultColor = defaultColors.getValue(EventType.MEETUP),
            parseColor = { null }
        )
        assertThat(result).isEqualTo(SignalGreen)
    }

    @Test
    fun `resolveEventAccentColorCore OTHER 无自定义类型 返回 SignalElectric`() {
        val result = resolveEventAccentColorCore(
            type = EventType.OTHER,
            customTypeName = "未知",
            eventTypes = emptyList(),
            defaultColor = defaultColors.getValue(EventType.OTHER),
            parseColor = { null }
        )
        assertThat(result).isEqualTo(SignalElectric)
    }

    @Test
    fun `resolveEventAccentColorCore OTHER 有自定义类型 color 有效 返回自定义色`() {
        val customType = CustomType(category = CustomCategories.EVENT_TYPE, name = "自定义", key = "OTHER", color = "#00FF00", icon = null, isDefault = false)
        val result = resolveEventAccentColorCore(
            type = EventType.OTHER,
            customTypeName = "自定义",
            eventTypes = listOf(customType),
            defaultColor = defaultColors.getValue(EventType.OTHER),
            parseColor = { parseHexColorOrNull(it) }
        )
        assertThat(result).isEqualTo(Color(0xFF00FF00))
    }
}
