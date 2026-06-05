package com.tang.prm.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.tang.prm.feature.encounter.chat.AddChatScreen
import com.tang.prm.feature.encounter.chat.ChatDetailScreen
import com.tang.prm.feature.encounter.chat.ChatScreen

internal fun NavGraphBuilder.chatGraph(navController: NavHostController) {
    composable<ChatRoute> {
        ChatScreen(navController = navController)
    }
    composable<AddChatRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<AddChatRoute>()
        val contactId = route.contactId.let { if (it == 0L) null else it }
        AddChatScreen(contactId = contactId, navController = navController)
    }
    composable<ChatDetailRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<ChatDetailRoute>()
        ChatDetailScreen(eventId = route.eventId, navController = navController)
    }
    composable<EditChatRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<EditChatRoute>()
        AddChatScreen(eventId = route.eventId, navController = navController)
    }
}
