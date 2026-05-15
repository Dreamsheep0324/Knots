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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.model.EventType
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.theme.CardBorder
import com.tang.prm.ui.theme.GridLine
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.SignalSky
import com.tang.prm.ui.theme.TextGray
import com.tang.prm.util.DateUtils

@Composable
internal fun TimelineView(footprints: List<FootprintItem>, eventTypes: List<com.tang.prm.domain.model.CustomType>, onFootprintClick: (FootprintItem) -> Unit = {}) {
    val dayFormat: (Long) -> String = { DateUtils.formatMonthDayChineseFull(it) }
    val grouped = remember(footprints) {
        footprints.groupBy { DateUtils.formatYearMonthChinese(it.date) }
    }
    val gridLineColor = GridLine

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .drawBehind {
                val centerX = size.width / 2
                drawLine(
                    color = gridLineColor,
                    start = Offset(centerX, 0f),
                    end = Offset(centerX, size.height),
                    strokeWidth = 4.dp.toPx()
                )
            }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            grouped.entries.forEachIndexed { monthIndex, (monthLabel, monthFootprints) ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SignalGreen
                    ) {
                        Text(
                            text = monthLabel,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                monthFootprints.forEachIndexed { index, footprint ->
                    TimelineItem(
                        footprint = footprint,
                        dayFormat = dayFormat,
                        isLeft = index % 2 == 0,
                        eventTypes = eventTypes,
                        onClick = { onFootprintClick(footprint) }
                    )
                    if (index < monthFootprints.size - 1) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                if (monthIndex < grouped.size - 1) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun TimelineItem(
    footprint: FootprintItem,
    dayFormat: (Long) -> String,
    isLeft: Boolean,
    eventTypes: List<com.tang.prm.domain.model.CustomType>,
    onClick: () -> Unit = {}
) {
    val (accentColor, lightColor, icon) = resolveEventTypeStyle(footprint.eventType, eventTypes)

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.weight(0.42f),
            contentAlignment = Alignment.CenterEnd
        ) {
            if (isLeft) {
                TimelineCard(footprint, dayFormat, accentColor, lightColor, icon, onClick)
            }
        }

        Box(
            modifier = Modifier.weight(0.08f),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(accentColor, CircleShape)
            )
        }

        Box(
            modifier = Modifier.weight(0.42f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (!isLeft) {
                TimelineCard(footprint, dayFormat, accentColor, lightColor, icon, onClick)
            }
        }
    }
}

@Composable
private fun TimelineCard(
    footprint: FootprintItem,
    dayFormat: (Long) -> String,
    accentColor: Color,
    lightColor: Color,
    icon: ImageVector,
    onClick: () -> Unit = {}
) {
    val eventTypeData = EventType.entries.find { it.name == footprint.eventType }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 3.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(lightColor, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = footprint.eventTitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = dayFormat(footprint.date),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = TextGray.copy(alpha = AnimationTokens.Alpha.half)
                        )
                        Icon(
                            Icons.Default.Place,
                            contentDescription = null,
                            tint = SignalSky,
                            modifier = Modifier.size(9.dp)
                        )
                        Text(
                            text = footprint.location,
                            fontSize = 10.sp,
                            color = SignalSky,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = lightColor
                ) {
                    Text(
                        text = eventTypeData?.displayName ?: footprint.eventType,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
                if (!footprint.weather.isNullOrBlank()) {
                    FootprintMetaTag(
                        icon = getWeatherIconForFootprint(footprint.weather),
                        text = footprint.weather,
                        bgColor = getWeatherColorForFootprint(footprint.weather).copy(alpha = AnimationTokens.Alpha.faint),
                        textColor = getWeatherColorForFootprint(footprint.weather)
                    )
                }
            }
        }
    }
}

internal fun LazyListScope.timelineItems(footprints: List<FootprintItem>, eventTypes: List<com.tang.prm.domain.model.CustomType>, onFootprintClick: (FootprintItem) -> Unit = {}) {
    item(key = "timeline") {
        TimelineView(footprints = footprints, eventTypes = eventTypes, onFootprintClick = onFootprintClick)
    }
}
