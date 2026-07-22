package com.tang.prm.feature.graph.editing

import com.tang.prm.domain.repository.ContactRelationRepository
import com.tang.prm.domain.usecase.EditRelationUseCase
import com.tang.prm.feature.graph.graph.RelationSheetState
import javax.inject.Inject

/**
 * 关系编辑流程协调器。
 *
 * 职责：
 * 1. 编辑模式下拖拽连线时，校验源/目标合法性并构造 sheet 状态
 * 2. 用户选择关系类型后，调用 [EditRelationUseCase] 持久化
 * 3. 长按边删除时，调用 UseCase 删除关系
 *
 * 设计意图：
 * - 抽离编辑流程的业务规则（如 source≠target、已存在关系预选类型），便于单测覆盖
 * - ViewModel 仅负责状态分发，编辑细节由本类封装
 *
 * 不变量：
 * - 同一对联系人最多一条关系（由 Repository 层 UNIQUE 索引保证），confirmRelation 会覆盖已有关系
 */
class RelationEditor @Inject constructor(
    private val editRelationUseCase: EditRelationUseCase,
    private val contactRelationRepository: ContactRelationRepository
) {

    /**
     * 准备关系类型选择 sheet。
     *
     * 校验规则：
     * - sourceId ≠ targetId（禁止自环）
     * - 若已存在关系，预选其类型 ID（让用户感知到"将覆盖"）
     *
     * @return sheet 状态；source=target 时返回 null（不弹 sheet）
     */
    suspend fun prepareNewEdgeSheet(
        sourceId: Long,
        sourceName: String,
        targetId: Long,
        targetName: String
    ): RelationSheetState.SelectType? {
        if (sourceId == targetId) return null
        val existing = contactRelationRepository.findRelation(sourceId, targetId)
        return RelationSheetState.SelectType(
            sourceId = sourceId,
            targetId = targetId,
            sourceName = sourceName,
            targetName = targetName,
            selectedTypeId = existing?.relationTypeId
        )
    }

    /**
     * 确认添加/更新关系。
     *
     * @param sheet 由 [prepareNewEdgeSheet] 构造的 sheet 状态
     * @param relationTypeId 用户选择的关系类型 ID
     * @param note 可选备注（如"大学室友"）
     * @return 关系记录 ID
     */
    suspend fun confirmRelation(
        sheet: RelationSheetState.SelectType,
        relationTypeId: Long,
        note: String?
    ): Long = editRelationUseCase.addRelation(
        contactIdA = sheet.sourceId,
        contactIdB = sheet.targetId,
        relationTypeId = relationTypeId,
        note = note
    )

    /**
     * 按关系记录 ID 删除（用于长按边删除）。
     */
    suspend fun deleteRelation(id: Long) = editRelationUseCase.deleteRelation(id)
}
