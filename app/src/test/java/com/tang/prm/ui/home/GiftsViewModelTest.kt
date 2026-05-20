package com.tang.prm.ui.home

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.Favorite
import com.tang.prm.domain.model.Gift
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.FavoriteRepository
import com.tang.prm.domain.repository.GiftRepository
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
class GiftsViewModelTest {

    @MockK
    private lateinit var giftRepository: GiftRepository

    @MockK
    private lateinit var contactRepository: ContactRepository

    @MockK
    private lateinit var favoriteRepository: FavoriteRepository

    private lateinit var viewModel: GiftsViewModel

    private val testContact = Contact(id = 1, name = "Alice")
    private val testGift = Gift(id = 1, contactId = 1, giftName = "Book", date = 1000L, isSent = true)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        every { giftRepository.getAllGifts() } returns flowOf(listOf(testGift))
        every { contactRepository.getAllContacts() } returns flowOf(listOf(testContact))
        every { favoriteRepository.getFavoritesByType(any<String>()) } returns flowOf(emptyList<Favorite>())

        viewModel = GiftsViewModel(giftRepository, contactRepository, favoriteRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_loadsGifts() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.gifts).hasSize(1)
            assertThat(state.gifts[0].giftName).isEqualTo("Book")
            assertThat(state.gifts[0].contactName).isEqualTo("Alice")
            assertThat(state.isLoading).isFalse()
        }
    }

    @Test
    fun init_loadsAvailableContacts() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.availableContacts).hasSize(1)
            assertThat(state.availableContacts[0].name).isEqualTo("Alice")
        }
    }

    @Test
    fun updateFilterType_updatesState() = runTest {
        viewModel.updateFilterType("sent")

        assertThat(viewModel.uiState.value.filterType).isEqualTo("sent")
    }

    @Test
    fun filterByContact_updatesSelectedContactId() = runTest {
        viewModel.filterByContact(1L)

        assertThat(viewModel.uiState.value.selectedContactId).isEqualTo(1L)
    }

    @Test
    fun clearContactFilter_clearsSelectedContactId() = runTest {
        viewModel.filterByContact(1L)
        viewModel.clearContactFilter()

        assertThat(viewModel.uiState.value.selectedContactId).isNull()
    }

    @Test
    fun deleteGift_callsRepository() = runTest {
        coEvery { giftRepository.deleteGiftById(1L) } returns Unit

        viewModel.deleteGift(1L)

        coVerify { giftRepository.deleteGiftById(1L) }
    }

    @Test
    fun init_loadsFavorites() = runTest {
        val fav = Favorite(id = 1, sourceType = "GIFT", sourceId = 1L, title = "Book")
        every { favoriteRepository.getFavoritesByType(any<String>()) } returns flowOf(listOf(fav))

        val freshViewModel = GiftsViewModel(giftRepository, contactRepository, favoriteRepository)

        freshViewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.favoriteGiftIds).contains(1L)
        }
    }
}
