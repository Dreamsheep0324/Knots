package com.tang.prm.feature.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.EventType
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.EventRepository
import com.tang.prm.domain.usecase.EventManageUseCase
import com.tang.prm.domain.usecase.UpdateInteractionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddChatUiState(
    val selectedContact: Contact? = null,
    val title: String = "",
    val dialogueLines: List<DialogueLineInput> = emptyList(),
    val remarks: String? = null,
    val selectedDate: Long = System.currentTimeMillis(),
    val contacts: List<Contact> = emptyList(),
    val showContactPicker: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val isSaved: Boolean = false,
    val editingEventId: Long? = null,
    val pendingImageLineId: Long? = null
)

@HiltViewModel
class AddChatViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val contactRepository: ContactRepository,
    private val dialogueLineManager: DialogueLineManager,
    private val eventManageUseCase: EventManageUseCase,
    private val updateInteractionUseCase: UpdateInteractionUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val contactId: Long? = savedStateHandle.get<Long>("contactId")?.let { if (it == 0L) null else it }
    private val eventId: Long? = savedStateHandle.get<Long>("eventId")

    private val _uiState = MutableStateFlow(AddChatUiState())
    val uiState: StateFlow<AddChatUiState> = _uiState.asStateFlow()

    init {
        loadContacts()
        if (eventId != null && eventId > 0) {
            loadEventForEdit(eventId)
        } else if (contactId != null && contactId > 0) {
            loadContact(contactId)
        }
    }

    private fun loadContacts() {
        viewModelScope.launch {
            contactRepository.getAllContacts().collectLatest { contacts ->
                _uiState.update { it.copy(contacts = contacts) }
            }
        }
    }

    private fun loadContact(id: Long) {
        viewModelScope.launch {
            contactRepository.getContactById(id).first()?.let { contact ->
                _uiState.update { it.copy(selectedContact = contact) }
            }
        }
    }

    private fun loadEventForEdit(id: Long) {
        viewModelScope.launch {
            eventRepository.getEventById(id).first()?.let { event ->
                val contact = event.participants.firstOrNull()
                val parsedLines = dialogueLineManager.parseDescriptionToLines(event.description, contact?.name)
                _uiState.update {
                    it.copy(
                        editingEventId = id,
                        selectedContact = contact,
                        title = event.title,
                        dialogueLines = parsedLines,
                        remarks = event.remarks,
                        selectedDate = event.time
                    )
                }
            }
        }
    }

    fun selectContact(contact: Contact) {
        _uiState.update { it.copy(selectedContact = contact, showContactPicker = false, hasUnsavedChanges = true) }
    }

    fun showContactPicker() {
        _uiState.update { it.copy(showContactPicker = true) }
    }

    fun hideContactPicker() {
        _uiState.update { it.copy(showContactPicker = false) }
    }

    fun updateTitle(value: String) {
        _uiState.update { it.copy(title = value, hasUnsavedChanges = true) }
    }

    fun updateRemarks(value: String) {
        _uiState.update { it.copy(remarks = value.ifBlank { null }, hasUnsavedChanges = true) }
    }

    fun updateDate(dateMillis: Long) {
        _uiState.update { it.copy(selectedDate = dateMillis, hasUnsavedChanges = true) }
    }

    fun addDialogueLine(isMe: Boolean) {
        val speaker = if (isMe) "我" else "对方"
        _uiState.update { it.copy(dialogueLines = dialogueLineManager.addLine(it.dialogueLines, speaker), hasUnsavedChanges = true) }
    }

    fun updateDialogueLine(lineId: Long, content: String) {
        _uiState.update { state ->
            state.copy(dialogueLines = dialogueLineManager.updateLine(state.dialogueLines, lineId, content), hasUnsavedChanges = true)
        }
    }

    fun updateDialogueLineImage(lineId: Long, imageUri: String?) {
        _uiState.update { state ->
            state.copy(dialogueLines = dialogueLineManager.updateLineImage(state.dialogueLines, lineId, imageUri), hasUnsavedChanges = true)
        }
    }

    fun requestImageForLine(lineId: Long) {
        _uiState.update { it.copy(pendingImageLineId = lineId) }
    }

    fun onImagePicked(uri: String) {
        val lineId = _uiState.value.pendingImageLineId ?: return
        _uiState.update { state ->
            state.copy(
                pendingImageLineId = null,
                dialogueLines = dialogueLineManager.updateLineImage(state.dialogueLines, lineId, uri),
                hasUnsavedChanges = true
            )
        }
    }

    fun cancelImageRequest() {
        _uiState.update { it.copy(pendingImageLineId = null) }
    }

    fun toggleDialogueSpeaker(lineId: Long) {
        _uiState.update { state ->
            state.copy(dialogueLines = dialogueLineManager.toggleSpeaker(state.dialogueLines, lineId), hasUnsavedChanges = true)
        }
    }

    fun removeDialogueLine(lineId: Long) {
        _uiState.update { state ->
            state.copy(dialogueLines = dialogueLineManager.removeLine(state.dialogueLines, lineId), hasUnsavedChanges = true)
        }
    }

    fun moveDialogueLine(lineId: Long, direction: Int) {
        _uiState.update { state ->
            val lines = state.dialogueLines
            val index = lines.indexOfFirst { it.id == lineId }
            if (index < 0) return@update state
            val newIndex = index + direction
            if (newIndex < 0 || newIndex >= lines.size) return@update state
            state.copy(dialogueLines = dialogueLineManager.moveLine(lines, index, newIndex), hasUnsavedChanges = true)
        }
    }

    fun saveChat() {
        viewModelScope.launch {
            val state = _uiState.value
            val contact = state.selectedContact ?: return@launch
            val description = dialogueLineManager.buildDescription(state.dialogueLines, contact.name).ifBlank { null }
            val imageUris = state.dialogueLines.mapNotNull { it.imageUri }

            if (state.editingEventId != null) {
                val existingEvent = eventRepository.getEventById(state.editingEventId).first() ?: return@launch
                val oldContactId = existingEvent.participants.firstOrNull()?.id
                val event = existingEvent.copy(
                    title = state.title.ifBlank { "与${contact.name}的对话" },
                    description = description,
                    photos = imageUris,
                    remarks = state.remarks,
                    time = state.selectedDate,
                    participants = listOf(contact)
                )
                eventRepository.updateEvent(event)
                if (oldContactId != null && oldContactId != contact.id) {
                    eventRepository.deleteEventParticipant(state.editingEventId, oldContactId)
                    eventRepository.insertEventParticipant(state.editingEventId, contact.id)
                }
            } else {
                val event = Event(
                    type = EventType.CONVERSATION,
                    title = state.title.ifBlank { "与${contact.name}的对话" },
                    description = description,
                    photos = imageUris,
                    remarks = state.remarks,
                    time = state.selectedDate,
                    participants = listOf(contact)
                )

                val eventId = eventManageUseCase.insertEventWithParticipants(event, listOf(contact.id))

                updateInteractionUseCase(
                    contact.id,
                    contact.intimacyScore,
                    state.selectedDate
                )
            }

            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
