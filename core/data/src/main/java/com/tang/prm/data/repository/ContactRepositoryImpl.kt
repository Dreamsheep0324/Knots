package com.tang.prm.data.repository

import com.tang.prm.data.local.dao.*
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toDomainWithAttributes
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.data.mapper.toAttributeEntities
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.util.escapeSqlWildcards
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import com.tang.prm.data.mapper.mapList
import com.tang.prm.data.mapper.mapNullable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepositoryImpl @Inject constructor(
    private val contactDao: ContactDao,
    private val contactAttributeDao: ContactAttributeDao,
    private val anniversaryDao: AnniversaryDao,
    private val giftDao: GiftDao,
    private val thoughtDao: ThoughtDao,
    private val circleDao: CircleDao,
    private val todoDao: TodoDao,
    private val reminderDao: ReminderDao,
    private val database: TangDatabase
) : ContactRepository {
    override fun getAllContacts(): Flow<List<Contact>> =
        contactDao.getAllContacts().combine(contactAttributeDao.getAttributesForAllContacts()) { contacts, attrs ->
            val attrMap = attrs.groupBy { it.contactId }
            contacts.map { it.toDomainWithAttributes(attrMap[it.id] ?: emptyList()) }
        }

    override fun getContactById(id: Long): Flow<Contact?> =
        contactDao.getContactById(id).combine(contactAttributeDao.getAttributesForContact(id)) { contact, attrs ->
            contact?.toDomainWithAttributes(attrs)
        }

    override fun searchContacts(keyword: String): Flow<List<Contact>> =
        contactDao.searchContacts(keyword.escapeSqlWildcards()).combine(contactAttributeDao.getAttributesForAllContacts()) { contacts, attrs ->
            val attrMap = attrs.groupBy { it.contactId }
            contacts.map { it.toDomainWithAttributes(attrMap[it.id] ?: emptyList()) }
        }

    override fun getContactsByGroup(groupId: Long): Flow<List<Contact>> =
        contactDao.getContactsByGroup(groupId).combine(contactAttributeDao.getAttributesForAllContacts()) { contacts, attrs ->
            val attrMap = attrs.groupBy { it.contactId }
            contacts.map { it.toDomainWithAttributes(attrMap[it.id] ?: emptyList()) }
        }

    override fun getFilteredContacts(keyword: String?, groupId: Long?, relationship: String?): Flow<List<Contact>> {
        val escapedKeyword = keyword?.escapeSqlWildcards()
        return contactDao.getFilteredContacts(escapedKeyword, groupId, relationship).combine(contactAttributeDao.getAttributesForAllContacts()) { contacts, attrs ->
            val attrMap = attrs.groupBy { it.contactId }
            contacts.map { it.toDomainWithAttributes(attrMap[it.id] ?: emptyList()) }
        }
    }

    override fun getTopContactsByIntimacy(limit: Int): Flow<List<Contact>> =
        contactDao.getTopContactsByIntimacy(limit).combine(contactAttributeDao.getAttributesForAllContacts()) { contacts, attrs ->
            val attrMap = attrs.groupBy { it.contactId }
            contacts.map { it.toDomainWithAttributes(attrMap[it.id] ?: emptyList()) }
        }

    override fun getRecentContacts(limit: Int): Flow<List<Contact>> =
        contactDao.getRecentContacts(limit).combine(contactAttributeDao.getAttributesForAllContacts()) { contacts, attrs ->
            val attrMap = attrs.groupBy { it.contactId }
            contacts.map { it.toDomainWithAttributes(attrMap[it.id] ?: emptyList()) }
        }

    override fun getContactCount(): Flow<Int> = contactDao.getContactCount()

    override suspend fun insertContact(contact: Contact): Long {
        val id = contactDao.insertContact(contact.toEntity())
        val attributes = contact.copy(id = id).toAttributeEntities()
        if (attributes.isNotEmpty()) {
            contactAttributeDao.insertAll(attributes)
        }
        return id
    }

    override suspend fun updateContact(contact: Contact) = database.withTransaction {
        contactDao.updateContact(contact.toEntity())
        contactAttributeDao.deleteAllForContact(contact.id)
        val attributes = contact.toAttributeEntities()
        if (attributes.isNotEmpty()) {
            contactAttributeDao.insertAll(attributes)
        }
    }

    override suspend fun deleteContact(id: Long) = database.withTransaction {
        anniversaryDao.deleteAnniversariesByContact(id)
        giftDao.deleteGiftsByContactId(id)
        thoughtDao.deleteThoughtsByContact(id)
        todoDao.deleteTodosByContact(id)
        reminderDao.deleteRemindersByContact(id)
        contactDao.deleteCrossRefsByContact(id)
        circleDao.deleteMemberRefsByContact(id)
        contactAttributeDao.deleteAllForContact(id)
        contactDao.deleteContactById(id)
    }

    override suspend fun updateContactInteraction(id: Long, score: Int, interactionTime: Long) =
        contactDao.updateContactInteraction(id, score, interactionTime)
}
