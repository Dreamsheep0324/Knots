package com.tang.prm.domain.divination.repository

import com.tang.prm.domain.divination.model.DivinationRecord
import kotlinx.coroutines.flow.Flow

interface DivinationRepository {
    fun getAllRecords(): Flow<List<DivinationRecord>>
    fun getRecordsByMethod(method: String): Flow<List<DivinationRecord>>
    suspend fun saveRecord(record: DivinationRecord): Long
    suspend fun deleteRecord(record: DivinationRecord)
    suspend fun updateAiAnalysis(id: Long, analysis: String)
}
