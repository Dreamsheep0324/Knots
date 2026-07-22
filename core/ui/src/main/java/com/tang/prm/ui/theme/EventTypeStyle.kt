package com.tang.prm.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.EventType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*

@Immutable
data class EventTypeStyle(
    val accentColor: Color,
    val lightColor: Color,
    val icon: ImageVector
)

@Composable
fun getEventTypeStyle(eventType: EventType): EventTypeStyle {
    return when (eventType) {
        EventType.MEETUP -> EventTypeStyle(SignalGreen, EventLightGreen, Icons.Default.People)
        EventType.DINING -> EventTypeStyle(SignalAmber, EventLightAmber, Icons.Default.Restaurant)
        EventType.TRAVEL -> EventTypeStyle(SignalSky, EventLightBlue, Icons.Default.Flight)
        EventType.CALL -> EventTypeStyle(SignalPurple, EventLightPurple, Icons.Default.Phone)
        EventType.GIFT_SENT, EventType.GIFT_RECEIVED -> EventTypeStyle(SignalCoral, EventLightRed, Icons.Default.CardGiftcard)
        EventType.CONVERSATION -> EventTypeStyle(SignalPurple, EventLightIndigo, Icons.AutoMirrored.Filled.Chat)
        EventType.OTHER -> EventTypeStyle(SignalElectric, EventLightIndigo, Icons.Default.MoreHoriz)
    }
}

/**
 * 智能解析事件图标。
 *
 * 优先级：
 * 1. 用户自定义类型（[eventTypes]）中能匹配到 → 使用 CustomType.icon 字段（通过 [getGenericIcon] 解析）
 *    - 非 OTHER 类型：按 key == type.name 匹配，其次按 name == type.name 匹配
 *    - OTHER 类型：按 name == customTypeName 匹配
 * 2. 非 OTHER 类型且无自定义类型匹配 → 直接返回 [getEventTypeStyle] 中对应的类型图标
 * 3. OTHER 类型且无自定义类型匹配 → 综合 customTypeName + title 进行关键词匹配
 *    - 含"餐/吃/饭/食/dining/meal" → Restaurant
 *    - 含"见/聚/面/约/meet" → People
 *    - 含"旅/行/飞/游/travel/trip" → Flight
 *    - 含"话/电/call/phone" → Phone
 *    - 含"礼/送/收/gift" → CardGiftcard
 *    - 含"聊/谈/对话/chat" → Chat
 *    - 其他 → Event（通用事件图标，不用 MoreHoriz 三个点）
 *
 * @param type 事件类型
 * @param customTypeName 自定义类型名（可选）
 * @param title 事件标题（可选，用于关键词匹配补充）
 * @param eventTypes 用户自定义事件类型列表（含 icon 字段，优先使用）
 */
@Composable
fun resolveEventIcon(
    type: EventType,
    customTypeName: String?,
    title: String?,
    eventTypes: List<CustomType> = emptyList()
): ImageVector {
    // 1. 优先查找用户自定义类型（CustomType.icon 字段）
    val customType = if (type != EventType.OTHER) {
        eventTypes.find { it.key == type.name } ?: eventTypes.find { it.name == type.name }
    } else {
        customTypeName?.let { ctn -> eventTypes.find { it.name == ctn } }
    }
    customType?.icon?.let { iconName ->
        getGenericIcon(iconName)?.let { return it }
    }

    // 2. 非 OTHER 类型无自定义匹配 → 返回类型默认图标
    if (type != EventType.OTHER) {
        return getEventTypeStyle(type).icon
    }

    // 3. OTHER 类型无自定义匹配 → 关键词匹配
    val combined = ((customTypeName ?: "") + " " + (title ?: "")).lowercase()
    return when {
        listOf("餐", "吃", "饭", "食", "dining", "meal", "lunch", "dinner").any { it in combined } -> Icons.Default.Restaurant
        listOf("见", "聚", "面", "约", "meet", "meetup").any { it in combined } -> Icons.Default.People
        listOf("旅", "行", "飞", "游", "出", "travel", "trip", "fly").any { it in combined } -> Icons.Default.Flight
        listOf("话", "电", "call", "phone").any { it in combined } -> Icons.Default.Phone
        listOf("礼", "送", "收", "gift").any { it in combined } -> Icons.Default.CardGiftcard
        listOf("聊", "谈", "对话", "chat", "talk").any { it in combined } -> Icons.AutoMirrored.Filled.Chat
        else -> Icons.Default.Event
    }
}

/**
 * 解析事件类型的主色调，优先使用用户自定义类型颜色。
 *
 * 优先级：
 * 1. 用户自定义类型（[eventTypes]）中能匹配到 → 使用 CustomType.color 字段
 * 2. 非 OTHER 类型 → [getEventTypeStyle] 中对应的类型主色
 * 3. OTHER 类型 → SignalElectric
 *
 * @param type 事件类型
 * @param customTypeName 自定义类型名（可选）
 * @param eventTypes 用户自定义事件类型列表
 */
@Composable
fun resolveEventAccentColor(
    type: EventType,
    customTypeName: String?,
    eventTypes: List<CustomType> = emptyList()
): Color {
    val customType = if (type != EventType.OTHER) {
        eventTypes.find { it.key == type.name } ?: eventTypes.find { it.name == type.name }
    } else {
        customTypeName?.let { ctn -> eventTypes.find { it.name == ctn } }
    }
    if (customType != null) {
        return customType.color?.let { parseHexColor(it) } ?: getEventTypeStyle(type).accentColor
    }
    return if (type != EventType.OTHER) {
        getEventTypeStyle(type).accentColor
    } else {
        SignalElectric
    }
}

/** 解析十六进制颜色字符串（支持 #RRGGBB / #AARRGGBB / 无前缀） */
private fun parseHexColor(hex: String): Color? = runCatching {
    val normalized = hex.removePrefix("#")
    val intValue = when (normalized.length) {
        6 -> ("FF$normalized").toLong(16)
        8 -> normalized.toLong(16)
        else -> return null
    }
    Color(intValue.toInt())
}.getOrNull()
