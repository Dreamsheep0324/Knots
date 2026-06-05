package com.tang.prm.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

// Bottom nav routes
@Serializable object HomeRoute
@Serializable object EventsRoute
@Serializable object AnniversariesRoute
@Serializable object ChatRoute
@Serializable object ContactsRoute

// Contact routes
@Serializable data class ContactDetailRoute(val contactId: Long)
@Serializable object AddContactRoute
@Serializable data class EditContactRoute(val contactId: Long)

// Event routes
@Serializable data class EventDetailRoute(val eventId: Long)
@Serializable data class EditEventRoute(val eventId: Long)
@Serializable data class AddEventRoute(val contactId: Long = 0L)

// Anniversary routes
@Serializable data class AnniversaryDetailRoute(val anniversaryId: Long)
@Serializable data class EditAnniversaryRoute(val anniversaryId: Long)
@Serializable data class AddAnniversaryRoute(val contactId: Long = 0L)

// Chat routes
@Serializable data class AddChatRoute(val contactId: Long = 0L)
@Serializable data class ChatDetailRoute(val eventId: Long)
@Serializable data class EditChatRoute(val eventId: Long)

// Gift routes
@Serializable object GiftsRoute
@Serializable object AddGiftRoute
@Serializable data class EditGiftRoute(val giftId: Long)
@Serializable data class GiftDetailRoute(val giftId: Long)

// Home sub-routes
@Serializable data class PhotoAlbumRoute(val photoId: Long = 0L)
@Serializable object FootprintsRoute
@Serializable object ThoughtsRoute
@Serializable object ContactListRoute
@Serializable object FavoritesRoute

// Divination routes
@Serializable object DivinationRoute
@Serializable object LiuyaoCastRoute
@Serializable object LiuyaoResultRoute
@Serializable object MeihuaMethodRoute
@Serializable object MeihuaResultRoute
@Serializable object ExternalOmenRoute
@Serializable object DivinationHistoryRoute

// Settings routes
@Serializable object SettingsRoute
@Serializable object ThemeSettingsRoute
@Serializable object AiConfigRoute
@Serializable object BackupRestoreRoute
@Serializable object AboutRoute

data class BottomNavItem(
    val route: String,
    val routeObject: Any,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = HomeRoute::class.qualifiedName!!,
        routeObject = HomeRoute,
        title = "首页",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        route = EventsRoute::class.qualifiedName!!,
        routeObject = EventsRoute,
        title = "事件",
        selectedIcon = Icons.Filled.Event,
        unselectedIcon = Icons.Outlined.Event
    ),
    BottomNavItem(
        route = AnniversariesRoute::class.qualifiedName!!,
        routeObject = AnniversariesRoute,
        title = "纪念",
        selectedIcon = Icons.Filled.Cake,
        unselectedIcon = Icons.Outlined.Cake
    ),
    BottomNavItem(
        route = ChatRoute::class.qualifiedName!!,
        routeObject = ChatRoute,
        title = "对话",
        selectedIcon = Icons.AutoMirrored.Filled.Chat,
        unselectedIcon = Icons.Outlined.Chat
    ),
    BottomNavItem(
        route = ContactsRoute::class.qualifiedName!!,
        routeObject = ContactsRoute,
        title = "人物",
        selectedIcon = Icons.Filled.People,
        unselectedIcon = Icons.Outlined.People
    )
)
