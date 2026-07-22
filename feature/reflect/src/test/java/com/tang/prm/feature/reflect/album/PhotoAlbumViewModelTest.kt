package com.tang.prm.feature.reflect.album

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.AlbumPhoto
import com.tang.prm.domain.model.SourceTypes
import com.tang.prm.domain.usecase.PhotoAlbumAggregationUseCase
import com.tang.prm.domain.usecase.PhotoAlbumAggregateData
import com.tang.prm.domain.usecase.FavoriteToggleUseCase
import com.tang.prm.domain.usecase.ObserveFavoritesUseCase
import io.mockk.coEvery
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
    private lateinit var photoAlbumUseCase: PhotoAlbumAggregationUseCase

    @MockK
    private lateinit var favoriteToggleUseCase: FavoriteToggleUseCase

    @MockK
    private lateinit var observeFavoritesUseCase: ObserveFavoritesUseCase

    private lateinit var viewModel: PhotoAlbumViewModel

    private val testPhoto = AlbumPhoto(
        id = "p1", uri = "photo1.jpg", sourceType = SourceTypes.ALBUM_EVENT,
        sourceId = 1, sourceTitle = "Meetup",
        contactId = 1L, contactName = "Alice", contactAvatar = null,
        date = 1000L, location = null
    )

    private val testAggregateData = PhotoAlbumAggregateData(
        allPhotos = listOf(testPhoto),
        contacts = emptyList()
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        every { photoAlbumUseCase.getAggregateData() } returns flowOf(testAggregateData)
        every { observeFavoritesUseCase.getFavoriteIds(SourceTypes.PHOTO) } returns flowOf(emptySet<Long>())
        coEvery { favoriteToggleUseCase(any(), any(), any(), any()) } returns true

        viewModel = PhotoAlbumViewModel(photoAlbumUseCase, favoriteToggleUseCase, observeFavoritesUseCase)
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
            val eventPhoto = state.photos.find { it.sourceType == SourceTypes.ALBUM_EVENT }
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
            viewModel.filterBySourceType(SourceTypes.ALBUM_EVENT)
            val state = awaitItem()
            assertThat(state.filterSourceType).isEqualTo(SourceTypes.ALBUM_EVENT)
        }
    }

    @Test
    fun clearFilters_resetsFilters() = runTest {
        viewModel.uiState.test {
            awaitItem()
            viewModel.filterByContact(1L)
            awaitItem()
            viewModel.filterBySourceType(SourceTypes.ALBUM_EVENT)
            awaitItem()
            viewModel.clearFilters()
            var state = awaitItem()
            while (state.selectedContactId != null || state.filterSourceType != null) {
                state = awaitItem()
            }
            assertThat(state.selectedContactId == null).isTrue()
            assertThat(state.filterSourceType == null).isTrue()
        }
    }
}
