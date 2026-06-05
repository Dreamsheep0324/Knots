package com.tang.prm.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.tang.prm.domain.model.Anniversary
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.util.DateUtils
import java.util.Calendar

internal data class OrbitalCalendarData(
    val daySignalMap: Map<Int, Int>,
    val dayOfWeekMap: Map<Int, Int>,
    val todayEvents: List<String>,
    val upcomingEvents: List<Pair<String, String>>,
    val nextEventCountdown: Pair<String, Pair<Int, Int>>?,
    val daysInMonth: Int,
    val isCurrentMonth: Boolean,
    val monthProgress: Float
)

@Composable
internal fun rememberOrbitalCalendarData(
    displayMonth: Int,
    displayYear: Int,
    events: List<Event>,
    anniversaries: List<Anniversary>
): OrbitalCalendarData {
    val todayCal = Calendar.getInstance()
    val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)
    val todayMonth = todayCal.get(Calendar.MONTH)
    val todayYear = todayCal.get(Calendar.YEAR)

    val displayCal = remember(displayYear, displayMonth) {
        Calendar.getInstance().apply { set(displayYear, displayMonth, 1) }
    }
    val daysInMonth = displayCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val isCurrentMonth = displayYear == todayYear && displayMonth == todayMonth
    val monthProgress = if (isCurrentMonth) todayDay.toFloat() / daysInMonth else 0f

    val daySignalMap = remember(events, anniversaries, displayYear, displayMonth) {
        buildMap<Int, Int> {
            events.forEach { event ->
                val cal = Calendar.getInstance().apply { timeInMillis = event.time }
                if (cal.get(Calendar.YEAR) == displayYear && cal.get(Calendar.MONTH) == displayMonth) {
                    val day = cal.get(Calendar.DAY_OF_MONTH)
                    this[day] = (this[day] ?: 0) + 1
                }
            }
            anniversaries.forEach { ann ->
                val cal = Calendar.getInstance().apply { timeInMillis = ann.date }
                if (cal.get(Calendar.YEAR) == displayYear || ann.isRepeat) {
                    if (cal.get(Calendar.MONTH) == displayMonth) {
                        val day = cal.get(Calendar.DAY_OF_MONTH)
                        this[day] = (this[day] ?: 0) + 1
                    }
                }
            }
        }
    }

    val dayOfWeekMap = remember(displayYear, displayMonth, daysInMonth) {
        (1..daysInMonth).associateWith { day ->
            val cal = Calendar.getInstance().apply { set(displayYear, displayMonth, day) }
            cal.get(Calendar.DAY_OF_WEEK) - 1
        }
    }

    val todayEvents = remember(events, anniversaries) {
        val evts = events.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.time }
            cal.get(Calendar.YEAR) == todayYear && cal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)
        }
        val anns = anniversaries.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.date }
            cal.get(Calendar.MONTH) == todayMonth && cal.get(Calendar.DAY_OF_MONTH) == todayDay
        }
        evts.map { it.title.ifBlank { it.type.displayName } } + anns.map { it.name }
    }

    val upcomingEvents = remember(events) {
        val now = System.currentTimeMillis()
        val threeDaysLater = now + 3 * 24 * 60 * 60 * 1000L
        events.filter { it.time in now..threeDaysLater }
            .sortedBy { it.time }
            .take(3)
            .map { it.title.ifBlank { it.type.displayName } to DateUtils.formatMonthDay(it.time) }
    }

    val nextEventCountdown = remember(events) {
        val now = System.currentTimeMillis()
        events.filter { it.time > now }
            .minByOrNull { it.time }
            ?.let {
                val diffMs = it.time - now
                val days = (diffMs / (24 * 60 * 60 * 1000)).toInt()
                val hours = ((diffMs % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000)).toInt()
                it.title.ifBlank { it.type.displayName } to (days to hours)
            }
    }

    return OrbitalCalendarData(
        daySignalMap = daySignalMap,
        dayOfWeekMap = dayOfWeekMap,
        todayEvents = todayEvents,
        upcomingEvents = upcomingEvents,
        nextEventCountdown = nextEventCountdown,
        daysInMonth = daysInMonth,
        isCurrentMonth = isCurrentMonth,
        monthProgress = monthProgress
    )
}
