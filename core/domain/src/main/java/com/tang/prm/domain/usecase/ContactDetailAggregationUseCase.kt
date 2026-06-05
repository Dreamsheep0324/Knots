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
        val hobbyFlow = customTypeRepository.getTypesByCategory(CustomCategories.HOBBY)
        val habitFlow = customTypeRepository.getTypesByCategory(CustomCategories.HABIT)
        val dietFlow = customTypeRepository.getTypesByCategory(CustomCategories.DIET)
        val skillFlow = customTypeRepository.getTypesByCategory(CustomCategories.SKILL)
        val eventTypeFlow = customTypeRepository.getTypesByCategory(CustomCategories.EVENT_TYPE)
        val relationshipFlow = customTypeRepository.getTypesByCategory(CustomCategories.RELATIONSHIP)
        val thoughtFavoritesFlow = favoriteToggleUseCase.getFavoriteIds(SourceTypes.THOUGHT)

        return combine(
            combine(contactFlow, eventsFlow, anniversariesFlow, giftsFlow, thoughtsFlow) { contact, events, anniversaries, gifts, thoughts ->
                ContactDataTuple(contact, events, anniversaries, gifts, thoughts)
            },
            combine(hobbyFlow, habitFlow, dietFlow, skillFlow, eventTypeFlow) { hobby, habit, diet, skill, eventTypes ->
                CustomTypePart1(hobby, habit, diet, skill, eventTypes)
            }.combine(relationshipFlow) { part1, relationshipTypes ->
                CustomTypePart2(part1.hobby, part1.habit, part1.diet, part1.skill, part1.eventTypes, relationshipTypes)
            },
            thoughtFavoritesFlow
        ) { contactData, customTypeData, thoughtFavoriteIds ->
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
                hobbyOptions = customTypeData.hobby,
                habitOptions = customTypeData.habit,
                dietOptions = customTypeData.diet,
                skillOptions = customTypeData.skill,
                eventTypes = customTypeData.eventTypes,
                relationshipTypes = customTypeData.relationshipTypes,
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

    private data class CustomTypePart1(
        val hobby: List<CustomType>,
        val habit: List<CustomType>,
        val diet: List<CustomType>,
        val skill: List<CustomType>,
        val eventTypes: List<CustomType>
    )

    private data class CustomTypePart2(
        val hobby: List<CustomType>,
        val habit: List<CustomType>,
        val diet: List<CustomType>,
        val skill: List<CustomType>,
        val eventTypes: List<CustomType>,
        val relationshipTypes: List<CustomType>
    )

    /** 代理写操作，使 VM 无需直接注入 Repository */
    suspend fun deleteContact(contactId: Long) = contactRepository.deleteContact(contactId)

    suspend fun updateThought(thought: Thought) = thoughtRepository.updateThought(thought)

    suspend fun deleteThought(id: Long) = thoughtRepository.deleteThought(id)
}
