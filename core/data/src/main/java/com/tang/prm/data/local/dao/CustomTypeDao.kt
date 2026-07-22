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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertType(type: CustomTypeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTypes(types: List<CustomTypeEntity>)

    @Query("DELETE FROM custom_types WHERE id = :id AND isDefault = 0")
    suspend fun deleteTypeById(id: Long)

    @Query("SELECT COUNT(*) FROM custom_types WHERE category = :category")
    suspend fun getTypeCountByCategory(category: String): Int

    /**
     * 按名称 + 分类精确查找预设类型。
     *
     * 用于 ContactRelationRepositoryImpl 解析预设关系类型 ID（如"朋友"），
     * 避免在内存中维护字符串与 ID 的映射。
     */
    @Query("SELECT * FROM custom_types WHERE category = :category AND name = :name LIMIT 1")
    suspend fun getByNameAndCategory(name: String, category: String): CustomTypeEntity?
}
