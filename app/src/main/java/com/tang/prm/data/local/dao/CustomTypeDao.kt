package com.tang.prm.data.local.dao

import androidx.room.*
import com.tang.prm.data.local.entity.CustomTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomTypeDao {
    @Query("SELECT * FROM custom_types WHERE category = :category ORDER BY sortOrder ASC")
    fun getTypesByCategory(category: String): Flow<List<CustomTypeEntity>>

    @Query("SELECT * FROM custom_types ORDER BY category, sortOrder ASC")
    fun getAllTypes(): Flow<List<CustomTypeEntity>>

    @Query("SELECT * FROM custom_types WHERE id = :id")
    fun getTypeById(id: Long): Flow<CustomTypeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertType(type: CustomTypeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTypes(types: List<CustomTypeEntity>)

    @Update
    suspend fun updateType(type: CustomTypeEntity)

    @Query("DELETE FROM custom_types WHERE id = :id AND isDefault = 0")
    suspend fun deleteTypeById(id: Long)

    @Query("SELECT COUNT(*) FROM custom_types WHERE category = :category")
    suspend fun getTypeCountByCategory(category: String): Int
}
