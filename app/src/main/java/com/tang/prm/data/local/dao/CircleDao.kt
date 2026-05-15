package com.tang.prm.data.local.dao

import androidx.room.*
import com.tang.prm.data.local.entity.CircleEntity
import com.tang.prm.data.local.entity.CircleMemberCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface CircleDao {
    @Query("SELECT * FROM circles ORDER BY sortOrder ASC")
    fun getAllCircles(): Flow<List<CircleEntity>>

    @Query("SELECT * FROM circles WHERE id = :id")
    fun getCircleById(id: Long): Flow<CircleEntity?>

    @Query("SELECT DISTINCT c.* FROM circles c INNER JOIN circle_member_cross_ref cm ON c.id = cm.circleId WHERE cm.contactId = :contactId ORDER BY c.sortOrder ASC")
    fun getCirclesForContact(contactId: Long): Flow<List<CircleEntity>>

    @Query("SELECT * FROM circles WHERE intimacyThreshold > 0")
    fun getIntimacyCircles(): Flow<List<CircleEntity>>

    @Query("SELECT * FROM circles WHERE parentCircleId = :parentId ORDER BY sortOrder ASC")
    fun getChildCircles(parentId: Long): Flow<List<CircleEntity>>

    @Query("SELECT * FROM circles WHERE parentCircleId IS NULL ORDER BY sortOrder ASC")
    fun getRootCircles(): Flow<List<CircleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCircle(circle: CircleEntity): Long

    @Update
    suspend fun updateCircle(circle: CircleEntity)

    @Query("DELETE FROM circles WHERE id = :id")
    suspend fun deleteCircleById(id: Long)

    @Query("DELETE FROM circles WHERE parentCircleId = :parentId")
    suspend fun deleteChildCircles(parentId: Long)

    @Transaction
    @Query("DELETE FROM circles WHERE id = :id OR parentCircleId = :id")
    suspend fun deleteCircleWithChildren(id: Long)

    @Query("SELECT COUNT(*) FROM circles")
    fun getCircleCount(): Flow<Int>

    @Query("SELECT contactId FROM circle_member_cross_ref WHERE circleId = :circleId")
    fun getMemberIdsForCircle(circleId: Long): Flow<List<Long>>

    @Query("SELECT contactId FROM circle_member_cross_ref WHERE circleId = :circleId")
    suspend fun getMemberIdsForCircleOnce(circleId: Long): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMemberCrossRef(crossRef: CircleMemberCrossRef)

    @Query("DELETE FROM circle_member_cross_ref WHERE circleId = :circleId AND contactId = :contactId")
    suspend fun deleteMemberCrossRef(circleId: Long, contactId: Long)

    @Query("DELETE FROM circle_member_cross_ref WHERE circleId = :circleId")
    suspend fun deleteAllMembersForCircle(circleId: Long)

    @Transaction
    suspend fun updateCircleWithMembers(circle: CircleEntity, memberIds: List<Long>) {
        updateCircle(circle)
        deleteAllMembersForCircle(circle.id)
        memberIds.forEach { contactId ->
            insertMemberCrossRef(CircleMemberCrossRef(circleId = circle.id, contactId = contactId))
        }
    }

    @Transaction
    suspend fun insertCircleWithMembers(circle: CircleEntity, memberIds: List<Long>): Long {
        val id = insertCircle(circle)
        memberIds.forEach { contactId ->
            insertMemberCrossRef(CircleMemberCrossRef(circleId = id, contactId = contactId))
        }
        return id
    }

    @Query("DELETE FROM circle_member_cross_ref WHERE contactId = :contactId")
    suspend fun deleteMemberRefsByContact(contactId: Long)
}
