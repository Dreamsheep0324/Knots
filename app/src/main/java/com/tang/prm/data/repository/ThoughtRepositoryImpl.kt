package com.tang.prm.data.repository

import com.tang.prm.data.local.dao.ThoughtDao
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.Thought
import com.tang.prm.domain.model.ThoughtType
import com.tang.prm.domain.repository.ThoughtRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThoughtRepositoryImpl @Inject constructor(
    private val thoughtDao: ThoughtDao
) : ThoughtRepository {

    override fun getAllThoughts(): Flow<List<Thought>> =
        thoughtDao.getAllThoughts().map { list -> list.map { it.toDomain() } }

    override fun getThoughtsByContact(contactId: Long): Flow<List<Thought>> =
        thoughtDao.getThoughtsByContact(contactId).map { list -> list.map { it.toDomain() } }

    override fun getThoughtsByType(type: ThoughtType): Flow<List<Thought>> =
        thoughtDao.getThoughtsByType(type.key).map { list -> list.map { it.toDomain() } }

    override fun getTodoThoughts(): Flow<List<Thought>> =
        thoughtDao.getTodoThoughts().map { list -> list.map { it.toDomain() } }

    override suspend fun insertThought(thought: Thought): Long =
        thoughtDao.insertThought(thought.toEntity())

    override suspend fun updateThought(thought: Thought) =
        thoughtDao.updateThought(thought.toEntity())

    override suspend fun deleteThought(id: Long) =
        thoughtDao.deleteThoughtById(id)

    override fun getThoughtCount(): Flow<Int> = thoughtDao.getThoughtCount()
}
