package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.ContactRelation
import com.tang.prm.domain.model.CustomCategories
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.PersonRelation
import com.tang.prm.domain.repository.ContactRelationRepository
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.CustomTypeRepository
import com.tang.prm.domain.repository.PersonRelationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

/**
 * 图谱视图所需的数据聚合。
 *
 * - [contacts]：全部联系人，作为节点候选
 * - [relations]：联系人之间的双向对称关系（ContactRelation），作为边
 * - [personRelations]：人物社交关系（PersonRelation，单向 owner→target），作为补充边
 * - [relationTypes]：RELATIONSHIP + PERSON_RELATION 两个类别的自定义类型，用于边的颜色/标签
 * - [eventTypes]：EVENT_TYPE 类别的自定义类型，用于事件节点的图标/颜色解析
 */
data class GraphData(
    val contacts: List<Contact> = emptyList(),
    val relations: List<ContactRelation> = emptyList(),
    val personRelations: List<PersonRelation> = emptyList(),
    val relationTypes: List<CustomType> = emptyList(),
    val eventTypes: List<CustomType> = emptyList()
)

/**
 * 观察图谱所需的所有数据：联系人 + 关系 + 人物关系 + 关系类型。
 *
 * 4 路 combine，任一上游变更都会重新发射聚合结果。
 * ViewModel 订阅此 Flow 并组装为 GraphUiState。
 *
 * 参照 [ObserveEventReferenceDataUseCase] 模式，在 combine 上游加 distinctUntilChanged
 * 避免重复发射（C-5 约定）。
 */
class ObserveGraphDataUseCase @Inject constructor(
    private val contactRepository: ContactRepository,
    private val contactRelationRepository: ContactRelationRepository,
    private val personRelationRepository: PersonRelationRepository,
    private val customTypeRepository: CustomTypeRepository
) {
    operator fun invoke(): Flow<GraphData> = combine(
        contactRepository.getAllContacts().distinctUntilChanged(),
        contactRelationRepository.observeAllRelations().distinctUntilChanged(),
        personRelationRepository.observeAllRelations().distinctUntilChanged(),
        observeAllRelationTypes(),
        customTypeRepository.getTypesByCategory(CustomCategories.EVENT_TYPE).distinctUntilChanged()
    ) { contacts, relations, personRelations, relationTypes, eventTypes ->
        GraphData(
            contacts = contacts,
            relations = relations,
            personRelations = personRelations,
            relationTypes = relationTypes,
            eventTypes = eventTypes
        )
    }

    /**
     * 合并观察 RELATIONSHIP + PERSON_RELATION 两个类别的自定义类型。
     *
     * - RELATIONSHIP：ContactRelation 的关系类型（用于"我→联系人"虚拟边和 ContactRelation 边的着色）
     * - PERSON_RELATION：PersonRelation 的关系类型（用于人物关系边的着色）
     *
     * 合并后去重，避免同 id 类型在图谱筛选条中出现两次。
     */
    private fun observeAllRelationTypes(): Flow<List<CustomType>> = combine(
        customTypeRepository.getTypesByCategory(CustomCategories.RELATIONSHIP).distinctUntilChanged(),
        customTypeRepository.getTypesByCategory(CustomCategories.PERSON_RELATION).distinctUntilChanged()
    ) { relationshipTypes, personRelationTypes ->
        (relationshipTypes + personRelationTypes).distinctBy { it.id }
    }
}
