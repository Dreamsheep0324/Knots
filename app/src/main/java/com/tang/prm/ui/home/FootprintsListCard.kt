package com.tang.prm.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.EventType
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.theme.CardBorder
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.SignalSky
import com.tang.prm.ui.theme.TextGray
import com.tang.prm.ui.theme.getEventTypeStyle
import com.tang.prm.ui.theme.getGenericIcon
import com.tang.prm.ui.theme.toComposeColor
import com.tang.prm.util.DateUtils

@Composable
internal fun ListCard(
    footprint: FootprintItem,
    dayFormat: (Long) -> String,
    eventTypes: List<CustomType>,
    onClick: () -> Unit = {}
) {
    val (accentColor, lightColor, icon) = resolveEventTypeStyle(footprint.eventType, eventTypes)
    val eventTypeData = EventType.entries.find { it.name == footprint.eventType }
    val eventTypeDisplayName = eventTypeData?.displayName ?: footprint.eventType

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 3.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(Dimens.paddingCard)) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(lightColor, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = footprint.eventTitle,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = lightColor
                        ) {
                            Text(
                                text = eventTypeDisplayName,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = accentColor
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = TextGray.copy(alpha = 0.4f),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = dayFormat(footprint.date),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = TextGray.copy(alpha = AnimationTokens.Alpha.visible)
                        )

                        Icon(
                            Icons.Default.Place,
                            contentDescription = null,
                            tint = SignalSky,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = footprint.location,
                            fontSize = 12.sp,
                            color = SignalSky,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }
            }

            val hasMeta = !footprint.weather.isNullOrBlank() || !footprint.emotion.isNullOrBlank()
            if (hasMeta || footprint.contactName != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (!footprint.weather.isNullOrBlank()) {
                            FootprintMetaTag(
                                icon = getWeatherIconForFootprint(footprint.weather),
                                text = footprint.weather,
                                bgColor = getWeatherColorForFootprint(footprint.weather).copy(alpha = AnimationTokens.Alpha.faint),
                                textColor = getWeatherColorForFootprint(footprint.weather)
                            )
                        }
                        if (!footprint.emotion.isNullOrBlank()) {
                            val eIcon = getEmotionIconForFootprint(footprint.emotion) ?: Icons.Default.Favorite
                            val eColor = getEmotionColorForFootprint(footprint.emotion) ?: SignalPurple
                            FootprintMetaTag(
                                icon = eIcon,
                                text = footprint.emotion,
                                bgColor = eColor.copy(alpha = AnimationTokens.Alpha.faint),
                                textColor = eColor
                            )
                        }
                    }

                    if (footprint.contactName != null) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(accentColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = footprint.contactName.firstOrNull()?.toString() ?: "?",
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = footprint.contactName,
                                fontSize = 12.sp,
                                color = TextGray.copy(alpha = AnimationTokens.Alpha.visible)
                            )
                        }
                    }
                }
            }
        }
    }
}

internal fun LazyListScope.listItems(footprints: List<FootprintItem>, eventTypes: List<CustomType>, onFootprintClick: (FootprintItem) -> Unit = {}) {
    items(items = footprints, key = { it.id }) { footprint ->
        ListCard(footprint = footprint, dayFormat = { DateUtils.formatMonthDayChineseFull(it) }, eventTypes = eventTypes, onClick = { onFootprintClick(footprint) })
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
internal fun FootprintMetaTag(
    icon: ImageVector,
    text: String,
    bgColor: Color,
    textColor: Color
) {
    Surface(shape = RoundedCornerShape(8.dp), color = bgColor) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)) {
            Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(11.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(text = text, fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = textColor, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
internal fun resolveEventTypeStyle(eventType: String, eventTypes: List<CustomType>): Triple<Color, Color, ImageVector> {
    val customType = if (eventType.isNotBlank()) eventTypes.find { it.key == eventType } ?: eventTypes.find { it.name == eventType } else null
    return if (customType != null) {
        val baseColor = customType.color?.let { it.toComposeColor(SignalPurple) } ?: SignalPurple
        Triple(baseColor, baseColor.copy(alpha = AnimationTokens.Alpha.subtle), customType.icon?.let { getGenericIcon(it) } ?: Icons.Default.Event)
    } else {
        val style = getEventTypeStyle(eventType)
        Triple(style.accentColor, style.lightColor, style.icon)
    }
}

@Composable
internal fun getEventTypeColor(eventType: String): Color = getEventTypeStyle(eventType).accentColor

@Composable
internal fun getEventTypeIcon(eventType: String): ImageVector = getEventTypeStyle(eventType).icon

@Composable
internal fun getEventTypeLightColor(eventType: String): Color = getEventTypeStyle(eventType).lightColor

internal fun getWeatherIconForFootprint(weather: String?): ImageVector {
    if (weather.isNullOrBlank()) return Icons.Default.WbSunny
    return com.tang.prm.ui.theme.getWeatherIcon(weather) ?: Icons.Default.WbSunny
}

@Composable
internal fun getWeatherColorForFootprint(weather: String?): Color {
    if (weather.isNullOrBlank()) return TextGray
    val colorStr = com.tang.prm.ui.theme.getWeatherColor(weather)
    return colorStr?.let { it.toComposeColor(SignalAmber) } ?: SignalAmber
}

internal fun getEmotionIconForFootprint(emotion: String?): ImageVector? {
    if (emotion.isNullOrBlank()) return null
    return com.tang.prm.ui.theme.getEmotionIcon(emotion)
}

internal fun getEmotionColorForFootprint(emotion: String?): Color? {
    if (emotion.isNullOrBlank()) return null
    val colorStr = com.tang.prm.ui.theme.getEmotionColor(emotion)
    return colorStr?.let { it.toComposeColor(SignalPurple) }
}
