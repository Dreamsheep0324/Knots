package com.tang.prm.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tang.prm.data.local.entity.PersonRelationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonRelationDao {
    /**
     * 观察某联系人的所有人物关系（含 JOIN 联系人表的姓名/头像）。
     *
     * 使用 LEFT JOIN 保证：
     * - targetContactId 为空（外部人物）的记录正常返回
     * - targetContactId 非空但联系人已被删除的记录也返回（contactName/contactAvatar 为 null），
     *   由 Repository 层兜底处理
     */
    @Query("""
        SELECT pr.*,
               c.name AS contactName,
               c.avatar AS contactAvatar
        FROM person_relations pr
        LEFT JOIN contacts c ON pr.targetContactId = c.id
        WHERE pr.ownerContactId = :ownerId
        ORDER BY pr.createdAt ASC
    """)
    fun observeRelationsWithTarget(ownerId: Long): Flow<List<PersonRelationWithTarget>>

    /**
     * 观察所有人物关系（含 JOIN 联系人表的姓名/头像）。
     *
     * 用于图谱功能：将所有 owner→target 关系聚合为图谱边。
     * 与 [observeRelationsWithTarget] 的差异：不按 ownerId 过滤，返回全量。
     */
    @Query("""
        SELECT pr.*,
               c.name AS contactName,
               c.avatar AS contactAvatar
        FROM person_relations pr
        LEFT JOIN contacts c ON pr.targetContactId = c.id
        ORDER BY pr.createdAt ASC
    """)
    fun observeAllWithTarget(): Flow<List<PersonRelationWithTarget>>

    @Query("SELECT * FROM person_relations WHERE id = :id")
    suspend fun getById(id: Long): PersonRelationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PersonRelationEntity): Long

    @Update
    suspend fun update(entity: PersonRelationEntity)

    @Query("DELETE FROM person_relations WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM person_relations WHERE ownerContactId = :ownerId")
    suspend fun deleteAllForOwner(ownerId: Long)
}
