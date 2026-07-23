package com.tang.prm.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.usecase.ConversationItem
import com.tang.prm.domain.usecase.ObserveConversationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class ChatDataState(
    val conversations: List<ConversationItem> = emptyList(),
    val isLoading: Boolean = false
)
class ChatDialogState
data class ChatUiState(
    val data: ChatDataState = ChatDataState(),
    val dialog: ChatDialogState = ChatDialogState()
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val observeConversationsUseCase: ObserveConversationsUseCase
) : ViewModel() {

    private val _dialogState = MutableStateFlow(ChatDialogState())

    val uiState: StateFlow<ChatUiState> = combine(
        observeConversationsUseCase()
            .map { conversations ->
                ChatDataState(conversations = conversations, isLoading = false)
            },
        _dialogState
    ) { data, dialog ->
        ChatUiState(data = data, dialog = dialog)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChatUiState())

}
