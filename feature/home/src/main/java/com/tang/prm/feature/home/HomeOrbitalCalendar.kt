package com.tang.prm.feature.home

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.model.Anniversary
import com.tang.prm.domain.model.Event
import com.tang.prm.ui.components.AppCard
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.SignalElectric
import com.tang.prm.ui.theme.TextGray
import java.util.Calendar
import java.util.Locale

@Composable
internal fun OrbitalCalendar(
    anniversaries: List<Anniversary>,
    events: List<Event>
) {
    val todayCal = Calendar.getInstance()
    val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)
    val todayMonth = todayCal.get(Calendar.MONTH)
    val todayYear = todayCal.get(Calendar.YEAR)

    var displayMonth by remember { mutableStateOf(todayMonth) }
    var displayYear by remember { mutableStateOf(todayYear) }

    val data = rememberOrbitalCalendarData(
        displayMonth = displayMonth,
        displayYear = displayYear,
        events = events,
        anniversaries = anniversaries
    )

    AppCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Dimens.paddingCard)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("轨道罗盘", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(8.dp))
                    if (data.isCurrentMonth && data.todayEvents.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier
                                .background(SignalElectric.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(SignalElectric))
                            Text("${data.todayEvents.size}项今日", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = SignalElectric)
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        val c = Calendar.getInstance().apply { set(displayYear, displayMonth, 1); add(Calendar.MONTH, -1) }
                        displayMonth = c.get(Calendar.MONTH); displayYear = c.get(Calendar.YEAR)
                    }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "上月", tint = TextGray, modifier = Modifier.size(16.dp))
                    }
                    Text("${displayYear}.${String.format(Locale.US, "%02d", displayMonth + 1)}",
                        fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    IconButton(onClick = {
                        val c = Calendar.getInstance().apply { set(displayYear, displayMonth, 1); add(Calendar.MONTH, 1) }
                        displayMonth = c.get(Calendar.MONTH); displayYear = c.get(Calendar.YEAR)
                    }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "下月", tint = TextGray, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            OrbitalCalendarCanvas(data = data, todayDay = todayDay)

            OrbitalCalendarEventList(
                isCurrentMonth = data.isCurrentMonth,
                todayEvents = data.todayEvents,
                upcomingEvents = data.upcomingEvents,
                nextEventCountdown = data.nextEventCountdown
            )

            OrbitalCalendarLegend()
        }
    }
}
