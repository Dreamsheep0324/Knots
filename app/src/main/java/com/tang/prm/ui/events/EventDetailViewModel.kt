package com.tang.prm.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.SourceTypes
import com.tang.prm.domain.repository.EventRepository
import com.tang.prm.domain.repository.FavoriteRepository
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

data class EventDetailUiState(
    val event: Event? = null,
    val isLoading: Boolean = true,
    val isRemarkSaved: Boolean = false,
    val isFavorite: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _eventId = MutableStateFlow(0L)
    private val _isRemarkSaved = MutableStateFlow(false)

    val uiState: StateFlow<EventDetailUiState> = _eventId.flatMapLatest { eventId ->
        if (eventId == 0L) return@flatMapLatest flowOf(EventDetailUiState())
        combine(
            eventRepository.getEventById(eventId),
            favoriteRepository.isFavorite(SourceTypes.EVENT, eventId),
            _isRemarkSaved
        ) { event, isFavorite, isRemarkSaved ->
            EventDetailUiState(
                event = event,
                isLoading = false,
                isFavorite = isFavorite,
                isRemarkSaved = isRemarkSaved
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EventDetailUiState())

    fun loadEvent(eventId: Long) {
        _eventId.value = eventId
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val event = uiState.value.event ?: return@launch
            favoriteRepository.toggleFavorite(
                type = SourceTypes.EVENT,
                sourceId = event.id,
                title = event.title,
                description = event.description
            )
        }
    }

    fun updateRemarks(remarks: String) {
        viewModelScope.launch {
            uiState.value.event?.let { event ->
                val updated = event.copy(remarks = remarks.ifBlank { null })
                eventRepository.updateEvent(updated)
                _isRemarkSaved.value = true
            }
        }
    }

    fun consumeRemarkSaved() {
        _isRemarkSaved.value = false
    }

    fun deleteEvent() {
        viewModelScope.launch {
            uiState.value.event?.let { event ->
                eventRepository.deleteEvent(event.id)
            }
        }
    }
}
