package com.tang.prm.feature.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.CookingStep
import com.tang.prm.domain.model.Ingredient
import com.tang.prm.domain.model.IngredientGroupType
import com.tang.prm.domain.model.RecipeDifficulty
import com.tang.prm.domain.model.RecipeTag
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.RecipeRepository
import com.tang.prm.domain.repository.RecipeTagRepository
import com.tang.prm.domain.usecase.CreateRecipeUseCase
import com.tang.prm.domain.usecase.RecipeDraft
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddRecipeUiState(
    val title: String = "",
    val description: String = "",
    val cuisine: String = "",
    val difficulty: RecipeDifficulty = RecipeDifficulty.EASY,
    val cookingTime: String = "",
    val servings: String = "",
    val mainIngredients: List<Ingredient> = listOf(Ingredient("", "", "", IngredientGroupType.MAIN)),
    val subIngredients: List<Ingredient> = listOf(Ingredient("", "", "", IngredientGroupType.SUB)),
    val seasoningIngredients: List<Ingredient> = emptyList(),
    val steps: List<CookingStep> = listOf(CookingStep(1, "")),
    val photos: List<String> = emptyList(),
    val notes: String = "",
    val rating: Int = 0,
    val selectedContactIds: Set<Long> = emptySet(),
    val availableContacts: List<com.tang.prm.domain.model.Contact> = emptyList(),
    val selectedTagNames: List<String> = emptyList(),
    val availableTags: List<RecipeTag> = emptyList(),
    val isEditing: Boolean = false,
    val editingRecipeId: Long = 0L,
    val isSaved: Boolean = false,
    val titleError: String? = null
)

@HiltViewModel
class AddRecipeViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val recipeTagRepository: RecipeTagRepository,
    private val contactRepository: ContactRepository,
    private val createRecipeUseCase: CreateRecipeUseCase
) : ViewModel() {

    companion object {
        const val MAX_PHOTOS = 9
    }


    private val _state = MutableStateFlow(AddRecipeUiState())
    private val _availableContacts = MutableStateFlow<List<com.tang.prm.domain.model.Contact>>(emptyList())
    private val _availableTags = MutableStateFlow<List<RecipeTag>>(emptyList())

    val uiState = combine(_state, _availableContacts, _availableTags) { state, contacts, tags ->
        state.copy(availableContacts = contacts, availableTags = tags)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AddRecipeUiState())

    init {
        viewModelScope.launch {
            contactRepository.getAllContacts().collect { contacts ->
                _availableContacts.value = contacts
            }
        }
        viewModelScope.launch {
            recipeTagRepository.getAllTags().collect { tags ->
                _availableTags.value = tags
            }
        }
    }

    fun loadForEdit(recipeId: Long) {
        if (recipeId == 0L) return
        viewModelScope.launch {
            val recipe = recipeRepository.getRecipeById(recipeId) ?: return@launch
            _state.value = AddRecipeUiState(
                title = recipe.title,
                description = recipe.description ?: "",
                cuisine = recipe.cuisine ?: "",
                difficulty = recipe.difficulty,
                cookingTime = recipe.cookingTime?.toString() ?: "",
                servings = recipe.servings?.toString() ?: "",
                mainIngredients = recipe.ingredients.filter { it.groupType == IngredientGroupType.MAIN }
                    .ifEmpty { listOf(Ingredient("", "", "", IngredientGroupType.MAIN)) },
                subIngredients = recipe.ingredients.filter { it.groupType == IngredientGroupType.SUB }
                    .ifEmpty { listOf(Ingredient("", "", "", IngredientGroupType.SUB)) },
                seasoningIngredients = recipe.ingredients.filter { it.groupType == IngredientGroupType.SEASONING },
                steps = recipe.steps.ifEmpty { listOf(CookingStep(1, "")) },
                photos = recipe.photos,
                notes = recipe.notes ?: "",
                rating = recipe.rating,
                selectedContactIds = recipe.likedByContactIds.toSet(),
                selectedTagNames = recipe.tags,
                isEditing = true,
                editingRecipeId = recipeId
            )
        }
    }

    fun updateTitle(value: String) {
        _state.value = _state.value.copy(title = value, titleError = null)
    }

    fun updateDescription(value: String) {
        _state.value = _state.value.copy(description = value)
    }

    fun updateCuisine(value: String) {
        _state.value = _state.value.copy(cuisine = value)
    }

    fun updateDifficulty(value: RecipeDifficulty) {
        _state.value = _state.value.copy(difficulty = value)
    }

    fun updateCookingTime(value: String) {
        _state.value = _state.value.copy(cookingTime = value.filter { it.isDigit() })
    }

    fun updateServings(value: String) {
        _state.value = _state.value.copy(servings = value.filter { it.isDigit() })
    }

    fun updateNotes(value: String) {
        _state.value = _state.value.copy(notes = value)
    }

    fun updateRating(value: Int) {
        _state.value = _state.value.copy(rating = value)
    }

    fun updatePhotos(photos: List<String>) {
        _state.value = _state.value.copy(photos = photos)
    }

    /**
     * 追加新照片（限制上限 9 张），不依赖 UI 层传入当前列表，避免回调捕获旧 state。
     */
    fun addPhotos(newPaths: List<String>) {
        val merged = (_state.value.photos + newPaths).take(MAX_PHOTOS)
        _state.value = _state.value.copy(photos = merged)
    }

    fun removePhotoAt(index: Int) {
        _state.value = _state.value.copy(
            photos = _state.value.photos.toMutableList().apply { removeAt(index) }
        )
    }

    // 食材分组编辑
    fun addIngredient(groupType: IngredientGroupType) {
        val newIngredient = Ingredient("", "", "", groupType)
        _state.value = when (groupType) {
            IngredientGroupType.MAIN -> _state.value.copy(
                mainIngredients = _state.value.mainIngredients + newIngredient
            )
            IngredientGroupType.SUB -> _state.value.copy(
                subIngredients = _state.value.subIngredients + newIngredient
            )
            IngredientGroupType.SEASONING -> _state.value.copy(
                seasoningIngredients = _state.value.seasoningIngredients + newIngredient
            )
        }
    }

    fun updateIngredient(groupType: IngredientGroupType, index: Int, ingredient: Ingredient) {
        _state.value = when (groupType) {
            IngredientGroupType.MAIN -> _state.value.copy(
                mainIngredients = _state.value.mainIngredients.toMutableList().apply { this[index] = ingredient }
            )
            IngredientGroupType.SUB -> _state.value.copy(
                subIngredients = _state.value.subIngredients.toMutableList().apply { this[index] = ingredient }
            )
            IngredientGroupType.SEASONING -> _state.value.copy(
                seasoningIngredients = _state.value.seasoningIngredients.toMutableList().apply { this[index] = ingredient }
            )
        }
    }

    fun removeIngredient(groupType: IngredientGroupType, index: Int) {
        _state.value = when (groupType) {
            IngredientGroupType.MAIN -> _state.value.copy(
                mainIngredients = _state.value.mainIngredients.toMutableList().apply { removeAt(index) }
            )
            IngredientGroupType.SUB -> _state.value.copy(
                subIngredients = _state.value.subIngredients.toMutableList().apply { removeAt(index) }
            )
            IngredientGroupType.SEASONING -> _state.value.copy(
                seasoningIngredients = _state.value.seasoningIngredients.toMutableList().apply { removeAt(index) }
            )
        }
    }

    // 步骤编辑
    fun addStep() {
        val newOrder = _state.value.steps.size + 1
        _state.value = _state.value.copy(
            steps = _state.value.steps + CookingStep(newOrder, "")
        )
    }

    fun updateStep(index: Int, description: String) {
        _state.value = _state.value.copy(
            steps = _state.value.steps.toMutableList().apply {
                this[index] = this[index].copy(description = description)
            }
        )
    }

    fun updateStepTimer(index: Int, timerSeconds: Int?) {
        _state.value = _state.value.copy(
            steps = _state.value.steps.toMutableList().apply {
                this[index] = this[index].copy(timerSeconds = timerSeconds)
            }
        )
    }

    fun removeStep(index: Int) {
        val newSteps = _state.value.steps.toMutableList().apply { removeAt(index) }
        // 重新分配序号
        _state.value = _state.value.copy(
            steps = newSteps.mapIndexed { i, step -> step.copy(order = i + 1) }
        )
    }

    fun moveStep(fromIndex: Int, toIndex: Int) {
        val steps = _state.value.steps.toMutableList()
        if (fromIndex < 0 || fromIndex >= steps.size || toIndex < 0 || toIndex >= steps.size) return
        val moved = steps.removeAt(fromIndex)
        steps.add(toIndex, moved)
        _state.value = _state.value.copy(
            steps = steps.mapIndexed { i, step -> step.copy(order = i + 1) }
        )
    }

    // 联系人选择
    fun toggleContact(contactId: Long) {
        val current = _state.value.selectedContactIds
        _state.value = _state.value.copy(
            selectedContactIds = if (current.contains(contactId)) {
                current - contactId
            } else {
                current + contactId
            }
        )
    }

    // 标签
    fun toggleTag(tagName: String) {
        val current = _state.value.selectedTagNames
        _state.value = _state.value.copy(
            selectedTagNames = if (current.contains(tagName)) {
                current - tagName
            } else {
                current + tagName
            }
        )
    }

    fun addNewTag(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            recipeTagRepository.insertTag(RecipeTag(name = name.trim()))
        }
        _state.value = _state.value.copy(
            selectedTagNames = _state.value.selectedTagNames + name.trim()
        )
    }

    fun deleteTag(tag: RecipeTag) {
        viewModelScope.launch {
            recipeTagRepository.deleteTag(tag.id)
            _state.value = _state.value.copy(
                selectedTagNames = _state.value.selectedTagNames - tag.name
            )
        }
    }

    fun save(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val state = _state.value
        if (state.title.isBlank()) {
            _state.value = state.copy(titleError = "请填写菜品名称")
            onError("请填写菜品名称")
            return
        }

        val draft = RecipeDraft(
            id = if (state.isEditing) state.editingRecipeId else 0,
            title = state.title,
            description = state.description,
            cuisine = state.cuisine,
            difficulty = state.difficulty,
            cookingTime = state.cookingTime,
            servings = state.servings,
            mainIngredients = state.mainIngredients,
            subIngredients = state.subIngredients,
            seasoningIngredients = state.seasoningIngredients,
            steps = state.steps,
            photos = state.photos,
            notes = state.notes,
            rating = state.rating,
            selectedContactIds = state.selectedContactIds,
            selectedTagNames = state.selectedTagNames
        )

        viewModelScope.launch {
            createRecipeUseCase.save(draft)
            _state.value = _state.value.copy(isSaved = true)
            onSuccess()
        }
    }
}
