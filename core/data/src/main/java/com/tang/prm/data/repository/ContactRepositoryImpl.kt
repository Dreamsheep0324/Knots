package com.tang.prm.data.repository

import com.tang.prm.data.local.dao.*
import com.tang.prm.data.local.dao.ContactListItemEntity
import com.tang.prm.data.local.entity.ContactAttributeEntity
import com.tang.prm.data.local.entity.ContactEntity
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toDomainWithAttributes
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.data.mapper.toAttributeEntities
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.data.util.ImageFileManager
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
        contactDao.getAllContacts().withAttributes(contactAttributeDao.getAttributesForAllContacts())

    override fun getContactListItems(): Flow<List<Contact>> =
        contactDao.getContactListItems().mapList { it.toDomain() }

    override fun getContactById(id: Long): Flow<Contact?> =
        contactDao.getContactById(id).combine(contactAttributeDao.getAttributesForContact(id)) { contact, attrs ->
            contact?.toDomainWithAttributes(attrs)
        }

    override fun searchContacts(keyword: String): Flow<List<Contact>> =
        contactDao.searchContacts(keyword.escapeSqlWildcards()).withAttributes(contactAttributeDao.getAttributesForAllContacts())

    override fun getContactsByGroup(groupId: Long): Flow<List<Contact>> =
        contactDao.getContactsByGroup(groupId).withAttributes(contactAttributeDao.getAttributesForAllContacts())

    override fun getFilteredContacts(keyword: String?, groupId: Long?, relationship: String?): Flow<List<Contact>> {
        val escapedKeyword = keyword?.escapeSqlWildcards()
        return contactDao.getFilteredContacts(escapedKeyword, groupId, relationship).withAttributes(contactAttributeDao.getAttributesForAllContacts())
    }

    override fun getTopContactsByIntimacy(limit: Int): Flow<List<Contact>> =
        contactDao.getTopContactsByIntimacy(limit).withAttributes(contactAttributeDao.getAttributesForAllContacts())

    override fun getRecentContacts(limit: Int): Flow<List<Contact>> =
        contactDao.getRecentContacts(limit).withAttributes(contactAttributeDao.getAttributesForAllContacts())

    override fun getContactCount(): Flow<Int> = contactDao.getContactCount()

    override suspend fun insertContact(contact: Contact): Long = database.withTransaction {
        val id = contactDao.insertContact(contact.toEntity())
        val attributes = contact.copy(id = id).toAttributeEntities()
        if (attributes.isNotEmpty()) {
            contactAttributeDao.insertAll(attributes)
        }
        id
    }

    override suspend fun updateContact(contact: Contact) {
        // 先在事务内收集待删除的旧头像路径 + 更新数据库
        val oldAvatar = database.withTransaction {
            val oldContact = contactDao.getContactByIdOnce(contact.id)
            contactDao.updateContact(contact.toEntity())
            contactAttributeDao.deleteAllForContact(contact.id)
            val attributes = contact.toAttributeEntities()
            if (attributes.isNotEmpty()) {
                contactAttributeDao.insertAll(attributes)
            }
            oldContact?.avatar?.takeIf { it != contact.avatar }
        }
        // 事务外删除文件
        oldAvatar?.let { ImageFileManager.deleteImage(it) }
    }

    override suspend fun deleteContact(id: Long) {
        // 先在事务内收集待删除文件路径 + 删除数据库记录
        val photosToDelete = database.withTransaction {
            val photos = mutableListOf<String>()
            giftDao.getGiftsByContactIdOnce(id).forEach { photos.addAll(it.photos) }
            contactDao.getContactByIdOnce(id)?.avatar?.let { photos.add(it) }
            contactDao.deleteContactById(id)
            photos
        }
        // 事务外删除文件（非事务性，失败不影响数据库一致性）
        ImageFileManager.deleteLocalPhotos(photosToDelete)
    }

    override suspend fun updateContactInteraction(id: Long, score: Int, interactionTime: Long) =
        contactDao.updateContactInteraction(id, score, interactionTime)
}

private fun Flow<List<ContactEntity>>.withAttributes(
    attrsFlow: Flow<List<ContactAttributeEntity>>
): Flow<List<Contact>> = combine(attrsFlow) { contacts, attrs ->
    val attrMap = attrs.groupBy { it.contactId }
    contacts.map { it.toDomainWithAttributes(attrMap[it.id] ?: emptyList()) }
}
