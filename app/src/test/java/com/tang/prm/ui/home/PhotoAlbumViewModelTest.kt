package com.tang.prm.ui.home

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.Favorite
import com.tang.prm.domain.model.Gift
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.EventRepository
import com.tang.prm.domain.repository.FavoriteRepository
import com.tang.prm.domain.repository.GiftRepository
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
class PhotoAlbumViewModelTest {

    @MockK
    private lateinit var eventRepository: EventRepository

    @MockK
    private lateinit var giftRepository: GiftRepository

    @MockK
    private lateinit var contactRepository: ContactRepository

    @MockK
    private lateinit var favoriteRepository: FavoriteRepository

    private lateinit var viewModel: PhotoAlbumViewModel

    private val testContact = Contact(id = 1, name = "Alice")
    private val testEvent = Event(
        id = 1,
        title = "Meetup",
        type = "MEETUP",
        time = 1000L,
        photos = listOf("photo1.jpg"),
        participants = listOf(testContact)
    )
    private val testGift = Gift(
        id = 1,
        contactId = 1,
        giftName = "Book",
        date = 2000L,
        isSent = true,
        photos = emptyList()
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        every { eventRepository.getAllEventsIncludingConversations() } returns flowOf(listOf(testEvent))
        every { giftRepository.getAllGifts() } returns flowOf(listOf(testGift))
        every { contactRepository.getAllContacts() } returns flowOf(listOf(testContact))
        every { favoriteRepository.getFavoritesByType(any<String>()) } returns flowOf(emptyList<Favorite>())

        viewModel = PhotoAlbumViewModel(eventRepository, giftRepository, contactRepository, favoriteRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_loadsPhotos() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.photos).isNotEmpty()
            assertThat(state.isLoading).isFalse()
        }
    }

    @Test
    fun init_extractsEventPhotos() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            val eventPhoto = state.photos.find { it.sourceType == "event" }
            assertThat(eventPhoto).isNotNull()
            assertThat(eventPhoto!!.sourceTitle).isEqualTo("Meetup")
        }
    }

    @Test
    fun init_computesStats() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.totalPhotoCount).isAtLeast(1)
        }
    }

    @Test
    fun filterByContact_updatesSelectedContactId() = runTest {
        viewModel.uiState.test {
            awaitItem()
            viewModel.filterByContact(1L)
            val state = awaitItem()
            assertThat(state.selectedContactId).isEqualTo(1L)
        }
    }

    @Test
    fun filterBySourceType_updatesFilter() = runTest {
        viewModel.uiState.test {
            awaitItem()
            viewModel.filterBySourceType("event")
            val state = awaitItem()
            assertThat(state.filterSourceType).isEqualTo("event")
        }
    }

    @Test
    fun clearFilters_resetsFilters() = runTest {
        viewModel.uiState.test {
            awaitItem()
            viewModel.filterByContact(1L)
            awaitItem()
            viewModel.filterBySourceType("event")
            awaitItem()
            viewModel.clearFilters()
            var state = awaitItem()
            while (state.selectedContactId != null || state.filterSourceType != null) {
                state = awaitItem()
            }
            assertThat(state.selectedContactId).isNull()
            assertThat(state.filterSourceType).isNull()
        }
    }
}
