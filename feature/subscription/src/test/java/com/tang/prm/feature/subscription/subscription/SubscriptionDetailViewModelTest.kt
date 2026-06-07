package com.tang.prm.feature.subscription.subscription

import app.cash.turbine.test
import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Subscription
import com.tang.prm.domain.model.SubscriptionCycle
import com.tang.prm.domain.model.SubscriptionStatus
import com.tang.prm.domain.repository.SubscriptionRepository
import io.mockk.coEvery
import io.mockk.coVerify
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

@ExtendWith(MockKExtension::class)
class SubscriptionDetailViewModelTest {

    private lateinit var subscriptionRepository: SubscriptionRepository
    private lateinit var viewModel: SubscriptionDetailViewModel

    private val testSubscription = Subscription(
        id = 1L, name = "Netflix", price = 15.0,
        cycle = SubscriptionCycle.MONTHLY, startDate = System.currentTimeMillis(),
        nextBillingDate = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
        status = SubscriptionStatus.ACTIVE
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        subscriptionRepository = mockk()

        every { subscriptionRepository.getSubscriptionById(1L) } returns flowOf(testSubscription)
        coEvery { subscriptionRepository.deleteSubscription(any()) } returns Unit

        val savedStateHandle = SavedStateHandle(mapOf("subscriptionId" to 1L))
        viewModel = SubscriptionDetailViewModel(savedStateHandle, subscriptionRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState loads subscription from repository`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.data.subscription).isEqualTo(testSubscription)
            assertThat(state.data.isLoading).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `showDeleteConfirm updates dialog state`() = runTest {
        viewModel.showDeleteConfirm()
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.dialog.showDeleteConfirm).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `hideDeleteConfirm resets dialog state`() = runTest {
        viewModel.showDeleteConfirm()
        viewModel.hideDeleteConfirm()
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.dialog.showDeleteConfirm).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteSubscription calls repository`() = runTest {
        viewModel.deleteSubscription()
        coVerify { subscriptionRepository.deleteSubscription(1L) }
    }

    @Test
    fun `invalid subscriptionId shows empty state`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("subscriptionId" to 0L))
        val vm = SubscriptionDetailViewModel(savedStateHandle, subscriptionRepository)
        vm.uiState.test {
            val state = awaitItem()
            assertThat(state.data.subscription).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
