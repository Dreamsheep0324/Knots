package com.tang.prm.feature.events.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.tang.prm.feature.events.AddEventScreen
import com.tang.prm.feature.events.EventDetailScreen
import com.tang.prm.feature.events.EventsScreen
import com.tang.prm.ui.navigation.AddEventRoute
import com.tang.prm.ui.navigation.EditEventRoute
import com.tang.prm.ui.navigation.EventDetailRoute
import com.tang.prm.ui.navigation.EventsRoute

fun NavGraphBuilder.eventsGraph(navController: NavHostController) {
    composable<EventsRoute> {
        EventsScreen(navController = navController)
    }
    composable<EventDetailRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<EventDetailRoute>()
        EventDetailScreen(eventId = route.eventId, navController = navController)
    }
    composable<EditEventRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<EditEventRoute>()
        AddEventScreen(eventId = route.eventId, navController = navController)
    }
    composable<AddEventRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<AddEventRoute>()
        val contactId = route.contactId.let { if (it == 0L) null else it }
        AddEventScreen(contactId = contactId, navController = navController)
    }
}
