package com.tang.prm.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.*
import com.tang.prm.domain.repository.SubscriptionRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SubscriptionStatsUseCaseTest {

    @MockK
    private lateinit var subscriptionRepository: SubscriptionRepository

    private lateinit var useCase: SubscriptionStatsUseCase

    @BeforeEach
    fun setUp() {
        useCase = SubscriptionStatsUseCase(subscriptionRepository)
    }

    private fun testSub(
        price: Double = 10.0,
        cycle: SubscriptionCycle = SubscriptionCycle.MONTHLY,
        category: String? = null,
        status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
        nextBillingDate: Long = System.currentTimeMillis() + 30 * 86_400_000L
    ) = Subscription(
        name = "Test",
        price = price,
        cycle = cycle,
        startDate = System.currentTimeMillis(),
        nextBillingDate = nextBillingDate,
        status = status,
        category = category
    )

    @Nested
    @DisplayName("月/年均值")
    inner class TotalsTest {

        @Test
        fun `empty subscriptions returns zero totals`() = runTest {
            every { subscriptionRepository.getAllSubscriptions() } returns flowOf(emptyList())

            useCase().test {
                val stats = awaitItem()
                assertThat(stats.monthlyTotal).isEqualTo(0.0)
                assertThat(stats.yearlyTotal).isEqualTo(0.0)
                assertThat(stats.activeCount).isEqualTo(0)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `active subscriptions summed correctly`() = runTest {
            val subs = listOf(
                testSub(price = 30.0, cycle = SubscriptionCycle.MONTHLY),
                testSub(price = 120.0, cycle = SubscriptionCycle.YEARLY)
            )
            every { subscriptionRepository.getAllSubscriptions() } returns flowOf(subs)

            useCase().test {
                val stats = awaitItem()
                assertThat(stats.monthlyTotal).isWithin(0.01).of(30.0 + 10.0) // 30 + 120/12
                assertThat(stats.yearlyTotal).isWithin(0.01).of(360.0 + 120.0) // 30*12 + 120
                assertThat(stats.activeCount).isEqualTo(2)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `expired subscriptions excluded from totals`() = runTest {
            // B-15 修复：EXPIRED 订阅需设置过期的 nextBillingDate，否则会被复活路径判定为 ACTIVE。
            val expiredBillingDate = System.currentTimeMillis() - 86_400_000L
            val subs = listOf(
                testSub(price = 30.0, status = SubscriptionStatus.ACTIVE),
                testSub(price = 50.0, status = SubscriptionStatus.EXPIRED, nextBillingDate = expiredBillingDate)
            )
            every { subscriptionRepository.getAllSubscriptions() } returns flowOf(subs)

            useCase().test {
                val stats = awaitItem()
                assertThat(stats.activeCount).isEqualTo(1)
                assertThat(stats.monthlyTotal).isWithin(0.01).of(30.0)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `revived subscriptions included in totals`() = runTest {
            // B-15 复活路径：status=EXPIRED 但 nextBillingDate 已续期到未来，应被统计为 ACTIVE。
            val renewedBillingDate = System.currentTimeMillis() + 30 * 86_400_000L
            val subs = listOf(
                testSub(price = 30.0, status = SubscriptionStatus.ACTIVE),
                testSub(price = 50.0, status = SubscriptionStatus.EXPIRED, nextBillingDate = renewedBillingDate)
            )
            every { subscriptionRepository.getAllSubscriptions() } returns flowOf(subs)

            useCase().test {
                val stats = awaitItem()
                assertThat(stats.activeCount).isEqualTo(2)
                assertThat(stats.monthlyTotal).isWithin(0.01).of(80.0)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    @DisplayName("分类统计")
    inner class CategoryTest {

        @Test
        fun `byCategory groups correctly`() = runTest {
            val subs = listOf(
                testSub(price = 30.0, category = "娱乐"),
                testSub(price = 50.0, category = "娱乐"),
                testSub(price = 20.0, category = "工具"),
                testSub(price = 10.0, category = null) // 未分类
            )
            every { subscriptionRepository.getAllSubscriptions() } returns flowOf(subs)

            useCase().test {
                val stats = awaitItem()
                assertThat(stats.byCategory).hasSize(3)
                assertThat(stats.byCategory["娱乐"]).isNotNull()
                assertThat(stats.byCategory["工具"]).isNotNull()
                assertThat(stats.byCategory["未分类"]).isNotNull()
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `byCategorySubscriptions groups subscription objects`() = runTest {
            val subs = listOf(
                testSub(price = 30.0, category = "娱乐"),
                testSub(price = 50.0, category = "娱乐"),
                testSub(price = 20.0, category = "工具")
            )
            every { subscriptionRepository.getAllSubscriptions() } returns flowOf(subs)

            useCase().test {
                val stats = awaitItem()
                assertThat(stats.byCategorySubscriptions["娱乐"]).hasSize(2)
                assertThat(stats.byCategorySubscriptions["工具"]).hasSize(1)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    @DisplayName("7天到期筛选")
    inner class ExpiringSoonTest {

        @Test
        fun `subscription expiring in 3 days is in expiring soon`() = runTest {
            val expiringSub = testSub(
                nextBillingDate = System.currentTimeMillis() + 3 * 86_400_000L
            )
            val safeSub = testSub(
                nextBillingDate = System.currentTimeMillis() + 30 * 86_400_000L
            )
            every { subscriptionRepository.getAllSubscriptions() } returns flowOf(listOf(expiringSub, safeSub))

            useCase().test {
                val stats = awaitItem()
                assertThat(stats.expiringSoon).hasSize(1)
                assertThat(stats.expiringSoon[0].nextBillingDate).isEqualTo(expiringSub.nextBillingDate)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `subscription expiring in 10 days not in expiring soon`() = runTest {
            val sub = testSub(
                nextBillingDate = System.currentTimeMillis() + 10 * 86_400_000L
            )
            every { subscriptionRepository.getAllSubscriptions() } returns flowOf(listOf(sub))

            useCase().test {
                val stats = awaitItem()
                assertThat(stats.expiringSoon).isEmpty()
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
