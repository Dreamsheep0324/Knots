package com.tang.prm.feature.reflect.favorites

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.tang.prm.domain.model.SourceTypes
import com.tang.prm.ui.navigation.EventDetailRoute
import com.tang.prm.ui.navigation.ChatDetailRoute
import com.tang.prm.ui.navigation.ThoughtsRoute
import com.tang.prm.ui.navigation.GiftDetailRoute
import com.tang.prm.ui.navigation.PhotoAlbumRoute
import com.tang.prm.domain.model.AppStrings

// Q-1 修复：18 个 isSystemInDarkTheme() 二选一颜色属性已迁移到 TermPalette.kt
// 通过 LocalTermColors CompositionLocal 提供主题感知的终端风格调色板

internal enum class FavoriteType(
    val code: String,
    val label: String,
    val filterLabel: String,
    val source: String,
    val shortCode: String
) {
    EVENT(SourceTypes.EVENT, "事件", "事件", "事件记录", "事件"),
    DIALOG(SourceTypes.DIALOG, "对话", "对话", "聊天记录", "对话"),
    THOUGHT(SourceTypes.THOUGHT, "想法", AppStrings.ContactDetail.THOUGHTS, "想法本", "想法"),
    GIFT(SourceTypes.GIFT, "礼物", "礼物", "礼物记录", "礼物"),
    PHOTO(SourceTypes.PHOTO, "图片", "图片", "相册", "图片");

    val borderColor: Color
        @Composable @ReadOnlyComposable get() = when (this) {
            EVENT -> FavoriteTypeBorderColorEvent
            DIALOG -> FavoriteTypeBorderColorDialog
            THOUGHT -> FavoriteTypeBorderColorThought
            GIFT -> FavoriteTypeBorderColorGift
            PHOTO -> FavoriteTypeBorderColorPhoto
        }

    companion object {
        fun fromCode(code: String): FavoriteType = entries.find { it.code == code } ?: EVENT
    }
}

internal fun getRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / (60 * 1000)
    val hours = diff / (60 * 60 * 1000)
    val days = diff / (24 * 60 * 60 * 1000)
    return when {
        minutes < 1 -> "刚刚"
        minutes < 60 -> "${minutes}分钟前"
        hours < 24 -> "${hours}小时前"
        days < 30 -> "${days}天前"
        days < 365 -> "${days / 30}月前"
        else -> "${days / 365}年前"
    }
}

internal fun getRouteForType(sourceType: String, sourceId: Long): Any? {
    return when (sourceType) {
        SourceTypes.EVENT -> EventDetailRoute(sourceId)
        SourceTypes.DIALOG -> ChatDetailRoute(sourceId)
        SourceTypes.THOUGHT -> ThoughtsRoute
        SourceTypes.GIFT -> GiftDetailRoute(sourceId)
        SourceTypes.PHOTO -> PhotoAlbumRoute.targeting(sourceId)
        else -> null
    }
}
