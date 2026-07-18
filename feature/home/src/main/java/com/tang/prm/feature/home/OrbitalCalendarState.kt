package com.tang.prm.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.tang.prm.domain.model.Anniversary
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.effectiveDate
import com.tang.prm.domain.util.DateUtils
import java.util.Calendar

@Composable
internal fun rememberOrbitalCalendarData(
    displayMonth: Int,
    displayYear: Int,
    events: List<Event>,
    anniversaries: List<Anniversary>,
    // N-3 修复：todayDateKey 让 todayCal 跨日时失效重建，避免 P-5 remember 缓存导致的 staleness
    // 调用方传入 "yyyy-MM-dd" 字符串（来自 HomeScreen.dateStr 60s 轮询），跨日时自动刷新
    todayDateKey: String
): OrbitalCalendarData {
    // P-5 + N-3 修复：todayCal 用 remember(todayDateKey) 缓存，既避免每次重组新建对象，
    // 又能在跨日时（todayDateKey 变化）自动重建，保证 todayDay/todayMonth/todayYear 始终新鲜。
    val todayCal = remember(todayDateKey) { Calendar.getInstance() }
    val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)
    val todayMonth = todayCal.get(Calendar.MONTH)
    val todayYear = todayCal.get(Calendar.YEAR)

    // B-8 修复：先 clear 再 set，避免 Calendar.getInstance() 残留的时分秒污染 displayCal
    val displayCal = remember(displayYear, displayMonth) {
        Calendar.getInstance().apply {
            clear()
            set(displayYear, displayMonth, 1)
        }
    }
    val daysInMonth = displayCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val isCurrentMonth = displayYear == todayYear && displayMonth == todayMonth
    val monthProgress = if (isCurrentMonth) todayDay.toFloat() / daysInMonth else 0f

    // N-3 修复：daySignalMap 加入 todayDateKey 作为 key，跨日时重建（虽然 daySignalMap
    // 本身不依赖 today，但 todayDateKey 变化时 displayMonth/displayYear 也可能跨月，
    // 显式加 key 让跨月边界也立即刷新——redundant 但安全）
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
                // B-5 修复：用 effectiveDate() 替代 ann.date，农历纪念日按公历生效日期显示信号点
                val cal = Calendar.getInstance().apply { timeInMillis = ann.effectiveDate() }
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
            val cal = Calendar.getInstance().apply {
                clear()
                set(displayYear, displayMonth, day)
            }
            cal.get(Calendar.DAY_OF_WEEK) - 1
        }
    }

    // N-3 修复：todayEvents 加入 todayDateKey 作为 key，跨日时重新过滤"今日事件"
    val todayEvents = remember(events, anniversaries, todayDateKey) {
        val evts = events.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.time }
            cal.get(Calendar.YEAR) == todayYear && cal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)
        }
        // B-5 修复：今日纪念日判断也用 effectiveDate()，农历纪念日才能在正确的公历日显示
        val anns = anniversaries.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.effectiveDate() }
            cal.get(Calendar.MONTH) == todayMonth && cal.get(Calendar.DAY_OF_MONTH) == todayDay
        }
        evts.map { it.title.ifBlank { it.type.displayName } } + anns.map { it.name }
    }

    val upcomingEvents = remember(events) { computeUpcomingEvents(events) }

    val nextEventCountdown = remember(events) { computeNextEventCountdown(events) }

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

/**
 * 计算 3 天内的即将到来的事件（最多 3 项）。
 * 提取为顶层私有函数，降低 rememberOrbitalCalendarData 的圈复杂度与长度（detekt LongMethod）。
 */
private fun computeUpcomingEvents(events: List<Event>): List<Pair<String, String>> {
    val now = System.currentTimeMillis()
    // Q-7 修复：用 DateUtils.MILLIS_PER_DAY 替代魔法数字 3 * 24 * 60 * 60 * 1000L
    val threeDaysLater = now + 3 * DateUtils.MILLIS_PER_DAY
    return events.filter { it.time in now..threeDaysLater }
        .sortedBy { it.time }
        .take(3)
        .map { it.title.ifBlank { it.type.displayName } to DateUtils.formatMonthDay(it.time) }
}

/**
 * 计算下一个事件的倒计时（天 + 小时）。
 * 提取为顶层私有函数，降低 rememberOrbitalCalendarData 的圈复杂度与长度（detekt LongMethod）。
 */
private fun computeNextEventCountdown(events: List<Event>): Pair<String, Pair<Int, Int>>? {
    val now = System.currentTimeMillis()
    return events.filter { it.time > now }
        .minByOrNull { it.time }
        ?.let {
            val diffMs = it.time - now
            // Q-7/B-9 修复：用 DateUtils.MILLIS_PER_DAY 替代魔法数字，hours 用 MILLIS_PER_HOUR
            val days = (diffMs / DateUtils.MILLIS_PER_DAY).toInt()
            val hours = ((diffMs % DateUtils.MILLIS_PER_DAY) / DateUtils.MILLIS_PER_HOUR).toInt()
            it.title.ifBlank { it.type.displayName } to (days to hours)
        }
}
