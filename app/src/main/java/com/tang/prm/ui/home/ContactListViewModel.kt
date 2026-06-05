package com.tang.prm.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.Circle
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.usecase.CircleSortMode
import com.tang.prm.domain.usecase.ContactListManageUseCase
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
    private val useCase: ContactListManageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactListUiState())
    val uiState: StateFlow<ContactListUiState> = _uiState.asStateFlow()

    private val searchManager = SearchStateManager()
    val searchState: StateFlow<SearchState> = searchManager.state

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(data = it.data.copy(isLoading = true)) }
            useCase.getContactListAggregate().collect { aggregate ->
                val hologramCircles = aggregate.circles.map { cwm ->
                    HologramCircle(circle = cwm.circle, members = cwm.members)
                }
                _uiState.update { state ->
                    state.copy(
                        data = state.data.copy(
                            circles = hologramCircles,
                            contacts = aggregate.contacts,
                            isLoading = false
                        )
                    )
                }
            }
        }
    }

    fun toggleCircleExpand(circleId: Long) {
        _uiState.update { state ->
            val newExpandedId = if (state.expandedCircleId == circleId) null else circleId
            state.copy(
                expandedCircleId = newExpandedId,
                flippedCardId = null,
                data = state.data.copy(
                    circles = state.data.circles.map { hc ->
                        hc.copy(
                            isExpanded = hc.circle.id == newExpandedId,
                            isFlipped = false,
                            selectedMemberId = null
                        )
                    }
                )
            )
        }
    }

    fun toggleCardFlip(contactId: Long) {
        _uiState.update { state ->
            val newFlippedId = if (state.flippedCardId == contactId) null else contactId
            state.copy(
                flippedCardId = newFlippedId,
                data = state.data.copy(
                    circles = state.data.circles.map { hc ->
                        hc.copy(isFlipped = hc.selectedMemberId == newFlippedId)
                    }
                )
            )
        }
    }

    fun selectMember(circleId: Long, contactId: Long?) {
        _uiState.update { state ->
            state.copy(
                flippedCardId = null,
                data = state.data.copy(
                    circles = state.data.circles.map { hc ->
                        if (hc.circle.id == circleId) {
                            hc.copy(selectedMemberId = contactId, isFlipped = false)
                        } else hc
                    }
                )
            )
        }
    }

    fun toggleSearch() = searchManager.toggleSearch()

    fun onSearchQueryChange(query: String) = searchManager.onQueryChange(query)

    fun showCreateDialog() {
        _uiState.update { it.copy(dialog = it.dialog.copy(showCreate = true)) }
    }

    fun hideCreateDialog() {
        _uiState.update { it.copy(dialog = ContactListDialogState()) }
    }

    fun showEditDialog(circle: Circle) {
        _uiState.update { it.copy(dialog = it.dialog.copy(showEdit = circle)) }
    }

    fun hideEditDialog() {
        _uiState.update { it.copy(dialog = ContactListDialogState()) }
    }

    fun showAddMemberDialog(circleId: Long) {
        _uiState.update { it.copy(dialog = it.dialog.copy(showAddMember = circleId)) }
    }

    fun hideAddMemberDialog() {
        _uiState.update { it.copy(dialog = ContactListDialogState()) }
    }

    fun showDeleteConfirm(circleId: Long) {
        _uiState.update { it.copy(dialog = it.dialog.copy(showDeleteConfirm = circleId)) }
    }

    fun hideDeleteConfirm() {
        _uiState.update { it.copy(dialog = ContactListDialogState()) }
    }

    fun createCircle(name: String, description: String?, color: String, waveform: String) {
        viewModelScope.launch {
            useCase.createCircle(name, description, color, waveform)
            hideCreateDialog()
        }
    }

    fun updateCircle(circle: Circle, name: String, description: String?, color: String, waveform: String) {
        viewModelScope.launch {
            useCase.updateCircle(circle, name, description, color, waveform)
            hideEditDialog()
        }
    }

    fun deleteCircle(circleId: Long) {
        viewModelScope.launch {
            useCase.deleteCircle(circleId)
            hideDeleteConfirm()
            if (_uiState.value.expandedCircleId == circleId) {
                _uiState.update { it.copy(expandedCircleId = null) }
            }
        }
    }

    fun addMemberToCircle(circleId: Long, contactId: Long) {
        viewModelScope.launch {
            val hc = _uiState.value.data.circles.find { it.circle.id == circleId } ?: return@launch
            useCase.addMemberToCircle(hc.circle, contactId)
            hideAddMemberDialog()
        }
    }

    fun removeMemberFromCircle(circleId: Long, contactId: Long) {
        viewModelScope.launch {
            val hc = _uiState.value.data.circles.find { it.circle.id == circleId } ?: return@launch
            useCase.removeMemberFromCircle(hc.circle, contactId)
        }
    }

    fun getAvailableContacts(circleId: Long): List<Contact> {
        val hc = _uiState.value.data.circles.find { it.circle.id == circleId } ?: return _uiState.value.data.contacts
        return useCase.getAvailableContacts(_uiState.value.data.contacts, hc.circle)
    }

    fun getFilteredContacts(): List<Contact> {
        val query = searchManager.state.value.query
        return useCase.getFilteredContacts(_uiState.value.data.contacts, query)
    }

    fun toggleSortMode() {
        _uiState.update { state ->
            val newMode = when (state.data.sortMode) {
                CircleSortMode.DEFAULT -> CircleSortMode.MEMBER_COUNT_DESC
                CircleSortMode.MEMBER_COUNT_DESC -> CircleSortMode.MEMBER_COUNT_ASC
                CircleSortMode.MEMBER_COUNT_ASC -> CircleSortMode.INTIMACY_DESC
                CircleSortMode.INTIMACY_DESC -> CircleSortMode.INTIMACY_ASC
                CircleSortMode.INTIMACY_ASC -> CircleSortMode.DEFAULT
            }
            state.copy(data = state.data.copy(sortMode = newMode))
        }
    }

    fun getSortedCircles(circles: List<HologramCircle>): List<HologramCircle> {
        val sorted = useCase.getSortedCircles(
            circles.map { com.tang.prm.domain.usecase.CircleWithMembers(it.circle, it.members) },
            _uiState.value.data.sortMode
        )
        val sortIndex = sorted.mapIndexed { index, cwm -> cwm.circle.id to index }.toMap()
        return circles.sortedBy { sortIndex[it.circle.id] ?: Int.MAX_VALUE }
    }

    companion object {
        val PresetColors = ContactListManageUseCase.PresetColors
        val WaveformTypes = ContactListManageUseCase.WaveformTypes
    }
}
