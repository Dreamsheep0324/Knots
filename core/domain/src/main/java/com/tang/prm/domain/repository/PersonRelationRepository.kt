package com.tang.prm.domain.repository

import com.tang.prm.domain.model.PersonRelation
import kotlinx.coroutines.flow.Flow

/**
 * 人物关系仓库：管理"某联系人与其他人之间的关系"。
 *
 * 与 [ContactRelationRepository] 的差异：
 * - 本仓库面向人物详情/编辑界面，支持外部人物（仅姓名）
 * - [ContactRelationRepository] 面向关系图谱，要求双方均为 App 内联系人
 *
 * 写路径保证不变量：
 * - [targetContactId] 与 [targetName] 互斥（一空一非空），由实现层校验
 * - 主体联系人删除时，所有相关记录由 FK CASCADE 自动清除
 * - 客体联系人删除时，[targetContactId] 被 FK SET NULL，记录保留并降级为外部人物
 */
interface PersonRelationRepository {
    /** 观察某联系人的所有人物关系，按创建时间升序。 */
    fun observeRelations(ownerId: Long): Flow<List<PersonRelation>>

    /**
     * 观察所有人物关系（全量），按创建时间升序。
     *
     * 用于图谱功能：将所有 owner→target 关系聚合为图谱边。
     * 外部人物（targetContactId 为空）的记录也会返回，由调用方按需过滤。
     */
    fun observeAllRelations(): Flow<List<PersonRelation>>

    suspend fun getById(id: Long): PersonRelation?

    suspend fun insert(relation: PersonRelation): Long

    suspend fun update(relation: PersonRelation)

    suspend fun deleteById(id: Long)

    suspend fun deleteAllForOwner(ownerId: Long)
}
