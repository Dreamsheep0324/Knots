package com.tang.prm.feature.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.EventType
import com.tang.prm.domain.util.DateUtils
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.components.AppCard
import com.tang.prm.ui.components.ContactAvatar
import com.tang.prm.ui.theme.*
import java.util.Calendar

// ═══════════════════════════════════════════════════════════════
// 日历组件
// ═══════════════════════════════════════════════════════════════

private val weekdays = listOf("日", "一", "二", "三", "四", "五", "六")

private data class CalendarCell(
    val day: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean,
    val hasEvents: Boolean,
    val timestamp: Long
)

@Composable
internal fun CalendarHeader(
    monthOffset: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit
) {
    val monthStart = remember(monthOffset) {
        EventsViewModel.getMonthRange(monthOffset).first
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = DateUtils.formatYearMonthChinese(monthStart),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CalendarNavButton(icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft, onClick = onPrevious)
            Surface(
                onClick = onToday,
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "今天",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            CalendarNavButton(icon = Icons.AutoMirrored.Filled.KeyboardArrowRight, onClick = onNext)
        }
    }
}

@Composable
private fun CalendarNavButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
internal fun CalendarWeekdays() {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        weekdays.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.visible),
                textAlign = TextAlign.Center,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
internal fun CalendarGrid(
    monthOffset: Int,
    selectedDate: Long,
    calendarEvents: List<Event>,
    onDateSelected: (Long) -> Unit
) {
    val cells = remember(monthOffset, selectedDate, calendarEvents) {
        computeCalendarCells(monthOffset, selectedDate, calendarEvents)
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        cells.chunked(7).forEachIndexed { weekIndex, week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEachIndexed { dayIndex, cell ->
                    CalendarDayCell(
                        cell = cell,
                        onDateSelected = onDateSelected,
                        modifier = Modifier.weight(1f).aspectRatio(1f)
                    )
                }
            }
        }
    }
}

private fun computeCalendarCells(
    monthOffset: Int,
    selectedDate: Long,
    calendarEvents: List<Event>
): List<CalendarCell> {
    val cal = Calendar.getInstance()
    cal.add(Calendar.MONTH, monthOffset)
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)

    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH)

    // 1=Sunday, 7=Saturday → offset = firstDayOfWeek - 1
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val offset = firstDayOfWeek - 1

    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Previous month trailing days
    val prevCal = (cal.clone() as Calendar).apply {
        add(Calendar.MONTH, -1)
    }
    val daysInPrevMonth = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Today
    val todayCal = Calendar.getInstance()
    val isCurrentMonth = todayCal.get(Calendar.YEAR) == year && todayCal.get(Calendar.MONTH) == month
    val todayDay = if (isCurrentMonth) todayCal.get(Calendar.DAY_OF_MONTH) else -1

    // Selected date
    val selectedCal = Calendar.getInstance().apply { timeInMillis = selectedDate }
    val isSelectedMonth = selectedCal.get(Calendar.YEAR) == year && selectedCal.get(Calendar.MONTH) == month
    val selectedDay = if (isSelectedMonth) selectedCal.get(Calendar.DAY_OF_MONTH) else -1

    // Event days
    val eventDays = mutableSetOf<Int>()
    calendarEvents.forEach { event ->
        val eventCal = Calendar.getInstance().apply { timeInMillis = event.time }
        if (eventCal.get(Calendar.YEAR) == year && eventCal.get(Calendar.MONTH) == month) {
            eventDays.add(eventCal.get(Calendar.DAY_OF_MONTH))
        }
    }

    val cells = mutableListOf<CalendarCell>()

    // Previous month days
    for (i in 0 until offset) {
        val day = daysInPrevMonth - offset + i + 1
        val dayCal = (prevCal.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, day)
        }
        cells.add(CalendarCell(day, isCurrentMonth = false, isToday = false, isSelected = false, hasEvents = false, timestamp = dayCal.timeInMillis))
    }

    // Current month days
    for (day in 1..daysInMonth) {
        val dayCal = (cal.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, day)
        }
        cells.add(CalendarCell(
            day = day,
            isCurrentMonth = true,
            isToday = day == todayDay,
            isSelected = day == selectedDay,
            hasEvents = eventDays.contains(day),
            timestamp = dayCal.timeInMillis
        ))
    }

    // Next month leading days
    val nextCal = (cal.clone() as Calendar).apply {
        add(Calendar.MONTH, 1)
    }
    val remaining = 42 - cells.size
    for (day in 1..remaining) {
        val dayCal = (nextCal.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, day)
        }
        cells.add(CalendarCell(day, isCurrentMonth = false, isToday = false, isSelected = false, hasEvents = false, timestamp = dayCal.timeInMillis))
    }

    return cells
}

@Composable
private fun CalendarDayCell(
    cell: CalendarCell,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        cell.isToday -> SignalGreen
        cell.isSelected -> SignalGreen.copy(alpha = 0.12f)
        else -> Color.Transparent
    }
    val textColor = when {
        cell.isToday -> Color.White
        !cell.isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        cell.isSelected -> SignalGreen
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .padding(3.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable(enabled = cell.isCurrentMonth) { onDateSelected(cell.timestamp) },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = cell.day.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (cell.isToday || cell.isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                fontSize = 17.sp
            )
            if (cell.hasEvents) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            if (cell.isToday) Color.White else SignalSky,
                            CircleShape
                        )
                )
            }
        }
    }
}

@Composable
internal fun CalendarStatsRow(stats: CalendarStats) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CalendarStatItem(
            value = stats.eventCount.toString(),
            label = "事件",
            color = SignalGreen,
            modifier = Modifier.weight(1f)
        )
        CalendarStatItem(
            value = stats.participantCount.toString(),
            label = "参与人",
            color = SignalSky,
            modifier = Modifier.weight(1f)
        )
        CalendarStatItem(
            value = "¥${formatAmount(stats.totalSpending)}",
            label = "花费",
            color = SignalAmber,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CalendarStatItem(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(vertical = 16.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            fontSize = 22.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
    }
}

private fun formatAmount(amount: Double): String {
    return if (amount == amount.toLong().toDouble()) {
        amount.toLong().toString()
    } else {
        String.format("%.1f", amount)
    }
}

// ═══════════════════════════════════════════════════════════════
// 今日事件列表（左面板下半部分）
// ═══════════════════════════════════════════════════════════════

@Composable
internal fun SelectedDateEventsSection(
    events: List<Event>,
    eventTypes: List<CustomType>,
    onEventClick: (Event) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "当日事件",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
            Text(
                text = "${events.size}件",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.visible),
                fontSize = 12.sp
            )
        }

        if (events.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "当天没有事件",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.visible),
                    fontSize = 14.sp
                )
            }
        } else {
            events.take(5).forEach { event ->
                CompactEventCard(
                    event = event,
                    eventTypes = eventTypes,
                    onClick = { onEventClick(event) }
                )
            }
            if (events.size > 5) {
                Text(
                    text = "还有 ${events.size - 5} 件事件",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.visible),
                    modifier = Modifier.padding(start = 6.dp, top = 4.dp),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun CompactEventCard(
    event: Event,
    eventTypes: List<CustomType>,
    onClick: () -> Unit
) {
    val accentColor = getEventAccentColor(event, eventTypes)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(accentColor, CircleShape)
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp
            )
            Text(
                text = DateUtils.formatTime(event.time),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.visible),
                fontSize = 13.sp
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 日期分组事件列表（右面板）
// ═══════════════════════════════════════════════════════════════

@Composable
internal fun DateGroupedEventList(
    events: List<Event>,
    eventTypes: List<CustomType>,
    onEventClick: (Event) -> Unit
) {
    val groupedEvents = remember(events) {
        events.groupBy { event ->
            val cal = Calendar.getInstance().apply { timeInMillis = event.time }
            Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        }.toList().sortedByDescending { (date, _) ->
            Calendar.getInstance().apply {
                set(date.first, date.second, date.third)
            }.timeInMillis
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        groupedEvents.forEach { (date, dateEvents) ->
            item(key = "header_${date.first}_${date.second}_${date.third}") {
                val cal = Calendar.getInstance().apply {
                    set(date.first, date.second, date.third)
                }
                DateGroupHeader(
                    dateLabel = DateUtils.formatMonthDayWeekday(cal.timeInMillis),
                    eventCount = dateEvents.size
                )
            }

            items(dateEvents.sortedBy { it.time }, key = { it.id }) { event ->
                CalendarEventCard(
                    event = event,
                    eventTypes = eventTypes,
                    onClick = { onEventClick(event) }
                )
            }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun DateGroupHeader(dateLabel: String, eventCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = dateLabel,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
        Text(
            text = "$eventCount 件",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.visible),
            fontSize = 12.sp
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        )
    }
}

@Composable
private fun CalendarEventCard(
    event: Event,
    eventTypes: List<CustomType>,
    onClick: () -> Unit
) {
    val accentColor = getEventAccentColor(event, eventTypes)
    val lightColor = accentColor.copy(alpha = AnimationTokens.Alpha.subtle)

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 左侧彩色圆点
            Box(
                modifier = Modifier
                    .padding(top = 7.dp)
                    .size(10.dp)
                    .background(accentColor, CircleShape)
            )

            // 右侧内容
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 标题 + 时间
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        fontSize = 16.sp
                    )
                    Text(
                        text = DateUtils.formatTime(event.time),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.visible),
                        fontSize = 13.sp
                    )
                }

                // 地点
                event.location?.let { location ->
                    if (location.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Place,
                                contentDescription = null,
                                tint = SignalSky,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = location,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // 标签行
                val tags = buildList {
                    val typeLabel = event.customTypeName ?: event.type.displayName
                    if (event.type != EventType.OTHER || event.customTypeName != null) {
                        add(EventTagData(typeLabel, accentColor, lightColor))
                    }
                    event.amount?.let { amount ->
                        if (amount > 0) {
                            add(EventTagData("¥${formatAmount(amount)}", SignalAmber, SignalAmber.copy(alpha = AnimationTokens.Alpha.faint)))
                        }
                    }
                    event.weather?.let { weather ->
                        if (weather.isNotBlank()) {
                            val wColor = getWeatherColor(weather)?.let { it.toComposeColor(SignalAmber) } ?: SignalAmber
                            add(EventTagData(weather, wColor, wColor.copy(alpha = AnimationTokens.Alpha.faint)))
                        }
                    }
                    if (event.photos.isNotEmpty()) {
                        add(EventTagData("${event.photos.size}张", SignalPurple, SignalPurple.copy(alpha = AnimationTokens.Alpha.faint)))
                    }
                }

                if (tags.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        tags.forEach { tag ->
                            EventTagChip(tag = tag)
                        }
                    }
                }

                // 参与者头像
                if (event.participants.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy((-8).dp)
                    ) {
                        event.participants.take(5).forEach { participant ->
                            ContactAvatar(
                                avatar = participant.avatar,
                                name = participant.name,
                                size = 26,
                                modifier = Modifier.size(26.dp).clip(CircleShape)
                            )
                        }
                        if (event.participants.size > 5) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+${event.participants.size - 5}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class EventTagData(
    val text: String,
    val textColor: Color,
    val bgColor: Color
)

@Composable
private fun EventTagChip(tag: EventTagData) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = tag.bgColor
    ) {
        Text(
            text = tag.text,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = tag.textColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// 辅助函数
// ═══════════════════════════════════════════════════════════════

@Composable
private fun getEventAccentColor(event: Event, eventTypes: List<CustomType>): Color {
    val customType = if (event.type != EventType.OTHER) {
        eventTypes.find { it.key == event.type.name } ?: eventTypes.find { it.name == event.type.name }
    } else {
        event.customTypeName?.let { ctn -> eventTypes.find { it.name == ctn } }
    }
    return if (customType != null) {
        customType.color?.let { it.toComposeColor(SignalPurple) } ?: SignalPurple
    } else {
        getEventTypeStyle(event.type).accentColor
    }
}
