package com.tang.prm.data.repository

import com.tang.prm.data.local.dao.DivinationRecordDao
import com.tang.prm.data.mapper.DivinationMapper
import com.tang.prm.domain.divination.model.DivinationRecord
import com.tang.prm.domain.divination.repository.DivinationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DivinationRepositoryImpl @Inject constructor(
    private val dao: DivinationRecordDao
) : DivinationRepository {

    override fun getAllRecords(): Flow<List<DivinationRecord>> {
        return dao.getAll().map { entities -> entities.map { DivinationMapper.toDomain(it) } }
    }

    override fun getRecordsByMethod(method: String): Flow<List<DivinationRecord>> {
        return dao.getByMethod(method).map { entities -> entities.map { DivinationMapper.toDomain(it) } }
    }

    override suspend fun saveRecord(record: DivinationRecord): Long {
        return dao.insert(DivinationMapper.toEntity(record))
    }

    override suspend fun deleteRecord(record: DivinationRecord) {
        dao.delete(DivinationMapper.toEntity(record))
    }

    override suspend fun updateAiAnalysis(id: Long, analysis: String) {
        dao.updateAiAnalysis(id, analysis)
    }
}
