package com.tang.prm.domain.repository

import com.tang.prm.domain.model.Anniversary
import com.tang.prm.domain.model.Contact
import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    fun getAllContacts(): Flow<List<Contact>>
    /** Lightweight query returning only columns needed for list display (avoids notes, customFields, etc.). */
    fun getContactListItems(): Flow<List<Contact>>
    fun getContactById(id: Long): Flow<Contact?>
    fun getFilteredContacts(keyword: String?, groupId: Long?, relationship: String?): Flow<List<Contact>>
    fun getContactCount(): Flow<Int>
    fun getAllIntimacyScores(): Flow<List<Int>>
    suspend fun insertContact(contact: Contact): Long
    suspend fun updateContact(contact: Contact)
    suspend fun deleteContact(id: Long)
    suspend fun updateContactInteraction(id: Long, score: Int, interactionTime: Long)

    /**
     * A-8 修复：跨 Contact/Anniversary 聚合的事务化写入。
     *
     * 在单一 Room 事务中插入联系人及其关联纪念日，任一失败回滚，
     * 避免出现"联系人已落库但生日纪念日丢失"的半残状态。
     *
     * @param contact 待插入的联系人（id 通常为 0，由 DB 自增）
     * @param anniversaries 关联纪念日列表（contactId 会被自动填充为新联系人 ID）
     * @return 新联系人 ID
     */
    suspend fun insertContactWithAnniversaries(contact: Contact, anniversaries: List<Anniversary>): Long

    /**
     * P-5 修复：批量事务化更新联系人。
     *
     * 用于 CleanCustomTypeUseCase 等需要更新 N 个联系人的场景，
     * 替代 N 次独立 updateContact（N 次独立事务 + N 次 SQL 往返）。
     */
    suspend fun updateContacts(contacts: List<Contact>)
}
