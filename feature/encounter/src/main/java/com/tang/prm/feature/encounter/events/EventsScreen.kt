package com.tang.prm.feature.encounter.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tang.prm.domain.model.AppStrings
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.animation.primitives.staggeredAppear
import com.tang.prm.ui.components.SearchBar
import com.tang.prm.ui.common.SearchState
import com.tang.prm.ui.navigation.AddEventRoute
import com.tang.prm.ui.navigation.EventDetailRoute
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    navController: NavController,
    viewModel: EventsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val viewModes = listOf(
        "list" to "列表",
        "timeline" to "时间线"
    )

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)
    ) {
        TopAppBar(
            title = {
                Text("事件", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
            },
            actions = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    viewModes.forEach { (key, label) ->
                        val selected = uiState.data.viewMode == key
                        Box(
                            modifier = Modifier.size(36.dp).background(if (selected) Primary else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).clickable { viewModel.onViewModeChange(key) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                when (key) {
                                    "list" -> Icons.AutoMirrored.Filled.ViewList
                                    else -> Icons.Default.Timeline
                                },
                                contentDescription = label,
                                tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                IconButton(onClick = { navController.navigate(AddEventRoute()) }) {
                    Box(modifier = Modifier.size(40.dp).background(Primary.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, contentDescription = "新建事件", tint = Primary, modifier = Modifier.size(22.dp))
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        SearchBar(
            query = searchState.query,
            onQueryChange = viewModel::onSearchQueryChange,
            modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.paddingPage),
            placeholder = "搜索事件、描述、地点、人物"
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            modifier = Modifier.padding(horizontal = Dimens.paddingPage),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                val isAllSelected = uiState.data.selectedType == null
                Surface(
                    onClick = { viewModel.selectType(null) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isAllSelected) Primary else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = AppStrings.Tabs.ALL,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isAllSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isAllSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(uiState.data.eventTypes, key = { it.id }) { eventType ->
                val isSelected = uiState.data.selectedType == eventType.name
                Surface(
                    onClick = { viewModel.selectType(eventType.name) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) Primary else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = eventType.name,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (uiState.data.displayEvents.isEmpty() && !uiState.data.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(100.dp).background(Primary.copy(alpha = AnimationTokens.Alpha.faint), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Event, contentDescription = null, tint = Primary, modifier = Modifier.size(48.dp))
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("还没有事件", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("记录你的每一个重要时刻", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(onClick = { navController.navigate(AddEventRoute()) }, colors = ButtonDefaults.buttonColors(containerColor = Primary), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("新建事件", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        } else {
            when (uiState.data.viewMode) {
                "list" -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(uiState.data.displayEvents, key = { _, it -> it.id }) { index, event ->
                            EventCard(event = event, eventTypes = uiState.data.eventTypes, onClick = { navController.navigate(EventDetailRoute(event.id)) }, modifier = Modifier.staggeredAppear(index = minOf(index, 15)))
                        }
                        item { Spacer(modifier = Modifier.height(100.dp).navigationBarsPadding()) }
                    }
                }
                "timeline" -> {
                    EventsTimelineView(events = uiState.data.displayEvents, onEventClick = { event -> navController.navigate(EventDetailRoute(event.id)) })
                }
            }
        }
    }
}
