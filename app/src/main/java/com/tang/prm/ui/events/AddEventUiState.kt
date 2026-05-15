package com.tang.prm.ui.events

import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CustomType

data class AddEventUiState(
    val title: String = "",
    val type: String = "",
    val description: String = "",
    val time: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val location: String = "",
    val emotion: String = "",
    val weather: String = "",
    val conversationSummary: String = "",
    val remarks: String = "",
    val photos: List<String> = emptyList(),
    val participants: List<Contact> = emptyList(),
    val availableContacts: List<Contact> = emptyList(),
    val eventTypes: List<CustomType> = emptyList(),
    val emotions: List<CustomType> = emptyList(),
    val weathers: List<CustomType> = emptyList(),
    val showContactPicker: Boolean = false,
    val isSaved: Boolean = false,
    val hasUnsavedChanges: Boolean = false
)
