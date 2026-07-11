package com.tang.prm.feature.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.EventType
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.components.AppCard
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.EventLightIndigo
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.SignalSky
import com.tang.prm.ui.theme.getEmotionColor
import com.tang.prm.ui.theme.getEmotionIcon
import com.tang.prm.ui.theme.getEventTypeStyle
import com.tang.prm.ui.theme.getGenericIcon
import com.tang.prm.ui.theme.getWeatherColor
import com.tang.prm.ui.theme.getWeatherIcon
import com.tang.prm.ui.theme.toComposeColor
import com.tang.prm.domain.util.DateUtils
import java.util.Calendar

@Composable
internal fun EventCard(event: Event, eventTypes: List<CustomType>, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val customType = if (event.type != EventType.OTHER) eventTypes.find { it.key == event.type.name } ?: eventTypes.find { it.name == event.type.name } else event.customTypeName?.let { ctn -> eventTypes.find { it.name == ctn } }
    val accentColor: Color
    val lightColor: Color
    val icon: ImageVector

    if (customType != null) {
        val baseColor = customType.color?.let {
            it.toComposeColor(SignalPurple)
        } ?: SignalPurple
        accentColor = baseColor
        lightColor = baseColor.copy(alpha = AnimationTokens.Alpha.subtle)
        icon = customType.icon?.let { getGenericIcon(it) } ?: Icons.Default.Event
    } else {
        val style = getEventTypeStyle(event.type)
        accentColor = style.accentColor
        lightColor = style.lightColor
        icon = style.icon
    }

    AppCard(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(Dimens.paddingCard)) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(modifier = Modifier.size(50.dp).background(lightColor, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = event.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        if (event.type != EventType.OTHER || event.customTypeName != null) {
                            Surface(shape = RoundedCornerShape(20.dp), color = lightColor) {
                                Text(text = event.customTypeName ?: event.type.displayName, modifier = Modifier.padding(10.dp, 4.dp), style = MaterialTheme.typography.labelSmall, color = accentColor, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.visible), modifier = Modifier.size(13.dp))
                        Text(text = DateUtils.formatRelativeTime(event.time), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)

                        event.location?.let {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.Place, contentDescription = null, tint = SignalSky, modifier = Modifier.size(13.dp))
                            Text(text = it, style = MaterialTheme.typography.bodySmall, color = SignalSky, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                        }
                    }
                }
            }

            event.description?.let { desc ->
                if (desc.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = desc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 20.sp)
                }
            }

            val hasMetadata = event.photos.isNotEmpty() || event.weather != null || event.emotion != null
            if (hasMetadata) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (event.photos.isNotEmpty()) {
                        MetaTag(icon = Icons.Default.PhotoCamera, text = "${event.photos.size}", bgColor = EventLightIndigo, textColor = SignalPurple)
                    }

                    event.weather?.let { weather ->
                        if (weather.isNotBlank()) {
                            val wColor = getWeatherColor(weather)?.let { it.toComposeColor(SignalAmber) } ?: SignalAmber
                            val wIcon = getWeatherIcon(weather) ?: Icons.Default.WbSunny
                            MetaTag(icon = wIcon, text = weather, bgColor = wColor.copy(alpha = AnimationTokens.Alpha.faint), textColor = wColor)
                        }
                    }

                    event.emotion?.let { emotion ->
                        if (emotion.isNotBlank()) {
                            val eColor = getEmotionColor(emotion)?.let { it.toComposeColor(SignalPurple) } ?: SignalPurple
                            val eIcon = getEmotionIcon(emotion) ?: Icons.Default.Favorite
                            MetaTag(icon = eIcon, text = emotion, bgColor = eColor.copy(alpha = AnimationTokens.Alpha.faint), textColor = eColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun MetaTag(icon: ImageVector, text: String, bgColor: Color, textColor: Color) {
    Surface(shape = RoundedCornerShape(10.dp), color = bgColor) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = text, style = MaterialTheme.typography.labelSmall, color = textColor, fontWeight = FontWeight.Medium, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
internal fun EventsTimelineView(
    events: List<Event>,
    onEventClick: (Event) -> Unit
) {
    val eventsByDate = events.groupBy { event ->
        val cal = Calendar.getInstance()
        cal.timeInMillis = event.time
        Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
    }.toList().sortedByDescending { (date, _) ->
        Calendar.getInstance().apply { set(date.first, date.second, date.third) }.timeInMillis
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        eventsByDate.forEach { (date, dateEvents) ->
            item {
                val cal = Calendar.getInstance().apply { set(date.first, date.second, date.third) }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 10.dp)) {
                    Text(text = DateUtils.formatMonthDayWeekday(cal.timeInMillis), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = "${dateEvents.size}件事", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.visible))
                }
            }

            items(dateEvents.size, key = { dateEvents[it].id }) { index ->
                val event = dateEvents[index]
                val isLast = index == dateEvents.size - 1

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(24.dp)) {
                        Box(modifier = Modifier.size(10.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                        if (!isLast) {
                            Box(modifier = Modifier.width(2.dp).height(72.dp).background(MaterialTheme.colorScheme.outline))
                        }
                    }

                    AppCard(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = if (isLast) 0.dp else 8.dp)
                            .clickable { onEventClick(event) }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = event.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(12.dp))
                                    Text(text = DateUtils.formatRelativeTime(event.time), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 11.sp)
                                }

                                event.location?.let {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Icon(Icons.Default.Place, contentDescription = null, tint = SignalSky, modifier = Modifier.size(12.dp))
                                        Text(text = it, style = MaterialTheme.typography.labelSmall, color = SignalSky, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(80.dp), fontSize = 11.sp)
                                    }
                                }

                                if (event.photos.isNotEmpty()) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = SignalPurple, modifier = Modifier.size(12.dp))
                                        Text(text = "${event.photos.size}张", style = MaterialTheme.typography.labelSmall, color = SignalPurple, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}
