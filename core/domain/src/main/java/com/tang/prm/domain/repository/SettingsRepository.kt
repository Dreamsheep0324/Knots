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

    val aiApiKey: Flow<String>
    suspend fun setAiApiKey(key: String)
    val aiBaseUrl: Flow<String>
    suspend fun setAiBaseUrl(url: String)
    val aiModel: Flow<String>
    suspend fun setAiModel(model: String)

    val aiGender: Flow<String>
    suspend fun setAiGender(gender: String)
    val aiBirthDate: Flow<String>
    suspend fun setAiBirthDate(date: String)
}
