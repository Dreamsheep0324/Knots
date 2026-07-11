package com.tang.prm.feature.circle

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Circle
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.usecase.CircleSortMode
import com.tang.prm.domain.usecase.CircleWithMembers
import com.tang.prm.domain.usecase.ContactListManageUseCase
import com.tang.prm.domain.usecase.ContactListAggregate
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ContactListViewModelTest {

    @MockK
    private lateinit var useCase: ContactListManageUseCase

    private lateinit var viewModel: ContactListViewModel
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        val circle = Circle(id = 1L, name = "朋友", description = null, color = "#FF0000", waveform = "sine")
        val contact = Contact(id = 10L, name = "Alice", intimacyScore = 80)
        val aggregate = ContactListAggregate(
            circles = listOf(CircleWithMembers(circle, listOf(contact))),
            contacts = listOf(contact)
        )
        every { useCase.getContactListAggregate() } returns flowOf(aggregate)
        every { useCase.getAvailableContacts(any(), any()) } returns emptyList()
        every { useCase.getSortedCircles(any(), any()) } answers {
            firstArg<List<CircleWithMembers>>()
        }

        viewModel = ContactListViewModel(useCase)
        // 启动后台收集器以激活 WhileSubscribed(5000)
        testScope.launch { viewModel.uiState.collect { } }
    }

    @AfterEach
    fun tearDown() {
        testScope.cancel()
        Dispatchers.resetMain()
    }

    @Nested
    @DisplayName("初始加载")
    inner class InitTest {

        @Test
        fun loadsCirclesFromUseCase() = runTest {
            viewModel.uiState.test {
                val state = awaitItem()
                assertThat(state.data.circles).isNotEmpty()
                assertThat(state.data.circles[0].circle.name).isEqualTo("朋友")
            }
        }

        @Test
        fun loadsContactsFromUseCase() = runTest {
            viewModel.uiState.test {
                val state = awaitItem()
                assertThat(state.data.contacts).hasSize(1)
            }
        }
    }

    @Nested
    @DisplayName("toggleCircleExpand")
    inner class ToggleExpandTest {

        @Test
        fun expandsCircle() {
            viewModel.toggleCircleExpand(1L)
            assertThat(viewModel.uiState.value.expandedCircleId).isEqualTo(1L)
            assertThat(viewModel.uiState.value.data.circles[0].isExpanded).isTrue()
        }

        @Test
        fun collapsesCircleWhenToggledAgain() {
            viewModel.toggleCircleExpand(1L)
            viewModel.toggleCircleExpand(1L)
            assertThat(viewModel.uiState.value.expandedCircleId).isNull()
            assertThat(viewModel.uiState.value.data.circles[0].isExpanded).isFalse()
        }
    }

    @Nested
    @DisplayName("toggleCardFlip")
    inner class ToggleFlipTest {

        @Test
        fun flipsCard() {
            viewModel.toggleCircleExpand(1L)
            viewModel.selectMember(1L, 10L)
            viewModel.toggleCardFlip(10L)
            assertThat(viewModel.uiState.value.flippedCardId).isEqualTo(10L)
        }
    }

    @Nested
    @DisplayName("selectMember")
    inner class SelectMemberTest {

        @Test
        fun selectsMember() {
            viewModel.toggleCircleExpand(1L)
            viewModel.selectMember(1L, 10L)
            val circle = viewModel.uiState.value.data.circles[0]
            assertThat(circle.selectedMemberId).isEqualTo(10L)
        }

        @Test
        fun deselectsMember() {
            viewModel.toggleCircleExpand(1L)
            viewModel.selectMember(1L, 10L)
            viewModel.selectMember(1L, null)
            val circle = viewModel.uiState.value.data.circles[0]
            assertThat(circle.selectedMemberId).isNull()
        }
    }

    @Nested
    @DisplayName("dialog state")
    inner class DialogTest {

        @Test
        fun showCreateDialog() {
            viewModel.showCreateDialog()
            assertThat(viewModel.uiState.value.dialog.showCreate).isTrue()
        }

        @Test
        fun hideCreateDialog() {
            viewModel.showCreateDialog()
            viewModel.hideCreateDialog()
            assertThat(viewModel.uiState.value.dialog.showCreate).isFalse()
        }

        @Test
        fun showDeleteConfirm() {
            viewModel.showDeleteConfirm(1L)
            assertThat(viewModel.uiState.value.dialog.showDeleteConfirm).isEqualTo(1L)
        }

        @Test
        fun hideDeleteConfirm() {
            viewModel.showDeleteConfirm(1L)
            viewModel.hideDeleteConfirm()
            assertThat(viewModel.uiState.value.dialog.showDeleteConfirm).isNull()
        }
    }

    @Nested
    @DisplayName("toggleSortMode")
    inner class SortModeTest {

        @Test
        fun cyclesThroughSortModes() {
            assertThat(viewModel.uiState.value.data.sortMode).isEqualTo(CircleSortMode.DEFAULT)
            viewModel.toggleSortMode()
            assertThat(viewModel.uiState.value.data.sortMode).isEqualTo(CircleSortMode.MEMBER_COUNT_DESC)
            viewModel.toggleSortMode()
            assertThat(viewModel.uiState.value.data.sortMode).isEqualTo(CircleSortMode.MEMBER_COUNT_ASC)
            viewModel.toggleSortMode()
            assertThat(viewModel.uiState.value.data.sortMode).isEqualTo(CircleSortMode.INTIMACY_DESC)
            viewModel.toggleSortMode()
            assertThat(viewModel.uiState.value.data.sortMode).isEqualTo(CircleSortMode.INTIMACY_ASC)
            viewModel.toggleSortMode()
            assertThat(viewModel.uiState.value.data.sortMode).isEqualTo(CircleSortMode.DEFAULT)
        }
    }
}
