package com.tang.prm.feature.gifts

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.Gift
import com.tang.prm.domain.model.GiftType
import com.tang.prm.domain.model.SaveResult
import com.tang.prm.domain.model.SourceTypes
import com.tang.prm.domain.repository.GiftRepository
import com.tang.prm.domain.usecase.FavoriteToggleUseCase
import com.tang.prm.domain.usecase.GiftListAggregateData
import com.tang.prm.domain.usecase.GiftRecord
import com.tang.prm.domain.usecase.ObserveGiftListUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
class GiftsViewModelTest {

    @MockK
    private lateinit var giftRepository: GiftRepository

    @MockK
    private lateinit var favoriteToggleUseCase: FavoriteToggleUseCase

    @MockK
    private lateinit var observeGiftListUseCase: ObserveGiftListUseCase

    private lateinit var viewModel: GiftsViewModel

    private val contact = Contact(id = 1L, name = "Alice", avatar = "avatar.jpg")
    private val gift = Gift(
        id = 10L, contactId = 1L, giftName = "巧克力", giftType = GiftType.FOOD,
        date = 2000L, isSent = true
    )

    private val aggregateFlow = MutableStateFlow(
        GiftListAggregateData(
            gifts = listOf(
                GiftRecord(gift = gift, contactName = "Alice", contactAvatar = "avatar.jpg")
            ),
            availableContacts = listOf(contact),
            favoriteGiftIds = emptySet()
        )
    )

    @BeforeEach
    fun setUp() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        every { observeGiftListUseCase() } returns aggregateFlow
        coEvery { favoriteToggleUseCase(any(), any(), any(), any()) } returns true

        viewModel = GiftsViewModel(giftRepository, favoriteToggleUseCase, observeGiftListUseCase)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    @DisplayName("初始加载")
    inner class InitTest {

        @Test
        fun loadsGiftsAndMapsContactName() = runTest {
            viewModel.uiState.test {
                val state = awaitItem()
                assertThat(state.data.gifts).hasSize(1)
                assertThat(state.data.gifts[0].giftName).isEqualTo("巧克力")
                assertThat(state.data.gifts[0].contactName).isEqualTo("Alice")
                assertThat(state.data.gifts[0].contactAvatar).isEqualTo("avatar.jpg")
            }
        }

        @Test
        fun unknownContact_fallsBackToDefaultName() = runTest {
            val giftWithUnknownContact = gift.copy(contactId = 999L)
            every { observeGiftListUseCase() } returns flowOf(
                GiftListAggregateData(
                    gifts = listOf(
                        GiftRecord(gift = giftWithUnknownContact, contactName = "未知人物", contactAvatar = null)
                    ),
                    availableContacts = listOf(contact),
                    favoriteGiftIds = emptySet()
                )
            )

            val vm = GiftsViewModel(giftRepository, favoriteToggleUseCase, observeGiftListUseCase)

            vm.uiState.test {
                val state = awaitItem()
                assertThat(state.data.gifts[0].contactName).isEqualTo("未知人物")
            }
        }

        @Test
        fun loadsAvailableContacts() = runTest {
            viewModel.uiState.test {
                val state = awaitItem()
                assertThat(state.data.availableContacts).hasSize(1)
                assertThat(state.data.availableContacts[0].name).isEqualTo("Alice")
            }
        }

        @Test
        fun isLoading_falseAfterLoad() = runTest {
            viewModel.uiState.test {
                val state = awaitItem()
                assertThat(state.data.isLoading).isFalse()
            }
        }
    }

    @Nested
    @DisplayName("筛选")
    inner class FilterTest {

        @Test
        fun updateFilterType_updatesState() = runTest {
            val job = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()
            viewModel.updateFilterType("sent")
            advanceUntilIdle()
            assertThat(viewModel.uiState.value.data.filterType).isEqualTo("sent")
            job.cancel()
        }

        @Test
        fun filterByContact_setsSelectedContactId() = runTest {
            val job = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()
            viewModel.filterByContact(42L)
            advanceUntilIdle()
            assertThat(viewModel.uiState.value.data.selectedContactId).isEqualTo(42L)
            job.cancel()
        }

        @Test
        fun clearContactFilter_clearsSelectedContactId() {
            viewModel.filterByContact(42L)
            viewModel.clearContactFilter()
            assertThat(viewModel.uiState.value.data.selectedContactId).isNull()
        }
    }

    @Nested
    @DisplayName("收藏")
    inner class FavoriteTest {

        @Test
        fun toggleFavorite_addsToFavoriteSet() = runTest {
            val baseAggregate = GiftListAggregateData(
                gifts = listOf(GiftRecord(gift = gift, contactName = "Alice", contactAvatar = "avatar.jpg")),
                availableContacts = listOf(contact),
                favoriteGiftIds = emptySet()
            )
            val aggregateFlowWithFav = MutableStateFlow(baseAggregate)
            every { observeGiftListUseCase() } returns aggregateFlowWithFav
            coEvery { favoriteToggleUseCase(any(), any(), any(), any()) } answers {
                val sourceId = secondArg<Long>()
                val current = aggregateFlowWithFav.value
                aggregateFlowWithFav.value = current.copy(favoriteGiftIds = current.favoriteGiftIds + sourceId)
                true
            }
            // 重新创建 viewModel 以使用新的 aggregate flow
            viewModel = GiftsViewModel(giftRepository, favoriteToggleUseCase, observeGiftListUseCase)
            val job = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()
            viewModel.toggleFavorite(10L, "巧克力", "Alice")
            advanceUntilIdle()
            assertThat(viewModel.uiState.value.data.favoriteGiftIds).contains(10L)
            job.cancel()
        }

        @Test
        fun toggleFavoriteTwice_removesFromFavoriteSet() = runTest {
            viewModel.toggleFavorite(10L, "巧克力", "Alice")
            viewModel.toggleFavorite(10L, "巧克力", "Alice")
            assertThat(viewModel.uiState.value.data.favoriteGiftIds).doesNotContain(10L)
        }

        @Test
        fun toggleFavorite_callsUseCaseWithCorrectParams() = runTest {
            viewModel.toggleFavorite(10L, "巧克力", "Alice")
            coVerify {
                favoriteToggleUseCase(
                    type = SourceTypes.GIFT,
                    sourceId = 10L,
                    title = "巧克力",
                    description = "来自Alice的礼物"
                )
            }
        }
    }

    @Nested
    @DisplayName("增删改")
    inner class CrudTest {

        @Test
        fun deleteGift_callsRepository() = runTest {
            coEvery { giftRepository.deleteGiftById(any()) } returns Unit
            viewModel.deleteGift(10L)
            coVerify { giftRepository.deleteGiftById(10L) }
        }

        @Test
        fun addGift_callsSaveGiftWithPhotos() = runTest {
            val giftRecord = GiftRecord(
                gift = gift, contactName = "Alice", contactAvatar = null
            )
            coEvery { giftRepository.saveGiftWithPhotos(any(), any()) } returns SaveResult(1L, 0)
            viewModel.addGift(giftRecord)
            coVerify { giftRepository.saveGiftWithPhotos(any(), any()) }
        }

        @Test
        fun addGift_withPhotoErrors_setsDialogErrorCount() = runTest {
            val job = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()
            val giftRecord = GiftRecord(
                gift = gift, contactName = "Alice", contactAvatar = null
            )
            coEvery { giftRepository.saveGiftWithPhotos(any(), any()) } returns SaveResult(1L, 2)
            viewModel.addGift(giftRecord)
            // 等待协程完成
            advanceUntilIdle()
            assertThat(viewModel.uiState.value.dialog.photoSaveErrorCount).isEqualTo(2)
            job.cancel()
        }

        @Test
        fun clearPhotoSaveError_resetsCount() {
            viewModel.clearPhotoSaveError()
            assertThat(viewModel.uiState.value.dialog.photoSaveErrorCount).isEqualTo(0)
        }

        @Test
        fun updateGift_callsRepositoryWithUpdatedAt() = runTest {
            coEvery { giftRepository.updateGift(any()) } returns Unit
            val giftRecord = GiftRecord(
                gift = gift, contactName = "Alice", contactAvatar = null
            )
            viewModel.updateGift(giftRecord)
            coVerify { giftRepository.updateGift(match { it.updatedAt > 0 }) }
        }
    }

    @Nested
    @DisplayName("getGiftFlow")
    inner class GetGiftFlowTest {

        @Test
        fun returnsFlowFromRepository() = runTest {
            every { giftRepository.getGiftById(any()) } returns flowOf(gift)
            val result = viewModel.getGiftFlow(10L).first()
            assertThat(result).isNotNull()
            assertThat(result?.giftName).isEqualTo("巧克力")
        }
    }
}
