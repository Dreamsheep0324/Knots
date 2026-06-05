@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.reflect.footprints

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.tang.prm.domain.model.FootprintItem
import com.tang.prm.ui.components.EmptyState
import com.tang.prm.ui.navigation.EventDetailRoute
import com.tang.prm.ui.theme.SignalElectric
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.TextGray

@Composable
fun FootprintsScreen(
    navController: NavController,
    viewModel: FootprintsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
            EmptyState(
                icon = Icons.Default.Place,
                title = "还没有足迹",
                description = "记录事件后会自动生成足迹",
                modifier = Modifier.padding(padding)
            )
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
                        HeroCard(footprint = latest, eventTypes = uiState.eventTypes, onClick = { navController.navigate(EventDetailRoute(latest.id)) })
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
                        EmptyState(
                            icon = Icons.Default.Place,
                            title = "没有符合条件的足迹"
                        )
                    }
                } else {
                    if (uiState.isTimelineView) {
                        timelineItems(uiState.footprints, uiState.eventTypes) { navController.navigate(EventDetailRoute(it.id)) }
                    } else {
                        listItems(uiState.footprints, uiState.eventTypes) { navController.navigate(EventDetailRoute(it.id)) }
                    }
                }
            }
        }
    }
}
