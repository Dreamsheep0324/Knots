package com.tang.prm.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.EventType
import com.tang.prm.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import com.tang.prm.domain.util.DateUtils
import javax.inject.Inject

data class ConversationUiModel(
    val eventId: Long,
    val contactId: Long?,
    val contactName: String,
    val avatar: String?,
    val title: String?,
    val lastMessage: String,
    val lastMessageTime: String
)

data class ChatDataState(
    val conversations: List<ConversationUiModel> = emptyList(),
    val isLoading: Boolean = false
)
data class ChatDialogState(
    val showDeleteConfirm: Long? = null
)
data class ChatUiState(
    val data: ChatDataState = ChatDataState(),
    val dialog: ChatDialogState = ChatDialogState()
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _dialogState = MutableStateFlow(ChatDialogState())

    val uiState: StateFlow<ChatUiState> = combine(
        eventRepository.getEventsByType(EventType.CONVERSATION.name)
            .map { events ->
                val conversations = events.sortedByDescending { it.createdAt }.map { event ->
                    val primaryContact = event.participants.firstOrNull()
                    ConversationUiModel(
                        eventId = event.id,
                        contactId = primaryContact?.id,
                        contactName = primaryContact?.name ?: event.title,
                        avatar = primaryContact?.avatar,
                        title = event.title,
                        lastMessage = event.conversationSummary ?: event.title,
                        lastMessageTime = DateUtils.formatShortDate(event.createdAt)
                    )
                }
                ChatDataState(conversations = conversations, isLoading = false)
            },
        _dialogState
    ) { data, dialog ->
        ChatUiState(data = data, dialog = dialog)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChatUiState())

    fun showDeleteConfirm(id: Long?) {
        _dialogState.value = ChatDialogState(showDeleteConfirm = id)
    }
}