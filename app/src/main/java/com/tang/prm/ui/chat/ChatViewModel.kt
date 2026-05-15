package com.tang.prm.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.tang.prm.util.DateUtils
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

data class ChatUiState(
    val conversations: List<ConversationUiModel> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadConversations()
    }

    private fun loadConversations() {
        viewModelScope.launch {
            eventRepository.getConversationEvents().collect { events ->
                val conversations = events
                    .sortedByDescending { it.createdAt }
                    .map { event ->
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

                _uiState.update { it.copy(conversations = conversations, isLoading = false) }
            }
        }
    }
}