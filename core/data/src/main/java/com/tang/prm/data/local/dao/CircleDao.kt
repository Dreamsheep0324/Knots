package com.tang.prm.data.local.dao

import androidx.room.*
import com.tang.prm.data.local.entity.CircleEntity
import com.tang.prm.data.local.entity.CircleMemberCrossRef
import com.tang.prm.data.local.entity.CircleWithMembers
import kotlinx.coroutines.flow.Flow

@Dao
interface CircleDao {
    @Transaction
    @Query("SELECT * FROM circles ORDER BY sortOrder ASC")
    fun getAllCirclesWithMembers(): Flow<List<CircleWithMembers>>

    @Transaction
    @Query("SELECT DISTINCT c.* FROM circles c INNER JOIN circle_member_cross_ref cm ON c.id = cm.circleId WHERE cm.contactId = :contactId ORDER BY c.sortOrder ASC")
    fun getCirclesForContactWithMembers(contactId: Long): Flow<List<CircleWithMembers>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCircle(circle: CircleEntity): Long

    @Update
    suspend fun updateCircle(circle: CircleEntity)

    @Query("DELETE FROM circles WHERE id = :id")
    suspend fun deleteCircleById(id: Long)

    /**
     * 递归收集指定圈及其全部后代（子圈、孙圈、任意深度）的 id。
     * 使用 SQLite 递归 CTE，避免应用层递归查询。
     */
    @Query(
        """
        WITH RECURSIVE descendants(id) AS (
            SELECT id FROM circles WHERE id = :rootId
            UNION ALL
            SELECT c.id FROM circles c
            INNER JOIN descendants d ON c.parentCircleId = d.id
        )
        SELECT id FROM descendants
        """
    )
    suspend fun getDescendantCircleIds(rootId: Long): List<Long>

    @Query("DELETE FROM circles WHERE id IN (:ids)")
    suspend fun deleteCirclesByIds(ids: List<Long>)

    /**
     * 递归删除指定圈及其全部后代（子圈、孙圈、任意深度）。
     * 旧实现仅匹配 `id = :id OR parentCircleId = :id`，只删一层，
     * 孙圈及更深后代因外键 SET_NULL 被孤儿化为根圈。
     * 现使用递归 CTE 收集全部后代 id 后批量删除。
     */
    @Transaction
    suspend fun deleteCircleWithChildren(id: Long) {
        val ids = getDescendantCircleIds(id)
        if (ids.isNotEmpty()) deleteCirclesByIds(ids)
    }

    @Query("SELECT COUNT(*) FROM circles")
    fun getCircleCount(): Flow<Int>

    // 仅测试使用：保留用于 CircleDaoTest 回归验证，生产代码走 updateCircleWithMembers（delete-all + insert）
    @Query("SELECT contactId FROM circle_member_cross_ref WHERE circleId = :circleId")
    fun getMemberIdsForCircle(circleId: Long): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMemberCrossRef(crossRef: CircleMemberCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMemberCrossRefs(crossRefs: List<CircleMemberCrossRef>)

    // 仅测试使用：保留用于 CircleDaoTest 回归验证，生产代码走 deleteAllMembersForCircle + insertMemberCrossRefs
    @Query("DELETE FROM circle_member_cross_ref WHERE circleId = :circleId AND contactId = :contactId")
    suspend fun deleteMemberCrossRef(circleId: Long, contactId: Long)

    @Query("DELETE FROM circle_member_cross_ref WHERE circleId = :circleId")
    suspend fun deleteAllMembersForCircle(circleId: Long)

    @Transaction
    suspend fun updateCircleWithMembers(circle: CircleEntity, memberIds: List<Long>) {
        updateCircle(circle)
        deleteAllMembersForCircle(circle.id)
        insertMemberCrossRefs(memberIds.map { contactId ->
            CircleMemberCrossRef(circleId = circle.id, contactId = contactId)
        })
    }

    @Transaction
    suspend fun insertCircleWithMembers(circle: CircleEntity, memberIds: List<Long>): Long {
        val id = insertCircle(circle)
        // B-10 修复：IGNORE 冲突时返回 -1，若继续插成员会用 -1 作 circleId 导致脏数据
        require(id > 0) { "圈子插入被 IGNORE 忽略（主键冲突），事务回滚" }
        insertMemberCrossRefs(memberIds.map { contactId ->
            CircleMemberCrossRef(circleId = id, contactId = contactId)
        })
        return id
    }
}
