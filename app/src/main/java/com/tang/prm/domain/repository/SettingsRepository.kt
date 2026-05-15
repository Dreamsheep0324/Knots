package com.tang.prm.domain.repository

import com.tang.prm.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeMode: Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
    val userName: Flow<String>
    suspend fun setUserName(name: String)
    val userSignature: Flow<String>
    suspend fun setUserSignature(signature: String)
}
