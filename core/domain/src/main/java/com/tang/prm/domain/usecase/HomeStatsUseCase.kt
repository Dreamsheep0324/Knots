package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.EventType
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
    val subscriptionCount: Int = 0
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
        subscriptionRepository.getSubscriptionCount().distinctUntilChanged()
    ) { args: Array<Int> ->
        HomeStats(
            giftCount = args[0],
            thoughtCount = args[1],
            contactCount = args[2],
            favoriteCount = args[3],
            circleCount = args[4],
            anniversaryCount = args[5],
            eventCount = args[6],
            conversationCount = args[7],
            footprintCount = args[8],
            photoCount = args[9] + args[10],
            subscriptionCount = args[11]
        )
    }
}
