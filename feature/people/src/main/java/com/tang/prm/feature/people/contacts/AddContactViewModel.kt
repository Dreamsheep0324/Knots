package com.tang.prm.feature.people.contacts

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CustomCategories
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.PersonRelation
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.PersonRelationRepository
import com.tang.prm.domain.usecase.CreateContactUseCase
import com.tang.prm.domain.usecase.ObserveContactFormReferenceDataUseCase
import com.tang.prm.feature.people.contacts.components.PersonRelationDraft
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
    val personRelationTypes: List<CustomType> = emptyList(),
    val personRelations: List<PersonRelation> = emptyList(),
    val availableContacts: List<Contact> = emptyList(),
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
    private val observeReferenceDataUseCase: ObserveContactFormReferenceDataUseCase,
    private val contactFormHelper: ContactFormHelper,
    private val personRelationRepository: PersonRelationRepository,
    private val contactRepository: ContactRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TAG = "AddContactViewModel"
    }

    private val contactId: Long = savedStateHandle.get<Long>("contactId") ?: 0L

    /**
     * 新建模式下人物关系尚未持久化，使用负数临时 ID 区分；保存主表单后回写真实 ID。
     */
    private var personRelationTempIdSeq = -1L

    private val _uiState = MutableStateFlow(AddContactUiState())
    val uiState: StateFlow<AddContactUiState> = _uiState.asStateFlow()

    init {
        loadReferenceData()
        loadAvailableContacts()
        if (contactId != 0L) {
            loadContact(contactId)
            observePersonRelations(contactId)
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
                        skillOptions = data.skills,
                        personRelationTypes = data.personRelationTypes
                    )
                }
            }
        }
    }

    private fun loadAvailableContacts() {
        viewModelScope.launch {
            contactRepository.getAllContacts().collect { contacts ->
                _uiState.update { state ->
                    state.copy(
                        availableContacts = if (state.isEditing) {
                            contacts.filterNot { it.id == state.id }
                        } else {
                            contacts
                        }
                    )
                }
            }
        }
    }

    private fun observePersonRelations(ownerId: Long) {
        viewModelScope.launch {
            personRelationRepository.observeRelations(ownerId).collect { relations ->
                _uiState.update { it.copy(personRelations = relations) }
            }
        }
    }

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
                        availableContacts = state.availableContacts.filterNot { c -> c.id == it.id }
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

    /**
     * 添加人物关系。
     * - 编辑模式：立即写入数据库，由 [observePersonRelations] 流刷新 UI
     * - 新建模式：暂存到内存（使用负数 ID），保存主表单时批量持久化
     */
    fun addPersonRelation(draft: PersonRelationDraft) {
        val now = System.currentTimeMillis()
        val relation = PersonRelation(
            id = 0L,
            ownerContactId = contactId,
            targetContactId = if (draft.isExternal) null else draft.selectedContact?.id,
            targetName = if (draft.isExternal) {
                draft.externalName
            } else {
                draft.selectedContact?.name
            },
            targetAvatar = if (draft.isExternal) {
                draft.externalAvatar
            } else {
                draft.selectedContact?.avatar
            },
            relationTypeId = draft.selectedTypeId,
            customLabel = draft.customLabel,
            note = draft.note,
            createdAt = now,
            updatedAt = now
        )

        if (contactId != 0L) {
            viewModelScope.launch {
                runCatching { personRelationRepository.insert(relation) }
                    .onFailure { Log.e(TAG, "添加人物关系失败", it) }
            }
        } else {
            val tempRelation = relation.copy(id = personRelationTempIdSeq--)
            updateField { it.copy(personRelations = it.personRelations + tempRelation) }
        }
    }

    /**
     * 删除人物关系。
     * - 编辑模式：立即从数据库删除
     * - 新建模式：从内存暂存列表中移除
     */
    fun removePersonRelation(relation: PersonRelation) {
        if (contactId != 0L) {
            viewModelScope.launch {
                runCatching { personRelationRepository.deleteById(relation.id) }
                    .onFailure { Log.e(TAG, "删除人物关系失败", it) }
            }
        } else {
            updateField { it.copy(personRelations = it.personRelations.filterNot { r -> r.id == relation.id }) }
        }
    }

    /**
     * 新建模式下保存主表单成功后，批量持久化内存中暂存的人物关系。
     */
    private suspend fun persistPersonRelations(ownerId: Long) {
        val pending = _uiState.value.personRelations
        if (pending.isEmpty()) return
        val now = System.currentTimeMillis()
        pending.forEach { relation ->
            runCatching {
                personRelationRepository.insert(
                    relation.copy(
                        id = 0L,
                        ownerContactId = ownerId,
                        updatedAt = now
                    )
                )
            }.onFailure { Log.e(TAG, "持久化人物关系失败: ${relation.targetName}", it) }
        }
    }

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
                        contactName = state.name,
                        contactAvatar = state.avatar
                    )
                    _uiState.update { it.copy(isSaving = false, isSaved = true) }
                } else {
                    val newId = createContactUseCase.createContact(
                        contact = contact,
                        contactName = state.name,
                        contactAvatar = state.avatar
                    )
                    // 新建模式下批量持久化人物关系
                    persistPersonRelations(newId)
                    _uiState.update { it.copy(isSaving = false, isSaved = true) }
                }
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
                CustomCategories.PERSON_RELATION -> _uiState.value.personRelationTypes
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
                CustomCategories.PERSON_RELATION -> {
                    // 类型被删除时由 FK SET NULL，关系记录保留，关系词降级到 customLabel 或"其他"
                    // 无需在 UI 状态中处理，observeRelations 流会自动刷新
                }
            }
        }
    }

}
