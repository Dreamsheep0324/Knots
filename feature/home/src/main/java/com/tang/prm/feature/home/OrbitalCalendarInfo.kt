package com.tang.prm.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.theme.GridLine
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalElectric
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.SignalSky
import com.tang.prm.ui.theme.SignalCoral

@Composable
internal fun OrbitalCalendarEventList(
    isCurrentMonth: Boolean,
    todayEvents: List<String>,
    upcomingEvents: List<Pair<String, String>>,
    nextEventCountdown: Pair<String, Pair<Int, Int>>?
) {
    if (isCurrentMonth && todayEvents.isNotEmpty()) {
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth().height(1.dp).background(GridLine.copy(alpha = AnimationTokens.Alpha.half))) {}
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(SignalElectric))
            Text("今日", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half))
        }
        Spacer(modifier = Modifier.height(4.dp))
        todayEvents.take(3).forEach { title ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(SignalElectric.copy(alpha = 0.5f)))
                Spacer(modifier = Modifier.width(6.dp))
                Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            }
        }
    }

    if (upcomingEvents.isNotEmpty()) {
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(SignalAmber))
            Text("即将到来", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
        Spacer(modifier = Modifier.height(4.dp))
        upcomingEvents.forEach { (title, date) ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(SignalAmber.copy(alpha = 0.5f)))
                Spacer(modifier = Modifier.width(6.dp))
                Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text(date, fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            }
        }
    }

    if (nextEventCountdown != null) {
        val (title, daysHours) = nextEventCountdown
        val (days, hours) = daysHours
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(SignalPurple))
            Text("NEXT", fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SignalElectric)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(SignalPurple.copy(alpha = AnimationTokens.Alpha.half)))
            Spacer(modifier = Modifier.width(6.dp))
            Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Text(if (days > 0) "${days}D ${hours}H" else "${hours}H",
                fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SignalElectric)
        }
    }
}

@Composable
internal fun OrbitalCalendarLegend() {
    Spacer(modifier = Modifier.height(6.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(SignalElectric))
            Text("今天", fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }
        Spacer(modifier = Modifier.width(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(SignalElectric.copy(alpha = 0.6f)))
            Text("事件", fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }
        Spacer(modifier = Modifier.width(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(SignalCoral.copy(alpha = 0.6f)))
            Text("周末", fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }
        Spacer(modifier = Modifier.width(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(SignalPurple.copy(alpha = 0.6f)))
            Text("古历", fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }
        Spacer(modifier = Modifier.width(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(SignalSky.copy(alpha = AnimationTokens.Alpha.visible)))
            Text("生肖", fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = SignalSky.copy(alpha = 0.7f))
        }
    }
}
