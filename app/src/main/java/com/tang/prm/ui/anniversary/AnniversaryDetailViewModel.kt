package com.tang.prm.ui.anniversary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.Anniversary
import com.tang.prm.domain.repository.AnniversaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnniversaryDetailUiState(
    val anniversary: Anniversary? = null,
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnniversaryDetailViewModel @Inject constructor(
    private val anniversaryRepository: AnniversaryRepository
) : ViewModel() {

    private val _anniversaryId = MutableStateFlow(0L)

    val uiState: StateFlow<AnniversaryDetailUiState> = _anniversaryId.flatMapLatest { id ->
        if (id == 0L) return@flatMapLatest flowOf(AnniversaryDetailUiState())
        anniversaryRepository.getAnniversaryById(id).map { anniversary ->
            AnniversaryDetailUiState(anniversary = anniversary, isLoading = false)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnniversaryDetailUiState())

    fun loadAnniversary(id: Long) {
        _anniversaryId.value = id
    }

    fun deleteAnniversary() {
        viewModelScope.launch {
            uiState.value.anniversary?.let { anniversary ->
                anniversaryRepository.deleteAnniversary(anniversary.id)
            }
        }
    }
}
