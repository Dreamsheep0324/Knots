package com.tang.prm.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.tang.prm.feature.home.HomeScreen
import com.tang.prm.ui.navigation.HomeRoute

fun NavGraphBuilder.homeGraph(
    navController: NavHostController,
    overlayVisible: Boolean,
    onOverlayVisibleChange: (Boolean) -> Unit,
    isTabletLayout: Boolean = false
) {
    composable<HomeRoute> {
        HomeScreen(
            navController = navController,
            isTabletLayout = isTabletLayout
        )
    }
}
