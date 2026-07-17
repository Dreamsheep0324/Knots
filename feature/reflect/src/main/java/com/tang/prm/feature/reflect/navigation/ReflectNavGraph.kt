package com.tang.prm.feature.reflect.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.tang.prm.feature.reflect.album.PhotoAlbumScreen
import com.tang.prm.feature.reflect.favorites.FavoritesScreen
import com.tang.prm.feature.reflect.footprints.FootprintsScreen
import com.tang.prm.feature.reflect.thoughts.ThoughtsScreen
import com.tang.prm.ui.navigation.FavoritesRoute
import com.tang.prm.ui.navigation.FootprintsRoute
import com.tang.prm.ui.navigation.PhotoAlbumRoute
import com.tang.prm.ui.navigation.ThoughtsRoute

fun NavGraphBuilder.reflectGraph(navController: NavHostController, isTabletLayout: Boolean = false) {
    composable<PhotoAlbumRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<PhotoAlbumRoute>()
        val photoId = route.photoId.let { if (it == 0L) null else it }
        PhotoAlbumScreen(navController = navController, initialPhotoId = photoId, isTabletLayout = isTabletLayout)
    }
    composable<FootprintsRoute> {
        FootprintsScreen(navController = navController, isTabletLayout = isTabletLayout)
    }
    composable<ThoughtsRoute> {
        ThoughtsScreen(
            onBack = { navController.popBackStack() },
            isTabletLayout = isTabletLayout
        )
    }
    composable<FavoritesRoute> {
        FavoritesScreen(navController = navController, isTabletLayout = isTabletLayout)
    }
}
