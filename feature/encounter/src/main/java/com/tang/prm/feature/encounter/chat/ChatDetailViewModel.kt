package com.tang.prm.feature.encounter.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.SourceTypes
import com.tang.prm.domain.repository.EventRepository
import com.tang.prm.domain.usecase.FavoriteToggleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatDetailUiState(
    val event: Event? = null,
    val isLoading: Boolean = true,
    val isFavorite: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val favoriteToggleUseCase: FavoriteToggleUseCase
) : ViewModel() {

    private val _eventId = MutableStateFlow(0L)

    val uiState: StateFlow<ChatDetailUiState> = _eventId.flatMapLatest { eventId ->
        if (eventId == 0L) return@flatMapLatest flowOf(ChatDetailUiState())
        combine(
            eventRepository.getEventById(eventId),
            favoriteToggleUseCase.isFavorite(SourceTypes.DIALOG, eventId)
        ) { event, isFavorite ->
            ChatDetailUiState(
                event = event,
                isLoading = false,
                isFavorite = isFavorite
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChatDetailUiState())

    fun loadEvent(eventId: Long) {
        _eventId.value = eventId
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val event = uiState.value.event ?: return@launch
            val contactName = event.participants?.firstOrNull()?.name ?: "对话"
            favoriteToggleUseCase(
                type = SourceTypes.DIALOG,
                sourceId = event.id,
                title = "与${contactName}的对话",
                description = event.description
            )
        }
    }

    fun deleteEvent() {
        viewModelScope.launch {
            uiState.value.event?.let { event ->
                eventRepository.deleteEvent(event.id)
            }
        }
    }
}
