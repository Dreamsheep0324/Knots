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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Place
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.theme.CardBorder
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalElectric
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.SignalSky
import com.tang.prm.ui.theme.TextGray
import com.tang.prm.util.DateUtils

@Composable
internal fun HeroCard(footprint: FootprintItem, eventTypes: List<com.tang.prm.domain.model.CustomType> = emptyList(), onClick: () -> Unit = {}) {
    val dateFormat: (Long) -> String = { DateUtils.formatMonthDayChineseFull(it) }
    val (accentColor, lightColor, icon) = resolveEventTypeStyle(footprint.eventType, eventTypes)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 3.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(lightColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(SignalGreen)
                    )
                    Text(
                        text = "最新足迹",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = SignalGreen
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = footprint.eventTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = null,
                        tint = SignalSky,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = footprint.location,
                        fontSize = 12.sp,
                        color = SignalSky,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = dateFormat(footprint.date),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TextGray.copy(alpha = AnimationTokens.Alpha.visible)
                )
                if (!footprint.weather.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            getWeatherIconForFootprint(footprint.weather),
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = getWeatherColorForFootprint(footprint.weather)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = footprint.weather,
                            fontSize = 11.sp,
                            color = TextGray.copy(alpha = AnimationTokens.Alpha.visible)
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun StatsRow(
    footprintCount: Int,
    cityCount: Int,
    contactCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            value = footprintCount.toString(),
            label = "足迹",
            icon = Icons.Default.Place,
            iconBg = SignalElectric.copy(alpha = 0.1f),
            iconTint = SignalElectric
        )
        StatCard(
            modifier = Modifier.weight(1f),
            value = cityCount.toString(),
            label = "城市",
            icon = Icons.Default.LocationCity,
            iconBg = SignalGreen.copy(alpha = 0.1f),
            iconTint = SignalGreen
        )
        StatCard(
            modifier = Modifier.weight(1f),
            value = contactCount.toString(),
            label = "同行",
            icon = Icons.Default.People,
            iconBg = SignalAmber.copy(alpha = 0.1f),
            iconTint = SignalAmber
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                color = TextGray.copy(alpha = AnimationTokens.Alpha.visible),
                letterSpacing = 0.5.sp
            )
        }
    }
}
