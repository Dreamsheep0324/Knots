package com.tang.prm.data.repository

import com.tang.prm.data.local.dao.*
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

    override suspend fun insertContact(contact: Contact): Long {
        val id = contactDao.insertContact(contact.toEntity())
        val attributes = contact.copy(id = id).toAttributeEntities()
        if (attributes.isNotEmpty()) {
            contactAttributeDao.insertAll(attributes)
        }
        return id
    }

    override suspend fun updateContact(contact: Contact) = database.withTransaction {
        // 清理旧头像文件（头像变更时删除旧文件）
        val oldContact = contactDao.getContactByIdOnce(contact.id)
        if (oldContact != null && oldContact.avatar != contact.avatar) {
            oldContact.avatar?.let { oldAvatar ->
                ImageFileManager.deleteImage(oldAvatar)
            }
        }
        contactDao.updateContact(contact.toEntity())
        contactAttributeDao.deleteAllForContact(contact.id)
        val attributes = contact.toAttributeEntities()
        if (attributes.isNotEmpty()) {
            contactAttributeDao.insertAll(attributes)
        }
    }

    override suspend fun deleteContact(id: Long) = database.withTransaction {
        // CASCADE 外键约束自动删除所有关联行，但不会清理图片文件
        // 先收集关联数据的图片路径（仅 GiftEntity 有 photos 字段）
        val photosToDelete = mutableListOf<String>()
        giftDao.getGiftsByContactIdOnce(id).forEach { photosToDelete.addAll(it.photos) }

        // 收集头像路径
        contactDao.getContactByIdOnce(id)?.avatar?.let { photosToDelete.add(it) }

        // 删除联系人 — CASCADE 自动删除所有关联表数据
        contactDao.deleteContactById(id)

        // 清理图片文件
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
