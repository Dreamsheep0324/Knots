package com.tang.prm.feature.people.contacts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.*
import com.tang.prm.domain.usecase.ContactDetailAggregationUseCase
import com.tang.prm.domain.usecase.FavoriteToggleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContactDetailDataState(
    val contact: Contact? = null,
    val events: List<Event> = emptyList(),
    val anniversaries: List<Anniversary> = emptyList(),
    val conversations: List<Event> = emptyList(),
    val gifts: List<Gift> = emptyList(),
    val thoughts: List<Thought> = emptyList(),
    val favoriteIds: Set<Long> = emptySet(),
    val isLoading: Boolean = false,
    val hobbyOptions: List<CustomType> = emptyList(),
    val habitOptions: List<CustomType> = emptyList(),
    val dietOptions: List<CustomType> = emptyList(),
    val skillOptions: List<CustomType> = emptyList(),
    val eventTypes: List<CustomType> = emptyList(),
    val relationshipTypes: List<CustomType> = emptyList()
)

data class ContactDetailDialogState(
    val selectedTab: Int = 0,
    val showDeleteDialog: Boolean = false
)

data class ContactDetailUiState(
    val data: ContactDetailDataState = ContactDetailDataState(),
    val dialog: ContactDetailDialogState = ContactDetailDialogState()
)

sealed class ContactDetailEvent {
    object ContactDeleted : ContactDetailEvent()
}

@HiltViewModel
class ContactDetailViewModel @Inject constructor(
    private val aggregationUseCase: ContactDetailAggregationUseCase,
    private val favoriteToggleUseCase: FavoriteToggleUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var contactId: Long = savedStateHandle.get<Long>("contactId") ?: 0L
    private var loadJob: Job? = null

    private val _uiState = MutableStateFlow(ContactDetailUiState())
    val uiState: StateFlow<ContactDetailUiState> = _uiState.asStateFlow()

    private val _events = Channel<ContactDetailEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadContactDetail()
    }

    /** 平板双栏模式：选中项变化时调用，触发详情刷新。 */
    fun setContactId(id: Long) {
        if (contactId != id) {
            contactId = id
            _uiState.update { ContactDetailUiState(data = ContactDetailDataState(isLoading = true)) }
            loadContactDetail()
        }
    }

    private fun loadContactDetail() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(data = it.data.copy(isLoading = true)) }

            aggregationUseCase.getContactDetail(contactId).collect { data ->
                _uiState.update { it.copy(
                    data = it.data.copy(
                        contact = data.contact,
                        events = data.events,
                        anniversaries = data.anniversaries,
                        conversations = data.conversations,
                        gifts = data.gifts,
                        thoughts = data.thoughts,
                        favoriteIds = data.favoriteIds,
                        hobbyOptions = data.hobbyOptions,
                        habitOptions = data.habitOptions,
                        dietOptions = data.dietOptions,
                        skillOptions = data.skillOptions,
                        eventTypes = data.eventTypes,
                        relationshipTypes = data.relationshipTypes,
                        isLoading = data.isLoading
                    )
                )}
            }
        }
    }

    fun onTabSelected(tabIndex: Int) {
        _uiState.update { it.copy(dialog = it.dialog.copy(selectedTab = tabIndex)) }
    }

    fun showDeleteDialog() {
        _uiState.update { it.copy(dialog = it.dialog.copy(showDeleteDialog = true)) }
    }

    fun hideDeleteDialog() {
        _uiState.update { it.copy(dialog = it.dialog.copy(showDeleteDialog = false)) }
    }

    fun deleteContact() {
        viewModelScope.launch {
            aggregationUseCase.deleteContact(contactId)
            _events.send(ContactDetailEvent.ContactDeleted)
        }
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

    fun toggleTodoDone(thought: Thought) {
        viewModelScope.launch {
            aggregationUseCase.updateThought(thought.copy(
                isDone = !thought.isDone,
                updatedAt = System.currentTimeMillis()
            ))
        }
    }

    fun deleteThought(id: Long) {
        viewModelScope.launch {
            aggregationUseCase.deleteThought(id)
        }
    }
}
