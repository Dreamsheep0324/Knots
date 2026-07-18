package com.tang.prm.feature.home

// Q-11 修复：OrbitalCalendarData 从 OrbitalCalendarState.kt 移到独立文件，
// 文件名与类名匹配，符合「一个文件一个公共类」约定，便于查找

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
