package com.tang.prm.feature.circle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.Circle
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.usecase.CircleManageUseCase
import com.tang.prm.domain.usecase.CircleSortMode
import com.tang.prm.domain.usecase.ContactListAggregationUseCase
import com.tang.prm.ui.common.SearchState
import com.tang.prm.ui.common.SearchStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HologramCircle(
    val circle: Circle,
    val members: List<Contact> = emptyList(),
    val isExpanded: Boolean = false,
    val isFlipped: Boolean = false,
    val selectedMemberId: Long? = null
)

data class ContactListDataState(
    val circles: List<HologramCircle> = emptyList(),
    val contacts: List<Contact> = emptyList(),
    val availableContacts: Map<Long, List<Contact>> = emptyMap(),
    val sortedCircles: List<HologramCircle> = emptyList(),
    val isLoading: Boolean = false,
    val sortMode: CircleSortMode = CircleSortMode.DEFAULT
)

data class ContactListDialogState(
    val showCreate: Boolean = false,
    val showEdit: Circle? = null,
    val showAddMember: Long? = null,
    val showDeleteConfirm: Long? = null
)

data class ContactListUiState(
    val data: ContactListDataState = ContactListDataState(),
    val dialog: ContactListDialogState = ContactListDialogState(),
    val expandedCircleId: Long? = null,
    val flippedCardId: Long? = null
)

@HiltViewModel
class ContactListViewModel @Inject constructor(
    private val aggregationUseCase: ContactListAggregationUseCase,
    private val circleManageUseCase: CircleManageUseCase
) : ViewModel() {

    private val searchManager = SearchStateManager()
    val searchState: StateFlow<SearchState> = searchManager.state

    private val _sortMode = MutableStateFlow(CircleSortMode.DEFAULT)
    private val _dialogState = MutableStateFlow(ContactListDialogState())
    private val _expandedCircleId = MutableStateFlow<Long?>(null)
    private val _flippedCardId = MutableStateFlow<Long?>(null)
    private val _selectedMember = MutableStateFlow<Map<Long, Long>>(emptyMap())

    val uiState: StateFlow<ContactListUiState> = combine(
        aggregationUseCase(),
        searchManager.state,
        _sortMode,
        _dialogState,
        combine(_expandedCircleId, _flippedCardId, _selectedMember) { expanded, flipped, selected ->
            Triple(expanded, flipped, selected)
        }
    ) { aggregate, search, sortMode, dialog, cardState ->
        val (expandedId, flippedId, selectedMembers) = cardState
        val hologramCircles = aggregate.circles.map { cwm ->
            val selectedMemberId = selectedMembers[cwm.circle.id]
            HologramCircle(
                circle = cwm.circle,
                members = cwm.members,
                isExpanded = cwm.circle.id == expandedId,
                isFlipped = selectedMemberId != null && selectedMemberId == flippedId,
                selectedMemberId = selectedMemberId
            )
        }
        val availableContacts = hologramCircles.associate { hc ->
            hc.circle.id to aggregationUseCase.getAvailableContacts(aggregate.contacts, hc.circle)
        }
        val sortedCircles = run {
            val sorted = aggregationUseCase.getSortedCircles(
                hologramCircles.map { com.tang.prm.domain.usecase.CircleWithMembers(it.circle, it.members) },
                sortMode
            )
            val sortIndex = sorted.mapIndexed { index, cwm -> cwm.circle.id to index }.toMap()
            hologramCircles.sortedBy { sortIndex[it.circle.id] ?: Int.MAX_VALUE }
        }
        ContactListUiState(
            data = ContactListDataState(
                circles = hologramCircles,
                contacts = aggregate.contacts,
                availableContacts = availableContacts,
                sortedCircles = sortedCircles,
                isLoading = false,
                sortMode = sortMode
            ),
            dialog = dialog,
            expandedCircleId = expandedId,
            flippedCardId = flippedId
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ContactListUiState(data = ContactListDataState(isLoading = true))
    )

    fun toggleCircleExpand(circleId: Long) {
        val newExpandedId = if (_expandedCircleId.value == circleId) null else circleId
        _expandedCircleId.value = newExpandedId
        _flippedCardId.value = null
        _selectedMember.value = emptyMap()
    }

    fun toggleCardFlip(contactId: Long) {
        _flippedCardId.value = if (_flippedCardId.value == contactId) null else contactId
    }

    fun selectMember(circleId: Long, contactId: Long?) {
        _flippedCardId.value = null
        _selectedMember.update { map ->
            if (contactId == null) {
                map - circleId
            } else {
                map + (circleId to contactId)
            }
        }
    }

    fun toggleSearch() = searchManager.toggleSearch()

    fun onSearchQueryChange(query: String) = searchManager.onQueryChange(query)

    fun showCreateDialog() {
        _dialogState.update { it.copy(showCreate = true) }
    }

    fun hideCreateDialog() {
        _dialogState.value = ContactListDialogState()
    }

    fun showEditDialog(circle: Circle) {
        _dialogState.update { it.copy(showEdit = circle) }
    }

    fun hideEditDialog() {
        _dialogState.value = ContactListDialogState()
    }

    fun showAddMemberDialog(circleId: Long) {
        _dialogState.update { it.copy(showAddMember = circleId) }
    }

    fun hideAddMemberDialog() {
        _dialogState.value = ContactListDialogState()
    }

    fun showDeleteConfirm(circleId: Long) {
        _dialogState.update { it.copy(showDeleteConfirm = circleId) }
    }

    fun hideDeleteConfirm() {
        _dialogState.value = ContactListDialogState()
    }

    fun createCircle(name: String, description: String?, color: String, waveform: String) {
        viewModelScope.launch {
            circleManageUseCase.createCircle(name, description, color, waveform)
            hideCreateDialog()
        }
    }

    fun updateCircle(circle: Circle, name: String, description: String?, color: String, waveform: String) {
        viewModelScope.launch {
            circleManageUseCase.updateCircle(circle, name, description, color, waveform)
            hideEditDialog()
        }
    }

    fun deleteCircle(circleId: Long) {
        viewModelScope.launch {
            circleManageUseCase.deleteCircle(circleId)
            hideDeleteConfirm()
            if (_expandedCircleId.value == circleId) {
                _expandedCircleId.value = null
            }
        }
    }

    fun addMemberToCircle(circleId: Long, contactId: Long) {
        viewModelScope.launch {
            val hc = uiState.value.data.circles.find { it.circle.id == circleId } ?: return@launch
            circleManageUseCase.addMemberToCircle(hc.circle, contactId)
            hideAddMemberDialog()
        }
    }

    fun removeMemberFromCircle(circleId: Long, contactId: Long) {
        viewModelScope.launch {
            val hc = uiState.value.data.circles.find { it.circle.id == circleId } ?: return@launch
            circleManageUseCase.removeMemberFromCircle(hc.circle, contactId)
        }
    }

    fun toggleSortMode() {
        _sortMode.value = when (_sortMode.value) {
            CircleSortMode.DEFAULT -> CircleSortMode.MEMBER_COUNT_DESC
            CircleSortMode.MEMBER_COUNT_DESC -> CircleSortMode.MEMBER_COUNT_ASC
            CircleSortMode.MEMBER_COUNT_ASC -> CircleSortMode.INTIMACY_DESC
            CircleSortMode.INTIMACY_DESC -> CircleSortMode.INTIMACY_ASC
            CircleSortMode.INTIMACY_ASC -> CircleSortMode.DEFAULT
        }
    }

}
