package com.tang.prm.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tang.prm.data.local.entity.ContactRelationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactRelationDao {
    @Query("SELECT * FROM contact_relations")
    fun observeAllRelations(): Flow<List<ContactRelationEntity>>

    @Query("SELECT * FROM contact_relations WHERE contactIdA = :contactId OR contactIdB = :contactId")
    fun observeRelationsForContact(contactId: Long): Flow<List<ContactRelationEntity>>

    @Query("""
        SELECT * FROM contact_relations
        WHERE (contactIdA = :a AND contactIdB = :b)
           OR (contactIdA = :b AND contactIdB = :a)
        LIMIT 1
    """)
    suspend fun findRelation(a: Long, b: Long): ContactRelationEntity?

    @Query("SELECT * FROM contact_relations WHERE id = :id")
    suspend fun getById(id: Long): ContactRelationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ContactRelationEntity): Long

    @Update
    suspend fun update(entity: ContactRelationEntity)

    @Delete
    suspend fun delete(entity: ContactRelationEntity)

    @Query("DELETE FROM contact_relations WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM contact_relations")
    fun getRelationCount(): Flow<Int>
}
