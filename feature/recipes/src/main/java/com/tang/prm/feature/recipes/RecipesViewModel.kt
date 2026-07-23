package com.tang.prm.feature.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.repository.RecipeRepository
import com.tang.prm.domain.usecase.ObserveRecipeListItemsUseCase
import com.tang.prm.domain.usecase.RecipeListItem
import com.tang.prm.ui.common.SearchStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecipesDataState(
    val allRecipes: List<RecipeListItem> = emptyList(),
    val displayList: List<RecipeListItem> = emptyList(),
    val selectedTag: String? = null,
    val isLoading: Boolean = false,
    val availableTags: List<String> = emptyList(),
    val contactMap: Map<Long, Contact> = emptyMap()
)

data class RecipesDialogState(
    val showDeleteConfirm: Boolean = false,
    val deleteTargetId: Long? = null
)

data class RecipesUiState(
    val data: RecipesDataState = RecipesDataState(),
    val dialog: RecipesDialogState = RecipesDialogState()
)

@HiltViewModel
class RecipesViewModel @Inject constructor(
    /** 仅用于 [deleteRecipe]；列表观察已下沉到 [observeRecipeListItemsUseCase]。 */
    private val recipeRepository: RecipeRepository,
    private val observeRecipeListItemsUseCase: ObserveRecipeListItemsUseCase
) : ViewModel() {

    private val searchManager = SearchStateManager()
    val searchState = searchManager.state

    private val _selectedTag = MutableStateFlow<String?>(null)
    private val _dialogState = MutableStateFlow(RecipesDialogState())
    private val _isLoading = MutableStateFlow(false)

    private val _categorized = MutableStateFlow<Pair<List<RecipeListItem>, List<String>>>(emptyList<RecipeListItem>() to emptyList())

    val uiState = combine(
        combine(
            observeRecipeListItemsUseCase(),
            searchManager.state,
            _selectedTag,
            _isLoading
        ) { aggregate, search, selectedTag, isLoading ->
            val items = aggregate.items
            val allTags = aggregate.availableTags
            _categorized.value = items to allTags

            val filtered = items.filter { item ->
                val matchesTag = selectedTag == null || item.tags.contains(selectedTag)
                val matchesSearch = search.query.isBlank() ||
                    item.title.contains(search.query, ignoreCase = true) ||
                    item.description?.contains(search.query, ignoreCase = true) == true ||
                    item.cuisine?.contains(search.query, ignoreCase = true) == true
                matchesTag && matchesSearch
            }
            RecipesDataState(
                allRecipes = items,
                displayList = filtered,
                selectedTag = selectedTag,
                isLoading = isLoading,
                availableTags = allTags,
                contactMap = aggregate.contactMap
            )
        },
        _dialogState
    ) { data, dialog ->
        RecipesUiState(data = data, dialog = dialog)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RecipesUiState())

    fun onSearchQueryChange(query: String) = searchManager.onQueryChange(query)
    fun toggleSearch() = searchManager.toggleSearch()
    fun deactivateSearch() = searchManager.deactivate()

    fun selectTag(tag: String?) {
        _selectedTag.value = tag
    }

    fun showDeleteConfirm(id: Long) {
        _dialogState.value = _dialogState.value.copy(showDeleteConfirm = true, deleteTargetId = id)
    }

    fun dismissDeleteConfirm() {
        _dialogState.value = _dialogState.value.copy(showDeleteConfirm = false, deleteTargetId = null)
    }

    fun deleteRecipe() {
        val id = _dialogState.value.deleteTargetId ?: return
        viewModelScope.launch {
            recipeRepository.deleteRecipe(id)
            _dialogState.value = _dialogState.value.copy(showDeleteConfirm = false, deleteTargetId = null)
        }
    }
}
