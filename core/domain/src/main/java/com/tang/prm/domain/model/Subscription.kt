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

/**
 * Q-8 修复：订阅提前提醒天数默认值。
 *
 * 订阅金额较大，提前 3 天提醒以便用户决定是否续费；与 [Anniversary.DEFAULT_ANNIVERSARY_REMINDER_DAYS]（1 天）
 * 区分——纪念日无金额压力，提前 1 天足够。两者默认值集中到常量避免散落字面量漂移。
 */
const val DEFAULT_SUBSCRIPTION_REMINDER_DAYS = 3

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
    val reminderDays: Int = DEFAULT_SUBSCRIPTION_REMINDER_DAYS,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val timezone: String = DEFAULT_SUBSCRIPTION_TIMEZONE
)

/**
 * 计算订阅的当前状态。
 *
 * B-15 修复：原逻辑存在两个问题：
 * 1. 双 if 重复——第一个 if 检查 `status != CANCELLED`，第二个检查 `status == CANCELLED`，
 *    两者覆盖的 nextBillingDate < now 场景的返回值都是 EXPIRED，第二个 if 实际是死代码。
 * 2. 缺少「EXPIRED → ACTIVE 复活」路径——若订阅 status 为 EXPIRED 但 nextBillingDate 被续期
 *    （如用户手动续费后更新了 nextBillingDate 但未改 status），原逻辑会继续返回 EXPIRED，
 *    UI 显示已过期，与实际状态不符。
 *
 * 修复后规则：
 * - nextBillingDate < now → EXPIRED（统一一个 if，无论 ACTIVE/CANCELLED）
 * - status == EXPIRED 且 nextBillingDate >= now → ACTIVE（复活路径）
 * - 其他 → status
 */
fun Subscription.computedStatus(): SubscriptionStatus {
    val now = System.currentTimeMillis()
    return when {
        nextBillingDate < now -> SubscriptionStatus.EXPIRED
        status == SubscriptionStatus.EXPIRED -> SubscriptionStatus.ACTIVE
        else -> status
    }
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
