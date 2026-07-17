package com.tang.prm.feature.reflect.footprints

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.theme.CardBorder
import com.tang.prm.ui.theme.GridLine
import com.tang.prm.ui.theme.SignalElectric
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.TextGray

@Composable
internal fun YearTabs(
    availableYears: List<Int>,
    selectedYear: Int?,
    totalCount: Int,
    totalFootprintsByYear: Map<Int, Int>,
    onYearSelect: (Int?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                YearTabChip(
                    label = "全部",
                    count = totalCount,
                    selected = selectedYear == null,
                    onClick = { onYearSelect(null) }
                )
            }
            items(availableYears, key = { it }) { year ->
                YearTabChip(
                    label = year.toString(),
                    count = totalFootprintsByYear[year] ?: 0,
                    selected = selectedYear == year,
                    onClick = { onYearSelect(year) }
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(GridLine.copy(alpha = 0.4f))
        )
    }
}

@Composable
internal fun YearTabChip(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) SignalElectric else MaterialTheme.colorScheme.surface,
        border = if (selected) null else BorderStroke(1.dp, CardBorder),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "($count)",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = if (selected) Color.White.copy(alpha = AnimationTokens.Alpha.strong) else TextGray.copy(alpha = AnimationTokens.Alpha.visible)
            )
        }
    }
}
