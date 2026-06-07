package com.tang.prm.feature.profile.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.tang.prm.feature.profile.AboutScreen
import com.tang.prm.feature.profile.AiConfigScreen
import com.tang.prm.feature.profile.BackupScreen
import com.tang.prm.feature.profile.SettingsScreen
import com.tang.prm.feature.profile.ThemeScreen
import com.tang.prm.feature.profile.WebDavSyncScreen
import com.tang.prm.ui.navigation.AboutRoute
import com.tang.prm.ui.navigation.AiConfigRoute
import com.tang.prm.ui.navigation.BackupRestoreRoute
import com.tang.prm.ui.navigation.SettingsRoute
import com.tang.prm.ui.navigation.ThemeSettingsRoute
import com.tang.prm.ui.navigation.WebDavSyncRoute

fun NavGraphBuilder.profileGraph(navController: NavHostController) {
    composable<SettingsRoute> {
        SettingsScreen(navController = navController)
    }
    composable<ThemeSettingsRoute> {
        ThemeScreen(navController = navController)
    }
    composable<AiConfigRoute> {
        AiConfigScreen(navController = navController)
    }
    composable<BackupRestoreRoute> {
        BackupScreen(navController = navController)
    }
    composable<WebDavSyncRoute> {
        WebDavSyncScreen(onBack = { navController.popBackStack() })
    }
    composable<AboutRoute> {
        AboutScreen(navController = navController)
    }
}
