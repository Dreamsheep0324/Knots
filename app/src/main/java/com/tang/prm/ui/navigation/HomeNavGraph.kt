package com.tang.prm.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.tang.prm.feature.divination.DivinationScreen
import com.tang.prm.feature.divination.DivinationHistoryScreen
import com.tang.prm.feature.divination.liuyao.LiuyaoCastScreen
import com.tang.prm.feature.divination.liuyao.LiuyaoResultScreen
import com.tang.prm.feature.divination.meihua.MeihuaMethodScreen
import com.tang.prm.feature.divination.meihua.MeihuaResultScreen
import com.tang.prm.feature.divination.meihua.ExternalOmenScreen
import com.tang.prm.feature.encounter.gifts.AddGiftScreen
import com.tang.prm.ui.home.ContactListScreen
import com.tang.prm.feature.reflect.favorites.FavoritesScreen
import com.tang.prm.feature.reflect.footprints.FootprintsScreen
import com.tang.prm.feature.encounter.gifts.GiftDetailScreen
import com.tang.prm.feature.encounter.gifts.GiftsScreen
import com.tang.prm.ui.home.HomeScreen
import com.tang.prm.feature.reflect.album.PhotoAlbumScreen
import com.tang.prm.feature.reflect.thoughts.ThoughtsScreen

internal fun NavGraphBuilder.homeGraph(
    navController: NavHostController,
    overlayVisible: Boolean,
    onOverlayVisibleChange: (Boolean) -> Unit
) {
    composable<HomeRoute> {
        HomeScreen(navController = navController)
    }
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
    composable<PhotoAlbumRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<PhotoAlbumRoute>()
        val photoId = route.photoId.let { if (it == 0L) null else it }
        PhotoAlbumScreen(navController = navController, initialPhotoId = photoId)
    }
    composable<FootprintsRoute> {
        FootprintsScreen(navController = navController)
    }
    composable<ThoughtsRoute> {
        ThoughtsScreen(
            onBack = { navController.popBackStack() }
        )
    }
    composable<ContactListRoute> {
        ContactListScreen(navController = navController)
    }
    composable<FavoritesRoute> {
        FavoritesScreen(navController = navController)
    }
    composable<DivinationRoute> {
        DivinationScreen(navController = navController)
    }
    composable<LiuyaoCastRoute> {
        LiuyaoCastScreen(navController = navController)
    }
    composable<LiuyaoResultRoute> {
        LiuyaoResultScreen(navController = navController)
    }
    composable<MeihuaMethodRoute> {
        MeihuaMethodScreen(navController = navController)
    }
    composable<MeihuaResultRoute> {
        MeihuaResultScreen(navController = navController)
    }
    composable<ExternalOmenRoute> {
        ExternalOmenScreen(navController = navController)
    }
    composable<DivinationHistoryRoute> {
        DivinationHistoryScreen(navController = navController)
    }
}
