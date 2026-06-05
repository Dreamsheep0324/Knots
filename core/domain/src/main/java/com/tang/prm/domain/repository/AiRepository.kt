package com.tang.prm.domain.repository

import kotlinx.coroutines.flow.Flow

interface AiRepository {
    fun streamChat(systemPrompt: String, userPrompt: String): Flow<String>
    suspend fun testConnection(): Result<String>
}
