package com.tang.prm.data.repository

import com.tang.prm.data.local.dao.PersonRelationDao
import com.tang.prm.data.mapper.mapList
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.PersonRelation
import com.tang.prm.domain.repository.PersonRelationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 人物关系 Repository 实现。
 *
 * 写路径保证不变量：
 * - [PersonRelation.targetContactId] 与 [PersonRelation.targetName] 互斥（一空一非空），
 *   违反时抛 [IllegalArgumentException]
 * - 主体联系人删除时，FK CASCADE 自动清除所有相关记录
 * - 客体联系人删除时，FK SET NULL 保留记录，下次查询时由 [toDomain] 兜底为"已删除联系人"
 *
 * 读路径：
 * - [observeRelations] 使用 LEFT JOIN contacts 获取联系人姓名/头像，
 *   并合并处理外部人物与已删除联系人两种情况
 */
@Singleton
class PersonRelationRepositoryImpl @Inject constructor(
    private val personRelationDao: PersonRelationDao
) : PersonRelationRepository {

    override fun observeRelations(ownerId: Long): Flow<List<PersonRelation>> =
        personRelationDao.observeRelationsWithTarget(ownerId).mapList { it.toDomain() }

    override fun observeAllRelations(): Flow<List<PersonRelation>> =
        personRelationDao.observeAllWithTarget().mapList { it.toDomain() }

    override suspend fun getById(id: Long): PersonRelation? =
        personRelationDao.getById(id)?.toDomain()

    override suspend fun insert(relation: PersonRelation): Long {
        validateMutex(relation)
        return personRelationDao.insert(relation.toEntity())
    }

    override suspend fun update(relation: PersonRelation) {
        validateMutex(relation)
        personRelationDao.update(relation.toEntity())
    }

    override suspend fun deleteById(id: Long) = personRelationDao.deleteById(id)

    override suspend fun deleteAllForOwner(ownerId: Long) =
        personRelationDao.deleteAllForOwner(ownerId)

    /**
     * 校验目标对象至少有一个非空标识：targetContactId 或 targetName。
     *
     * - 两者都为空：无法确定目标对象，拒绝写入
     * - targetContactId 非空时，targetName 可作为冗余缓存（由 [toEntity] 在落库前清空）
     *
     * 数据库层的互斥约束由 [com.tang.prm.data.mapper.toEntity] 保证：
     * App 联系人（targetContactId != null）的 targetName/targetAvatar 会被清空后写入。
     */
    private fun validateMutex(relation: PersonRelation) {
        if (relation.targetContactId == null && relation.targetName.isNullOrBlank()) {
            throw IllegalArgumentException(
                "targetContactId 与 targetName 至少需要一个非空，" +
                    "当前 targetContactId=${relation.targetContactId}, targetName=${relation.targetName}"
            )
        }
    }
}
