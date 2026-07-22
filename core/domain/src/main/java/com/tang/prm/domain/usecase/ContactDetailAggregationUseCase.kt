package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.*
import com.tang.prm.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 联系人详情聚合数据
 *
 * 将 ContactDetailVM 中的 12 路 combine 聚合逻辑提取到 Domain 层，
 * 使 VM 依赖从 7 降到 4。
 */
data class ContactDetailAggregateData(
    val contact: Contact? = null,
    val events: List<Event> = emptyList(),
    val anniversaries: List<Anniversary> = emptyList(),
    val conversations: List<Event> = emptyList(),
    val gifts: List<Gift> = emptyList(),
    val thoughts: List<Thought> = emptyList(),
    val favoriteIds: Set<Long> = emptySet(),
    val hobbyOptions: List<CustomType> = emptyList(),
    val habitOptions: List<CustomType> = emptyList(),
    val dietOptions: List<CustomType> = emptyList(),
    val skillOptions: List<CustomType> = emptyList(),
    val eventTypes: List<CustomType> = emptyList(),
    val relationshipTypes: List<CustomType> = emptyList(),
    val personRelations: List<PersonRelation> = emptyList(),
    val personRelationTypes: List<CustomType> = emptyList(),
    val isLoading: Boolean = true
)

/**
 * 联系人详情聚合 UseCase
 *
 * 将 6 个 Repository + 1 个 UseCase 的 12 路 combine 聚合逻辑封装为单一数据源，
 * ContactDetailVM 从 7 依赖降至 4 依赖。
 *
 * A-4/A-5 修复：聚合 UseCase 收敛为纯读路径，删除 updateThought/deleteThought 写操作
 * （下沉到 [ThoughtWriteUseCase]）；FavoriteToggleUseCase 替换为 [ObserveFavoritesUseCase]
 * 只读依赖，符合接口隔离原则。
 */
class ContactDetailAggregationUseCase @Inject constructor(
    private val contactRepository: ContactRepository,
    private val eventRepository: EventRepository,
    private val anniversaryRepository: AnniversaryRepository,
    private val giftRepository: GiftRepository,
    private val thoughtRepository: ThoughtRepository,
    private val customTypeRepository: CustomTypeRepository,
    private val observeFavoritesUseCase: ObserveFavoritesUseCase,
    private val personRelationRepository: PersonRelationRepository
) {
    fun getContactDetail(contactId: Long): Flow<ContactDetailAggregateData> {
        // C-5 修复：统一约定在 UseCase 层对 combine 上游加 distinctUntilChanged，
        // 防止 Room Flow 在表数据未实际变化时重发导致 combine 重新计算与多余 UI 重组。
        val contactFlow = contactRepository.getContactById(contactId).distinctUntilChanged()
        val eventsFlow = eventRepository.getEventsByContact(contactId).distinctUntilChanged()
        val anniversariesFlow = anniversaryRepository.getAnniversariesByContact(contactId).distinctUntilChanged()
        val giftsFlow = giftRepository.getGiftsByContactId(contactId).distinctUntilChanged()
        val thoughtsFlow = thoughtRepository.getThoughtsByContact(contactId).distinctUntilChanged()
        val customTypesFlow = customTypeRepository.getAllTypesGroupedByCategory().distinctUntilChanged()
        val thoughtFavoritesFlow = observeFavoritesUseCase.getFavoriteIds(SourceTypes.THOUGHT).distinctUntilChanged()
        val personRelationsFlow = personRelationRepository.observeRelations(contactId).distinctUntilChanged()

        return combine(
            combine(contactFlow, eventsFlow, anniversariesFlow, giftsFlow, thoughtsFlow) { contact, events, anniversaries, gifts, thoughts ->
                ContactDataTuple(contact, events, anniversaries, gifts, thoughts)
            },
            customTypesFlow,
            thoughtFavoritesFlow,
            personRelationsFlow
        ) { contactData, customTypesMap, thoughtFavoriteIds, personRelations ->
            val conversations = contactData.events.filter { it.type == EventType.CONVERSATION }
            val nonConversationEvents = contactData.events.filter { it.type != EventType.CONVERSATION }

            ContactDetailAggregateData(
                contact = contactData.contact,
                events = nonConversationEvents,
                anniversaries = contactData.anniversaries,
                conversations = conversations,
                gifts = contactData.gifts,
                thoughts = contactData.thoughts,
                favoriteIds = thoughtFavoriteIds,
                hobbyOptions = customTypesMap[CustomCategories.HOBBY].orEmpty(),
                habitOptions = customTypesMap[CustomCategories.HABIT].orEmpty(),
                dietOptions = customTypesMap[CustomCategories.DIET].orEmpty(),
                skillOptions = customTypesMap[CustomCategories.SKILL].orEmpty(),
                eventTypes = customTypesMap[CustomCategories.EVENT_TYPE].orEmpty(),
                relationshipTypes = customTypesMap[CustomCategories.RELATIONSHIP].orEmpty(),
                personRelations = personRelations,
                personRelationTypes = customTypesMap[CustomCategories.PERSON_RELATION].orEmpty(),
                isLoading = false
            )
        }
    }

    private data class ContactDataTuple(
        val contact: Contact?,
        val events: List<Event>,
        val anniversaries: List<Anniversary>,
        val gifts: List<Gift>,
        val thoughts: List<Thought>
    )

    /**
     * 删除联系人及其关联数据。
     *
     * REP-A-1 修复：跨聚合操作由 UseCase 协调，避免 ContactRepositoryImpl 注入跨聚合 DAO。
     * - 礼物照片清理：通过 GiftRepository.deleteGiftsByContactId（含照片文件删除 + 收藏清理）
     * - 联系人头像清理 + 联系人删除：由 ContactRepository.deleteContact 处理
     * - todos/reminders/anniversaries：由 FK CASCADE 自动删除
     *
     * 注意：A-4 修复后，thought 的 update/delete 由 [ThoughtWriteUseCase] 承担，
     * 本 UseCase 只负责读聚合 + 删除联系人这一跨聚合协调。
     */
    suspend fun deleteContact(contactId: Long) {
        giftRepository.deleteGiftsByContactId(contactId)
        contactRepository.deleteContact(contactId)
    }
}
