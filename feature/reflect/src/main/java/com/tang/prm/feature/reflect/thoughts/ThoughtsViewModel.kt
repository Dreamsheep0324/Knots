package com.tang.prm.feature.reflect.thoughts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.SourceTypes
import com.tang.prm.domain.model.Thought
import com.tang.prm.domain.model.ThoughtType
import com.tang.prm.domain.usecase.ContactThoughts
import com.tang.prm.domain.usecase.FavoriteToggleUseCase
import com.tang.prm.domain.usecase.ObserveFavoritesUseCase
import com.tang.prm.domain.usecase.GamificationState
import com.tang.prm.domain.usecase.ThoughtListUseCase
import com.tang.prm.domain.usecase.ThoughtWriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ThoughtsDataState(
    val allThoughts: List<Thought> = emptyList(),
    val contacts: List<Contact> = emptyList(),
    val contactMap: Map<Long, Contact> = emptyMap(),
    val contactThoughts: List<ContactThoughts> = emptyList(),
    val todoThoughts: List<Thought> = emptyList(),
    val filteredThoughts: List<Thought> = emptyList(),
    val selectedFilter: String = "all",
    val selectedContactId: Long? = null,
    val favoriteIds: Set<Long> = emptySet()
)

data class ThoughtsDialogState(
    val showDialog: Boolean = false,
    val editingThought: Thought? = null,
    val dialogType: ThoughtType = ThoughtType.MURMUR
)

data class ThoughtsUiState(
    val data: ThoughtsDataState = ThoughtsDataState(),
    val dialog: ThoughtsDialogState = ThoughtsDialogState(),
    val gamification: GamificationState? = null
)

private data class FilterState(
    val selectedFilter: String,
    val selectedContactId: Long?
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ThoughtsViewModel @Inject constructor(
    private val thoughtWriteUseCase: ThoughtWriteUseCase,
    private val favoriteToggleUseCase: FavoriteToggleUseCase,
    private val observeFavoritesUseCase: ObserveFavoritesUseCase,
    private val thoughtListUseCase: ThoughtListUseCase
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow("all")
    private val _selectedContactId = MutableStateFlow<Long?>(null)
    private val _showDialog = MutableStateFlow(false)
    private val _editingThought = MutableStateFlow<Thought?>(null)
    private val _dialogType = MutableStateFlow(ThoughtType.MURMUR)
    private val _favoriteIds = MutableStateFlow<Set<Long>>(emptySet())

    init {
        viewModelScope.launch {
            observeFavoritesUseCase.getFavoriteIds(SourceTypes.THOUGHT).collect { ids ->
                _favoriteIds.value = ids
            }
        }
    }

    private val filterState = combine(
        _selectedFilter,
        _selectedContactId
    ) { selectedFilter, selectedContactId ->
        FilterState(selectedFilter, selectedContactId)
    }

    // C-6 修复：移除私有 DialogState（与 ThoughtsDialogState 字段完全重复），直接复用公共类型
    private val dialogState = combine(
        _showDialog,
        _editingThought,
        _dialogType
    ) { showDialog, editingThought, dialogType ->
        ThoughtsDialogState(showDialog, editingThought, dialogType)
    }

    private val thoughtListState = filterState.flatMapLatest { filter ->
        thoughtListUseCase(
            selectedFilter = filter.selectedFilter,
            selectedContactId = filter.selectedContactId,
            searchQuery = ""
        )
    }

    val uiState = combine(
        thoughtListState,
        filterState,
        dialogState,
        _favoriteIds
    ) { listState, filter, dialog, favIds ->
        ThoughtsUiState(
            data = ThoughtsDataState(
                allThoughts = listState.allThoughts,
                contacts = listState.contacts,
                contactMap = listState.contactMap,
                contactThoughts = listState.contactThoughts,
                todoThoughts = listState.todoThoughts,
                filteredThoughts = listState.filteredThoughts,
                selectedFilter = filter.selectedFilter,
                selectedContactId = filter.selectedContactId,
                favoriteIds = favIds
            ),
            dialog = dialog,
            gamification = listState.gamification
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThoughtsUiState())

    fun thoughtExp(thought: Thought): Int = thoughtListUseCase.thoughtExp(thought)

    fun onFilterSelected(filter: String) {
        _selectedFilter.value = filter
        _selectedContactId.value = null
    }

    fun onContactFilterSelected(contactId: Long?) {
        if (_selectedContactId.value == contactId) {
            _selectedContactId.value = null
        } else {
            _selectedContactId.value = contactId
        }
    }

    fun showAddDialog(type: ThoughtType) {
        _dialogType.value = type
        _editingThought.value = null
        _showDialog.value = true
    }

    fun showEditDialog(thought: Thought) {
        _dialogType.value = thought.type
        _editingThought.value = thought
        _showDialog.value = true
    }

    fun dismissDialog() {
        _showDialog.value = false
        _editingThought.value = null
    }

    fun insertThought(
        content: String,
        type: ThoughtType,
        contactId: Long? = null,
        isPrivate: Boolean = false,
        isTodo: Boolean = false,
        dueDate: Long? = null
    ) {
        viewModelScope.launch {
            val thought = Thought(
                content = content,
                type = type,
                contactId = contactId,
                isPrivate = isPrivate,
                isTodo = isTodo,
                dueDate = dueDate
            )
            thoughtWriteUseCase.insert(thought)
        }
        dismissDialog()
    }

    fun updateThought(thought: Thought) {
        viewModelScope.launch {
            thoughtWriteUseCase.update(thought)
        }
        dismissDialog()
    }

    fun deleteThought(id: Long) {
        viewModelScope.launch {
            thoughtWriteUseCase.delete(id)
        }
    }

    fun toggleTodoDone(thought: Thought) {
        viewModelScope.launch {
            thoughtWriteUseCase.toggleDone(thought)
        }
    }

    /** O(1) Map 查找替代原 O(n) 线性扫描 */
    fun getContactName(contactId: Long?): String? {
        if (contactId == null) return null
        return uiState.value.data.contactMap[contactId]?.name
    }

    fun getContactAvatar(contactId: Long?): String? {
        if (contactId == null) return null
        return uiState.value.data.contactMap[contactId]?.avatar
    }

    fun toggleFavorite(thoughtId: Long, content: String) {
        viewModelScope.launch {
            favoriteToggleUseCase(
                type = SourceTypes.THOUGHT,
                sourceId = thoughtId,
                title = content.take(50),
                description = content
            )
        }
    }
}
