package com.tang.prm.domain.model

enum class SubscriptionCycle(val displayName: String) {
    WEEKLY("每周"),
    MONTHLY("每月"),
    QUARTERLY("每季"),
    YEARLY("每年"),
    ONE_TIME("一次性")
}

enum class SubscriptionStatus {
    ACTIVE,
    CANCELLED,
    EXPIRED
}

private const val WEEKS_PER_MONTH = 52.0 / 12.0

/**
 * 订阅时区默认值。Domain 层 [Subscription] 与 Data 层 [com.tang.prm.data.local.entity.SubscriptionEntity]
 * 共用此常量，避免两层默认值漂移（历史 BUG：MAP-B-4）。
 */
const val DEFAULT_SUBSCRIPTION_TIMEZONE = "Asia/Shanghai"

data class Subscription(
    val id: Long = 0,
    val name: String,
    val icon: String? = null,
    val category: String? = null,
    val price: Double,
    val currency: String = "CNY",
    val cycle: SubscriptionCycle,
    val startDate: Long,
    val nextBillingDate: Long,
    val status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
    val reminderDays: Int = 3,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val timezone: String = DEFAULT_SUBSCRIPTION_TIMEZONE
)

fun Subscription.computedStatus(): SubscriptionStatus {
    if (nextBillingDate < System.currentTimeMillis() && status != SubscriptionStatus.CANCELLED) {
        return SubscriptionStatus.EXPIRED
    }
    if (status == SubscriptionStatus.CANCELLED && nextBillingDate < System.currentTimeMillis()) {
        return SubscriptionStatus.EXPIRED
    }
    return status
}

fun Subscription.monthlyEquivalent(): Double = when (cycle) {
    SubscriptionCycle.WEEKLY -> price * WEEKS_PER_MONTH
    SubscriptionCycle.MONTHLY -> price
    SubscriptionCycle.QUARTERLY -> price / 3
    SubscriptionCycle.YEARLY -> price / 12
    SubscriptionCycle.ONE_TIME -> 0.0
}

fun Subscription.yearlyEquivalent(): Double = when (cycle) {
    SubscriptionCycle.WEEKLY -> price * 52
    SubscriptionCycle.MONTHLY -> price * 12
    SubscriptionCycle.QUARTERLY -> price * 4
    SubscriptionCycle.YEARLY -> price
    SubscriptionCycle.ONE_TIME -> price
}
