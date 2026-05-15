package com.tang.prm.data.local.dao

import androidx.room.*
import com.tang.prm.data.local.entity.AnniversaryEntity
import com.tang.prm.data.local.entity.AnniversaryWithContact
import kotlinx.coroutines.flow.Flow

@Dao
interface AnniversaryDao {
    @Transaction
    @Query("SELECT * FROM anniversaries ORDER BY date ASC")
    fun getAllAnniversariesWithContact(): Flow<List<AnniversaryWithContact>>

    @Transaction
    @Query("SELECT * FROM anniversaries WHERE id = :id")
    fun getAnniversaryByIdWithContact(id: Long): Flow<AnniversaryWithContact?>

    @Transaction
    @Query("SELECT * FROM anniversaries WHERE contactId = :contactId ORDER BY date ASC")
    fun getAnniversariesByContactWithContact(contactId: Long): Flow<List<AnniversaryWithContact>>

    @Transaction
    @Query("SELECT * FROM anniversaries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getAnniversariesInRangeWithContact(startDate: Long, endDate: Long): Flow<List<AnniversaryWithContact>>

    @Transaction
    @Query("SELECT * FROM anniversaries WHERE type = :type ORDER BY date ASC")
    fun getAnniversariesByTypeWithContact(type: String): Flow<List<AnniversaryWithContact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnniversary(anniversary: AnniversaryEntity): Long

    @Update
    suspend fun updateAnniversary(anniversary: AnniversaryEntity)

    @Query("DELETE FROM anniversaries WHERE id = :id")
    suspend fun deleteAnniversaryById(id: Long)

    @Query("SELECT COUNT(*) FROM anniversaries")
    fun getAnniversaryCount(): Flow<Int>

    @Query("DELETE FROM anniversaries WHERE contactId = :contactId")
    suspend fun deleteAnniversariesByContact(contactId: Long)
}
