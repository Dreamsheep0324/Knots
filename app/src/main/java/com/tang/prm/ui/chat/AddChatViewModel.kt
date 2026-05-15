package com.tang.prm.ui.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.EventTypes
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

private val lineIdCounter = AtomicLong(0)

data class DialogueLineInput(
    val id: Long = lineIdCounter.incrementAndGet(),
    val isMe: Boolean = true,
    val content: String = "",
    val imageUri: String? = null
)

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
            contactRepository.getAllContacts().collect { contacts ->
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
                val parsedLines = parseDescriptionToLines(event.description, contact?.name)
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

    private fun parseDescriptionToLines(description: String?, contactName: String?): List<DialogueLineInput> {
        if (description.isNullOrBlank()) return emptyList()
        val lines = description.split("\n").filter { it.isNotBlank() }
        return lines.mapNotNull { line ->
            val trimmed = line.trimStart()
            when {
                trimmed.startsWith("我：") || trimmed.startsWith("我:") -> {
                    val sepIdx = trimmed.indexOfFirst { it == '：' || it == ':' }
                    if (sepIdx >= 0) {
                        val raw = trimmed.substring(sepIdx + 1).trim()
                        val (text, uri) = extractImageTag(raw)
                        DialogueLineInput(isMe = true, content = text, imageUri = uri)
                    } else null
                }
                contactName != null && (trimmed.startsWith("$contactName：") || trimmed.startsWith("$contactName:")) -> {
                    val sepIdx = trimmed.indexOfFirst { it == '：' || it == ':' }
                    if (sepIdx >= 0) {
                        val raw = trimmed.substring(sepIdx + 1).trim()
                        val (text, uri) = extractImageTag(raw)
                        DialogueLineInput(isMe = false, content = text, imageUri = uri)
                    } else null
                }
                trimmed.startsWith("对方：") || trimmed.startsWith("对方:") -> {
                    val sepIdx = trimmed.indexOfFirst { it == '：' || it == ':' }
                    if (sepIdx >= 0) {
                        val raw = trimmed.substring(sepIdx + 1).trim()
                        val (text, uri) = extractImageTag(raw)
                        DialogueLineInput(isMe = false, content = text, imageUri = uri)
                    } else null
                }
                else -> null
            }
        }
    }

    private fun extractImageTag(raw: String): Pair<String, String?> {
        val regex = Regex("""\[img:(.+?)]""")
        val match = regex.find(raw)
        return if (match != null) {
            val uri = match.groupValues[1]
            val text = raw.replace(match.value, "").trim()
            text to uri
        } else {
            raw to null
        }
    }

    private fun buildDescription(lines: List<DialogueLineInput>, contactName: String?): String {
        return lines.filter { it.content.isNotBlank() || it.imageUri != null }.joinToString("\n") { line ->
            val speaker = if (line.isMe) "我" else (contactName ?: "对方")
            val imageTag = if (line.imageUri != null) "[img:${line.imageUri}]" else ""
            val text = line.content + if (imageTag.isNotEmpty()) " $imageTag" else ""
            "$speaker：$text"
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
        val currentLines = _uiState.value.dialogueLines
        val newLine = DialogueLineInput(isMe = isMe)
        _uiState.update { it.copy(dialogueLines = currentLines + newLine, hasUnsavedChanges = true) }
    }

    fun updateDialogueLine(lineId: Long, content: String) {
        _uiState.update { state ->
            state.copy(dialogueLines = state.dialogueLines.map { line ->
                if (line.id == lineId) line.copy(content = content) else line
            }, hasUnsavedChanges = true)
        }
    }

    fun updateDialogueLineImage(lineId: Long, imageUri: String?) {
        _uiState.update { state ->
            state.copy(dialogueLines = state.dialogueLines.map { line ->
                if (line.id == lineId) line.copy(imageUri = imageUri) else line
            }, hasUnsavedChanges = true)
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
                dialogueLines = state.dialogueLines.map { line ->
                    if (line.id == lineId) line.copy(imageUri = uri) else line
                },
                hasUnsavedChanges = true
            )
        }
    }

    fun cancelImageRequest() {
        _uiState.update { it.copy(pendingImageLineId = null) }
    }

    fun toggleDialogueSpeaker(lineId: Long) {
        _uiState.update { state ->
            state.copy(dialogueLines = state.dialogueLines.map { line ->
                if (line.id == lineId) line.copy(isMe = !line.isMe) else line
            }, hasUnsavedChanges = true)
        }
    }

    fun removeDialogueLine(lineId: Long) {
        _uiState.update { state ->
            state.copy(dialogueLines = state.dialogueLines.filter { it.id != lineId }, hasUnsavedChanges = true)
        }
    }

    fun moveDialogueLine(lineId: Long, direction: Int) {
        _uiState.update { state ->
            val lines = state.dialogueLines.toMutableList()
            val index = lines.indexOfFirst { it.id == lineId }
            if (index < 0) return@update state
            val newIndex = index + direction
            if (newIndex < 0 || newIndex >= lines.size) return@update state
            val item = lines.removeAt(index)
            lines.add(newIndex, item)
            state.copy(dialogueLines = lines, hasUnsavedChanges = true)
        }
    }

    fun saveChat() {
        viewModelScope.launch {
            val state = _uiState.value
            val contact = state.selectedContact ?: return@launch
            val description = buildDescription(state.dialogueLines, contact.name).ifBlank { null }
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
                    type = EventTypes.CONVERSATION,
                    title = state.title.ifBlank { "与${contact.name}的对话" },
                    description = description,
                    photos = imageUris,
                    remarks = state.remarks,
                    time = state.selectedDate,
                    participants = listOf(contact)
                )

                val eventId = eventRepository.insertEventWithParticipants(event, listOf(contact.id))

                contactRepository.updateContactInteraction(
                    contact.id,
                    contact.intimacyScore.coerceIn(0, 100),
                    state.selectedDate
                )
            }

            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
