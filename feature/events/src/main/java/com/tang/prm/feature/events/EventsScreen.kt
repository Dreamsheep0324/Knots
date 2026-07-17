package com.tang.prm.feature.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.Event
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.animation.primitives.staggeredAppear
import com.tang.prm.ui.components.SearchBar
import com.tang.prm.ui.components.SegmentedOption
import com.tang.prm.ui.components.SegmentedToggleButton
import com.tang.prm.ui.common.SearchState
import com.tang.prm.ui.navigation.AddEventRoute
import com.tang.prm.ui.navigation.EventDetailRoute
import com.tang.prm.ui.theme.Dimens

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
    val onEventClick: (Event) -> Unit = { event -> navController.navigate(EventDetailRoute(event.id)) }
    val onCreateEvent: () -> Unit = { navController.navigate(AddEventRoute()) }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)
    ) {
        EventsTopBar(
            isTabletLayout = isTabletLayout,
            viewMode = uiState.data.viewMode,
            viewModeOptions = viewModeOptions,
            onViewModeChange = viewModel::onViewModeChange,
            onCreateEvent = onCreateEvent
        )

        if (isTabletLayout) {
            TabletCalendarEventsContent(
                uiState = uiState,
                searchState = searchState,
                callbacks = EventsTabletCallbacks(
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    onTypeSelect = viewModel::selectType,
                    onPreviousMonth = viewModel::onPreviousMonth,
                    onNextMonth = viewModel::onNextMonth,
                    onTodayClick = viewModel::onTodayClick,
                    onDateSelected = viewModel::onCalendarDateSelected,
                    onEventClick = onEventClick,
                    onCreateEvent = onCreateEvent
                )
            )
        } else {
            EventsPhoneContent(
                uiState = uiState,
                searchState = searchState,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onTypeSelect = viewModel::selectType,
                onEventClick = onEventClick,
                onCreateEvent = onCreateEvent
            )
        }
    }
}

/**
 * 平板事件界面回调集合，避免 [TabletCalendarEventsContent] 参数列表过长。
 */
private data class EventsTabletCallbacks(
    val onSearchQueryChange: (String) -> Unit,
    val onTypeSelect: (String?) -> Unit,
    val onPreviousMonth: () -> Unit,
    val onNextMonth: () -> Unit,
    val onTodayClick: () -> Unit,
    val onDateSelected: (Long) -> Unit,
    val onEventClick: (Event) -> Unit,
    val onCreateEvent: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventsTopBar(
    isTabletLayout: Boolean,
    viewMode: String,
    viewModeOptions: List<SegmentedOption<String>>,
    onViewModeChange: (String) -> Unit,
    onCreateEvent: () -> Unit
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
                    selectedKey = viewMode,
                    onSelectionChange = onViewModeChange
                )
            }
            IconButton(onClick = onCreateEvent) {
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
}

@Composable
private fun EventsPhoneContent(
    uiState: EventsUiState,
    searchState: SearchState,
    onSearchQueryChange: (String) -> Unit,
    onTypeSelect: (String?) -> Unit,
    onEventClick: (Event) -> Unit,
    onCreateEvent: () -> Unit
) {
    SearchBar(
        query = searchState.query,
        onQueryChange = onSearchQueryChange,
        modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.paddingPage),
        placeholder = "搜索事件、描述、地点、人物"
    )

    Spacer(modifier = Modifier.height(6.dp))

    EventTypeFilterRow(
        eventTypes = uiState.data.eventTypes,
        selectedType = uiState.data.selectedType,
        onTypeSelect = onTypeSelect,
        style = FilterChipStyle.Compact
    )

    if (uiState.data.displayEvents.isEmpty() && !uiState.data.isLoading) {
        EventsPhoneEmptyState(onCreateEvent = onCreateEvent)
    } else {
        when (uiState.data.viewMode) {
            "list" -> EventsListContent(
                events = uiState.data.displayEvents,
                eventTypes = uiState.data.eventTypes,
                onEventClick = onEventClick
            )
            "timeline" -> EventsTimelineView(
                events = uiState.data.displayEvents,
                onEventClick = onEventClick
            )
        }
    }
}

@Composable
private fun EventsPhoneEmptyState(onCreateEvent: () -> Unit) {
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
                onClick = onCreateEvent,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("新建事件", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun EventsListContent(
    events: List<Event>,
    eventTypes: List<CustomType>,
    onEventClick: (Event) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(events, key = { _, it -> it.id }) { _, event ->
            EventCard(
                event = event,
                eventTypes = eventTypes,
                onClick = { onEventClick(event) }
            )
        }
        item { Spacer(modifier = Modifier.height(100.dp).navigationBarsPadding()) }
    }
}

// ═══════════════════════════════════════════════════════════════
// 平板日历+列表布局 (方案A)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun TabletCalendarEventsContent(
    uiState: EventsUiState,
    searchState: SearchState,
    callbacks: EventsTabletCallbacks
) {
    // 每次进入界面生成新的触发键，让 staggeredAppear 重新播放
    val appearKey = remember { Any() }
    Row(modifier = Modifier.fillMaxSize()) {
        TabletCalendarLeftPanel(
            uiState = uiState,
            callbacks = callbacks,
            appearKey = appearKey,
            modifier = Modifier.width(500.dp).fillMaxHeight()
        )
        TabletCalendarRightPanel(
            uiState = uiState,
            searchState = searchState,
            callbacks = callbacks,
            appearKey = appearKey,
            modifier = Modifier.weight(1f).fillMaxHeight()
        )
    }
}

@Composable
private fun TabletCalendarLeftPanel(
    uiState: EventsUiState,
    callbacks: EventsTabletCallbacks,
    appearKey: Any,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .staggeredAppear(index = 0, triggerKey = appearKey)
    ) {
        CalendarHeader(
            monthOffset = uiState.data.calendarMonthOffset,
            onPrevious = callbacks.onPreviousMonth,
            onNext = callbacks.onNextMonth,
            onToday = callbacks.onTodayClick
        )
        Spacer(modifier = Modifier.height(4.dp))
        CalendarWeekdays()
        CalendarGrid(
            monthOffset = uiState.data.calendarMonthOffset,
            selectedDate = uiState.data.selectedCalendarDate,
            calendarEvents = uiState.data.calendarEvents,
            onDateSelected = callbacks.onDateSelected
        )
        CalendarStatsRow(stats = uiState.data.calendarStats)
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        )
        SelectedDateEventsSection(
            events = uiState.data.selectedDateEvents,
            eventTypes = uiState.data.eventTypes,
            onEventClick = callbacks.onEventClick
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun TabletCalendarRightPanel(
    uiState: EventsUiState,
    searchState: SearchState,
    callbacks: EventsTabletCallbacks,
    appearKey: Any,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
            .staggeredAppear(index = 1, triggerKey = appearKey)
    ) {
        SearchBar(
            query = searchState.query,
            onQueryChange = callbacks.onSearchQueryChange,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            placeholder = "搜索事件、描述、地点、人物"
        )

        EventTypeFilterRow(
            eventTypes = uiState.data.eventTypes,
            selectedType = uiState.data.selectedType,
            onTypeSelect = callbacks.onTypeSelect,
            style = FilterChipStyle.Prominent
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
            TabletRightPanelEmptyState(onCreateEvent = callbacks.onCreateEvent)
        } else {
            DateGroupedEventList(
                events = uiState.data.displayEvents,
                eventTypes = uiState.data.eventTypes,
                onEventClick = callbacks.onEventClick
            )
        }
    }
}

@Composable
private fun TabletRightPanelEmptyState(onCreateEvent: () -> Unit) {
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
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onCreateEvent,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("新建事件", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
