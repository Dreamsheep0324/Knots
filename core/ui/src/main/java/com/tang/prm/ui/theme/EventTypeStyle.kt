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
        EventType.MEETUP -> EventTypeStyle(SignalGreen, SemanticGreenBg, Icons.Default.People)
        EventType.DINING -> EventTypeStyle(SignalAmber, SemanticAmberBg, Icons.Default.Restaurant)
        EventType.TRAVEL -> EventTypeStyle(SignalSky, SemanticBlueBg, Icons.Default.Flight)
        EventType.CALL -> EventTypeStyle(SignalPurple, SemanticPurpleBg, Icons.Default.Phone)
        EventType.GIFT_SENT, EventType.GIFT_RECEIVED -> EventTypeStyle(SignalCoral, SemanticCoralBg, Icons.Default.CardGiftcard)
        EventType.CONVERSATION -> EventTypeStyle(SignalPurple, SemanticIndigoBg, Icons.AutoMirrored.Filled.Chat)
        EventType.OTHER -> EventTypeStyle(SignalElectric, SemanticIndigoBg, Icons.Default.MoreHoriz)
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
 *    - 含"电话/致电/通话/call/phone" → Phone
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
    val defaultIcon = getEventTypeStyle(type).icon
    return resolveEventIconCore(
        type = type,
        customTypeName = customTypeName,
        title = title,
        eventTypes = eventTypes,
        defaultIcon = defaultIcon,
        resolveIconByName = { getGenericIcon(it) }
    )
}

/**
 * [resolveEventIcon] 的纯函数核心（可单独测试）。
 *
 * 通过 [defaultIcon] 和 [resolveIconByName] 注入依赖，
 * 使核心逻辑脱离 Compose 运行时即可验证。
 */
internal fun resolveEventIconCore(
    type: EventType,
    customTypeName: String?,
    title: String?,
    eventTypes: List<CustomType>,
    defaultIcon: ImageVector,
    resolveIconByName: (String) -> ImageVector?
): ImageVector {
    // 1. 优先查找用户自定义类型（CustomType.icon 字段）
    val customType = resolveCustomType(type, customTypeName, eventTypes)
    customType?.icon?.let { iconName ->
        resolveIconByName(iconName)?.let { return it }
    }

    // 2. 非 OTHER 类型无自定义匹配 → 返回类型默认图标
    if (type != EventType.OTHER) {
        return defaultIcon
    }

    // 3. OTHER 类型无自定义匹配 → 关键词匹配
    val combined = ((customTypeName ?: "") + " " + (title ?: "")).lowercase()
    return resolveOtherTypeIconByKeywords(combined)
}

/**
 * 解析用户自定义类型（纯函数，可单独测试）。
 *
 * 优先级：key == type.name → name == type.name（非 OTHER）；name == customTypeName（OTHER）。
 */
internal fun resolveCustomType(
    type: EventType,
    customTypeName: String?,
    eventTypes: List<CustomType>
): CustomType? = if (type != EventType.OTHER) {
    eventTypes.find { it.key == type.name } ?: eventTypes.find { it.name == type.name }
} else {
    customTypeName?.let { ctn -> eventTypes.find { it.name == ctn } }
}

/**
 * OTHER 类型事件的关键词图标匹配（纯函数，可单独测试）。
 *
 * 内部对输入做 [String.lowercase]，因此调用方无需预先转换大小写。
 *
 * 匹配优先级：餐食 > 见面 > 旅行 > 通话 > 礼物 > 聊天 > 通用 Event。
 *
 * 注：通话分支用「电话」而非单字「话」，避免「对话」等含「话」的聊天语义被误判为通话。
 */
internal fun resolveOtherTypeIconByKeywords(combined: String): ImageVector {
    val text = combined.lowercase()
    return when {
        listOf("餐", "吃", "饭", "食", "dining", "meal", "lunch", "dinner").any { it in text } -> Icons.Default.Restaurant
        listOf("见", "聚", "面", "约", "meet", "meetup").any { it in text } -> Icons.Default.People
        listOf("旅", "行", "飞", "游", "出", "travel", "trip", "fly").any { it in text } -> Icons.Default.Flight
        listOf("电话", "致电", "通话", "call", "phone").any { it in text } -> Icons.Default.Phone
        listOf("礼", "送", "收", "gift").any { it in text } -> Icons.Default.CardGiftcard
        listOf("聊", "谈", "对话", "chat", "talk").any { it in text } -> Icons.AutoMirrored.Filled.Chat
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
    val defaultColor = getEventTypeStyle(type).accentColor
    return resolveEventAccentColorCore(
        type = type,
        customTypeName = customTypeName,
        eventTypes = eventTypes,
        defaultColor = defaultColor,
        parseColor = { parseHexColorOrNull(it) }
    )
}

/**
 * [resolveEventAccentColor] 的纯函数核心（可单独测试）。
 */
internal fun resolveEventAccentColorCore(
    type: EventType,
    customTypeName: String?,
    eventTypes: List<CustomType>,
    defaultColor: Color,
    parseColor: (String) -> Color?
): Color {
    val customType = resolveCustomType(type, customTypeName, eventTypes)
    if (customType != null) {
        return customType.color?.let { parseColor(it) } ?: defaultColor
    }
    return if (type != EventType.OTHER) {
        defaultColor
    } else {
        SignalElectric
    }
}
