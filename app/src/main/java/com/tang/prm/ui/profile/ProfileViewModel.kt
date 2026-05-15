package com.tang.prm.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val userName: String = "用户",
    val userSignature: String = "用心管理每一段关系"
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = combine(
        settingsRepository.userName,
        settingsRepository.userSignature
    ) { name, signature ->
        ProfileUiState(userName = name, userSignature = signature)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())

    fun updateProfile(name: String, signature: String) {
        viewModelScope.launch {
            settingsRepository.setUserName(name)
            settingsRepository.setUserSignature(signature)
        }
    }
}
