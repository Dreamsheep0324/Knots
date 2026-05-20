package com.tang.prm.ui.contacts

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Anniversary
import com.tang.prm.domain.model.AnniversaryType
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CustomCategories
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.Favorite
import com.tang.prm.domain.model.Gift
import com.tang.prm.domain.model.SourceTypes
import com.tang.prm.domain.model.Thought
import com.tang.prm.domain.model.ThoughtType
import com.tang.prm.domain.repository.AnniversaryRepository
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.CustomTypeRepository
import com.tang.prm.domain.repository.EventRepository
import com.tang.prm.domain.repository.FavoriteRepository
import com.tang.prm.domain.repository.GiftRepository
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
class ContactDetailViewModelTest {

    @MockK
    private lateinit var contactRepository: ContactRepository

    @MockK
    private lateinit var eventRepository: EventRepository

    @MockK
    private lateinit var anniversaryRepository: AnniversaryRepository

    @MockK
    private lateinit var giftRepository: GiftRepository

    @MockK
    private lateinit var thoughtRepository: ThoughtRepository

    @MockK
    private lateinit var favoriteRepository: FavoriteRepository

    @MockK
    private lateinit var customTypeRepository: CustomTypeRepository

    private lateinit var viewModel: ContactDetailViewModel

    private val testContact = Contact(id = 1, name = "Alice")
    private val testEvent = Event(id = 1, title = "Meetup", type = "MEETUP", time = 1000L)
    private val testAnniversary = Anniversary(id = 1, contactId = 1, name = "Birthday", type = AnniversaryType.BIRTHDAY, date = 1000L)
    private val testGift = Gift(id = 1, contactId = 1, giftName = "Book", date = 1000L, isSent = true)
    private val testThought = Thought(id = 1, content = "Nice", type = ThoughtType.MURMUR)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        val savedStateHandle = SavedStateHandle(mapOf("contactId" to 1L))

        every { contactRepository.getContactById(1L) } returns flowOf(testContact)
        every { eventRepository.getEventsByContact(1L) } returns flowOf(listOf(testEvent))
        every { anniversaryRepository.getAnniversariesByContact(1L) } returns flowOf(listOf(testAnniversary))
        every { giftRepository.getGiftsByContactId(1L) } returns flowOf(listOf(testGift))
        every { thoughtRepository.getThoughtsByContact(1L) } returns flowOf(listOf(testThought))
        every { customTypeRepository.getTypesByCategory(CustomCategories.HOBBY) } returns flowOf(emptyList<CustomType>())
        every { customTypeRepository.getTypesByCategory(CustomCategories.HABIT) } returns flowOf(emptyList<CustomType>())
        every { customTypeRepository.getTypesByCategory(CustomCategories.DIET) } returns flowOf(emptyList<CustomType>())
        every { customTypeRepository.getTypesByCategory(CustomCategories.SKILL) } returns flowOf(emptyList<CustomType>())
        every { customTypeRepository.getTypesByCategory(CustomCategories.EVENT_TYPE) } returns flowOf(emptyList<CustomType>())
        every { favoriteRepository.getFavoritesByType(SourceTypes.THOUGHT) } returns flowOf(emptyList<Favorite>())

        viewModel = ContactDetailViewModel(
            contactRepository,
            eventRepository,
            anniversaryRepository,
            giftRepository,
            thoughtRepository,
            favoriteRepository,
            customTypeRepository,
            savedStateHandle
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadContact_loadsContactDetails() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.contact).isNotNull()
            assertThat(state.contact!!.name).isEqualTo("Alice")
            assertThat(state.isLoading).isFalse()
        }
    }

    @Test
    fun loadContact_loadsRelatedEvents() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.events).hasSize(1)
            assertThat(state.events[0].title).isEqualTo("Meetup")
        }
    }

    @Test
    fun loadContact_loadsRelatedAnniversaries() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.anniversaries).hasSize(1)
            assertThat(state.anniversaries[0].name).isEqualTo("Birthday")
        }
    }

    @Test
    fun loadContact_loadsRelatedGifts() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.gifts).hasSize(1)
            assertThat(state.gifts[0].giftName).isEqualTo("Book")
        }
    }

    @Test
    fun loadContact_loadsRelatedThoughts() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.thoughts).hasSize(1)
            assertThat(state.thoughts[0].content).isEqualTo("Nice")
        }
    }

    @Test
    fun deleteContact_callsRepository() = runTest {
        coEvery { contactRepository.deleteContact(1L) } returns Unit

        var deleted = false
        viewModel.deleteContact { deleted = true }

        coVerify { contactRepository.deleteContact(1L) }
        assertThat(deleted).isTrue()
    }

    @Test
    fun onTabSelected_updatesSelectedTab() = runTest {
        viewModel.onTabSelected(2)

        assertThat(viewModel.uiState.value.selectedTab).isEqualTo(2)
    }

    @Test
    fun showDeleteDialog_updatesState() = runTest {
        viewModel.showDeleteDialog()

        assertThat(viewModel.uiState.value.showDeleteDialog).isTrue()

        viewModel.hideDeleteDialog()

        assertThat(viewModel.uiState.value.showDeleteDialog).isFalse()
    }
}
