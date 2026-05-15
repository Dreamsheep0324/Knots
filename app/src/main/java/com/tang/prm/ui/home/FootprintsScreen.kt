@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ViewTimeline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tang.prm.ui.navigation.Screen
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.theme.CardBorder
import com.tang.prm.ui.theme.GridLine
import com.tang.prm.ui.theme.SignalElectric
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.TextGray
import java.util.Calendar

@Composable
fun FootprintsScreen(
    navController: NavController,
    viewModel: FootprintsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SignalGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "足迹",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = TextGray
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleView() }) {
                        Icon(
                            imageVector = if (uiState.isTimelineView) Icons.Default.ViewTimeline else Icons.AutoMirrored.Filled.List,
                            contentDescription = if (uiState.isTimelineView) "切换列表视图" else "切换时间轴视图",
                            tint = SignalElectric
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SignalGreen)
            }
        } else if (uiState.totalFootprintCount == 0) {
            EmptyFootprintState(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.surface),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                uiState.footprints.firstOrNull()?.let { latest ->
                    item(key = "hero") {
                        HeroCard(footprint = latest, eventTypes = uiState.eventTypes, onClick = { navController.navigate(Screen.EventDetail.createRoute(latest.id)) })
                    }
                }

                item(key = "stats") {
                    StatsRow(
                        footprintCount = uiState.totalFootprintCount,
                        cityCount = uiState.totalCityCount,
                        contactCount = uiState.totalContactCount
                    )
                }

                item(key = "yearTabs") {
                    YearTabs(
                        availableYears = uiState.availableYears,
                        selectedYear = uiState.selectedYear,
                        footprints = uiState.footprints,
                        onYearSelect = { viewModel.selectYear(it) }
                    )
                }

                if (uiState.footprints.isEmpty()) {
                    item(key = "empty") {
                        EmptyFilterStateFootprints(message = "没有符合条件的足迹")
                    }
                } else {
                    if (uiState.isTimelineView) {
                        timelineItems(uiState.footprints, uiState.eventTypes) { navController.navigate(Screen.EventDetail.createRoute(it.id)) }
                    } else {
                        listItems(uiState.footprints, uiState.eventTypes) { navController.navigate(Screen.EventDetail.createRoute(it.id)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun YearTabs(
    availableYears: List<Int>,
    selectedYear: Int?,
    footprints: List<FootprintItem>,
    onYearSelect: (Int?) -> Unit
) {
    val calendar = Calendar.getInstance()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val allCount = footprints.size
            item {
                YearTabChip(
                    label = "全部",
                    count = allCount,
                    selected = selectedYear == null,
                    onClick = { onYearSelect(null) }
                )
            }
            items(availableYears) { year ->
                val yearCount = footprints.count {
                    calendar.timeInMillis = it.date
                    calendar.get(Calendar.YEAR) == year
                }
                YearTabChip(
                    label = year.toString(),
                    count = yearCount,
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
private fun YearTabChip(
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

@Composable
internal fun EmptyFootprintState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(SignalGreen.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Place,
                    contentDescription = null,
                    tint = SignalGreen,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "还没有足迹",
                fontFamily = FontFamily.Monospace,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "在事件中添加地点信息后足迹会自动记录在这里",
                fontSize = 13.sp,
                color = TextGray.copy(alpha = 0.55f)
            )
        }
    }
}

@Composable
internal fun EmptyFilterStateFootprints(message: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(TextGray.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Place,
                    contentDescription = null,
                    tint = TextGray,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = TextGray.copy(alpha = 0.55f)
            )
        }
    }
}
