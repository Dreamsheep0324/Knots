package com.tang.prm.feature.divination

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.divination.model.DivinationRecord
import com.tang.prm.domain.divination.repository.DivinationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DivinationHistoryViewModel @Inject constructor(
    private val repository: DivinationRepository
) : ViewModel() {

    val records = repository.getAllRecords().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun deleteRecord(record: DivinationRecord) {
        viewModelScope.launch {
            repository.deleteRecord(record)
        }
    }
}
