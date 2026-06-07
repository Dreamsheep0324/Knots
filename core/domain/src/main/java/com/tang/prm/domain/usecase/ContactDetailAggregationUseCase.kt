package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.*
import com.tang.prm.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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
    val isLoading: Boolean = true
)

/**
 * 联系人详情聚合 UseCase
 *
 * 将 6 个 Repository + 1 个 UseCase 的 12 路 combine 聚合逻辑封装为单一数据源，
 * ContactDetailVM 从 7 依赖降至 4 依赖。
 */
class ContactDetailAggregationUseCase @Inject constructor(
    private val contactRepository: ContactRepository,
    private val eventRepository: EventRepository,
    private val anniversaryRepository: AnniversaryRepository,
    private val giftRepository: GiftRepository,
    private val thoughtRepository: ThoughtRepository,
    private val customTypeRepository: CustomTypeRepository,
    private val favoriteToggleUseCase: FavoriteToggleUseCase
) {
    fun getContactDetail(contactId: Long): Flow<ContactDetailAggregateData> {
        val contactFlow = contactRepository.getContactById(contactId)
        val eventsFlow = eventRepository.getEventsByContact(contactId)
        val anniversariesFlow = anniversaryRepository.getAnniversariesByContact(contactId)
        val giftsFlow = giftRepository.getGiftsByContactId(contactId)
        val thoughtsFlow = thoughtRepository.getThoughtsByContact(contactId)
        val customTypesFlow = customTypeRepository.getAllTypesGroupedByCategory()
        val thoughtFavoritesFlow = favoriteToggleUseCase.getFavoriteIds(SourceTypes.THOUGHT)

        return combine(
            combine(contactFlow, eventsFlow, anniversariesFlow, giftsFlow, thoughtsFlow) { contact, events, anniversaries, gifts, thoughts ->
                ContactDataTuple(contact, events, anniversaries, gifts, thoughts)
            },
            customTypesFlow,
            thoughtFavoritesFlow
        ) { contactData, customTypesMap, thoughtFavoriteIds ->
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

    /** 代理写操作，使 VM 无需直接注入 Repository */
    suspend fun deleteContact(contactId: Long) = contactRepository.deleteContact(contactId)

    suspend fun updateThought(thought: Thought) = thoughtRepository.updateThought(thought)

    suspend fun deleteThought(id: Long) = thoughtRepository.deleteThought(id)
}
