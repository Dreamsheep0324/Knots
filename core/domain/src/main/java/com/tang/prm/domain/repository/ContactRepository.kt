package com.tang.prm.domain.repository

import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.ContactGroup
import com.tang.prm.domain.model.ContactTag
import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    fun getAllContacts(): Flow<List<Contact>>
    fun getContactById(id: Long): Flow<Contact?>
    fun searchContacts(keyword: String): Flow<List<Contact>>
    fun getContactsByGroup(groupId: Long): Flow<List<Contact>>
    fun getFilteredContacts(keyword: String?, groupId: Long?, relationship: String?): Flow<List<Contact>>
    fun getTopContactsByIntimacy(limit: Int): Flow<List<Contact>>
    fun getRecentContacts(limit: Int): Flow<List<Contact>>
    fun getContactCount(): Flow<Int>
    suspend fun insertContact(contact: Contact): Long
    suspend fun updateContact(contact: Contact)
    suspend fun deleteContact(id: Long)
    suspend fun updateContactInteraction(id: Long, score: Int, interactionTime: Long)
}

interface ContactGroupRepository {
    fun getAllGroups(): Flow<List<ContactGroup>>
    fun getGroupById(id: Long): Flow<ContactGroup?>
    suspend fun insertGroup(group: ContactGroup): Long
    suspend fun updateGroup(group: ContactGroup)
    suspend fun deleteGroupById(id: Long)
}

interface ContactTagRepository {
    fun getAllTags(): Flow<List<ContactTag>>
    fun getTagById(id: Long): Flow<ContactTag?>
    suspend fun insertTag(tag: ContactTag): Long
    suspend fun updateTag(tag: ContactTag)
    suspend fun deleteTagById(id: Long)
}
