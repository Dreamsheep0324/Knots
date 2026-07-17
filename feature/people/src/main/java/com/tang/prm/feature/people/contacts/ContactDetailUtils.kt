package com.tang.prm.feature.people.contacts

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.tang.prm.domain.model.AnniversaryType
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.EventType
import com.tang.prm.domain.model.Gift
import com.tang.prm.ui.theme.AnniversaryBirthday
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.SignalSky

/**
 * 跨手机/平板详情页共享的解析逻辑。
 *
 * 卡片 UI 可有差异化实现，但数据层规则必须单一来源，避免重复同步。
 */

// ─────────────────────────────────────────────────────────────────
// C-4: 事件类型解析
// ─────────────────────────────────────────────────────────────────

/**
 * 解析事件对应的自定义类型。
 *
 * 规则：
 * - 非OTHER类型：优先按 key 匹配（事件类型名），其次按 name 匹配
 * - OTHER类型：使用事件上记录的 customTypeName 查找
 *
 * @return 匹配的自定义类型，无匹配时返回 null（由调用方决定 fallback 策略）
 */
internal fun resolveEventCustomType(
    event: Event,
    eventTypes: List<CustomType>
): CustomType? {
    return if (event.type != EventType.OTHER) {
        eventTypes.find { it.key == event.type.name }
            ?: eventTypes.find { it.name == event.type.name }
    } else {
        event.customTypeName?.let { ctn -> eventTypes.find { it.name == ctn } }
    }
}

// ─────────────────────────────────────────────────────────────────
// C-5: 礼物方向
// ─────────────────────────────────────────────────────────────────

/**
 * 礼物方向枚举：送出 / 收到。
 *
 * 颜色、图标、文案在此单一来源，避免手机/平板详情页各自硬编码产生不一致。
 */
internal enum class GiftDirection(
    val color: Color,
    val icon: ImageVector,
    val label: String
) {
    SENT(SignalGreen, Icons.Default.NorthEast, "送出"),
    RECEIVED(SignalSky, Icons.Default.SouthWest, "收到")
}

/** 根据礼物方向（送出/收到）返回对应的视觉规则。 */
internal fun Gift.direction(): GiftDirection =
    if (isSent) GiftDirection.SENT else GiftDirection.RECEIVED

// ─────────────────────────────────────────────────────────────────
// C-6: 纪念日类型颜色
// ─────────────────────────────────────────────────────────────────

/**
 * 纪念日类型对应的主色调。
 *
 * 颜色映射作为设计规则集中于此，避免手机/平板各写一份 when 分支。
 */
internal val AnniversaryType.accentColor: Color
    get() = when (this) {
        AnniversaryType.BIRTHDAY -> AnniversaryBirthday
        AnniversaryType.ANNIVERSARY -> SignalPurple
        AnniversaryType.HOLIDAY -> SignalAmber
    }
