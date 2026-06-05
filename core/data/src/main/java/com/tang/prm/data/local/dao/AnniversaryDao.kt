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

    /** 未过期的纪念日候选集：重复型（需农历计算下次日期）+ 一次性但日期 >= today */
    @Transaction
    @Query("SELECT * FROM anniversaries WHERE isRepeat = 1 OR date >= :todayStart ORDER BY date ASC")
    fun getUpcomingCandidatesWithContact(todayStart: Long): Flow<List<AnniversaryWithContact>>

    /** 已过期的纪念日候选集：重复型（需农历计算确认已过）+ 一次性且日期 < today */
    @Transaction
    @Query("SELECT * FROM anniversaries WHERE isRepeat = 1 OR date < :todayStart ORDER BY date DESC")
    fun getPastCandidatesWithContact(todayStart: Long): Flow<List<AnniversaryWithContact>>

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
