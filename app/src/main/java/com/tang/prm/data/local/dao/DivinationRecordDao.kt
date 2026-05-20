package com.tang.prm.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.tang.prm.data.local.entity.DivinationRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DivinationRecordDao {
    @Query("SELECT * FROM divination_records ORDER BY createdAt DESC")
    fun getAll(): Flow<List<DivinationRecordEntity>>

    @Query("SELECT * FROM divination_records WHERE method = :method ORDER BY createdAt DESC")
    fun getByMethod(method: String): Flow<List<DivinationRecordEntity>>

    @Insert
    suspend fun insert(record: DivinationRecordEntity): Long

    @Delete
    suspend fun delete(record: DivinationRecordEntity)

    @Query("DELETE FROM divination_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE divination_records SET aiAnalysis = :analysis WHERE id = :id")
    suspend fun updateAiAnalysis(id: Long, analysis: String)
}
