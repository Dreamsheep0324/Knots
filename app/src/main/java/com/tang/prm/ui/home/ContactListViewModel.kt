package com.tang.prm.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.Circle
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CardRarity
import com.tang.prm.domain.model.getCardRarity
import com.tang.prm.domain.repository.CircleRepository
import com.tang.prm.domain.repository.ContactRepository
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

enum class CircleSortMode {
    DEFAULT, MEMBER_COUNT_ASC, MEMBER_COUNT_DESC, INTIMACY_ASC, INTIMACY_DESC
}

data class ContactListUiState(
    val circles: List<HologramCircle> = emptyList(),
    val contacts: List<Contact> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val showCreateDialog: Boolean = false,
    val showEditDialog: Circle? = null,
    val showAddMemberDialog: Long? = null,
    val showDeleteConfirm: Long? = null,
    val expandedCircleId: Long? = null,
    val flippedCardId: Long? = null,
    val sortMode: CircleSortMode = CircleSortMode.DEFAULT
)

@HiltViewModel
class ContactListViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val circleRepository: CircleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactListUiState())
    val uiState: StateFlow<ContactListUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            combine(
                contactRepository.getAllContacts(),
                circleRepository.getAllCircles()
            ) { contacts, circles ->
                contacts to circles
            }.collect { (contacts, circles) ->
                val hologramCircles = circles.map { circle ->
                    val members = contacts.filter { it.id in circle.memberIds }
                    HologramCircle(circle = circle, members = members)
                }
                _uiState.update { state ->
                    state.copy(
                        circles = hologramCircles,
                        contacts = contacts,
                        isLoading = false
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
                circles = state.circles.map { hc ->
                    hc.copy(
                        isExpanded = hc.circle.id == newExpandedId,
                        isFlipped = false,
                        selectedMemberId = null
                    )
                }
            )
        }
    }

    fun toggleCardFlip(contactId: Long) {
        _uiState.update { state ->
            val newFlippedId = if (state.flippedCardId == contactId) null else contactId
            state.copy(
                flippedCardId = newFlippedId,
                circles = state.circles.map { hc ->
                    hc.copy(isFlipped = hc.selectedMemberId == newFlippedId)
                }
            )
        }
    }

    fun selectMember(circleId: Long, contactId: Long?) {
        _uiState.update { state ->
            state.copy(
                flippedCardId = null,
                circles = state.circles.map { hc ->
                    if (hc.circle.id == circleId) {
                        hc.copy(selectedMemberId = contactId, isFlipped = false)
                    } else hc
                }
            )
        }
    }

    fun toggleSearch() {
        _uiState.update { it.copy(isSearchActive = !it.isSearchActive, searchQuery = "") }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun showCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = true) }
    }

    fun hideCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = false) }
    }

    fun showEditDialog(circle: Circle) {
        _uiState.update { it.copy(showEditDialog = circle) }
    }

    fun hideEditDialog() {
        _uiState.update { it.copy(showEditDialog = null) }
    }

    fun showAddMemberDialog(circleId: Long) {
        _uiState.update { it.copy(showAddMemberDialog = circleId) }
    }

    fun hideAddMemberDialog() {
        _uiState.update { it.copy(showAddMemberDialog = null) }
    }

    fun showDeleteConfirm(circleId: Long) {
        _uiState.update { it.copy(showDeleteConfirm = circleId) }
    }

    fun hideDeleteConfirm() {
        _uiState.update { it.copy(showDeleteConfirm = null) }
    }

    fun createCircle(name: String, description: String?, color: String, waveform: String) {
        viewModelScope.launch {
            val newCircle = Circle(name = name, description = description, color = color, waveform = waveform)
            circleRepository.insertCircle(newCircle)
            hideCreateDialog()
        }
    }

    fun updateCircle(circle: Circle, name: String, description: String?, color: String, waveform: String) {
        viewModelScope.launch {
            val updated = circle.copy(name = name, description = description, color = color, waveform = waveform, updatedAt = System.currentTimeMillis())
            circleRepository.updateCircle(updated)
            hideEditDialog()
        }
    }

    fun deleteCircle(circleId: Long) {
        viewModelScope.launch {
            circleRepository.deleteCircleWithChildren(circleId)
            hideDeleteConfirm()
            if (_uiState.value.expandedCircleId == circleId) {
                _uiState.update { it.copy(expandedCircleId = null) }
            }
        }
    }

    fun addMemberToCircle(circleId: Long, contactId: Long) {
        viewModelScope.launch {
            val hc = _uiState.value.circles.find { it.circle.id == circleId } ?: return@launch
            if (!hc.circle.memberIds.contains(contactId)) {
                val updated = hc.circle.copy(memberIds = hc.circle.memberIds + contactId, updatedAt = System.currentTimeMillis())
                circleRepository.updateCircle(updated)
            }
            hideAddMemberDialog()
        }
    }

    fun removeMemberFromCircle(circleId: Long, contactId: Long) {
        viewModelScope.launch {
            val hc = _uiState.value.circles.find { it.circle.id == circleId } ?: return@launch
            val updated = hc.circle.copy(memberIds = hc.circle.memberIds - contactId, updatedAt = System.currentTimeMillis())
            circleRepository.updateCircle(updated)
        }
    }

    fun getAvailableContacts(circleId: Long): List<Contact> {
        val hc = _uiState.value.circles.find { it.circle.id == circleId } ?: return _uiState.value.contacts
        return _uiState.value.contacts.filter { it.id !in hc.circle.memberIds }
    }

    fun getFilteredContacts(): List<Contact> {
        val query = _uiState.value.searchQuery
        return if (query.isBlank()) _uiState.value.contacts
        else _uiState.value.contacts.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.phone?.contains(query, ignoreCase = true) == true
        }
    }

    fun toggleSortMode() {
        _uiState.update { state ->
            val newMode = when (state.sortMode) {
                CircleSortMode.DEFAULT -> CircleSortMode.MEMBER_COUNT_DESC
                CircleSortMode.MEMBER_COUNT_DESC -> CircleSortMode.MEMBER_COUNT_ASC
                CircleSortMode.MEMBER_COUNT_ASC -> CircleSortMode.INTIMACY_DESC
                CircleSortMode.INTIMACY_DESC -> CircleSortMode.INTIMACY_ASC
                CircleSortMode.INTIMACY_ASC -> CircleSortMode.DEFAULT
            }
            state.copy(sortMode = newMode)
        }
    }

    fun getSortedCircles(circles: List<HologramCircle>): List<HologramCircle> {
        return when (_uiState.value.sortMode) {
            CircleSortMode.DEFAULT -> circles
            CircleSortMode.MEMBER_COUNT_ASC -> circles.sortedBy { it.members.size }
            CircleSortMode.MEMBER_COUNT_DESC -> circles.sortedByDescending { it.members.size }
            CircleSortMode.INTIMACY_ASC -> circles.sortedBy { it.members.map { m -> m.intimacyScore }.averageOrZero() }
            CircleSortMode.INTIMACY_DESC -> circles.sortedByDescending { it.members.map { m -> m.intimacyScore }.averageOrZero() }
        }
    }

    private fun List<Int>.averageOrZero(): Double {
        return if (isEmpty()) 0.0 else average()
    }

    companion object {
        val PresetColors = listOf(
            "#2196F3" to "科技蓝",
            "#00BCD4" to "青蓝",
            "#3F51B5" to "靛蓝",
            "#673AB7" to "深紫",
            "#9C27B0" to "紫色",
            "#E91E63" to "粉红",
            "#F44336" to "红色",
            "#FF5722" to "橙红",
            "#FF9800" to "橙色",
            "#FFC107" to "琥珀",
            "#8BC34A" to "浅绿",
            "#4CAF50" to "绿色"
        )

        val WaveformTypes = listOf(
            "sine" to "正弦波",
            "cosine" to "余弦波",
            "square" to "方波",
            "sawtooth" to "锯齿波",
            "triangle" to "三角波",
            "pulse" to "脉冲波",
            "noise" to "噪声波",
            "heartbeat" to "心跳波",
            "exponential" to "指数波",
            "damped" to "阻尼波",
            "step" to "阶梯波",
            "compound" to "复合波"
        )
    }
}
