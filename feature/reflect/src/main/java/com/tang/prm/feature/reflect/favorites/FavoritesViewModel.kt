package com.tang.prm.feature.reflect.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.Favorite
import com.tang.prm.domain.model.AppStrings
import com.tang.prm.domain.usecase.ObserveFavoritesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class FavoritesUiState(
    val favorites: List<Favorite> = emptyList(),
    val selectedFilter: Int = 0,
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val observeFavoritesUseCase: ObserveFavoritesUseCase
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(0)
    val filterLabels = listOf(AppStrings.Tabs.ALL) + FavoriteType.entries.map { it.filterLabel }
    private val filterCodes = listOf("") + FavoriteType.entries.map { it.code }

    val uiState: StateFlow<FavoritesUiState> = _selectedFilter
        .flatMapLatest { filterIndex ->
            val sourceType = filterCodes[filterIndex].ifBlank { null }
            observeFavoritesUseCase.observe(sourceType).map { favorites ->
                FavoritesUiState(
                    favorites = favorites,
                    selectedFilter = filterIndex,
                    isLoading = false
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FavoritesUiState())

    fun setFilter(index: Int) {
        _selectedFilter.value = index
    }
}
