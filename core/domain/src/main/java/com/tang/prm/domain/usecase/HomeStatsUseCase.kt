package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.EventType
import com.tang.prm.domain.model.IntimacyTier
import com.tang.prm.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class HomeStats(
    val giftCount: Int = 0,
    val thoughtCount: Int = 0,
    val contactCount: Int = 0,
    val favoriteCount: Int = 0,
    val circleCount: Int = 0,
    val anniversaryCount: Int = 0,
    val eventCount: Int = 0,
    val conversationCount: Int = 0,
    val photoCount: Int = 0,
    val footprintCount: Int = 0,
    val subscriptionCount: Int = 0,
    val tierDistribution: Map<IntimacyTier, Int> = emptyMap()
)

class HomeStatsUseCase @Inject constructor(
    private val giftRepository: GiftRepository,
    private val thoughtRepository: ThoughtRepository,
    private val contactRepository: ContactRepository,
    private val favoriteRepository: FavoriteRepository,
    private val circleRepository: CircleRepository,
    private val anniversaryRepository: AnniversaryRepository,
    private val eventRepository: EventRepository,
    private val subscriptionRepository: SubscriptionRepository
) {
    fun getStats(): Flow<HomeStats> = combine(
        giftRepository.getGiftCount().distinctUntilChanged(),
        thoughtRepository.getThoughtCount().distinctUntilChanged(),
        contactRepository.getContactCount().distinctUntilChanged(),
        favoriteRepository.getFavoriteCount().distinctUntilChanged(),
        circleRepository.getCircleCount().distinctUntilChanged(),
        anniversaryRepository.getAnniversaryCount().distinctUntilChanged(),
        eventRepository.getEventCount().distinctUntilChanged(),
        eventRepository.getEventsByType(EventType.CONVERSATION.name).map { it.size }.distinctUntilChanged(),
        eventRepository.getEventsWithLocation().map { it.size }.distinctUntilChanged(),
        eventRepository.getPhotoCount().distinctUntilChanged(),
        giftRepository.getPhotoCount().distinctUntilChanged(),
        subscriptionRepository.getSubscriptionCount().distinctUntilChanged(),
        contactRepository.getAllContacts()
            .map { contacts ->
                contacts.groupingBy { IntimacyTier.of(it.intimacyScore) }
                    .eachCount()
            }
            .distinctUntilChanged()
    ) { args: Array<Any> ->
        HomeStats(
            giftCount = args[0] as Int,
            thoughtCount = args[1] as Int,
            contactCount = args[2] as Int,
            favoriteCount = args[3] as Int,
            circleCount = args[4] as Int,
            anniversaryCount = args[5] as Int,
            eventCount = args[6] as Int,
            conversationCount = args[7] as Int,
            footprintCount = args[8] as Int,
            photoCount = (args[9] as Int) + (args[10] as Int),
            subscriptionCount = args[11] as Int,
            tierDistribution = args[12] as Map<IntimacyTier, Int>
        )
    }
}
