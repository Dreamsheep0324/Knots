package com.tang.prm.ui.home

import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.*
import com.tang.prm.domain.repository.ReminderRepository
import com.tang.prm.domain.repository.SettingsRepository
import com.tang.prm.domain.repository.TodoRepository
import com.tang.prm.domain.usecase.HomeAggregateData
import com.tang.prm.domain.usecase.HomeDataAggregationUseCase
import com.tang.prm.domain.usecase.HomeStats
import com.tang.prm.domain.usecase.HomeStatsUseCase
import com.tang.prm.feature.home.HomeViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
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
class HomeViewModelTest {

    @MockK
    private lateinit var homeDataUseCase: HomeDataAggregationUseCase

    @MockK
    private lateinit var homeStatsUseCase: HomeStatsUseCase

    @MockK
    private lateinit var settingsRepository: SettingsRepository

    @MockK
    private lateinit var todoRepository: TodoRepository

    @MockK
    private lateinit var reminderRepository: ReminderRepository

    private lateinit var viewModel: HomeViewModel

    private val emptyHomeData = HomeAggregateData(
        frequentContacts = emptyList(),
        recentEvents = emptyList(),
        allEvents = emptyList(),
        upcomingAnniversaries = emptyList(),
        allAnniversaries = emptyList(),
        pendingTodos = emptyList(),
        todayReminders = emptyList()
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        every { homeDataUseCase.getAggregateData() } returns flowOf(emptyHomeData)
        every { homeStatsUseCase.getStats() } returns flowOf(HomeStats())
        every { settingsRepository.userName } returns flowOf("测试用户")

        viewModel = HomeViewModel(
            homeDataUseCase,
            homeStatsUseCase,
            settingsRepository,
            todoRepository,
            reminderRepository
        )
    }

    @AfterEach
    fun tearDown() {
        // 取消 ViewModel 的 viewModelScope，清理 greetingFlow 在 Dispatchers.Default 上
        // 运行的无限循环协程，避免未捕获异常推迟到下一个测试
        viewModel.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun init_loadsStats() = runTest {
        val stats = HomeStats(giftCount = 5, contactCount = 10)
        every { homeStatsUseCase.getStats() } returns flowOf(stats)

        val freshViewModel = HomeViewModel(
            homeDataUseCase, homeStatsUseCase, settingsRepository,
            todoRepository, reminderRepository
        )

        freshViewModel.uiState.test {
            // Skip initial loading state (isLoading=true, giftCount=0)
            awaitItem()
            // Wait for combined state
            val state = awaitItem()
            assertThat(state.giftCount).isEqualTo(5)
            assertThat(state.contactCount).isEqualTo(10)
            cancelAndIgnoreRemainingEvents()
        }
        // 清理 freshViewModel 的协程，避免 greetingFlow 在 Default 调度器上泄漏
        freshViewModel.viewModelScope.cancel()
    }

    @Test
    fun toggleTodoCompletion_callsRepository() = runTest {
        coEvery { todoRepository.updateTodoCompletion(1L, true) } returns Unit

        viewModel.toggleTodoCompletion(1L, true)

        coVerify { todoRepository.updateTodoCompletion(1L, true) }
    }

    @Test
    fun completeReminder_callsRepository() = runTest {
        coEvery { reminderRepository.markReminderCompleted(1L) } returns Unit

        viewModel.completeReminder(1L)

        coVerify { reminderRepository.markReminderCompleted(1L) }
    }
}
