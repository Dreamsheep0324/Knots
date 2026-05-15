package com.tang.prm.ui.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.model.Anniversary
import com.tang.prm.domain.model.AnniversaryType
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.Gift
import com.tang.prm.domain.model.Thought
import com.tang.prm.domain.model.EventTypes
import com.tang.prm.ui.components.AppCard
import com.tang.prm.ui.theme.AnniversaryBirthday
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.SignalSky
import com.tang.prm.util.DateUtils
import com.tang.prm.domain.model.AppStrings
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.getEventTypeStyle
import com.tang.prm.ui.theme.getGenericIcon
import com.tang.prm.ui.theme.toComposeColor

@Composable
internal fun EventsContent(events: List<Event>, eventTypes: List<CustomType>, onEventClick: (Long) -> Unit) {
    ContentSection(emptyIcon = Icons.Default.Event, emptyText = "暂无事件记录") {
        events.forEach { event -> EventCard(event = event, eventTypes = eventTypes, onClick = { onEventClick(event.id) }) }
    }
}

@Composable
internal fun AnniversariesContent(anniversaries: List<Anniversary>, onAnniversaryClick: (Long) -> Unit) {
    ContentSection(emptyIcon = Icons.Default.Cake, emptyText = "暂无纪念日") {
        anniversaries.forEach { a -> AnniversaryCard(anniversary = a, onClick = { onAnniversaryClick(a.id) }) }
    }
}

@Composable
internal fun GiftsContent(gifts: List<Gift>, onGiftClick: (Long) -> Unit) {
    ContentSection(emptyIcon = Icons.Default.CardGiftcard, emptyText = "暂无礼物记录") {
        gifts.forEach { gift -> GiftCard(gift = gift, onClick = { onGiftClick(gift.id) }) }
    }
}

@Composable
internal fun ThoughtsContent(thoughts: List<Thought>, onThoughtClick: (Long) -> Unit) {
    ContentSection(emptyIcon = Icons.Default.Lightbulb, emptyText = "暂无想法记录") {
        thoughts.forEach { thought -> ThoughtCard(thought = thought, onClick = { onThoughtClick(thought.id) }) }
    }
}

@Composable
internal fun ChatContent(conversations: List<Event>, onConversationClick: (Long) -> Unit) {
    ContentSection(emptyIcon = Icons.AutoMirrored.Filled.Chat, emptyText = "暂无对话记录") {
        conversations.forEach { c -> ChatCard(conversation = c, onClick = { onConversationClick(c.id) }) }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
private fun ContentSection(emptyIcon: ImageVector, emptyText: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        content()
    }
}

@Composable
private fun EventCard(event: Event, eventTypes: List<CustomType>, onClick: () -> Unit) {
    val typeLabel = getEventTypeLabel(event.type)
    val customType = if (event.type.isNotBlank()) eventTypes.find { it.key == event.type } ?: eventTypes.find { it.name == event.type } else null
    val accentColor: Color
    val icon: ImageVector

    if (customType != null) {
        accentColor = customType.color?.let { it.toComposeColor(SignalPurple) } ?: SignalPurple
        icon = customType.icon?.let { getGenericIcon(it) } ?: Icons.Default.Event
    } else {
        val typeStyle = getEventTypeStyle(event.type)
        accentColor = typeStyle.accentColor
        icon = typeStyle.icon
    }

    ContentCard(onClick = onClick, icon = icon, iconColor = accentColor) {
        Text(text = event.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(4.dp), color = accentColor.copy(alpha = 0.1f)) {
                Text(text = typeLabel, style = MaterialTheme.typography.labelSmall, color = accentColor, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp)
            }
            Text(text = DateUtils.formatDate(event.time), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            event.location?.takeIf { it.isNotBlank() }?.let {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(10.dp))
                    Text(text = it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun AnniversaryCard(anniversary: Anniversary, onClick: () -> Unit) {
    val accentColor = when (anniversary.type) {
        AnniversaryType.BIRTHDAY -> AnniversaryBirthday
        AnniversaryType.ANNIVERSARY -> SignalPurple
        AnniversaryType.HOLIDAY -> SignalAmber
    }
    ContentCard(onClick = onClick, icon = Icons.Default.Cake, iconColor = accentColor) {
        Text(text = anniversary.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(4.dp), color = accentColor.copy(alpha = 0.1f)) {
                Text(text = anniversary.type.displayName, style = MaterialTheme.typography.labelSmall, color = accentColor, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp)
            }
            Text(text = DateUtils.formatDate(anniversary.date), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (anniversary.isRepeat) {
                Icon(Icons.Default.Repeat, contentDescription = "每年", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(10.dp))
            }
        }
    }
}

@Composable
private fun GiftCard(gift: Gift, onClick: () -> Unit) {
    val directionColor = if (gift.isSent) SignalGreen else SignalSky
    val directionIcon = if (gift.isSent) Icons.Default.NorthEast else Icons.Default.SouthWest
    ContentCard(onClick = onClick, icon = Icons.Default.CardGiftcard, iconColor = SignalAmber) {
        Text(text = gift.giftName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(4.dp), color = directionColor.copy(alpha = 0.1f)) {
                Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Icon(directionIcon, contentDescription = null, tint = directionColor, modifier = Modifier.size(10.dp))
                    Text(text = if (gift.isSent) "送出" else "收到", style = MaterialTheme.typography.labelSmall, color = directionColor, fontWeight = FontWeight.Medium, fontSize = 10.sp)
                }
            }
            gift.date.let { Text(text = DateUtils.formatDate(it), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            gift.amount?.let { Text(text = "¥${String.format("%.0f", it)}", style = MaterialTheme.typography.labelSmall, color = SignalAmber, fontWeight = FontWeight.Medium, fontSize = 10.sp) }
        }
    }
}

@Composable
private fun ThoughtCard(thought: Thought, onClick: () -> Unit) {
    ContentCard(onClick = onClick, icon = Icons.Default.Lightbulb, iconColor = SignalAmber) {
        Text(text = thought.content, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface, lineHeight = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = DateUtils.formatDate(thought.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ChatCard(conversation: Event, onClick: () -> Unit) {
    ContentCard(onClick = onClick, icon = Icons.AutoMirrored.Filled.Chat, iconColor = SignalPurple) {
        Text(text = conversation.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            conversation.emotion?.takeIf { it.isNotBlank() }?.let {
                Surface(shape = RoundedCornerShape(4.dp), color = SignalPurple.copy(alpha = 0.1f)) {
                    Text(text = it, style = MaterialTheme.typography.labelSmall, color = SignalPurple, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp)
                }
            }
            Text(text = DateUtils.formatRelativeTime(conversation.time), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ContentCard(onClick: () -> Unit, icon: ImageVector, iconColor: Color, content: @Composable ColumnScope.() -> Unit) {
    AppCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Dimens.paddingCard),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier.size(44.dp).background(iconColor.copy(alpha = AnimationTokens.Alpha.faint), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) { content() }
        }
    }
}

internal fun getEventTypeLabel(type: String): String = when (type) {
    "MEAL" -> AppStrings.EventType.DINING
    EventTypes.MEETUP -> AppStrings.EventType.MEETUP
    EventTypes.TRAVEL -> "旅行"
    "ENTERTAINMENT" -> "娱乐"
    EventTypes.CONVERSATION -> "对话"
    "MEETING" -> "会议"
    "CELEBRATION" -> "庆祝"
    "SPORT" -> "运动"
    EventTypes.OTHER -> AppStrings.EventType.OTHER
    else -> type
}
