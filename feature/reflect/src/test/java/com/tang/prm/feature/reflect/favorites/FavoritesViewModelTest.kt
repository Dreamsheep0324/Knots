package com.tang.prm.feature.reflect.favorites

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Favorite
import com.tang.prm.domain.repository.FavoriteRepository
import com.tang.prm.domain.usecase.ObserveFavoritesUseCase
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
class FavoritesViewModelTest {

    @MockK
    private lateinit var favoriteRepository: FavoriteRepository

    private lateinit var observeFavoritesUseCase: ObserveFavoritesUseCase
    private lateinit var viewModel: FavoritesViewModel

    private val testFavorites = listOf(
        Favorite(id = 1, sourceType = "EVENT", sourceId = 10L, title = "Event 1"),
        Favorite(id = 2, sourceType = "THOUGHT", sourceId = 20L, title = "Thought 1"),
        Favorite(id = 3, sourceType = "GIFT", sourceId = 30L, title = "Gift 1")
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        every { favoriteRepository.getAllFavorites() } returns flowOf(testFavorites)

        observeFavoritesUseCase = ObserveFavoritesUseCase(favoriteRepository)
        viewModel = FavoritesViewModel(observeFavoritesUseCase)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_loadsFavorites() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.favorites).hasSize(3)
            assertThat(state.isLoading).isFalse()
        }
    }

    @Test
    fun setFilter_event_showsOnlyEventFavorites() = runTest {
        viewModel.setFilter(1)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.favorites).hasSize(1)
            assertThat(state.favorites[0].sourceType).isEqualTo("EVENT")
        }
    }

    @Test
    fun setFilter_all_showsAllFavorites() = runTest {
        viewModel.setFilter(0)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.favorites).hasSize(3)
        }
    }

    @Test
    fun init_emptyFavorites_showsEmptyList() = runTest {
        every { favoriteRepository.getAllFavorites() } returns flowOf(emptyList<Favorite>())

        val freshViewModel = FavoritesViewModel(observeFavoritesUseCase)

        freshViewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.favorites).isEmpty()
        }
    }
}
