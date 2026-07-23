package com.tang.prm.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tang.prm.data.local.entity.ContactAttributeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactAttributeDao {
    @Query("SELECT * FROM contact_attributes ORDER BY contactId, category, id")
    fun getAttributesForAllContacts(): Flow<List<ContactAttributeEntity>>

    @Query("SELECT * FROM contact_attributes WHERE contactId = :contactId ORDER BY category, id")
    fun getAttributesForContact(contactId: Long): Flow<List<ContactAttributeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(attributes: List<ContactAttributeEntity>)

    @Query("DELETE FROM contact_attributes WHERE contactId = :contactId")
    suspend fun deleteAllForContact(contactId: Long)
}
