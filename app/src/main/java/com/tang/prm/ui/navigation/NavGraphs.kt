package com.tang.prm.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.navigation.NavBackStackEntry
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tang.prm.ui.anniversary.AddAnniversaryScreen
import com.tang.prm.ui.anniversary.AnniversaryDetailScreen
import com.tang.prm.ui.anniversary.AnniversariesScreen
import com.tang.prm.ui.chat.AddChatScreen
import com.tang.prm.ui.chat.ChatDetailScreen
import com.tang.prm.ui.chat.ChatScreen
import com.tang.prm.ui.contacts.AddContactScreen
import com.tang.prm.ui.contacts.ContactDetailScreen
import com.tang.prm.ui.contacts.ContactsScreen
import com.tang.prm.ui.events.AddEventScreen
import com.tang.prm.ui.events.EventDetailScreen
import com.tang.prm.ui.events.EventsScreen
import com.tang.prm.ui.home.ContactListScreen
import com.tang.prm.ui.home.FavoritesScreen
import com.tang.prm.ui.home.FootprintsScreen
import com.tang.prm.ui.home.GiftDetailScreen
import com.tang.prm.ui.home.GiftsScreen
import com.tang.prm.ui.home.HomeScreen
import com.tang.prm.ui.home.AddGiftScreen
import com.tang.prm.ui.home.PhotoAlbumScreen
import com.tang.prm.ui.home.ThoughtsScreen
import com.tang.prm.ui.profile.AboutScreen
import com.tang.prm.ui.profile.BackupScreen
import com.tang.prm.ui.profile.SettingsScreen
import com.tang.prm.ui.profile.ThemeScreen

private inline fun <reified T> androidx.navigation.NavBackStackEntry.navArg(key: String): T {
    val args = arguments ?: throw IllegalArgumentException("No arguments")
    return when (T::class) {
        Long::class -> args.getLong(key) as T
        String::class -> args.getString(key) as T
        Int::class -> args.getInt(key) as T
        else -> throw IllegalArgumentException("Unsupported type ${T::class}")
    }
}

internal fun navTransitions(bottomRoutes: Set<String>): NavTransitions {
    return NavTransitions(
        enterTransition = {
            if (targetState.destination.route in bottomRoutes && initialState.destination.route in bottomRoutes) {
                fadeIn(tween(0))
            } else {
                fadeIn(tween(450, delayMillis = 50, easing = FastOutSlowInEasing)) +
                    scaleIn(
                        animationSpec = tween(450, delayMillis = 50, easing = FastOutSlowInEasing),
                        initialScale = 0.96f
                    )
            }
        },
        exitTransition = {
            if (targetState.destination.route in bottomRoutes && initialState.destination.route in bottomRoutes) {
                fadeOut(tween(0))
            } else {
                fadeOut(tween(200)) +
                    scaleOut(
                        animationSpec = tween(200),
                        targetScale = 0.96f
                    )
            }
        },
        popEnterTransition = {
            if (targetState.destination.route in bottomRoutes && initialState.destination.route in bottomRoutes) {
                fadeIn(tween(0))
            } else {
                fadeIn(tween(450, delayMillis = 50, easing = FastOutSlowInEasing)) +
                    scaleIn(
                        animationSpec = tween(450, delayMillis = 50, easing = FastOutSlowInEasing),
                        initialScale = 0.96f
                    )
            }
        },
        popExitTransition = {
            if (targetState.destination.route in bottomRoutes && initialState.destination.route in bottomRoutes) {
                fadeOut(tween(0))
            } else {
                fadeOut(tween(200)) +
                    scaleOut(
                        animationSpec = tween(200),
                        targetScale = 0.96f
                    )
            }
        }
    )
}

internal data class NavTransitions(
    val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition,
    val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition,
    val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition,
    val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition
)

fun NavGraphBuilder.homeGraph(
    navController: NavHostController,
    overlayVisible: Boolean,
    onOverlayVisibleChange: (Boolean) -> Unit
) {
    composable(Screen.Home.route) {
        HomeScreen(navController = navController)
    }
    composable(Screen.Gifts.route) {
        GiftsScreen(navController = navController)
    }
    composable(Screen.AddGift.route) {
        AddGiftScreen(navController = navController)
    }
    composable(
        route = Screen.EditGift.route,
        arguments = listOf(navArgument("giftId") { type = NavType.LongType })
    ) { backStackEntry ->
        val giftId = backStackEntry.navArg<Long>("giftId")
        AddGiftScreen(giftId = giftId, navController = navController)
    }
    composable(
        route = Screen.GiftDetail.route,
        arguments = listOf(navArgument("giftId") { type = NavType.LongType })
    ) { backStackEntry ->
        val giftId = backStackEntry.navArg<Long>("giftId")
        GiftDetailScreen(giftId = giftId, navController = navController)
    }
    composable(
        route = Screen.PhotoAlbum.route,
        arguments = listOf(navArgument("photoId") {
            type = NavType.LongType
            defaultValue = 0L
        })
    ) { backStackEntry ->
        val photoId = backStackEntry.navArg<Long>("photoId").let { if (it == 0L) null else it }
        PhotoAlbumScreen(navController = navController, initialPhotoId = photoId)
    }
    composable(Screen.Footprints.route) {
        FootprintsScreen(navController = navController)
    }
    composable(Screen.Thoughts.route) {
        ThoughtsScreen(
            onBack = { navController.popBackStack() }
        )
    }
    composable(Screen.ContactList.route) {
        ContactListScreen(navController = navController)
    }
    composable(Screen.Favorites.route) {
        FavoritesScreen(navController = navController)
    }
}

fun NavGraphBuilder.eventsGraph(navController: NavHostController) {
    composable(Screen.Events.route) {
        EventsScreen(navController = navController)
    }
    composable(
        route = Screen.EventDetail.route,
        arguments = listOf(navArgument("eventId") { type = NavType.LongType })
    ) { backStackEntry ->
        val eventId = backStackEntry.navArg<Long>("eventId")
        EventDetailScreen(eventId = eventId, navController = navController)
    }
    composable(
        route = Screen.EditEvent.route,
        arguments = listOf(navArgument("eventId") { type = NavType.LongType })
    ) { backStackEntry ->
        val eventId = backStackEntry.navArg<Long>("eventId")
        AddEventScreen(eventId = eventId, navController = navController)
    }
    composable(
        route = Screen.AddEvent.route,
        arguments = listOf(navArgument("contactId") {
            type = NavType.LongType
            defaultValue = 0L
        })
    ) { backStackEntry ->
        val contactId = backStackEntry.navArg<Long>("contactId").let { if (it == 0L) null else it }
        AddEventScreen(contactId = contactId, navController = navController)
    }
}

fun NavGraphBuilder.anniversaryGraph(navController: NavHostController) {
    composable(Screen.Anniversaries.route) {
        AnniversariesScreen(navController = navController)
    }
    composable(
        route = Screen.AddAnniversary.route,
        arguments = listOf(navArgument("contactId") {
            type = NavType.LongType
            defaultValue = 0L
        })
    ) { backStackEntry ->
        val contactId = backStackEntry.navArg<Long>("contactId").let { if (it == 0L) null else it }
        AddAnniversaryScreen(contactId = contactId, navController = navController)
    }
    composable(
        route = Screen.AnniversaryDetail.route,
        arguments = listOf(navArgument("anniversaryId") { type = NavType.LongType })
    ) { backStackEntry ->
        val anniversaryId = backStackEntry.navArg<Long>("anniversaryId")
        AnniversaryDetailScreen(anniversaryId = anniversaryId, navController = navController)
    }
    composable(
        route = Screen.EditAnniversary.route,
        arguments = listOf(navArgument("anniversaryId") { type = NavType.LongType })
    ) { backStackEntry ->
        val anniversaryId = backStackEntry.navArg<Long>("anniversaryId")
        AddAnniversaryScreen(anniversaryId = anniversaryId, navController = navController)
    }
}

fun NavGraphBuilder.chatGraph(navController: NavHostController) {
    composable(Screen.Chat.route) {
        ChatScreen(navController = navController)
    }
    composable(
        route = Screen.AddChat.route,
        arguments = listOf(navArgument("contactId") {
            type = NavType.LongType
            defaultValue = 0L
        })
    ) { backStackEntry ->
        val contactId = backStackEntry.navArg<Long>("contactId").let { if (it == 0L) null else it }
        AddChatScreen(contactId = contactId, navController = navController)
    }
    composable(
        route = Screen.ChatDetail.route,
        arguments = listOf(navArgument("eventId") { type = NavType.LongType })
    ) { backStackEntry ->
        val eventId = backStackEntry.navArg<Long>("eventId")
        ChatDetailScreen(eventId = eventId, navController = navController)
    }
    composable(
        route = Screen.EditChat.route,
        arguments = listOf(navArgument("eventId") { type = NavType.LongType })
    ) { backStackEntry ->
        val eventId = backStackEntry.navArg<Long>("eventId")
        AddChatScreen(eventId = eventId, navController = navController)
    }
}

fun NavGraphBuilder.contactsGraph(
    navController: NavHostController,
    onOverlayVisibleChange: (Boolean) -> Unit
) {
    composable(Screen.Contacts.route) {
        ContactsScreen(navController = navController, onOverlayVisibleChange = onOverlayVisibleChange)
    }
    composable(Screen.AddContact.route) {
        AddContactScreen(navController = navController)
    }
    composable(
        route = Screen.ContactDetail.route,
        arguments = listOf(navArgument("contactId") { type = NavType.LongType })
    ) { backStackEntry ->
        val contactId = backStackEntry.navArg<Long>("contactId")
        ContactDetailScreen(contactId = contactId, navController = navController)
    }
    composable(
        route = Screen.EditContact.route,
        arguments = listOf(navArgument("contactId") { type = NavType.LongType })
    ) { backStackEntry ->
        val contactId = backStackEntry.navArg<Long>("contactId")
        AddContactScreen(contactId = contactId, navController = navController)
    }
}

fun NavGraphBuilder.settingsGraph(navController: NavHostController) {
    composable(Screen.Settings.route) {
        SettingsScreen(navController = navController)
    }
    composable(Screen.ThemeSettings.route) {
        ThemeScreen(navController = navController)
    }
    composable(Screen.BackupRestore.route) {
        BackupScreen(navController = navController)
    }
    composable(Screen.About.route) {
        AboutScreen(navController = navController)
    }
}
