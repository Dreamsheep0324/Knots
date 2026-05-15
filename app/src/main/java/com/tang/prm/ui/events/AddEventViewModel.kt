package com.tang.prm.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CustomCategories
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.CustomTypeRepository
import com.tang.prm.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.tang.prm.ui.theme.weatherIconDefs
import com.tang.prm.ui.theme.emotionIconDefs

@HiltViewModel
class AddEventViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val contactRepository: ContactRepository,
    private val customTypeRepository: CustomTypeRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddEventUiState())
    val uiState: StateFlow<AddEventUiState> = _uiState.asStateFlow()
    private var editingEventId: Long? = null

    init { loadContacts(); loadCustomTypes() }

    private fun loadContacts() {
        viewModelScope.launch { contactRepository.getAllContacts().collect { list -> _uiState.update { it.copy(availableContacts = list) } } }
    }

    private fun loadCustomTypes() {
        viewModelScope.launch { customTypeRepository.getTypesByCategory(CustomCategories.EVENT_TYPE).collect { list -> _uiState.update { it.copy(eventTypes = list) } } }
        viewModelScope.launch {
            customTypeRepository.getTypesByCategory(CustomCategories.EMOTION).collect { list ->
                if (list.isEmpty()) seedDefaultEmotions()
                _uiState.update { it.copy(emotions = list) }
            }
        }
        viewModelScope.launch {
            customTypeRepository.getTypesByCategory(CustomCategories.WEATHER).collect { list ->
                if (list.isEmpty()) seedDefaultWeathers()
                _uiState.update { it.copy(weathers = list) }
            }
        }
    }

    private suspend fun seedDefaultWeathers() {
        if (customTypeRepository.getTypeCountByCategory(CustomCategories.WEATHER) > 0) return
        customTypeRepository.insertTypes(weatherIconDefs.mapIndexed { index, def ->
            CustomType(category = CustomCategories.WEATHER, name = def.name, color = def.color, sortOrder = index)
        })
    }

    private suspend fun seedDefaultEmotions() {
        if (customTypeRepository.getTypeCountByCategory(CustomCategories.EMOTION) > 0) return
        customTypeRepository.insertTypes(emotionIconDefs.mapIndexed { index, def ->
            CustomType(category = CustomCategories.EMOTION, name = def.name, color = def.color, sortOrder = index)
        })
    }

    fun loadEvent(eventId: Long) {
        editingEventId = eventId
        viewModelScope.launch {
            eventRepository.getEventById(eventId).first()?.let { event ->
                _uiState.update {
                    it.copy(title = event.title, type = event.type, description = event.description ?: "",
                        time = event.time, endTime = event.endTime, location = event.location ?: "",
                        emotion = event.emotion ?: "", weather = event.weather ?: "",
                        conversationSummary = event.conversationSummary ?: "", remarks = event.remarks ?: "",
                        photos = event.photos, participants = event.participants)
                }
            }
        }
    }

    fun updateTitle(value: String) = _uiState.update { it.copy(title = value, hasUnsavedChanges = true) }
    fun updateType(type: String) = _uiState.update { it.copy(type = type, hasUnsavedChanges = true) }
    fun updateDescription(value: String) = _uiState.update { it.copy(description = value, hasUnsavedChanges = true) }
    fun updateTime(value: Long) = _uiState.update { it.copy(time = value, hasUnsavedChanges = true) }
    fun updateEndTime(value: Long?) = _uiState.update { it.copy(endTime = value, hasUnsavedChanges = true) }
    fun updateLocation(value: String) = _uiState.update { it.copy(location = value, hasUnsavedChanges = true) }
    fun updateEmotion(value: String) = _uiState.update { it.copy(emotion = value, hasUnsavedChanges = true) }
    fun updateWeather(value: String) = _uiState.update { it.copy(weather = value, hasUnsavedChanges = true) }
    fun updateConversationSummary(value: String) = _uiState.update { it.copy(conversationSummary = value, hasUnsavedChanges = true) }
    fun updateRemarks(value: String) = _uiState.update { it.copy(remarks = value, hasUnsavedChanges = true) }
    fun updatePhotos(value: List<String>) = _uiState.update { it.copy(photos = value, hasUnsavedChanges = true) }
    fun addPhoto(uri: String) = _uiState.update { it.copy(photos = it.photos + uri, hasUnsavedChanges = true) }
    fun removePhoto(uri: String) = _uiState.update { it.copy(photos = it.photos.filter { p -> p != uri }, hasUnsavedChanges = true) }
    fun showContactPicker() = _uiState.update { it.copy(showContactPicker = true) }
    fun hideContactPicker() = _uiState.update { it.copy(showContactPicker = false) }

    fun addParticipantById(contactId: Long) {
        viewModelScope.launch { contactRepository.getContactById(contactId).first()?.let { addParticipant(it) } }
    }

    fun addParticipant(contact: Contact) {
        _uiState.update { state ->
            if (state.participants.any { it.id == contact.id }) state
            else state.copy(participants = state.participants + contact, hasUnsavedChanges = true)
        }
    }

    fun removeParticipant(contact: Contact) {
        _uiState.update { state -> state.copy(participants = state.participants.filter { it.id != contact.id }, hasUnsavedChanges = true) }
    }

    fun addEventType(name: String, color: String? = null, icon: String? = null) {
        viewModelScope.launch { customTypeRepository.insertType(CustomType(category = CustomCategories.EVENT_TYPE, name = name, color = color, icon = icon, sortOrder = _uiState.value.eventTypes.size)) }
    }
    fun deleteEventType(type: CustomType) { viewModelScope.launch { customTypeRepository.deleteTypeById(type.id) } }
    fun addEmotion(name: String, color: String? = null) {
        viewModelScope.launch { customTypeRepository.insertType(CustomType(category = CustomCategories.EMOTION, name = name, color = color, sortOrder = _uiState.value.emotions.size)) }
    }
    fun deleteEmotion(type: CustomType) { viewModelScope.launch { customTypeRepository.deleteTypeById(type.id) } }
    fun addWeather(name: String, color: String? = null) {
        viewModelScope.launch { customTypeRepository.insertType(CustomType(category = CustomCategories.WEATHER, name = name, color = color, sortOrder = _uiState.value.weathers.size)) }
    }
    fun deleteWeather(type: CustomType) { viewModelScope.launch { customTypeRepository.deleteTypeById(type.id) } }

    fun saveEvent() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.title.isBlank() || state.type.isBlank()) return@launch
            val event = Event(id = editingEventId ?: 0, title = state.title, type = state.type,
                description = state.description.ifBlank { null }, time = state.time, endTime = state.endTime,
                location = state.location.ifBlank { null }, emotion = state.emotion.ifBlank { null },
                weather = state.weather.ifBlank { null }, conversationSummary = state.conversationSummary.ifBlank { null },
                remarks = state.remarks.ifBlank { null }, photos = state.photos, participants = state.participants)
            if (editingEventId != null) {
                eventRepository.updateEventWithParticipants(event, state.participants.map { it.id })
                state.participants.forEach { p ->
                    contactRepository.updateContactInteraction(p.id, p.intimacyScore.coerceIn(0, 100), state.time)
                }
            } else {
                val eid = eventRepository.insertEventWithParticipants(event, state.participants.map { it.id })
                state.participants.forEach { p ->
                    contactRepository.updateContactInteraction(p.id, p.intimacyScore.coerceIn(0, 100), state.time)
                }
            }
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
