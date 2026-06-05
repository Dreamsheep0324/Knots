package com.tang.prm.domain.repository

import com.tang.prm.domain.model.Thought
import com.tang.prm.domain.model.ThoughtType
import kotlinx.coroutines.flow.Flow

interface ThoughtRepository {
    fun getAllThoughts(): Flow<List<Thought>>
    fun getThoughtsByContact(contactId: Long): Flow<List<Thought>>
    fun getThoughtsByType(type: ThoughtType): Flow<List<Thought>>
    fun getTodoThoughts(): Flow<List<Thought>>
    suspend fun insertThought(thought: Thought): Long
    suspend fun updateThought(thought: Thought)
    suspend fun deleteThought(id: Long)

    fun getThoughtCount(): Flow<Int>
}
