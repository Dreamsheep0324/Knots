package com.tang.prm.feature.remember.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.tang.prm.feature.remember.anniversary.AddAnniversaryScreen
import com.tang.prm.feature.remember.anniversary.AnniversaryDetailScreen
import com.tang.prm.feature.remember.anniversary.AnniversariesScreen
import com.tang.prm.ui.navigation.AddAnniversaryRoute
import com.tang.prm.ui.navigation.AnniversariesRoute
import com.tang.prm.ui.navigation.AnniversaryDetailRoute
import com.tang.prm.ui.navigation.EditAnniversaryRoute

fun NavGraphBuilder.rememberGraph(
    navController: NavHostController,
    isTabletLayout: Boolean = false
) {
    composable<AnniversariesRoute> {
        AnniversariesScreen(navController = navController, isTabletLayout = isTabletLayout)
    }
    composable<AddAnniversaryRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<AddAnniversaryRoute>()
        val contactId = route.contactId.let { if (it == 0L) null else it }
        AddAnniversaryScreen(contactId = contactId, navController = navController)
    }
    composable<AnniversaryDetailRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<AnniversaryDetailRoute>()
        AnniversaryDetailScreen(anniversaryId = route.anniversaryId, navController = navController)
    }
    composable<EditAnniversaryRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<EditAnniversaryRoute>()
        AddAnniversaryScreen(anniversaryId = route.anniversaryId, navController = navController)
    }
}
