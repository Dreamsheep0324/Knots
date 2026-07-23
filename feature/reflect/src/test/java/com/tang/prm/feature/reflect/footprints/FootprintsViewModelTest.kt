package com.tang.prm.feature.reflect.footprints

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.FootprintItem
import com.tang.prm.domain.usecase.FootprintAggregationUseCase
import com.tang.prm.domain.usecase.FootprintAggregateData
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
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

@ExtendWith(MockKExtension::class)
class FootprintsViewModelTest {

    @MockK
    private lateinit var footprintUseCase: FootprintAggregationUseCase

    private lateinit var viewModel: FootprintsViewModel

    private val testFootprint = FootprintItem(
        id = 1, location = "Beijing", date = 1000L,
        eventType = "TRAVEL", eventTitle = "Trip",
        contactId = 1L, contactName = "Alice", contactAvatar = null,
        description = null, weather = null, emotion = null, photoCount = 0
    )

    private val testAggregateData = FootprintAggregateData(
        footprints = listOf(testFootprint),
        contacts = emptyList(),
        eventTypes = emptyList()
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        every { footprintUseCase() } returns flowOf(testAggregateData)

        viewModel = FootprintsViewModel(footprintUseCase)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_loadsFootprints() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.footprints).hasSize(1)
            assertThat(state.footprints[0].location).isEqualTo("Beijing")
            assertThat(state.footprints[0].eventTitle).isEqualTo("Trip")
            assertThat(state.isLoading).isFalse()
        }
    }

    @Test
    fun init_computesStats() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.totalFootprintCount).isEqualTo(1)
        }
    }

    @Test
    fun selectYear_filtersByYear() = runTest {
        viewModel.selectYear(1970)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.selectedYear).isEqualTo(1970)
        }
    }

    @Test
    fun toggleView_switchesTimelineView() = runTest {
        viewModel.uiState.test {
            val initialState = awaitItem()
            val initialView = initialState.isTimelineView

            viewModel.toggleView()

            val state = awaitItem()
            assertThat(state.isTimelineView).isEqualTo(!initialView)
        }
    }
}
