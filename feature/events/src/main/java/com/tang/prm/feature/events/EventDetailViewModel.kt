package com.tang.prm.feature.events

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.SourceTypes
import com.tang.prm.domain.repository.EventRepository
import com.tang.prm.domain.usecase.FavoriteToggleUseCase
import com.tang.prm.domain.usecase.ObserveFavoritesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EventDetailDataState(
    val event: Event? = null,
    val isLoading: Boolean = true,
    val isFavorite: Boolean = false
)

data class EventDetailDialogState(
    val isRemarkSaved: Boolean = false
)

data class EventDetailUiState(
    val data: EventDetailDataState = EventDetailDataState(),
    val dialog: EventDetailDialogState = EventDetailDialogState()
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EventDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val eventRepository: EventRepository,
    private val favoriteToggleUseCase: FavoriteToggleUseCase,
    private val observeFavoritesUseCase: ObserveFavoritesUseCase
) : ViewModel() {

    private val _eventIdFlow = MutableStateFlow(savedStateHandle["eventId"] ?: 0L)
    private val _dialogState = MutableStateFlow(EventDetailDialogState())

    val uiState: StateFlow<EventDetailUiState> = _eventIdFlow.flatMapLatest { eventId ->
        if (eventId == 0L) {
            combine(flowOf(EventDetailDataState()), _dialogState) { data, dialog ->
                EventDetailUiState(data = data, dialog = dialog)
            }
        } else {
            combine(
                eventRepository.getEventById(eventId),
                observeFavoritesUseCase.isFavorite(SourceTypes.EVENT, eventId),
                _dialogState
            ) { event, isFavorite, dialog ->
                EventDetailUiState(
                    data = EventDetailDataState(
                        event = event,
                        isLoading = false,
                        isFavorite = isFavorite
                    ),
                    dialog = dialog
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EventDetailUiState())

    /** 平板双栏模式：选中项变化时调用，触发详情刷新。 */
    fun setEventId(id: Long) {
        _eventIdFlow.value = id
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val event = uiState.value.data.event ?: return@launch
            favoriteToggleUseCase(
                type = SourceTypes.EVENT,
                sourceId = event.id,
                title = event.title,
                description = event.description
            )
        }
    }

    fun updateRemarks(remarks: String) {
        viewModelScope.launch {
            uiState.value.data.event?.let { event ->
                val updated = event.copy(remarks = remarks.ifBlank { null })
                eventRepository.updateEvent(updated)
                _dialogState.update { it.copy(isRemarkSaved = true) }
            }
        }
    }

    fun consumeRemarkSaved() {
        _dialogState.update { it.copy(isRemarkSaved = false) }
    }

    fun deleteEvent() {
        viewModelScope.launch {
            uiState.value.data.event?.let { event ->
                eventRepository.deleteEvent(event.id)
            }
        }
    }
}
