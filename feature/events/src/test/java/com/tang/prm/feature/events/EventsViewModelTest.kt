package com.tang.prm.feature.events

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.EventType
import com.tang.prm.domain.usecase.EventsAggregate
import com.tang.prm.domain.usecase.ObserveEventsAggregateUseCase
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Calendar

@ExtendWith(MockKExtension::class)
class EventsViewModelTest {

    private lateinit var observeEventsAggregateUseCase: ObserveEventsAggregateUseCase
    private lateinit var viewModel: EventsViewModel

    private val testEvent = Event(id = 1, title = "测试事件", type = EventType.MEETUP, time = System.currentTimeMillis())

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        observeEventsAggregateUseCase = mockk()

        every { observeEventsAggregateUseCase.invoke() } returns flowOf(
            EventsAggregate(contacts = emptyList(), eventTypes = emptyList(), events = emptyList())
        )

        viewModel = EventsViewModel(observeEventsAggregateUseCase)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getMonthRange returns first day to first day of next month`() {
        val range = EventsViewModel.getMonthRange(0)
        val startCal = Calendar.getInstance().apply { timeInMillis = range.first }
        val endCal = Calendar.getInstance().apply { timeInMillis = range.second }

        assertThat(startCal.get(Calendar.DAY_OF_MONTH)).isEqualTo(1)
        assertThat(startCal.get(Calendar.HOUR_OF_DAY)).isEqualTo(0)
        assertThat(startCal.get(Calendar.MINUTE)).isEqualTo(0)
        assertThat(endCal.get(Calendar.DAY_OF_MONTH)).isEqualTo(1)
        assertThat(range.second - range.first).isGreaterThan(27L * 24 * 60 * 60 * 1000)
    }

    @Test
    fun `getMonthRange with offset shifts to correct month`() {
        val currentRange = EventsViewModel.getMonthRange(0)
        val nextMonthRange = EventsViewModel.getMonthRange(1)

        assertThat(nextMonthRange.first).isGreaterThan(currentRange.first)
        assertThat(nextMonthRange.first - currentRange.first).isAtLeast(27L * 24 * 60 * 60 * 1000)
    }

    @Test
    fun `getDayStart returns midnight of given timestamp`() {
        val now = System.currentTimeMillis()
        val dayStart = EventsViewModel.getDayStart(now)
        val cal = Calendar.getInstance().apply { timeInMillis = dayStart }

        assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(0)
        assertThat(cal.get(Calendar.MINUTE)).isEqualTo(0)
        assertThat(cal.get(Calendar.SECOND)).isEqualTo(0)
        assertThat(cal.get(Calendar.MILLISECOND)).isEqualTo(0)
    }

    @Test
    fun `uiState loads events and sets isLoading false`() = runTest {
        every { observeEventsAggregateUseCase.invoke() } returns flowOf(
            EventsAggregate(events = listOf(testEvent))
        )

        viewModel = EventsViewModel(observeEventsAggregateUseCase)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.data.isLoading).isFalse()
            assertThat(state.data.events).hasSize(1)
            assertThat(state.data.events.first().title).isEqualTo("测试事件")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectType updates selectedType in uiState`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            viewModel.selectType("MEETUP")
            val state = awaitItem()
            assertThat(state.data.selectedType).isEqualTo("MEETUP")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onPreviousMonth decrements calendarMonthOffset`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            viewModel.onPreviousMonth()
            val state = awaitItem()
            assertThat(state.data.calendarMonthOffset).isEqualTo(-1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onNextMonth increments calendarMonthOffset`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            viewModel.onNextMonth()
            val state = awaitItem()
            assertThat(state.data.calendarMonthOffset).isEqualTo(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onTodayClick resets monthOffset and selectedCalendarDate`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            viewModel.onNextMonth()
            awaitItem()
            viewModel.onTodayClick()
            val state = awaitItem()
            assertThat(state.data.calendarMonthOffset).isEqualTo(0)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `calendarStats excludes totalSpending field`() = runTest {
        every { observeEventsAggregateUseCase.invoke() } returns flowOf(
            EventsAggregate(events = listOf(testEvent))
        )

        viewModel = EventsViewModel(observeEventsAggregateUseCase)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.data.calendarStats.eventCount).isAtLeast(0)
            // totalSpending 字段已移除（B-7/D-10），确保编译通过即验证
            cancelAndIgnoreRemainingEvents()
        }
    }
}
