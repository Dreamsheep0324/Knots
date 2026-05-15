package com.tang.prm.data.repository

import com.tang.prm.data.local.dao.*
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.util.escapeSqlWildcards
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepositoryImpl @Inject constructor(
    private val contactDao: ContactDao,
    private val anniversaryDao: AnniversaryDao,
    private val giftDao: GiftDao,
    private val thoughtDao: ThoughtDao,
    private val circleDao: CircleDao,
    private val todoDao: TodoDao,
    private val reminderDao: ReminderDao,
    private val database: TangDatabase
) : ContactRepository {
    override fun getAllContacts(): Flow<List<Contact>> =
        contactDao.getAllContacts().map { entities -> entities.map { it.toDomain() } }

    override fun getContactById(id: Long): Flow<Contact?> =
        contactDao.getContactById(id).map { it?.toDomain() }

    override fun searchContacts(keyword: String): Flow<List<Contact>> =
        contactDao.searchContacts(keyword.escapeSqlWildcards()).map { entities -> entities.map { it.toDomain() } }

    override fun getContactsByGroup(groupId: Long): Flow<List<Contact>> =
        contactDao.getContactsByGroup(groupId).map { entities -> entities.map { it.toDomain() } }

    override fun getFilteredContacts(keyword: String?, groupId: Long?, relationship: String?): Flow<List<Contact>> {
        val escapedKeyword = keyword?.escapeSqlWildcards()
        return contactDao.getFilteredContacts(escapedKeyword, groupId, relationship).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTopContactsByIntimacy(limit: Int): Flow<List<Contact>> =
        contactDao.getTopContactsByIntimacy(limit).map { entities -> entities.map { it.toDomain() } }

    override fun getRecentContacts(limit: Int): Flow<List<Contact>> =
        contactDao.getRecentContacts(limit).map { entities -> entities.map { it.toDomain() } }

    override fun getContactCount(): Flow<Int> = contactDao.getContactCount()

    override suspend fun insertContact(contact: Contact): Long =
        contactDao.insertContact(contact.toEntity())

    override suspend fun updateContact(contact: Contact) =
        contactDao.updateContact(contact.toEntity())

    override suspend fun deleteContact(id: Long) = database.withTransaction {
        anniversaryDao.deleteAnniversariesByContact(id)
        giftDao.deleteGiftsByContactId(id)
        thoughtDao.deleteThoughtsByContact(id)
        todoDao.deleteTodosByContact(id)
        reminderDao.deleteRemindersByContact(id)
        contactDao.deleteCrossRefsByContact(id)
        circleDao.deleteMemberRefsByContact(id)
        contactDao.deleteContactById(id)
    }

    override suspend fun updateContactInteraction(id: Long, score: Int, interactionTime: Long) =
        contactDao.updateContactInteraction(id, score, interactionTime)
}
