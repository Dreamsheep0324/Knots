package com.tang.prm.data.local.dao

import androidx.room.*
import com.tang.prm.data.local.entity.ContactEntity
import com.tang.prm.data.local.entity.ContactGroupEntity
import com.tang.prm.data.local.entity.ContactTagEntity
import com.tang.prm.data.local.entity.ContactTagCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY updatedAt DESC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE id = :id")
    fun getContactById(id: Long): Flow<ContactEntity?>

    @Query("SELECT * FROM contacts WHERE name LIKE '%' || :keyword || '%' ESCAPE '\\' OR nickname LIKE '%' || :keyword || '%' ESCAPE '\\' OR phone LIKE '%' || :keyword || '%' ESCAPE '\\'")
    fun searchContacts(keyword: String): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE groupId = :groupId")
    fun getContactsByGroup(groupId: Long): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE (:keyword IS NULL OR name LIKE '%' || :keyword || '%' ESCAPE '\\' OR nickname LIKE '%' || :keyword || '%' ESCAPE '\\' OR phone LIKE '%' || :keyword || '%' ESCAPE '\\') AND (:groupId IS NULL OR groupId = :groupId) AND (:relationship IS NULL OR relationship = :relationship) ORDER BY intimacyScore DESC")
    fun getFilteredContacts(keyword: String?, groupId: Long?, relationship: String?): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts ORDER BY intimacyScore DESC LIMIT :limit")
    fun getTopContactsByIntimacy(limit: Int): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts ORDER BY lastInteractionTime DESC LIMIT :limit")
    fun getRecentContacts(limit: Int): Flow<List<ContactEntity>>

    @Query("SELECT COUNT(*) FROM contacts")
    fun getContactCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity): Long

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteContactById(id: Long)

    @Query("UPDATE contacts SET intimacyScore = :score, lastInteractionTime = :interactionTime, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateContactInteraction(id: Long, score: Int, interactionTime: Long, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM contacts WHERE id IN (:ids)")
    fun getContactsByIds(ids: List<Long>): Flow<List<ContactEntity>>

    @Query("DELETE FROM contact_tag_cross_ref WHERE contactId = :contactId")
    suspend fun deleteCrossRefsByContact(contactId: Long)
}

@Dao
interface ContactGroupDao {
    @Query("SELECT * FROM contact_groups ORDER BY sortOrder ASC")
    fun getAllGroups(): Flow<List<ContactGroupEntity>>

    @Query("SELECT * FROM contact_groups WHERE id = :id")
    fun getGroupById(id: Long): Flow<ContactGroupEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: ContactGroupEntity): Long

    @Update
    suspend fun updateGroup(group: ContactGroupEntity)

    @Query("DELETE FROM contact_groups WHERE id = :id")
    suspend fun deleteGroupById(id: Long)
}

@Dao
interface ContactTagDao {
    @Query("SELECT * FROM contact_tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<ContactTagEntity>>

    @Query("SELECT * FROM contact_tags WHERE id = :id")
    fun getTagById(id: Long): Flow<ContactTagEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: ContactTagEntity): Long

    @Update
    suspend fun updateTag(tag: ContactTagEntity)

    @Query("DELETE FROM contact_tags WHERE id = :id")
    suspend fun deleteTagById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContactTagCrossRef(crossRef: ContactTagCrossRef)

    @Query("SELECT * FROM contact_tag_cross_ref WHERE contactId = :contactId")
    fun getTagsForContact(contactId: Long): Flow<List<ContactTagCrossRef>>
}
