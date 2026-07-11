package com.tang.prm.feature.subscription.subscription

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.CustomCategories
import com.tang.prm.domain.model.Subscription
import com.tang.prm.domain.model.SubscriptionCycle
import com.tang.prm.domain.model.SubscriptionStatus
import com.tang.prm.domain.repository.CustomTypeRepository
import com.tang.prm.domain.repository.SubscriptionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class AddSubscriptionViewModelTest {

    private lateinit var subscriptionRepository: SubscriptionRepository
    private lateinit var customTypeRepository: CustomTypeRepository
    private lateinit var viewModel: AddSubscriptionViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        subscriptionRepository = mockk()
        customTypeRepository = mockk()

        every { customTypeRepository.getTypesByCategory(CustomCategories.SUBSCRIPTION_CATEGORY) } returns flowOf(emptyList())
        coEvery { subscriptionRepository.insertSubscription(any()) } returns 1L
        coEvery { subscriptionRepository.updateSubscription(any()) } returns Unit
        coEvery { subscriptionRepository.getSubscriptionByIdOnce(any()) } returns null

        viewModel = AddSubscriptionViewModel(subscriptionRepository, customTypeRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.name).isEmpty()
            assertThat(state.cycle).isEqualTo(SubscriptionCycle.MONTHLY)
            assertThat(state.isSaved).isFalse()
            assertThat(state.isEdit).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateName changes state`() = runTest {
        viewModel.updateName("Netflix")
        assertThat(viewModel.uiState.value.name).isEqualTo("Netflix")
    }

    @Test
    fun `updateCycle recalculates nextBillingDate`() = runTest {
        viewModel.updateCycle(SubscriptionCycle.YEARLY)
        val state = viewModel.uiState.value
        assertThat(state.cycle).isEqualTo(SubscriptionCycle.YEARLY)
        val diffMs = state.nextBillingDate - state.startDate
        assertThat(diffMs).isGreaterThan(365L * 24 * 60 * 60 * 900)
    }

    @Test
    fun `saveSubscription inserts new subscription`() = runTest {
        viewModel.updateName("Netflix")
        viewModel.updatePrice("15.0")
        viewModel.saveSubscription()
        coVerify { subscriptionRepository.insertSubscription(match { it.name == "Netflix" && it.price == 15.0 }) }
    }

    @Test
    fun `saveSubscription marks isSaved true`() = runTest {
        viewModel.updateName("Test")
        viewModel.updatePrice("10")
        viewModel.saveSubscription()
        assertThat(viewModel.uiState.value.isSaved).isTrue()
    }

    @Test
    fun `initForEdit loads existing subscription`() = runTest {
        val existing = Subscription(
            id = 5L, name = "Spotify", price = 9.99,
            cycle = SubscriptionCycle.MONTHLY, startDate = System.currentTimeMillis(),
            nextBillingDate = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
            status = SubscriptionStatus.ACTIVE
        )
        coEvery { subscriptionRepository.getSubscriptionByIdOnce(5L) } returns existing

        viewModel.initForEdit(5L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.name).isEqualTo("Spotify")
        assertThat(state.isEdit).isTrue()
        assertThat(state.subscriptionId).isEqualTo(5L)
    }

    @Test
    fun `saveSubscription in edit mode calls update`() = runTest {
        val existing = Subscription(
            id = 5L, name = "Spotify", price = 9.99,
            cycle = SubscriptionCycle.MONTHLY, startDate = System.currentTimeMillis(),
            nextBillingDate = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000
        )
        coEvery { subscriptionRepository.getSubscriptionByIdOnce(5L) } returns existing

        viewModel.initForEdit(5L)
        advanceUntilIdle()

        viewModel.updateName("Spotify Premium")
        viewModel.saveSubscription()

        coVerify { subscriptionRepository.updateSubscription(any()) }
    }

    @Test
    fun `update methods set hasUnsavedChanges`() = runTest {
        viewModel.updateName("Test")
        assertThat(viewModel.uiState.value.hasUnsavedChanges).isTrue()
    }

    @Test
    fun `ONE_TIME cycle nextBillingDate equals startDate`() = runTest {
        viewModel.updateCycle(SubscriptionCycle.ONE_TIME)
        val state = viewModel.uiState.value
        assertThat(state.nextBillingDate).isEqualTo(state.startDate)
    }
}
