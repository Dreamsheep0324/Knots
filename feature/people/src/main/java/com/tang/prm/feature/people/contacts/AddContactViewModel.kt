package com.tang.prm.feature.people.contacts

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CustomCategories
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.usecase.CreateContactUseCase
import com.tang.prm.domain.usecase.GetContactForEditUseCase
import com.tang.prm.domain.usecase.ObserveContactFormReferenceDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.tang.prm.domain.util.DateUtils
import com.tang.prm.domain.util.parseListField
import com.tang.prm.domain.util.serializeListField
import javax.inject.Inject

data class AddContactUiState(
    val id: Long = 0,
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val company: String? = null,
    val jobTitle: String? = null,
    val birthday: String? = null,
    val isLunarBirthday: Boolean = false,
    val isLeapMonthBirthday: Boolean = false,
    val knowingDate: String? = null,
    val nickname: String? = null,
    val relationship: String? = null,
    val city: String? = null,
    val address: String? = null,
    val education: String? = null,
    val hobbies: List<String> = emptyList(),
    val habits: List<String> = emptyList(),
    val diets: List<String> = emptyList(),
    val skills: List<String> = emptyList(),
    val notes: String? = null,
    val avatar: String? = null,
    val intimacyScore: Int = 50,
    val groupId: Long? = null,
    val relationships: List<CustomType> = emptyList(),
    val educations: List<CustomType> = emptyList(),
    val hobbyOptions: List<CustomType> = emptyList(),
    val habitOptions: List<CustomType> = emptyList(),
    val dietOptions: List<CustomType> = emptyList(),
    val skillOptions: List<CustomType> = emptyList(),
    val isEditing: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val isSaved: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val createdAt: Long = 0
)

@HiltViewModel
class AddContactViewModel @Inject constructor(
    private val createContactUseCase: CreateContactUseCase,
    private val getContactForEditUseCase: GetContactForEditUseCase,
    private val observeReferenceDataUseCase: ObserveContactFormReferenceDataUseCase,
    private val contactFormHelper: ContactFormHelper,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TAG = "AddContactViewModel"
    }

    private val contactId: Long = savedStateHandle.get<Long>("contactId") ?: 0L

    private val _uiState = MutableStateFlow(AddContactUiState())
    val uiState: StateFlow<AddContactUiState> = _uiState.asStateFlow()

    init {
        loadReferenceData()
        if (contactId != 0L) {
            loadContact(contactId)
        }
    }

    private fun loadReferenceData() {
        viewModelScope.launch {
            observeReferenceDataUseCase.invoke().collect { data ->
                _uiState.update { state ->
                    state.copy(
                        relationships = data.relationships,
                        educations = data.educations,
                        hobbyOptions = data.hobbies,
                        habitOptions = data.habits,
                        dietOptions = data.diets,
                        skillOptions = data.skills
                    )
                }
            }
        }
    }

    private fun loadContact(id: Long) {
        viewModelScope.launch {
            val contact = getContactForEditUseCase(id)
            contact?.let {
                _uiState.update { state ->
                    state.copy(
                        id = it.id,
                        name = it.name,
                        phone = it.phone ?: "",
                        email = it.email ?: "",
                        company = it.company,
                        jobTitle = it.jobTitle,
                        nickname = it.nickname,
                        relationship = it.relationship,
                        city = it.city,
                        address = it.address,
                        education = it.education,
                        hobbies = parseListField(it.hobby),
                        habits = parseListField(it.habit),
                        diets = parseListField(it.diet),
                        skills = parseListField(it.skill),
                        notes = it.notes,
                        avatar = it.avatar,
                        intimacyScore = it.intimacyScore,
                        groupId = it.groupId,
                        birthday = it.birthday?.let { b ->
                            DateUtils.formatDate(b)
                        },
                        knowingDate = it.knowingDate?.let { kd ->
                            DateUtils.formatDate(kd)
                        },
                        isEditing = true,
                        createdAt = it.createdAt,
                        isLunarBirthday = it.isLunarBirthday,
                        isLeapMonthBirthday = it.isLeapMonthBirthday
                    )
                }
            }
        }
    }

    private fun updateField(reducer: (AddContactUiState) -> AddContactUiState) {
        _uiState.update { reducer(it).copy(hasUnsavedChanges = true) }
    }

    fun updateName(value: String) = updateField { it.copy(name = value) }
    fun updatePhone(value: String) = updateField { it.copy(phone = value) }
    fun updateEmail(value: String) = updateField { it.copy(email = value) }
    fun updateCompany(value: String) = updateField { it.copy(company = value.ifBlank { null }) }
    fun updateJobTitle(value: String) = updateField { it.copy(jobTitle = value.ifBlank { null }) }
    fun updateBirthday(value: String?) = updateField { it.copy(birthday = value) }
    fun updateKnowingDate(value: String?) = updateField { it.copy(knowingDate = value) }
    fun updateNickname(value: String) = updateField { it.copy(nickname = value.ifBlank { null }) }
    fun updateRelationship(value: String) = updateField { it.copy(relationship = value.ifBlank { null }) }
    fun updateCity(value: String) = updateField { it.copy(city = value.ifBlank { null }) }
    fun updateAddress(value: String) = updateField { it.copy(address = value.ifBlank { null }) }
    fun updateEducation(value: String) = updateField { it.copy(education = value.ifBlank { null }) }
    fun updateHobbies(values: List<String>) = updateField { it.copy(hobbies = values) }
    fun updateHabits(values: List<String>) = updateField { it.copy(habits = values) }
    fun updateDiets(values: List<String>) = updateField { it.copy(diets = values) }
    fun updateSkills(values: List<String>) = updateField { it.copy(skills = values) }
    fun updateAvatar(value: String) = updateField { it.copy(avatar = value.ifBlank { null }) }
    fun updateNotes(value: String) = updateField { it.copy(notes = value.ifBlank { null }) }
    fun updateIntimacyScore(value: Int) = updateField { it.copy(intimacyScore = value.coerceIn(0, 100)) }

    fun saveContact() {
        val state = _uiState.value
        if (state.name.isBlank() || state.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            try {
                val contact = buildContactFromState(state)

                if (state.isEditing) {
                    createContactUseCase.updateContact(
                        contact = contact,
                        isLunarBirthday = state.isLunarBirthday,
                        isLeapMonthBirthday = state.isLeapMonthBirthday,
                        contactName = state.name,
                        contactAvatar = state.avatar
                    )
                } else {
                    createContactUseCase.createContact(
                        contact = contact,
                        isLunarBirthday = state.isLunarBirthday,
                        contactName = state.name,
                        contactAvatar = state.avatar
                    )
                }

                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                Log.e(TAG, "保存联系人失败", e)
                _uiState.update { it.copy(isSaving = false, saveError = "保存失败：${e.message ?: "未知错误"}") }
            }
        }
    }

    fun clearSaveError() {
        _uiState.update { it.copy(saveError = null) }
    }

    private fun buildContactFromState(state: AddContactUiState): Contact {
        val birthdayLong = state.birthday?.let { DateUtils.parseDateToMillis(it) }
        val knowingDateLong = state.knowingDate?.let { DateUtils.parseDateToMillis(it) }
        return Contact(
            id = if (state.isEditing) state.id else 0,
            name = state.name,
            phone = state.phone.ifBlank { null },
            email = state.email.ifBlank { null },
            city = state.city,
            company = state.company,
            jobTitle = state.jobTitle,
            nickname = state.nickname,
            relationship = state.relationship,
            address = state.address,
            education = state.education,
            hobby = serializeListField(state.hobbies),
            habit = serializeListField(state.habits),
            diet = serializeListField(state.diets),
            skill = serializeListField(state.skills),
            notes = state.notes,
            avatar = state.avatar,
            intimacyScore = state.intimacyScore,
            groupId = state.groupId,
            birthday = birthdayLong,
            isLunarBirthday = state.isLunarBirthday,
            isLeapMonthBirthday = state.isLeapMonthBirthday,
            knowingDate = knowingDateLong,
            createdAt = if (state.isEditing) state.createdAt else System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    fun addCustomType(category: String, name: String, color: String? = null, icon: String? = null) {
        viewModelScope.launch {
            val currentList = when (category) {
                CustomCategories.RELATIONSHIP -> _uiState.value.relationships
                CustomCategories.EDUCATION -> _uiState.value.educations
                CustomCategories.HOBBY -> _uiState.value.hobbyOptions
                CustomCategories.HABIT -> _uiState.value.habitOptions
                CustomCategories.DIET -> _uiState.value.dietOptions
                CustomCategories.SKILL -> _uiState.value.skillOptions
                else -> emptyList()
            }
            contactFormHelper.addCustomType(category, name, currentList.size, color, icon)
        }
    }

    fun deleteCustomType(type: CustomType) {
        viewModelScope.launch {
            contactFormHelper.deleteCustomType(type.id, type.category, type.name)
            when (type.category) {
                CustomCategories.RELATIONSHIP -> {
                    if (_uiState.value.relationship == type.name) {
                        updateField { it.copy(relationship = null) }
                    }
                }
                CustomCategories.EDUCATION -> {
                    if (_uiState.value.education == type.name) {
                        updateField { it.copy(education = null) }
                    }
                }
                CustomCategories.HOBBY, CustomCategories.HABIT,
                CustomCategories.DIET, CustomCategories.SKILL -> {
                    val currentList = when (type.category) {
                        CustomCategories.HOBBY -> _uiState.value.hobbies
                        CustomCategories.HABIT -> _uiState.value.habits
                        CustomCategories.DIET -> _uiState.value.diets
                        else -> _uiState.value.skills
                    }
                    val updated = currentList.filter { it != type.name }
                    if (updated.size != currentList.size) {
                        when (type.category) {
                            CustomCategories.HOBBY -> updateField { it.copy(hobbies = updated) }
                            CustomCategories.HABIT -> updateField { it.copy(habits = updated) }
                            CustomCategories.DIET -> updateField { it.copy(diets = updated) }
                            CustomCategories.SKILL -> updateField { it.copy(skills = updated) }
                        }
                    }
                }
            }
        }
    }

}
