package com.tang.prm.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.SourceTypes
import com.tang.prm.domain.model.Thought
import com.tang.prm.domain.model.ThoughtType
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.FavoriteRepository
import com.tang.prm.domain.repository.ThoughtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

fun calcLevel(exp: Int): Int {
    var level = 1
    while (expForLevel(level + 1) <= exp) level++
    return level
}

fun expForLevel(level: Int): Int {
    if (level <= 1) return 0
    return 15 * level * (level - 1) / 2
}

private fun calcStreak(thoughts: List<Thought>): Int {
    if (thoughts.isEmpty()) return 0
    val calendar = java.util.Calendar.getInstance()
    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
    calendar.set(java.util.Calendar.MINUTE, 0)
    calendar.set(java.util.Calendar.SECOND, 0)
    calendar.set(java.util.Calendar.MILLISECOND, 0)
    val dayMs = 24 * 60 * 60 * 1000L
    val daysWithThoughts = thoughts.map { it.createdAt / dayMs }.toSet()
    var streak = 0
    var checkDay = calendar.timeInMillis / dayMs
    if (daysWithThoughts.contains(checkDay)) {
        streak = 1
        checkDay--
        while (daysWithThoughts.contains(checkDay)) { streak++; checkDay-- }
    }
    return streak
}

data class ContactThoughts(
    val contact: Contact,
    val thoughts: List<Thought>,
    val latestThought: Thought?
)

data class ThoughtsUiState(
    val allThoughts: List<Thought> = emptyList(),
    val contacts: List<Contact> = emptyList(),
    val contactThoughts: List<ContactThoughts> = emptyList(),
    val todoThoughts: List<Thought> = emptyList(),
    val filteredThoughts: List<Thought> = emptyList(),
    val selectedFilter: String = "all",
    val selectedContactId: Long? = null,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val isLoading: Boolean = false,
    val showDialog: Boolean = false,
    val editingThought: Thought? = null,
    val dialogType: ThoughtType = ThoughtType.MURMUR,
    val favoriteIds: Set<Long> = emptySet()
) {
    val totalCount: Int get() = allThoughts.size
    val friendCount: Int get() = allThoughts.count { it.type == ThoughtType.FRIEND }
    val planCount: Int get() = allThoughts.count { it.type == ThoughtType.PLAN }
    val murmurCount: Int get() = allThoughts.count { it.type == ThoughtType.MURMUR }
    val nonTodoThoughts: List<Thought> get() = allThoughts.filter { !it.isTodo }
    val todoDoneCount: Int get() = todoThoughts.count { it.isDone }
    val todoTotalCount: Int get() = todoThoughts.size
    val currentExp: Int get() = allThoughts.size * 3 +
            todoThoughts.count { it.isDone } * 2 +
            contactThoughts.size * 1 +
            calcStreakExp()
    val currentLevel: Int get() = calcLevel(currentExp)
    val nextLevel: Int get() = currentLevel + 1
    val expInLevel: Int get() = currentExp - expForLevel(currentLevel)
    val expNeeded: Int get() = expForLevel(nextLevel) - expForLevel(currentLevel)
    val levelProgress: Float get() = if (expNeeded > 0) expInLevel.toFloat() / expNeeded else 1f
    val streak: Int get() = calcStreak(allThoughts)

    private fun calcStreakExp(): Int {
        return calcStreak(allThoughts) * 1
    }

    fun thoughtExp(thought: Thought): Int = 3 + (if (thought.isDone) 2 else 0) + (if (thought.contactId != null) 1 else 0)
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ThoughtsViewModel @Inject constructor(
    private val thoughtRepository: ThoughtRepository,
    private val contactRepository: ContactRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow("all")
    private val _selectedContactId = MutableStateFlow<Long?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _isSearching = MutableStateFlow(false)
    private val _showDialog = MutableStateFlow(false)
    private val _editingThought = MutableStateFlow<Thought?>(null)
    private val _dialogType = MutableStateFlow(ThoughtType.MURMUR)
    private val _favoriteIds = MutableStateFlow<Set<Long>>(emptySet())

    init {
        viewModelScope.launch {
            favoriteRepository.getFavoritesByType(SourceTypes.THOUGHT).collect { favList ->
                _favoriteIds.value = favList.map { it.sourceId }.toSet()
            }
        }
    }

    val uiState = combine(
        thoughtRepository.getAllThoughts(),
        thoughtRepository.getTodoThoughts(),
        contactRepository.getAllContacts(),
        _selectedFilter,
        _selectedContactId,
        _searchQuery,
        _isSearching,
        _showDialog,
        _editingThought,
        _dialogType,
        _favoriteIds
    ) { values ->
        val thoughts = values[0] as List<Thought>
        val todos = values[1] as List<Thought>
        val contacts = values[2] as List<Contact>
        val filter = values[3] as String
        val contactIdFilter = values[4] as Long?
        val query = values[5] as String
        val isSearching = values[6] as Boolean
        val showDialog = values[7] as Boolean
        val editingThought = values[8] as Thought?
        val dialogType = values[9] as ThoughtType
        val favIds = values[10] as Set<Long>

        val contactMap = contacts.associateBy { it.id }
        val contactThoughts = thoughts
            .filter { it.contactId != null }
            .groupBy { it.contactId }
            .mapNotNull { (contactId, list) ->
                val contact = contactMap[contactId] ?: return@mapNotNull null
                ContactThoughts(
                    contact = contact,
                    thoughts = list,
                    latestThought = list.firstOrNull()
                )
            }

        val byType = when (filter) {
            "friend" -> thoughts.filter { it.type == ThoughtType.FRIEND }
            "plan" -> thoughts.filter { it.type == ThoughtType.PLAN }
            "murmur" -> thoughts.filter { it.type == ThoughtType.MURMUR }
            "todo" -> thoughts.filter { it.isTodo }
            else -> thoughts
        }

        val byContact = if (contactIdFilter != null) {
            byType.filter { it.contactId == contactIdFilter }
        } else {
            byType
        }

        val filtered = if (query.isBlank()) byContact else byContact.filter {
            it.content.contains(query, ignoreCase = true) ||
            contactMap[it.contactId]?.name?.contains(query, ignoreCase = true) == true
        }

        ThoughtsUiState(
            allThoughts = thoughts,
            contacts = contacts,
            contactThoughts = contactThoughts,
            todoThoughts = todos,
            filteredThoughts = filtered,
            selectedFilter = filter,
            selectedContactId = contactIdFilter,
            searchQuery = query,
            isSearching = isSearching,
            showDialog = showDialog,
            editingThought = editingThought,
            dialogType = dialogType,
            favoriteIds = favIds
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThoughtsUiState())

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

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSearchToggle() {
        _isSearching.value = !_isSearching.value
        if (!_isSearching.value) _searchQuery.value = ""
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
            thoughtRepository.insertThought(thought)
        }
        dismissDialog()
    }

    fun updateThought(thought: Thought) {
        viewModelScope.launch {
            thoughtRepository.updateThought(thought.copy(updatedAt = System.currentTimeMillis()))
        }
        dismissDialog()
    }

    fun deleteThought(id: Long) {
        viewModelScope.launch {
            thoughtRepository.deleteThought(id)
        }
    }

    fun toggleTodoDone(thought: Thought) {
        viewModelScope.launch {
            thoughtRepository.updateThought(thought.copy(
                isDone = !thought.isDone,
                updatedAt = System.currentTimeMillis()
            ))
        }
    }

    fun getContactName(contactId: Long?): String? {
        if (contactId == null) return null
        return uiState.value.contacts.find { it.id == contactId }?.name
    }

    fun getContactAvatar(contactId: Long?): String? {
        if (contactId == null) return null
        return uiState.value.contacts.find { it.id == contactId }?.avatar
    }

    fun toggleFavorite(thoughtId: Long, content: String) {
        viewModelScope.launch {
            favoriteRepository.toggleFavorite(
                type = SourceTypes.THOUGHT,
                sourceId = thoughtId,
                title = content.take(50),
                description = content
            )
        }
    }
}
