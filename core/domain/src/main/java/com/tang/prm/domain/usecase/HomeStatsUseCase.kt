package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.EventType
import com.tang.prm.domain.model.IntimacyTier
import com.tang.prm.domain.repository.AnniversaryRepository
import com.tang.prm.domain.repository.CircleRepository
import com.tang.prm.domain.repository.ContactRelationRepository
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.EventStatsRepository
import com.tang.prm.domain.repository.FavoriteRepository
import com.tang.prm.domain.repository.GiftRepository
import com.tang.prm.domain.repository.RecipeRepository
import com.tang.prm.domain.repository.SubscriptionRepository
import com.tang.prm.domain.repository.ThoughtRepository
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
    val relationCount: Int = 0,
    val tierDistribution: Map<IntimacyTier, Int> = emptyMap()
)

/**
 * 第一组：礼物/想法/联系人/收藏/圈子 计数（5 路）。
 */
private data class CountStatsA(
    val gift: Int,
    val thought: Int,
    val contact: Int,
    val favorite: Int,
    val circle: Int
)

/**
 * 第二组：纪念日/事件/订阅/食谱/关系 计数（5 路）。
 */
private data class CountStatsB(
    val anniversary: Int,
    val event: Int,
    val subscription: Int,
    val recipe: Int,
    val relation: Int
)

/**
 * 第三组：事件细分 + 亲密度分布（5 路）。
 * photoCount = eventPhoto + giftPhoto 在此层计算，避免跨组相加。
 */
private data class EventBreakdown(
    val conversation: Int,
    val footprint: Int,
    val photoCount: Int,
    val tierDistribution: Map<IntimacyTier, Int>
)

class HomeStatsUseCase @Inject constructor(
    private val giftRepository: GiftRepository,
    private val thoughtRepository: ThoughtRepository,
    private val contactRepository: ContactRepository,
    private val favoriteRepository: FavoriteRepository,
    private val circleRepository: CircleRepository,
    private val anniversaryRepository: AnniversaryRepository,
    private val eventStatsRepository: EventStatsRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val recipeRepository: RecipeRepository,
    private val contactRelationRepository: ContactRelationRepository
) {
    operator fun invoke(): Flow<HomeStats> {
        val statsA: Flow<CountStatsA> = combine(
            giftRepository.getGiftCount().distinctUntilChanged(),
            thoughtRepository.getThoughtCount().distinctUntilChanged(),
            contactRepository.getContactCount().distinctUntilChanged(),
            favoriteRepository.getFavoriteCount().distinctUntilChanged(),
            circleRepository.getCircleCount().distinctUntilChanged()
        ) { gift, thought, contact, favorite, circle ->
            CountStatsA(gift, thought, contact, favorite, circle)
        }
        val statsB: Flow<CountStatsB> = combine(
            anniversaryRepository.getAnniversaryCount().distinctUntilChanged(),
            eventStatsRepository.getEventCount().distinctUntilChanged(),
            subscriptionRepository.getSubscriptionCount().distinctUntilChanged(),
            recipeRepository.getRecipeCount().distinctUntilChanged(),
            contactRelationRepository.getRelationCount().distinctUntilChanged()
        ) { anniversary, event, subscription, recipe, relation ->
            CountStatsB(anniversary, event, subscription, recipe, relation)
        }
        val breakdown: Flow<EventBreakdown> = combine(
            eventStatsRepository.getEventCountByType(EventType.CONVERSATION.name).distinctUntilChanged(),
            eventStatsRepository.getEventCountWithLocation().distinctUntilChanged(),
            eventStatsRepository.getPhotoCount().distinctUntilChanged(),
            giftRepository.getPhotoCount().distinctUntilChanged(),
            contactRepository.getAllIntimacyScores()
                .map { scores -> scores.groupingBy { IntimacyTier.of(it) }.eachCount() }
                .distinctUntilChanged()
        ) { conversation, footprint, eventPhoto, giftPhoto, tierDistribution ->
            EventBreakdown(conversation, footprint, eventPhoto + giftPhoto, tierDistribution)
        }
        return combine(statsA, statsB, breakdown) { a, b, c ->
            HomeStats(
                giftCount = a.gift,
                thoughtCount = a.thought,
                contactCount = a.contact,
                favoriteCount = a.favorite,
                circleCount = a.circle,
                anniversaryCount = b.anniversary,
                eventCount = b.event,
                conversationCount = c.conversation,
                photoCount = c.photoCount,
                footprintCount = c.footprint,
                subscriptionCount = b.subscription,
                recipeCount = b.recipe,
                relationCount = b.relation,
                tierDistribution = c.tierDistribution
            )
        }
    }
}
