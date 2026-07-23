package com.tang.prm.domain.usecase

import com.tang.prm.domain.repository.ContactRelationRepository
import javax.inject.Inject

/**
 * 关系编辑用例：添加、删除。
 *
 * 所有写操作通过 [ContactRelationRepository] 完成事务化 upsert/delete。
 *
 * 不变量：
 * - 调用方无需关心 contactIdA/B 顺序，Repository 内部规范化 (a < b)
 * - 同一对联系人最多一条关系，upsert 自动覆盖（保留原 createdAt）
 */
class EditRelationUseCase @Inject constructor(
    private val contactRelationRepository: ContactRelationRepository
) {
    /**
     * 添加或更新一条关系。
     *
     * @param contactIdA 联系人 A
     * @param contactIdB 联系人 B
     * @param relationTypeId 关系类型 ID（关联 custom_types.RELATIONSHIP）
     * @param note 可选备注（如"大学室友"）
     * @return 关系记录 ID
     */
    suspend fun addRelation(
        contactIdA: Long,
        contactIdB: Long,
        relationTypeId: Long,
        note: String?
    ): Long = contactRelationRepository.upsertRelation(
        contactIdA = contactIdA,
        contactIdB = contactIdB,
        relationTypeId = relationTypeId,
        note = note
    )

    /** 按 ID 删除关系。 */
    suspend fun deleteRelation(id: Long) = contactRelationRepository.deleteRelation(id)

    /**
     * 若一对联系人之间存在关系则删除。
     *
     * 用于编辑模式"长按边删除"：UI 只知道两端 contactId，
     * 由 UseCase 查询并删除，避免 ViewModel 直接访问 Repository 的查询接口。
     */
    suspend fun deleteRelationIfExists(contactIdA: Long, contactIdB: Long) {
        contactRelationRepository.findRelation(contactIdA, contactIdB)?.let { relation ->
            contactRelationRepository.deleteRelation(relation.id)
        }
    }
}
