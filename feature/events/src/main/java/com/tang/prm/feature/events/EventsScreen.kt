package com.tang.prm.feature.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tang.prm.domain.model.AppStrings
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.animation.primitives.staggeredAppear
import com.tang.prm.ui.components.SearchBar
import com.tang.prm.ui.components.SegmentedOption
import com.tang.prm.ui.components.SegmentedToggleButton
import com.tang.prm.ui.common.SearchState
import com.tang.prm.ui.navigation.AddEventRoute
import com.tang.prm.ui.navigation.EventDetailRoute
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.toComposeColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    navController: NavController,
    isTabletLayout: Boolean = false,
    viewModel: EventsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val viewModeOptions = listOf(
        SegmentedOption("list", Icons.AutoMirrored.Filled.ViewList, "列表"),
        SegmentedOption("timeline", Icons.Default.Timeline, "时间线")
    )

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)
    ) {
        TopAppBar(
            title = {
                Text(
                    "事件",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            actions = {
                if (!isTabletLayout) {
                    SegmentedToggleButton(
                        options = viewModeOptions,
                        selectedKey = uiState.data.viewMode,
                        onSelectionChange = viewModel::onViewModeChange
                    )
                }
                IconButton(onClick = { navController.navigate(AddEventRoute()) }) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "新建事件",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        if (isTabletLayout) {
            TabletCalendarEventsContent(
                uiState = uiState,
                searchState = searchState,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onTypeSelect = viewModel::selectType,
                onPreviousMonth = viewModel::onPreviousMonth,
                onNextMonth = viewModel::onNextMonth,
                onTodayClick = viewModel::onTodayClick,
                onDateSelected = viewModel::onCalendarDateSelected,
                onEventClick = { event -> navController.navigate(EventDetailRoute(event.id)) }
            )
        } else {
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
                        color = if (isAllSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
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
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
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
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = AnimationTokens.Alpha.faint), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Event,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            "还没有事件",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "记录你的每一个重要时刻",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { navController.navigate(AddEventRoute()) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
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
                                EventCard(
                                    event = event,
                                    eventTypes = uiState.data.eventTypes,
                                    onClick = { navController.navigate(EventDetailRoute(event.id)) }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(100.dp).navigationBarsPadding()) }
                        }
                    }
                    "timeline" -> {
                        EventsTimelineView(
                            events = uiState.data.displayEvents,
                            onEventClick = { event -> navController.navigate(EventDetailRoute(event.id)) }
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 平板日历+列表布局 (方案A)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun TabletCalendarEventsContent(
    uiState: EventsUiState,
    searchState: SearchState,
    onSearchQueryChange: (String) -> Unit,
    onTypeSelect: (String?) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClick: () -> Unit,
    onDateSelected: (Long) -> Unit,
    onEventClick: (com.tang.prm.domain.model.Event) -> Unit
) {
    // 每次进入界面生成新的触发键，让 staggeredAppear 重新播放
    val appearKey = remember { Any() }
    Row(modifier = Modifier.fillMaxSize()) {
        // ── 左面板：日历 + 统计 + 当日事件 ──
        Column(
            modifier = Modifier
                .width(500.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .staggeredAppear(index = 0, triggerKey = appearKey)
        ) {
            CalendarHeader(
                monthOffset = uiState.data.calendarMonthOffset,
                onPrevious = onPreviousMonth,
                onNext = onNextMonth,
                onToday = onTodayClick
            )
            Spacer(modifier = Modifier.height(4.dp))
            CalendarWeekdays()
            CalendarGrid(
                monthOffset = uiState.data.calendarMonthOffset,
                selectedDate = uiState.data.selectedCalendarDate,
                calendarEvents = uiState.data.calendarEvents,
                onDateSelected = onDateSelected
            )
            CalendarStatsRow(stats = uiState.data.calendarStats)
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            )
            SelectedDateEventsSection(
                events = uiState.data.selectedDateEvents,
                eventTypes = uiState.data.eventTypes,
                onEventClick = onEventClick
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── 右面板：搜索 + 筛选 + 事件列表 ──
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
                .staggeredAppear(index = 1, triggerKey = appearKey)
        ) {
            SearchBar(
                query = searchState.query,
                onQueryChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                placeholder = "搜索事件、描述、地点、人物"
            )

            TabletFilterChips(
                eventTypes = uiState.data.eventTypes,
                selectedType = uiState.data.selectedType,
                onTypeSelect = onTypeSelect
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "事件列表",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${uiState.data.displayEvents.size} 件",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (uiState.data.displayEvents.isEmpty() && !uiState.data.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Event,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.visible),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "没有符合条件的事件",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                DateGroupedEventList(
                    events = uiState.data.displayEvents,
                    eventTypes = uiState.data.eventTypes,
                    onEventClick = onEventClick
                )
            }
        }
    }
}

@Composable
private fun TabletFilterChips(
    eventTypes: List<com.tang.prm.domain.model.CustomType>,
    selectedType: String?,
    onTypeSelect: (String?) -> Unit
) {
    LazyRow(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            val isAllSelected = selectedType == null
            Surface(
                onClick = { onTypeSelect(null) },
                shape = RoundedCornerShape(22.dp),
                color = if (isAllSelected) SignalGreen else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (isAllSelected) Color.White else SignalGreen,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppStrings.Tabs.ALL,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isAllSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isAllSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        }
        items(eventTypes, key = { it.id }) { eventType ->
            val isSelected = selectedType == eventType.name
            val chipColor = eventType.color?.toComposeColor(SignalPurple) ?: SignalPurple
            Surface(
                onClick = { onTypeSelect(eventType.name) },
                shape = RoundedCornerShape(22.dp),
                color = if (isSelected) chipColor else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (isSelected) Color.White else chipColor,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = eventType.name,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
