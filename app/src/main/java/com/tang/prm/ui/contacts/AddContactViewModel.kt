package com.tang.prm.ui.contacts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.Anniversary
import com.tang.prm.domain.model.AnniversaryType
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.ContactGroup
import com.tang.prm.domain.model.CustomCategories
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.repository.AnniversaryRepository
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.ContactGroupRepository
import com.tang.prm.domain.repository.CustomTypeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.tang.prm.util.DateUtils
import org.json.JSONArray
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
    val groups: List<ContactGroup> = emptyList(),
    val relationships: List<CustomType> = emptyList(),
    val educations: List<CustomType> = emptyList(),
    val hobbyOptions: List<CustomType> = emptyList(),
    val habitOptions: List<CustomType> = emptyList(),
    val dietOptions: List<CustomType> = emptyList(),
    val skillOptions: List<CustomType> = emptyList(),
    val isEditing: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val isSaved: Boolean = false,
    val createdAt: Long = 0
)

@HiltViewModel
class AddContactViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val groupRepository: ContactGroupRepository,
    private val customTypeRepository: CustomTypeRepository,
    private val anniversaryRepository: AnniversaryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val contactId: Long = savedStateHandle.get<Long>("contactId") ?: 0L

    private val _uiState = MutableStateFlow(AddContactUiState())
    val uiState: StateFlow<AddContactUiState> = _uiState.asStateFlow()

    init {
        loadGroups()
        loadCustomTypes(CustomCategories.RELATIONSHIP) { state, types -> state.copy(relationships = types) }
        loadCustomTypes(CustomCategories.EDUCATION) { state, types -> state.copy(educations = types) }
        loadCustomTypes(CustomCategories.HOBBY) { state, types -> state.copy(hobbyOptions = types) }
        loadCustomTypes(CustomCategories.HABIT) { state, types -> state.copy(habitOptions = types) }
        loadCustomTypes(CustomCategories.DIET) { state, types -> state.copy(dietOptions = types) }
        loadCustomTypes(CustomCategories.SKILL) { state, types -> state.copy(skillOptions = types) }
        if (contactId != 0L) {
            loadContact(contactId)
        }
    }

    private fun loadGroups() {
        viewModelScope.launch {
            groupRepository.getAllGroups().collect { groups ->
                _uiState.update { it.copy(groups = groups) }
            }
        }
    }

    private fun loadCustomTypes(
        category: String,
        reducer: (AddContactUiState, List<CustomType>) -> AddContactUiState
    ) {
        viewModelScope.launch {
            customTypeRepository.getTypesByCategory(category).collect { types ->
                _uiState.update { reducer(it, types) }
            }
        }
    }

    private fun loadContact(id: Long) {
        viewModelScope.launch {
            contactRepository.getContactById(id).collect { contact ->
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
                            isLunarBirthday = it.isLunarBirthday
                        )
                    }
                }
            }
        }
    }

    fun updateName(value: String) {
        _uiState.update { it.copy(name = value, hasUnsavedChanges = true) }
    }

    fun updatePhone(value: String) {
        _uiState.update { it.copy(phone = value, hasUnsavedChanges = true) }
    }

    fun updateEmail(value: String) {
        _uiState.update { it.copy(email = value, hasUnsavedChanges = true) }
    }

    fun updateCompany(value: String) {
        _uiState.update { it.copy(company = value.ifBlank { null }, hasUnsavedChanges = true) }
    }

    fun updateJobTitle(value: String) {
        _uiState.update { it.copy(jobTitle = value.ifBlank { null }, hasUnsavedChanges = true) }
    }

    fun updateBirthday(value: String?) {
        _uiState.update { it.copy(birthday = value, hasUnsavedChanges = true) }
    }

    fun updateIsLunarBirthday(value: Boolean) {
        _uiState.update { it.copy(isLunarBirthday = value, hasUnsavedChanges = true) }
    }

    fun updateKnowingDate(value: String?) {
        _uiState.update { it.copy(knowingDate = value, hasUnsavedChanges = true) }
    }

    fun updateNickname(value: String) {
        _uiState.update { it.copy(nickname = value.ifBlank { null }, hasUnsavedChanges = true) }
    }

    fun updateRelationship(value: String) {
        _uiState.update { it.copy(relationship = value.ifBlank { null }, hasUnsavedChanges = true) }
    }

    fun updateCity(value: String) {
        _uiState.update { it.copy(city = value.ifBlank { null }, hasUnsavedChanges = true) }
    }

    fun updateAddress(value: String) {
        _uiState.update { it.copy(address = value.ifBlank { null }, hasUnsavedChanges = true) }
    }

    fun updateEducation(value: String) {
        _uiState.update { it.copy(education = value.ifBlank { null }, hasUnsavedChanges = true) }
    }

    fun updateHobbies(values: List<String>) {
        _uiState.update { it.copy(hobbies = values, hasUnsavedChanges = true) }
    }

    fun updateHabits(values: List<String>) {
        _uiState.update { it.copy(habits = values, hasUnsavedChanges = true) }
    }

    fun updateDiets(values: List<String>) {
        _uiState.update { it.copy(diets = values, hasUnsavedChanges = true) }
    }

    fun updateSkills(values: List<String>) {
        _uiState.update { it.copy(skills = values, hasUnsavedChanges = true) }
    }

    fun updateAvatar(value: String) {
        _uiState.update { it.copy(avatar = value.ifBlank { null }, hasUnsavedChanges = true) }
    }

    fun updateNotes(value: String) {
        _uiState.update { it.copy(notes = value.ifBlank { null }, hasUnsavedChanges = true) }
    }

    fun updateIntimacyScore(value: Int) {
        _uiState.update { it.copy(intimacyScore = value.coerceIn(0, 100), hasUnsavedChanges = true) }
    }

    fun saveContact() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.name.isBlank()) return@launch

            val birthdayLong = state.birthday?.let {
                DateUtils.parseDateToMillis(it)
            }

            val knowingDateLong = state.knowingDate?.let {
                DateUtils.parseDateToMillis(it)
            }

            val contact = Contact(
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
                knowingDate = knowingDateLong,
                createdAt = if (state.isEditing) state.createdAt else System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            if (state.isEditing) {
                contactRepository.updateContact(contact)
                if (birthdayLong != null) {
                    syncBirthdayAnniversary(state.id, birthdayLong, state.isLunarBirthday, state.name, state.avatar)
                }
            } else {
                val newId = contactRepository.insertContact(contact)

                if (birthdayLong != null) {
                    anniversaryRepository.insertAnniversary(
                        Anniversary(
                            contactId = newId,
                            name = "生日",
                            type = AnniversaryType.BIRTHDAY,
                            date = birthdayLong,
                            isLunar = state.isLunarBirthday,
                            isRepeat = true,
                            remarks = null,
                            contactName = state.name,
                            contactAvatar = state.avatar
                        )
                    )
                }
            }

            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun addCustomType(category: String, name: String, color: String? = null) {
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
            val newType = CustomType(
                category = category,
                name = name,
                color = color,
                sortOrder = currentList.size
            )
            customTypeRepository.insertType(newType)
        }
    }

    fun deleteCustomType(type: CustomType) {
        viewModelScope.launch {
            customTypeRepository.deleteTypeById(type.id)
        }
    }

    private fun parseListField(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(value)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (e: Exception) {
            value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
    }

    private fun serializeListField(list: List<String>): String? {
        if (list.isEmpty()) return null
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        return arr.toString()
    }

    private suspend fun syncBirthdayAnniversary(contactId: Long, birthdayLong: Long, isLunar: Boolean, contactName: String, contactAvatar: String?) {
        anniversaryRepository.getAnniversariesByContact(contactId).first().filter { it.type == AnniversaryType.BIRTHDAY }.forEach { anniversary ->
            anniversaryRepository.updateAnniversary(
                anniversary.copy(
                    date = birthdayLong,
                    isLunar = isLunar,
                    contactName = contactName,
                    contactAvatar = contactAvatar
                )
            )
        }
    }
}
