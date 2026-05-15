package com.tang.prm.data.local.dao

import androidx.room.*
import com.tang.prm.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY createdAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE sourceType = :type ORDER BY createdAt DESC")
    fun getFavoritesByType(type: String): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE sourceType = :type AND sourceId = :sourceId LIMIT 1")
    suspend fun getFavoriteBySource(type: String, sourceId: Long): FavoriteEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE sourceType = :type AND sourceId = :sourceId)")
    fun isFavorite(type: String, sourceId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity): Long

    @Query("DELETE FROM favorites WHERE sourceType = :type AND sourceId = :sourceId")
    suspend fun deleteFavoriteBySource(type: String, sourceId: Long)

    @Query("SELECT COUNT(*) FROM favorites WHERE sourceType = :type")
    fun getCountByType(type: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM favorites")
    fun getTotalCount(): Flow<Int>

    // Source types referenced: see SourceTypes for 'EVENT', 'DIALOG', 'PHOTO'
    @Query("DELETE FROM favorites WHERE sourceId = :eventId AND sourceType IN ('EVENT', 'DIALOG', 'PHOTO')")
    suspend fun deleteEventFavorites(eventId: Long)
}
