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
    val recipeCount: Int = 0,
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
    private val subscriptionRepository: SubscriptionRepository,
    private val recipeRepository: RecipeRepository
) {
    fun getStats(): Flow<HomeStats> = combine(
        giftRepository.getGiftCount().distinctUntilChanged(),
        thoughtRepository.getThoughtCount().distinctUntilChanged(),
        contactRepository.getContactCount().distinctUntilChanged(),
        favoriteRepository.getFavoriteCount().distinctUntilChanged(),
        circleRepository.getCircleCount().distinctUntilChanged(),
        anniversaryRepository.getAnniversaryCount().distinctUntilChanged(),
        eventRepository.getEventCount().distinctUntilChanged(),
        eventRepository.getEventCountByType(EventType.CONVERSATION.name).distinctUntilChanged(),
        eventRepository.getEventCountWithLocation().distinctUntilChanged(),
        eventRepository.getPhotoCount().distinctUntilChanged(),
        giftRepository.getPhotoCount().distinctUntilChanged(),
        subscriptionRepository.getSubscriptionCount().distinctUntilChanged(),
        recipeRepository.getRecipeCount().distinctUntilChanged(),
        contactRepository.getAllIntimacyScores()
            .map { scores ->
                scores.groupingBy { IntimacyTier.of(it) }.eachCount()
            }
            .distinctUntilChanged()
    ) { args: Array<Any> ->
        // combine 变长参数返回 Array<Any>，类型由调用方保证；
        // 使用安全转换 as? 防御上游 DAO 返回类型变更导致的 ClassCastException。
        HomeStats(
            giftCount = args.intAt(0),
            thoughtCount = args.intAt(1),
            contactCount = args.intAt(2),
            favoriteCount = args.intAt(3),
            circleCount = args.intAt(4),
            anniversaryCount = args.intAt(5),
            eventCount = args.intAt(6),
            conversationCount = args.intAt(7),
            footprintCount = args.intAt(8),
            photoCount = args.intAt(9) + args.intAt(10),
            subscriptionCount = args.intAt(11),
            recipeCount = args.intAt(12),
            tierDistribution = args.intimacyMapAt(13)
        )
    }

    /** 安全提取 Int，类型不匹配时返回 0 而非抛出 ClassCastException。 */
    private fun Array<Any>.intAt(index: Int): Int = this[index] as? Int ?: 0

    /** 安全提取亲密度分布 Map，类型不匹配时返回空 Map。 */
    @Suppress("UNCHECKED_CAST")
    private fun Array<Any>.intimacyMapAt(index: Int): Map<IntimacyTier, Int> =
        this[index] as? Map<IntimacyTier, Int> ?: emptyMap()
}
