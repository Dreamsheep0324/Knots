package com.tang.prm.feature.people.contacts

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.*
import com.tang.prm.domain.usecase.ContactDetailAggregationUseCase
import com.tang.prm.domain.usecase.FavoriteToggleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ContactDetailViewModel @Inject constructor(
    private val aggregationUseCase: ContactDetailAggregationUseCase,
    private val favoriteToggleUseCase: FavoriteToggleUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TAG = "ContactDetailViewModel"
    }

    private val _contactId = MutableStateFlow(savedStateHandle.get<Long>("contactId") ?: 0L)
    private val _dialogState = MutableStateFlow(ContactDetailDialogState())

    val uiState: StateFlow<ContactDetailUiState> = combine(
        _contactId.flatMapLatest { id ->
            aggregationUseCase.getContactDetail(id)
                .map { data ->
                    ContactDetailDataState(
                        contact = data.contact,
                        events = data.events,
                        anniversaries = data.anniversaries,
                        conversations = data.conversations,
                        gifts = data.gifts,
                        thoughts = data.thoughts,
                        favoriteIds = data.favoriteIds,
                        isLoading = data.isLoading,
                        hobbyOptions = data.hobbyOptions,
                        habitOptions = data.habitOptions,
                        dietOptions = data.dietOptions,
                        skillOptions = data.skillOptions,
                        eventTypes = data.eventTypes,
                        relationshipTypes = data.relationshipTypes
                    )
                }
                .onStart { emit(ContactDetailDataState(isLoading = true)) }
        },
        _dialogState
    ) { data, dialog ->
        ContactDetailUiState(data = data, dialog = dialog)
    }.catch { e ->
        Log.e(TAG, "联系人详情数据流异常", e)
        emit(ContactDetailUiState(data = ContactDetailDataState(isLoading = false)))
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ContactDetailUiState(data = ContactDetailDataState(isLoading = true))
    )

    private val _events = Channel<ContactDetailEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    /** 平板双栏模式：选中项变化时调用，触发详情刷新。 */
    fun setContactId(id: Long) {
        if (_contactId.value != id) {
            _dialogState.value = ContactDetailDialogState()
            _contactId.value = id
        }
    }

    fun onTabSelected(tabIndex: Int) {
        _dialogState.update { it.copy(selectedTab = tabIndex) }
    }

    fun showDeleteDialog() {
        _dialogState.update { it.copy(showDeleteDialog = true) }
    }

    fun hideDeleteDialog() {
        _dialogState.update { it.copy(showDeleteDialog = false) }
    }

    fun deleteContact() {
        viewModelScope.launch {
            runCatching {
                aggregationUseCase.deleteContact(_contactId.value)
                _events.send(ContactDetailEvent.ContactDeleted)
            }.onFailure { Log.e(TAG, "删除联系人失败", it) }
        }
    }

    fun toggleFavorite(thoughtId: Long, content: String) {
        viewModelScope.launch {
            runCatching {
                favoriteToggleUseCase(
                    type = SourceTypes.THOUGHT,
                    sourceId = thoughtId,
                    title = content.take(50),
                    description = content
                )
            }.onFailure { Log.e(TAG, "切换收藏失败", it) }
        }
    }

    fun toggleTodoDone(thought: Thought) {
        viewModelScope.launch {
            runCatching {
                aggregationUseCase.updateThought(thought.copy(
                    isDone = !thought.isDone,
                    updatedAt = System.currentTimeMillis()
                ))
            }.onFailure { Log.e(TAG, "更新待办状态失败", it) }
        }
    }

    fun deleteThought(id: Long) {
        viewModelScope.launch {
            runCatching {
                aggregationUseCase.deleteThought(id)
            }.onFailure { Log.e(TAG, "删除想法失败", it) }
        }
    }
}
