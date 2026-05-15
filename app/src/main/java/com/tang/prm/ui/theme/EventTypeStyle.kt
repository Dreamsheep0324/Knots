package com.tang.prm.ui.theme

import androidx.compose.runtime.Composable
import com.tang.prm.domain.model.EventTypes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*

data class EventTypeStyle(
    val accentColor: Color,
    val lightColor: Color,
    val icon: ImageVector
)

/**
 * 根据事件类型字符串获取对应的视觉样式
 */
@Composable
fun getEventTypeStyle(eventType: String): EventTypeStyle {
    return when (eventType) {
        EventTypes.MEETUP -> EventTypeStyle(SignalGreen, EventLightGreen, Icons.Default.People)
        EventTypes.DINING -> EventTypeStyle(SignalAmber, EventLightAmber, Icons.Default.Restaurant)
        EventTypes.TRAVEL -> EventTypeStyle(SignalSky, EventLightBlue, Icons.Default.Flight)
        EventTypes.CALL -> EventTypeStyle(SignalPurple, EventLightPurple, Icons.Default.Phone)
        EventTypes.GIFT_SENT, EventTypes.GIFT_RECEIVED -> EventTypeStyle(SignalCoral, EventLightRed, Icons.Default.CardGiftcard)
        EventTypes.MONEY_LEND, EventTypes.MONEY_BORROW -> EventTypeStyle(EventMoneyTeal, EventLightTeal, Icons.Default.AttachMoney)
        EventTypes.CONVERSATION -> EventTypeStyle(SignalPurple, EventLightIndigo, Icons.AutoMirrored.Filled.Chat)
        EventTypes.OTHER -> EventTypeStyle(SignalElectric, EventLightIndigo, Icons.Default.MoreHoriz)
        else -> EventTypeStyle(SignalElectric, EventLightIndigo, Icons.Default.Event)
    }
}
