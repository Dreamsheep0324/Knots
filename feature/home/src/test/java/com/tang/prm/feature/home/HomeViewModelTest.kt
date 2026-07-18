package com.tang.prm.feature.home

import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.*
import com.tang.prm.domain.usecase.HomeAggregateData
import com.tang.prm.domain.usecase.HomeDataAggregationUseCase
import com.tang.prm.domain.usecase.HomeSettingsUseCase
import com.tang.prm.domain.usecase.HomeStats
import com.tang.prm.domain.usecase.HomeStatsUseCase
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
    private lateinit var homeSettingsUseCase: HomeSettingsUseCase

    private lateinit var viewModel: HomeViewModel

    // D-2~D-5 修复：HomeAggregateData 仅保留 recentEvents/upcomingAnniversaries/pendingTodos
    private val emptyHomeData = HomeAggregateData(
        recentEvents = emptyList(),
        upcomingAnniversaries = emptyList(),
        pendingTodos = emptyList()
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        every { homeDataUseCase.getAggregateData() } returns flowOf(emptyHomeData)
        every { homeStatsUseCase.getStats() } returns flowOf(HomeStats())
        every { homeSettingsUseCase.getDecorPhotoPath() } returns flowOf(null)

        // A-1 修复：ViewModel 通过 HomeSettingsUseCase 访问 Repository，不再直接依赖 Repository
        viewModel = HomeViewModel(
            homeDataUseCase,
            homeStatsUseCase,
            homeSettingsUseCase
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
            homeDataUseCase, homeStatsUseCase, homeSettingsUseCase
        )

        freshViewModel.uiState.test {
            // UnconfinedTestDispatcher 下首个 item 可能是 initial 或 combined
            var state = awaitItem()
            // A-2 修复：isLoading 嵌套在 data 中，stats 嵌套在 data.stats 中
            if (state.data.isLoading) {
                state = awaitItem()
            }
            assertThat(state.data.stats.giftCount).isEqualTo(5)
            assertThat(state.data.stats.contactCount).isEqualTo(10)
            cancelAndIgnoreRemainingEvents()
        }
        // 清理 freshViewModel 的协程，避免 greetingFlow 在 Default 调度器上泄漏
        freshViewModel.viewModelScope.cancel()
    }
}
