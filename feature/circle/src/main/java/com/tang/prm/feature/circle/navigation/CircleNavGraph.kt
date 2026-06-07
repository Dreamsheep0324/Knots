package com.tang.prm.feature.circle.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.tang.prm.feature.circle.ContactListScreen
import com.tang.prm.ui.navigation.ContactListRoute

fun NavGraphBuilder.circleGraph(navController: NavHostController) {
    composable<ContactListRoute> {
        ContactListScreen(navController = navController)
    }
}
