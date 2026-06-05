package com.tang.prm.data.repository

import com.tang.prm.data.local.dao.DivinationRecordDao
import com.tang.prm.data.mapper.mapList
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.divination.model.DivinationRecord
import com.tang.prm.domain.divination.repository.DivinationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DivinationRepositoryImpl @Inject constructor(
    private val dao: DivinationRecordDao
) : DivinationRepository {

    override fun getAllRecords(): Flow<List<DivinationRecord>> {
        return dao.getAll().mapList { it.toDomain() }
    }

    override fun getRecordsByMethod(method: String): Flow<List<DivinationRecord>> {
        return dao.getByMethod(method).mapList { it.toDomain() }
    }

    override suspend fun saveRecord(record: DivinationRecord): Long {
        return dao.insert(record.toEntity())
    }

    override suspend fun deleteRecord(record: DivinationRecord) {
        dao.delete(record.toEntity())
    }

    override suspend fun updateAiAnalysis(id: Long, analysis: String) {
        dao.updateAiAnalysis(id, analysis)
    }
}
