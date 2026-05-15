package com.tang.prm.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.Favorite
import com.tang.prm.domain.repository.FavoriteRepository
import com.tang.prm.domain.model.AppStrings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val favorites: List<Favorite> = emptyList(),
    val selectedFilter: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(0)
    val filterLabels = listOf(AppStrings.Tabs.ALL) + FavoriteType.entries.map { it.filterLabel }
    private val filterCodes = listOf("") + FavoriteType.entries.map { it.code }

    val uiState: StateFlow<FavoritesUiState> = combine(
        favoriteRepository.getAllFavorites(),
        _selectedFilter
    ) { favorites, filterIndex ->
        val filtered = if (filterIndex == 0) favorites
        else favorites.filter { it.sourceType == filterCodes[filterIndex] }
        FavoritesUiState(
            favorites = filtered,
            selectedFilter = filterIndex,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FavoritesUiState())

    fun setFilter(index: Int) {
        _selectedFilter.value = index
    }

    fun removeFavorite(favorite: Favorite) {
        viewModelScope.launch {
            favoriteRepository.deleteFavoriteBySource(favorite.sourceType, favorite.sourceId)
        }
    }
}
