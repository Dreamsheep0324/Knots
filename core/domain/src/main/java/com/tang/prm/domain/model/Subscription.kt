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
    val timezone: String = "UTC"
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
    SubscriptionCycle.WEEKLY -> price * 4.33
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
