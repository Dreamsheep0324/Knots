package com.tang.prm.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class SubscriptionTest {

    @Nested
    @DisplayName("monthlyEquivalent")
    inner class MonthlyEquivalentTest {

        @Test
        fun weekly_priceConvertedToMonthly() {
            val sub = testSubscription(price = 52.0, cycle = SubscriptionCycle.WEEKLY)
            // 52 * (52/12) = 52 * 4.333... = 225.333...
            assertThat(sub.monthlyEquivalent()).isWithin(0.01).of(52.0 * 52.0 / 12.0)
        }

        @Test
        fun monthly_priceUnchanged() {
            val sub = testSubscription(price = 30.0, cycle = SubscriptionCycle.MONTHLY)
            assertThat(sub.monthlyEquivalent()).isEqualTo(30.0)
        }

        @Test
        fun quarterly_dividedBy3() {
            val sub = testSubscription(price = 90.0, cycle = SubscriptionCycle.QUARTERLY)
            assertThat(sub.monthlyEquivalent()).isEqualTo(30.0)
        }

        @Test
        fun yearly_dividedBy12() {
            val sub = testSubscription(price = 120.0, cycle = SubscriptionCycle.YEARLY)
            assertThat(sub.monthlyEquivalent()).isEqualTo(10.0)
        }

        @Test
        fun oneTime_returnsZero() {
            val sub = testSubscription(price = 99.0, cycle = SubscriptionCycle.ONE_TIME)
            assertThat(sub.monthlyEquivalent()).isEqualTo(0.0)
        }
    }

    @Nested
    @DisplayName("yearlyEquivalent")
    inner class YearlyEquivalentTest {

        @Test
        fun weekly_priceTimes52() {
            val sub = testSubscription(price = 10.0, cycle = SubscriptionCycle.WEEKLY)
            assertThat(sub.yearlyEquivalent()).isEqualTo(520.0)
        }

        @Test
        fun monthly_priceTimes12() {
            val sub = testSubscription(price = 30.0, cycle = SubscriptionCycle.MONTHLY)
            assertThat(sub.yearlyEquivalent()).isEqualTo(360.0)
        }

        @Test
        fun quarterly_priceTimes4() {
            val sub = testSubscription(price = 90.0, cycle = SubscriptionCycle.QUARTERLY)
            assertThat(sub.yearlyEquivalent()).isEqualTo(360.0)
        }

        @Test
        fun yearly_priceUnchanged() {
            val sub = testSubscription(price = 120.0, cycle = SubscriptionCycle.YEARLY)
            assertThat(sub.yearlyEquivalent()).isEqualTo(120.0)
        }

        @Test
        fun oneTime_returnsPrice() {
            val sub = testSubscription(price = 99.0, cycle = SubscriptionCycle.ONE_TIME)
            assertThat(sub.yearlyEquivalent()).isEqualTo(99.0)
        }
    }

    @Nested
    @DisplayName("computedStatus")
    inner class ComputedStatusTest {

        @Test
        fun activeWithFutureBilling_returnsActive() {
            val sub = testSubscription(
                status = SubscriptionStatus.ACTIVE,
                nextBillingDate = System.currentTimeMillis() + 86_400_000L
            )
            assertThat(sub.computedStatus()).isEqualTo(SubscriptionStatus.ACTIVE)
        }

        @Test
        fun activeWithPastBilling_returnsExpired() {
            val sub = testSubscription(
                status = SubscriptionStatus.ACTIVE,
                nextBillingDate = System.currentTimeMillis() - 86_400_000L
            )
            assertThat(sub.computedStatus()).isEqualTo(SubscriptionStatus.EXPIRED)
        }

        @Test
        fun cancelledWithFutureBilling_returnsCancelled() {
            val sub = testSubscription(
                status = SubscriptionStatus.CANCELLED,
                nextBillingDate = System.currentTimeMillis() + 86_400_000L
            )
            assertThat(sub.computedStatus()).isEqualTo(SubscriptionStatus.CANCELLED)
        }

        @Test
        fun cancelledWithPastBilling_returnsExpired() {
            val sub = testSubscription(
                status = SubscriptionStatus.CANCELLED,
                nextBillingDate = System.currentTimeMillis() - 86_400_000L
            )
            assertThat(sub.computedStatus()).isEqualTo(SubscriptionStatus.EXPIRED)
        }

        @Test
        fun expiredWithFutureBilling_revivesToActive() {
            // B-15 修复：status=EXPIRED 但 nextBillingDate 已被续期到未来 → 复活为 ACTIVE
            val sub = testSubscription(
                status = SubscriptionStatus.EXPIRED,
                nextBillingDate = System.currentTimeMillis() + 86_400_000L
            )
            assertThat(sub.computedStatus()).isEqualTo(SubscriptionStatus.ACTIVE)
        }

        @Test
        fun expiredWithPastBilling_remainsExpired() {
            // B-15 修复：status=EXPIRED 且 nextBillingDate 仍过期 → 保持 EXPIRED
            val sub = testSubscription(
                status = SubscriptionStatus.EXPIRED,
                nextBillingDate = System.currentTimeMillis() - 86_400_000L
            )
            assertThat(sub.computedStatus()).isEqualTo(SubscriptionStatus.EXPIRED)
        }
    }

    @Nested
    @DisplayName("SubscriptionCycle")
    inner class SubscriptionCycleTest {

        @ParameterizedTest
        @CsvSource("WEEKLY,每周", "MONTHLY,每月", "QUARTERLY,每季", "YEARLY,每年", "ONE_TIME,一次性")
        fun displayName(cycle: SubscriptionCycle, expected: String) {
            assertThat(cycle.displayName).isEqualTo(expected)
        }
    }

    private fun testSubscription(
        price: Double = 10.0,
        cycle: SubscriptionCycle = SubscriptionCycle.MONTHLY,
        status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
        nextBillingDate: Long = System.currentTimeMillis() + 86_400_000L
    ) = Subscription(
        name = "Test",
        price = price,
        cycle = cycle,
        startDate = System.currentTimeMillis(),
        nextBillingDate = nextBillingDate,
        status = status
    )
}
