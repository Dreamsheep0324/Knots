package com.tang.prm.data.repository

import android.content.Context
import com.tang.prm.data.local.dao.*
import com.tang.prm.data.local.dao.ContactListItemEntity
import com.tang.prm.data.local.entity.ContactAttributeEntity
import com.tang.prm.data.local.entity.ContactEntity
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toDomainWithAttributes
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.data.mapper.toAttributeEntities
import com.tang.prm.data.mapper.toEntity as anniversaryToEntity
import com.tang.prm.domain.model.Anniversary
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.data.util.ImageFileManager
import com.tang.prm.util.escapeSqlWildcards
import androidx.room.withTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val database: TangDatabase,
    @ApplicationContext private val context: Context
) : ContactRepository {
    override fun getAllContacts(): Flow<List<Contact>> =
        contactDao.getAllContacts().withAttributes(contactAttributeDao.getAttributesForAllContacts())

    override fun getContactListItems(): Flow<List<Contact>> =
        contactDao.getContactListItems().mapList { it.toDomain() }

    override fun getContactById(id: Long): Flow<Contact?> =
        contactDao.getContactById(id).combine(contactAttributeDao.getAttributesForContact(id)) { contact, attrs ->
            contact?.toDomainWithAttributes(attrs)
        }

    override fun getFilteredContacts(keyword: String?, groupId: Long?, relationship: String?): Flow<List<Contact>> {
        val escapedKeyword = keyword?.escapeSqlWildcards()
        return contactDao.getFilteredContacts(escapedKeyword, groupId, relationship).withAttributes(contactAttributeDao.getAttributesForAllContacts())
    }

    override fun getContactCount(): Flow<Int> = contactDao.getContactCount()

    override fun getAllIntimacyScores(): Flow<List<Int>> = contactDao.getAllIntimacyScores()

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
        oldAvatar?.let { ImageFileManager.deleteImage(context, it) }
    }

    override suspend fun deleteContact(id: Long) {
        // REP-A-1/REP-Q-3 修复：移除 giftDao/todoDao/reminderDao 跨聚合注入。
        // - 礼物照片清理由上层 UseCase 协调 GiftRepository.deleteGiftsByContactId 处理
        // - todos/reminders/gifts/anniversaries 由 FK CASCADE 自动删除
        // 此处只负责收集联系人头像 + 删除联系人本身
        val avatarToDelete = database.withTransaction {
            val avatar = contactDao.getContactByIdOnce(id)?.avatar
            contactDao.deleteContactById(id)  // FK CASCADE 自动删除 gifts/todos/reminders/anniversaries
            avatar
        }
        // 事务外删除头像文件（非事务性，失败不影响数据库一致性）
        avatarToDelete?.let { ImageFileManager.deleteImage(context, it) }
    }

    override suspend fun updateContactInteraction(id: Long, score: Int, interactionTime: Long) =
        contactDao.updateContactInteraction(id, score, interactionTime)

    override suspend fun insertContactWithAnniversaries(
        contact: Contact,
        anniversaries: List<Anniversary>
    ): Long = database.withTransaction {
        // A-8 修复：单一事务包裹跨聚合写入，任一失败回滚
        val newId = contactDao.insertContact(contact.toEntity())
        val attributes = contact.copy(id = newId).toAttributeEntities()
        if (attributes.isNotEmpty()) {
            contactAttributeDao.insertAll(attributes)
        }
        anniversaries.forEach { anniversary ->
            // 自动填充新联系人 ID
            anniversaryDao.insertAnniversary(anniversary.copy(contactId = newId).anniversaryToEntity())
        }
        newId
    }

    override suspend fun updateContacts(contacts: List<Contact>) {
        if (contacts.isEmpty()) return
        // P-5 修复：单一事务包裹批量更新，避免 N 次独立事务 + N 次 SQL 往返
        // 注意：批量场景下不收集头像文件（调用方需自行处理头像清理，CleanCustomType 场景不涉及头像）
        database.withTransaction {
            contacts.forEach { contact ->
                contactDao.updateContact(contact.toEntity())
                contactAttributeDao.deleteAllForContact(contact.id)
                val attributes = contact.toAttributeEntities()
                if (attributes.isNotEmpty()) {
                    contactAttributeDao.insertAll(attributes)
                }
            }
        }
    }
}

private fun Flow<List<ContactEntity>>.withAttributes(
    attrsFlow: Flow<List<ContactAttributeEntity>>
): Flow<List<Contact>> = combine(attrsFlow) { contacts, attrs ->
    val attrMap = attrs.groupBy { it.contactId }
    contacts.map { it.toDomainWithAttributes(attrMap[it.id] ?: emptyList()) }
}
