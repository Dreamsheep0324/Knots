package com.tang.prm.ui.theme

import androidx.compose.runtime.Composable
import com.tang.prm.domain.model.EventType
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

@Composable
fun getEventTypeStyle(eventType: EventType): EventTypeStyle {
    return when (eventType) {
        EventType.MEETUP -> EventTypeStyle(SignalGreen, EventLightGreen, Icons.Default.People)
        EventType.DINING -> EventTypeStyle(SignalAmber, EventLightAmber, Icons.Default.Restaurant)
        EventType.TRAVEL -> EventTypeStyle(SignalSky, EventLightBlue, Icons.Default.Flight)
        EventType.CALL -> EventTypeStyle(SignalPurple, EventLightPurple, Icons.Default.Phone)
        EventType.GIFT_SENT, EventType.GIFT_RECEIVED -> EventTypeStyle(SignalCoral, EventLightRed, Icons.Default.CardGiftcard)
        EventType.MONEY_LEND, EventType.MONEY_BORROW -> EventTypeStyle(EventMoneyTeal, EventLightTeal, Icons.Default.AttachMoney)
        EventType.CONVERSATION -> EventTypeStyle(SignalPurple, EventLightIndigo, Icons.AutoMirrored.Filled.Chat)
        EventType.OTHER -> EventTypeStyle(SignalElectric, EventLightIndigo, Icons.Default.MoreHoriz)
    }
}
