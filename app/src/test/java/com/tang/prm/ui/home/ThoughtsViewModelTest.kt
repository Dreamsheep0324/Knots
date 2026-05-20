package com.tang.prm.ui.home

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.Favorite
import com.tang.prm.domain.model.Thought
import com.tang.prm.domain.model.ThoughtType
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.FavoriteRepository
import com.tang.prm.domain.repository.ThoughtRepository
import io.mockk.coEvery
import io.mockk.coVerify
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
class ThoughtsViewModelTest {

    @MockK
    private lateinit var thoughtRepository: ThoughtRepository

    @MockK
    private lateinit var contactRepository: ContactRepository

    @MockK
    private lateinit var favoriteRepository: FavoriteRepository

    private lateinit var viewModel: ThoughtsViewModel

    private val testThought = Thought(id = 1, content = "Hello", type = ThoughtType.MURMUR)
    private val testFriendThought = Thought(id = 2, content = "Friend thought", type = ThoughtType.FRIEND, contactId = 1L)
    private val testContact = Contact(id = 1, name = "Alice")

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        every { thoughtRepository.getAllThoughts() } returns flowOf(listOf(testThought, testFriendThought))
        every { thoughtRepository.getTodoThoughts() } returns flowOf(emptyList<Thought>())
        every { contactRepository.getAllContacts() } returns flowOf(listOf(testContact))
        every { favoriteRepository.getFavoritesByType(any<String>()) } returns flowOf(emptyList<Favorite>())

        viewModel = ThoughtsViewModel(thoughtRepository, contactRepository, favoriteRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_loadsThoughts() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.allThoughts).hasSize(2)
            assertThat(state.isLoading).isFalse()
        }
    }

    @Test
    fun init_loadsContactThoughts() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.contactThoughts).hasSize(1)
            assertThat(state.contactThoughts[0].contact.name).isEqualTo("Alice")
        }
    }

    @Test
    fun onFilterSelected_friend_filtersByFriendType() = runTest {
        viewModel.onFilterSelected("friend")

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.selectedFilter).isEqualTo("friend")
            assertThat(state.filteredThoughts).hasSize(1)
            assertThat(state.filteredThoughts[0].type).isEqualTo(ThoughtType.FRIEND)
        }
    }

    @Test
    fun onFilterSelected_murmur_filtersByMurmurType() = runTest {
        viewModel.onFilterSelected("murmur")

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.selectedFilter).isEqualTo("murmur")
            assertThat(state.filteredThoughts).hasSize(1)
            assertThat(state.filteredThoughts[0].type).isEqualTo(ThoughtType.MURMUR)
        }
    }

    @Test
    fun onFilterSelected_all_showsAllThoughts() = runTest {
        viewModel.onFilterSelected("all")

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.filteredThoughts).hasSize(2)
        }
    }

    @Test
    fun insertThought_callsRepository() = runTest {
        coEvery { thoughtRepository.insertThought(any()) } returns 1L

        viewModel.insertThought("New thought", ThoughtType.PLAN)

        coVerify { thoughtRepository.insertThought(match { it.content == "New thought" && it.type == ThoughtType.PLAN }) }
    }

    @Test
    fun deleteThought_callsRepository() = runTest {
        coEvery { thoughtRepository.deleteThought(1L) } returns Unit

        viewModel.deleteThought(1L)

        coVerify { thoughtRepository.deleteThought(1L) }
    }

    @Test
    fun onContactFilterSelected_filtersByContact() = runTest {
        viewModel.onContactFilterSelected(1L)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.selectedContactId).isEqualTo(1L)
        }
    }
}
