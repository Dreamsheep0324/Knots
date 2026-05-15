package com.tang.prm.ui.anniversary

import com.tang.prm.domain.model.AnniversaryType
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CustomType
import com.tang.prm.util.DateUtils

data class AddAnniversaryUiState(
    val id: Long = 0,
    val name: String = "",
    val contactId: Long? = null,
    val contactName: String? = null,
    val selectedType: AnniversaryType = AnniversaryType.ANNIVERSARY,
    val typeName: String = "",
    val selectedIcon: String = "Cake",
    val date: Long = System.currentTimeMillis(),
    val dateText: String = DateUtils.formatDate(System.currentTimeMillis()),
    val isLunar: Boolean = false,
    val isRepeat: Boolean = true,
    val remarks: String = "",
    val contacts: List<Contact> = emptyList(),
    val anniversaryTypes: List<CustomType> = emptyList(),
    val isEditing: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val isSaved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
