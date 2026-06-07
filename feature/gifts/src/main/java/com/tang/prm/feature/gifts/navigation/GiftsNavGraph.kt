package com.tang.prm.feature.gifts.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.tang.prm.feature.gifts.AddGiftScreen
import com.tang.prm.feature.gifts.GiftDetailScreen
import com.tang.prm.feature.gifts.GiftsScreen
import com.tang.prm.ui.navigation.AddGiftRoute
import com.tang.prm.ui.navigation.EditGiftRoute
import com.tang.prm.ui.navigation.GiftDetailRoute
import com.tang.prm.ui.navigation.GiftsRoute

fun NavGraphBuilder.giftsGraph(navController: NavHostController) {
    composable<GiftsRoute> {
        GiftsScreen(navController = navController)
    }
    composable<AddGiftRoute> {
        AddGiftScreen(navController = navController)
    }
    composable<EditGiftRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<EditGiftRoute>()
        AddGiftScreen(giftId = route.giftId, navController = navController)
    }
    composable<GiftDetailRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<GiftDetailRoute>()
        GiftDetailScreen(giftId = route.giftId, navController = navController)
    }
}
