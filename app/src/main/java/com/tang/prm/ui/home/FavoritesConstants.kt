package com.tang.prm.ui.home

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.tang.prm.domain.model.SourceTypes
import com.tang.prm.ui.navigation.Screen
import com.tang.prm.domain.model.AppStrings

private val DarkTermBorder = Color(0xFF30363D)
private val DarkTermText = Color(0xFFE6EDF3)
private val DarkTermMuted = Color(0xFF8B949E)
private val DarkTermComment = Color(0xFF6E7681)
private val DarkTermHighlight = Color(0xFFC9D1D9)
private val DarkTermDim = Color(0xFF484F58)
private val DarkTermTableBorder = Color(0xFF21262D)
private val DarkTermRowHover = Color(0xFF1C2129)
private val DarkTermDetailBorder = Color(0xFF30363D)
private val DarkTermActiveTab = Color(0xFFE6EDF3)
private val DarkTermInactiveTab = Color(0xFF6E7681)
private val DarkTermTagBg = Color(0xFF30363D)
private val DarkTermSelectedTagBg = Color(0xFF484F58)

private val DarkTypeBorderEvent = Color(0xFF58A6FF)
private val DarkTypeBorderDialog = Color(0xFF79C0FF)
private val DarkTypeBorderThought = Color(0xFFBC8CFF)
private val DarkTypeBorderGift = Color(0xFFD29922)
private val DarkTypeBorderPhoto = Color(0xFF3FB950)

internal val TermBorder: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkTermBorder else Color(0xFFE2E8F0)
internal val TermText: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkTermText else Color(0xFF1E293B)
internal val TermMuted: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkTermMuted else Color(0xFF64748B)
internal val TermComment: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkTermComment else Color(0xFF94A3B8)
internal val TermHighlight: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkTermHighlight else Color(0xFF334155)
internal val TermDim: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkTermDim else Color(0xFFCBD5E1)
internal val TermTableBorder: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkTermTableBorder else Color(0xFFE2E8F0)
internal val TermRowHover: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkTermRowHover else Color(0xFFF1F5F9)
internal val TermDetailBorder: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkTermDetailBorder else Color(0xFFE2E8F0)
internal val TermActiveTab: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkTermActiveTab else Color(0xFF1E293B)
internal val TermInactiveTab: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkTermInactiveTab else Color(0xFF64748B)
internal val TermTagBg: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkTermTagBg else Color(0xFF1E293B)
internal val TermSelectedTagBg: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkTermSelectedTagBg else Color(0xFF1E293B)

internal val FavoriteTypeBorderColorEvent: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkTypeBorderEvent else Color(0xFF1E293B)
internal val FavoriteTypeBorderColorDialog: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkTypeBorderDialog else Color(0xFF475569)
internal val FavoriteTypeBorderColorThought: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkTypeBorderThought else Color(0xFF94A3B8)
internal val FavoriteTypeBorderColorGift: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkTypeBorderGift else Color(0xFF334155)
internal val FavoriteTypeBorderColorPhoto: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkTypeBorderPhoto else Color(0xFFCBD5E1)

internal enum class FavoriteType(
    val code: String,
    val label: String,
    val filterLabel: String,
    val size: String,
    val source: String,
    val shortCode: String
) {
    EVENT(SourceTypes.EVENT, "事件", "事件", "2.1K", "事件记录", "事件"),
    DIALOG(SourceTypes.DIALOG, "对话", "对话", "4.8K", "聊天记录", "对话"),
    THOUGHT(SourceTypes.THOUGHT, "想法", AppStrings.ContactDetail.THOUGHTS, "1.2K", "想法本", "想法"),
    GIFT(SourceTypes.GIFT, "礼物", "礼物", "856K", "礼物记录", "礼物"),
    PHOTO(SourceTypes.PHOTO, "图片", "图片", "3.5M", "相册", "图片");

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

internal fun getRouteForType(sourceType: String, sourceId: Long): String? {
    return when (sourceType) {
        SourceTypes.EVENT -> Screen.EventDetail.createRoute(sourceId)
        SourceTypes.DIALOG -> Screen.ChatDetail.createRoute(sourceId)
        SourceTypes.THOUGHT -> Screen.Thoughts.route
        SourceTypes.GIFT -> Screen.GiftDetail.createRoute(sourceId)
        SourceTypes.PHOTO -> Screen.PhotoAlbum.createRoute(sourceId)
        else -> null
    }
}
