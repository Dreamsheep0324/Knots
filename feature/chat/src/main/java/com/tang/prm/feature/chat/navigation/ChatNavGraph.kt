package com.tang.prm.feature.chat.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.tang.prm.feature.chat.AddChatScreen
import com.tang.prm.feature.chat.ChatDetailScreen
import com.tang.prm.feature.chat.ChatScreen
import com.tang.prm.ui.navigation.AddChatRoute
import com.tang.prm.ui.navigation.ChatDetailRoute
import com.tang.prm.ui.navigation.ChatRoute
import com.tang.prm.ui.navigation.EditChatRoute

fun NavGraphBuilder.chatGraph(navController: NavHostController) {
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
