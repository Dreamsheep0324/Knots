package com.tang.prm.feature.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.Ingredient
import com.tang.prm.domain.model.Recipe
import com.tang.prm.domain.repository.RecipeRepository
import com.tang.prm.domain.usecase.RecipeScalingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecipeDetailDataState(
    val recipe: Recipe? = null,
    val contacts: List<Contact> = emptyList(),
    val isLoading: Boolean = false,
    val currentServings: Int = 0,
    val originalServings: Int = 0,
    val scaledIngredients: List<Ingredient> = emptyList()
)

data class RecipeDetailDialogState(
    val showDeleteConfirm: Boolean = false
)

data class RecipeDetailUiState(
    val data: RecipeDetailDataState = RecipeDetailDataState(),
    val dialog: RecipeDetailDialogState = RecipeDetailDialogState()
)

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val recipeScalingUseCase: RecipeScalingUseCase
) : ViewModel() {

    private val _recipe = MutableStateFlow<Recipe?>(null)
    private val _currentServings = MutableStateFlow(0)
    private val _isLoading = MutableStateFlow(false)
    private val _dialogState = MutableStateFlow(RecipeDetailDialogState())

    private var loadedRecipeId: Long = 0L

    private val _dataState = combine(
        _recipe,
        _currentServings,
        _isLoading
    ) { recipe, servings, isLoading ->
        if (recipe != null) {
            val originalServings = recipe.servings ?: 0
            val scaled = recipeScalingUseCase.scaleIngredients(
                recipe.ingredients,
                originalServings,
                servings
            )
            RecipeDetailDataState(
                recipe = recipe,
                isLoading = isLoading,
                currentServings = servings,
                originalServings = originalServings,
                scaledIngredients = scaled
            )
        } else {
            RecipeDetailDataState(isLoading = isLoading)
        }
    }

    val uiState = combine(_dataState, _dialogState) { data, dialog ->
        RecipeDetailUiState(data = data, dialog = dialog)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RecipeDetailUiState())

    fun loadRecipe(recipeId: Long) {
        if (recipeId == loadedRecipeId) return
        loadedRecipeId = recipeId
        _isLoading.value = true
        viewModelScope.launch {
            val recipe = recipeRepository.getRecipeByIdOnce(recipeId)
            if (recipe != null) {
                _recipe.value = recipe
                _currentServings.value = recipe.servings ?: 1
            }
            _isLoading.value = false
        }
    }

    fun adjustServings(delta: Int) {
        val current = _currentServings.value
        val newServings = (current + delta).coerceAtLeast(1)
        _currentServings.value = newServings
    }

    fun showDeleteConfirm() {
        _dialogState.value = _dialogState.value.copy(showDeleteConfirm = true)
    }

    fun dismissDeleteConfirm() {
        _dialogState.value = _dialogState.value.copy(showDeleteConfirm = false)
    }

    fun deleteRecipe(onSuccess: () -> Unit) {
        val id = _recipe.value?.id ?: return
        viewModelScope.launch {
            recipeRepository.deleteRecipe(id)
            _dialogState.value = _dialogState.value.copy(showDeleteConfirm = false)
            onSuccess()
        }
    }

    fun toggleFavorite() {
        val recipe = _recipe.value ?: return
        viewModelScope.launch {
            val updated = recipe.copy(
                isFavorite = !recipe.isFavorite,
                updatedAt = System.currentTimeMillis()
            )
            recipeRepository.updateRecipe(updated)
            _recipe.value = updated
        }
    }
}
