package com.tang.prm.data.local.dao

import androidx.room.*
import com.tang.prm.data.local.entity.GiftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GiftDao {
    @Query("SELECT * FROM gifts ORDER BY date DESC")
    fun getAllGifts(): Flow<List<GiftEntity>>

    @Query("SELECT * FROM gifts WHERE id = :id")
    fun getGiftById(id: Long): Flow<GiftEntity?>

    @Query("SELECT * FROM gifts WHERE contactId = :contactId ORDER BY date DESC")
    fun getGiftsByContactId(contactId: Long): Flow<List<GiftEntity>>

    @Query("SELECT * FROM gifts WHERE isSent = :isSent ORDER BY date DESC")
    fun getGiftsBySentType(isSent: Boolean): Flow<List<GiftEntity>>

    @Query("SELECT DISTINCT contactId FROM gifts")
    fun getContactsWithGifts(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGift(gift: GiftEntity): Long

    @Update
    suspend fun updateGift(gift: GiftEntity)

    @Query("DELETE FROM gifts WHERE id = :id")
    suspend fun deleteGiftById(id: Long)

    @Query("DELETE FROM gifts WHERE contactId = :contactId")
    suspend fun deleteGiftsByContactId(contactId: Long)

    @Query("SELECT COUNT(*) FROM gifts")
    fun getGiftCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(json_array_length(photos)), 0) FROM gifts WHERE photos IS NOT NULL")
    fun getGiftPhotoCount(): Flow<Int>
}
