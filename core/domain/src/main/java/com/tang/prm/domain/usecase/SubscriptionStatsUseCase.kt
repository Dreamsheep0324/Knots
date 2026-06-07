package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.Subscription
import com.tang.prm.domain.model.SubscriptionStatus
import com.tang.prm.domain.model.computedStatus
import com.tang.prm.domain.model.monthlyEquivalent
import com.tang.prm.domain.model.yearlyEquivalent
import com.tang.prm.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.time.Duration.Companion.days
import kotlin.time.DurationUnit

class SubscriptionStatsUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) {
    data class SubscriptionStats(
        val monthlyTotal: Double,
        val yearlyTotal: Double,
        val byCategory: Map<String, Double>,
        val byCategorySubscriptions: Map<String, List<Subscription>>,
        val expiringSoon: List<Subscription>,
        val activeSubscriptions: List<Subscription>,
        val activeCount: Int
    )

    fun getStats(): Flow<SubscriptionStats> =
        subscriptionRepository.getAllSubscriptions()
            .map { subs ->
                val active = subs.filter { it.computedStatus() == SubscriptionStatus.ACTIVE }
                val monthlyTotal = active.sumOf { it.monthlyEquivalent() }
                val yearlyTotal = active.sumOf { it.yearlyEquivalent() }
                val byCategory = active.groupBy { it.category ?: "未分类" }
                    .mapValues { (_, list) -> list.sumOf { it.yearlyEquivalent() } }
                val byCategorySubscriptions = active.groupBy { it.category ?: "未分类" }
                val expiringSoon = subs.filter {
                    it.computedStatus() == SubscriptionStatus.ACTIVE &&
                    it.nextBillingDate - System.currentTimeMillis() < 7.days.inWholeMilliseconds
                }
                SubscriptionStats(monthlyTotal, yearlyTotal, byCategory, byCategorySubscriptions, expiringSoon, active, active.size)
            }
            .distinctUntilChanged()
}
