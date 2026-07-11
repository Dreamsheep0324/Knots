package com.tang.prm.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.UpdateResult
import com.tang.prm.domain.repository.UpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 应用更新检查 ViewModel — 供 TangNavHost（启动时自动检查）和 AboutScreen（手动检查）共用。
 */
@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val updateRepository: UpdateRepository
) : ViewModel() {

    fun checkForUpdate(currentVersion: String, onResult: (UpdateResult) -> Unit) {
        viewModelScope.launch {
            onResult(updateRepository.checkForUpdate(currentVersion))
        }
    }
}
