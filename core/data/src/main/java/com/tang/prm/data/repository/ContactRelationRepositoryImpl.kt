package com.tang.prm.data.repository

import com.tang.prm.data.local.dao.ContactRelationDao
import com.tang.prm.data.local.entity.ContactRelationEntity
import com.tang.prm.data.mapper.mapList
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.domain.model.ContactRelation
import com.tang.prm.domain.repository.ContactRelationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 关系图谱 Repository 实现。
 *
 * 写路径保证不变量：
 * - 一对联系人之间最多一条 [ContactRelation]：upsert 前规范化 (a < b)，依靠 UNIQUE 索引兜底
 *
 * 读路径：
 * - observe* 返回 Flow，UI 通过 StateIn 订阅
 * - findRelation 为 suspend 快照查询，供编辑流程使用
 */
@Singleton
class ContactRelationRepositoryImpl @Inject constructor(
    private val contactRelationDao: ContactRelationDao
) : ContactRelationRepository {

    override fun observeAllRelations(): Flow<List<ContactRelation>> =
        contactRelationDao.observeAllRelations().mapList { it.toDomain() }

    override fun observeRelationsForContact(contactId: Long): Flow<List<ContactRelation>> =
        contactRelationDao.observeRelationsForContact(contactId).mapList { it.toDomain() }

    override fun getRelationCount(): Flow<Int> = contactRelationDao.getRelationCount()

    override suspend fun findRelation(contactIdA: Long, contactIdB: Long): ContactRelation? =
        contactRelationDao.findRelation(contactIdA, contactIdB)?.toDomain()

    override suspend fun upsertRelation(
        contactIdA: Long,
        contactIdB: Long,
        relationTypeId: Long,
        note: String?
    ): Long {
        // 规范化：保证 contactIdA < contactIdB，匹配唯一索引
        val (a, b) = if (contactIdA < contactIdB) {
            contactIdA to contactIdB
        } else {
            contactIdB to contactIdA
        }
        val now = System.currentTimeMillis()
        val existing = contactRelationDao.findRelation(a, b)
        val entity = ContactRelationEntity(
            id = existing?.id ?: 0,
            contactIdA = a,
            contactIdB = b,
            relationTypeId = relationTypeId,
            note = note,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now
        )
        return contactRelationDao.insert(entity)
    }

    override suspend fun deleteRelation(id: Long) = contactRelationDao.deleteById(id)
}
