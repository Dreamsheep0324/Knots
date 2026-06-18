package com.tang.prm.feature.people.contacts

import app.cash.turbine.test
import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.*
import com.tang.prm.domain.usecase.ContactDetailAggregationUseCase
import com.tang.prm.domain.usecase.ContactDetailAggregateData
import com.tang.prm.domain.usecase.FavoriteToggleUseCase
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
class ContactDetailViewModelTest {

    private lateinit var aggregationUseCase: ContactDetailAggregationUseCase
    private lateinit var favoriteToggleUseCase: FavoriteToggleUseCase
    private lateinit var viewModel: ContactDetailViewModel

    private val testAggregateData = ContactDetailAggregateData(
        contact = Contact(id = 1, name = "测试"),
        events = listOf(Event(id = 1, title = "事件", time = 1000L)),
        thoughts = listOf(Thought(id = 1, contactId = 1, content = "感悟")),
        isLoading = false
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        aggregationUseCase = mockk()
        favoriteToggleUseCase = mockk()

        every { aggregationUseCase.getContactDetail(1L) } returns flowOf(testAggregateData)
        coEvery { aggregationUseCase.deleteContact(any()) } returns Unit
        coEvery { aggregationUseCase.updateThought(any()) } returns Unit
        coEvery { aggregationUseCase.deleteThought(any()) } returns Unit
        coEvery { favoriteToggleUseCase(any(), any(), any(), any()) } returns true

        val savedStateHandle = SavedStateHandle(mapOf("contactId" to 1L))
        viewModel = ContactDetailViewModel(aggregationUseCase, favoriteToggleUseCase, savedStateHandle)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads contact detail from useCase`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.data.contact?.name).isEqualTo("测试")
            assertThat(state.data.isLoading).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onTabSelected updates dialog state`() = runTest {
        viewModel.onTabSelected(2)
        viewModel.uiState.test {
            assertThat(awaitItem().dialog.selectedTab).isEqualTo(2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `showDeleteDialog sets flag`() = runTest {
        viewModel.showDeleteDialog()
        viewModel.uiState.test {
            assertThat(awaitItem().dialog.showDeleteDialog).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `hideDeleteDialog clears flag`() = runTest {
        viewModel.showDeleteDialog()
        viewModel.hideDeleteDialog()
        viewModel.uiState.test {
            assertThat(awaitItem().dialog.showDeleteDialog).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteContact calls useCase`() = runTest {
        viewModel.deleteContact()
        coVerify { aggregationUseCase.deleteContact(1L) }
    }

    @Test
    fun `toggleFavorite calls favoriteToggleUseCase`() = runTest {
        viewModel.toggleFavorite(1L, "感悟内容")
        coVerify { favoriteToggleUseCase(type = SourceTypes.THOUGHT, sourceId = 1L, title = "感悟内容", description = any()) }
    }

    @Test
    fun `toggleTodoDone updates thought isDone`() = runTest {
        val thought = Thought(id = 1, contactId = 1, content = "待办", isDone = false)
        viewModel.toggleTodoDone(thought)
        coVerify { aggregationUseCase.updateThought(match { it.isDone }) }
    }

    @Test
    fun `deleteThought calls useCase`() = runTest {
        viewModel.deleteThought(1L)
        coVerify { aggregationUseCase.deleteThought(1L) }
    }
}
