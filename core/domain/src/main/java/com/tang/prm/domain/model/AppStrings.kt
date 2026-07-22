package com.tang.prm.domain.model

/**
 * 全局 UI 文案常量。
 *
 * Q-6 + D-7 + C-7 修复：原 [EventType] 子对象（MEETUP/CONVERSATION/OTHER）与
 * [EventType] enum 的 displayName 重复且无引用，已删除。事件类型文案统一从
 * `EventType.MEETUP.displayName` 等获取，单一来源。
 */
object AppStrings {
    object Tabs {
        const val ALL = "全部"
        const val UPCOMING = "即将到来"
        const val PAST = "已过期"
    }

    object ContactDetail {
        const val PROFILE = "资料"
        const val EVENTS = "事件"
        const val ANNIVERSARY = "纪念"
        const val GIFTS = "礼物"
        const val THOUGHTS = "想法"
        const val CHATS = "对话"
    }
}
