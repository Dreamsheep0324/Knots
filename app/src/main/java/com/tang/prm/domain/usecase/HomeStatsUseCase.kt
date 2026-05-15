package com.tang.prm.domain.usecase

import com.tang.prm.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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
    val footprintCount: Int = 0
)

class HomeStatsUseCase @Inject constructor(
    private val giftRepository: GiftRepository,
    private val thoughtRepository: ThoughtRepository,
    private val contactRepository: ContactRepository,
    private val favoriteRepository: FavoriteRepository,
    private val circleRepository: CircleRepository,
    private val anniversaryRepository: AnniversaryRepository,
    private val eventRepository: EventRepository
) {
    fun getStats(): Flow<HomeStats> = combine(
        giftRepository.getGiftCount(),
        thoughtRepository.getThoughtCount(),
        contactRepository.getContactCount(),
        favoriteRepository.getFavoriteCount(),
        circleRepository.getCircleCount(),
        anniversaryRepository.getAnniversaryCount(),
        eventRepository.getEventCount(),
        eventRepository.getConversationCount(),
        eventRepository.getPhotoCount(),
        eventRepository.getFootprintCount(),
        giftRepository.getGiftPhotoCount()
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
            photoCount = args[8] + args[10],
            footprintCount = args[9]
        )
    }
}
