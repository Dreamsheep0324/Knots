package com.tang.prm.feature.people.contacts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.ContactGroup
import com.tang.prm.domain.model.CustomCategories
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.ContactGroupRepository
import com.tang.prm.domain.repository.CustomTypeRepository
import com.tang.prm.domain.usecase.CreateContactUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.tang.prm.domain.util.DateUtils
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
    private val createContactUseCase: CreateContactUseCase,
    private val contactFormHelper: ContactFormHelper,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

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
            combine(
                groupRepository.getAllGroups(),
                combine(
                    customTypeRepository.getTypesByCategory(CustomCategories.RELATIONSHIP),
                    customTypeRepository.getTypesByCategory(CustomCategories.EDUCATION),
                    customTypeRepository.getTypesByCategory(CustomCategories.HOBBY)
                ) { relationships, educations, hobbies ->
                    Triple(relationships, educations, hobbies)
                },
                combine(
                    customTypeRepository.getTypesByCategory(CustomCategories.HABIT),
                    customTypeRepository.getTypesByCategory(CustomCategories.DIET),
                    customTypeRepository.getTypesByCategory(CustomCategories.SKILL)
                ) { habits, diets, skills ->
                    Triple(habits, diets, skills)
                }
            ) { groups, ref1, ref2 ->
                CombinedRefData(groups, ref1.first, ref1.second, ref1.third, ref2.first, ref2.second, ref2.third)
            }.collect { data ->
                _uiState.update { state ->
                    state.copy(
                        groups = data.groups,
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

    private data class CombinedRefData(
        val groups: List<ContactGroup>,
        val relationships: List<CustomType>,
        val educations: List<CustomType>,
        val hobbies: List<CustomType>,
        val habits: List<CustomType>,
        val diets: List<CustomType>,
        val skills: List<CustomType>
    )

    private fun loadContact(id: Long) {
        viewModelScope.launch {
            val contact = contactRepository.getContactById(id).first()
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
                        hobbies = contactFormHelper.parseListField(it.hobby),
                        habits = contactFormHelper.parseListField(it.habit),
                        diets = contactFormHelper.parseListField(it.diet),
                        skills = contactFormHelper.parseListField(it.skill),
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
    fun updateIsLunarBirthday(value: Boolean) = updateField { it.copy(isLunarBirthday = value, isLeapMonthBirthday = if (!value) false else it.isLeapMonthBirthday) }
    fun updateIsLeapMonthBirthday(value: Boolean) = updateField { it.copy(isLeapMonthBirthday = value) }
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
                hobby = contactFormHelper.serializeListField(state.hobbies),
                habit = contactFormHelper.serializeListField(state.habits),
                diet = contactFormHelper.serializeListField(state.diets),
                skill = contactFormHelper.serializeListField(state.skills),
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

            _uiState.update { it.copy(isSaved = true) }
        }
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
