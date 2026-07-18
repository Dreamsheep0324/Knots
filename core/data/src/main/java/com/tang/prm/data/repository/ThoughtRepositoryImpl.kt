package com.tang.prm.data.repository

import androidx.room.withTransaction
import com.tang.prm.data.local.dao.ThoughtDao
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.mapper.mapList
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.SourceTypes
import com.tang.prm.domain.model.Thought
import com.tang.prm.domain.repository.FavoriteRepository
import com.tang.prm.domain.repository.ThoughtRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThoughtRepositoryImpl @Inject constructor(
    private val thoughtDao: ThoughtDao,
    private val favoriteRepository: FavoriteRepository,
    private val database: TangDatabase
) : ThoughtRepository {

    override fun getAllThoughts(): Flow<List<Thought>> =
        thoughtDao.getAllThoughts().mapList { it.toDomain() }

    override fun getThoughtsByContact(contactId: Long): Flow<List<Thought>> =
        thoughtDao.getThoughtsByContact(contactId).mapList { it.toDomain() }

    override fun getTodoThoughts(): Flow<List<Thought>> =
        thoughtDao.getTodoThoughts().mapList { it.toDomain() }

    override suspend fun insertThought(thought: Thought): Long =
        thoughtDao.insertThought(thought.toEntity())

    override suspend fun updateThought(thought: Thought) =
        thoughtDao.updateThought(thought.toEntity())

    override suspend fun deleteThought(id: Long) = database.withTransaction {
        // 清理收藏 + 删除数据库记录在同一事务中，避免孤儿收藏
        favoriteRepository.deleteFavoriteBySource(SourceTypes.THOUGHT, id)
        thoughtDao.deleteThoughtById(id)
    }

    override fun getThoughtCount(): Flow<Int> = thoughtDao.getThoughtCount()
}
