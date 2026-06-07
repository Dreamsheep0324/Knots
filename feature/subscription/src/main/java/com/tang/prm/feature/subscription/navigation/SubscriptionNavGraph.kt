package com.tang.prm.feature.subscription.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.tang.prm.feature.subscription.subscription.AddSubscriptionScreen
import com.tang.prm.feature.subscription.subscription.SubscriptionDetailScreen
import com.tang.prm.feature.subscription.subscription.SubscriptionStatsScreen
import com.tang.prm.feature.subscription.subscription.SubscriptionsScreen
import com.tang.prm.ui.navigation.AddSubscriptionRoute
import com.tang.prm.ui.navigation.EditSubscriptionRoute
import com.tang.prm.ui.navigation.SubscriptionDetailRoute
import com.tang.prm.ui.navigation.SubscriptionStatsRoute
import com.tang.prm.ui.navigation.SubscriptionsRoute

fun NavGraphBuilder.subscriptionGraph(navController: NavHostController) {
    composable<SubscriptionsRoute> {
        SubscriptionsScreen(navController = navController)
    }
    composable<SubscriptionStatsRoute> {
        SubscriptionStatsScreen(onBack = { navController.popBackStack() })
    }
    composable<AddSubscriptionRoute> {
        AddSubscriptionScreen(
            subscriptionId = 0L,
            onBack = { navController.popBackStack() },
            onSaved = { navController.popBackStack() }
        )
    }
    composable<EditSubscriptionRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<EditSubscriptionRoute>()
        AddSubscriptionScreen(
            subscriptionId = route.subscriptionId,
            onBack = { navController.popBackStack() },
            onSaved = { navController.popBackStack() }
        )
    }
    composable<SubscriptionDetailRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<SubscriptionDetailRoute>()
        SubscriptionDetailScreen(
            subscriptionId = route.subscriptionId,
            navController = navController
        )
    }
}
