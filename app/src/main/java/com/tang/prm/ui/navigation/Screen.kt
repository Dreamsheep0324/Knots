package com.tang.prm.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Events : Screen("events")
    object Anniversaries : Screen("anniversaries")
    object Chat : Screen("chat")
    object Contacts : Screen("contacts")
    object ContactDetail : Screen("contact/{contactId}") {
        fun createRoute(contactId: Long) = "contact/$contactId"
    }
    object AddContact : Screen("add_contact")
    object EditContact : Screen("edit_contact/{contactId}") {
        fun createRoute(contactId: Long) = "edit_contact/$contactId"
    }
    object EventDetail : Screen("event/{eventId}") {
        fun createRoute(eventId: Long) = "event/$eventId"
    }
    object EditEvent : Screen("edit_event/{eventId}") {
        fun createRoute(eventId: Long) = "edit_event/$eventId"
    }
    object AddEvent : Screen("add_event?contactId={contactId}") {
        fun createRoute(contactId: Long? = null) = if (contactId != null) "add_event?contactId=$contactId" else "add_event"
    }
    object AnniversaryDetail : Screen("anniversary/{anniversaryId}") {
        fun createRoute(anniversaryId: Long) = "anniversary/$anniversaryId"
    }
    object EditAnniversary : Screen("edit_anniversary/{anniversaryId}") {
        fun createRoute(anniversaryId: Long) = "edit_anniversary/$anniversaryId"
    }
    object AddAnniversary : Screen("add_anniversary?contactId={contactId}") {
        fun createRoute(contactId: Long? = null) = if (contactId != null) "add_anniversary?contactId=$contactId" else "add_anniversary"
    }
    object AddChat : Screen("add_chat?contactId={contactId}") {
        fun createRoute(contactId: Long? = null) = if (contactId != null) "add_chat?contactId=$contactId" else "add_chat"
    }
    object ChatDetail : Screen("chat/{eventId}") {
        fun createRoute(eventId: Long) = "chat/$eventId"
    }
    object EditChat : Screen("edit_chat/{eventId}") {
        fun createRoute(eventId: Long) = "edit_chat/$eventId"
    }
    object Settings : Screen("settings")
    object ThemeSettings : Screen("theme_settings")
    object BackupRestore : Screen("backup_restore")
    object About : Screen("about")
    object Gifts : Screen("gifts")
    object AddGift : Screen("add_gift")
    object EditGift : Screen("edit_gift/{giftId}") {
        fun createRoute(giftId: Long) = "edit_gift/$giftId"
    }
    object GiftDetail : Screen("gift/{giftId}") {
        fun createRoute(giftId: Long) = "gift/$giftId"
    }
    object PhotoAlbum : Screen("photo_album?photoId={photoId}") {
        fun createRoute(photoId: Long? = null) = if (photoId != null) "photo_album?photoId=$photoId" else "photo_album"
    }
    object Footprints : Screen("footprints")
    object Thoughts : Screen("thoughts")
    object ContactList : Screen("contact_list")
    object Favorites : Screen("favorites")
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = Screen.Home.route,
        title = "首页",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        route = Screen.Events.route,
        title = "事件",
        selectedIcon = Icons.Filled.Event,
        unselectedIcon = Icons.Outlined.Event
    ),
    BottomNavItem(
        route = Screen.Anniversaries.route,
        title = "纪念",
        selectedIcon = Icons.Filled.Cake,
        unselectedIcon = Icons.Outlined.Cake
    ),
    BottomNavItem(
        route = Screen.Chat.route,
        title = "对话",
        selectedIcon = Icons.AutoMirrored.Filled.Chat,
        unselectedIcon = Icons.Outlined.Chat
    ),
    BottomNavItem(
        route = Screen.Contacts.route,
        title = "人物",
        selectedIcon = Icons.Filled.People,
        unselectedIcon = Icons.Outlined.People
    )
)
