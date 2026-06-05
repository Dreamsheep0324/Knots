package com.tang.prm.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.tang.prm.feature.remember.anniversary.AddAnniversaryScreen
import com.tang.prm.feature.remember.anniversary.AnniversaryDetailScreen
import com.tang.prm.feature.remember.anniversary.AnniversariesScreen
import com.tang.prm.feature.people.contacts.AddContactScreen
import com.tang.prm.feature.people.contacts.AddContactViewModel
import com.tang.prm.feature.people.contacts.ContactDetailScreen
import com.tang.prm.feature.people.contacts.ContactsScreen
import com.tang.prm.ui.components.photo.PhotoPickerConfig
import com.tang.prm.ui.components.photo.rememberPhotoPickerLauncher
import com.tang.prm.ui.profile.AboutScreen
import com.tang.prm.ui.profile.AiConfigScreen
import com.tang.prm.ui.profile.BackupScreen
import com.tang.prm.ui.profile.SettingsScreen
import com.tang.prm.ui.profile.ThemeScreen

internal fun NavGraphBuilder.anniversaryGraph(navController: NavHostController) {
    composable<AnniversariesRoute> {
        AnniversariesScreen(navController = navController)
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

internal fun NavGraphBuilder.contactsGraph(
    navController: NavHostController,
    onOverlayVisibleChange: (Boolean) -> Unit
) {
    composable<ContactsRoute> {
        ContactsScreen(navController = navController, onOverlayVisibleChange = onOverlayVisibleChange)
    }
    composable<AddContactRoute> {
        val viewModel: AddContactViewModel = hiltViewModel()
        val avatarPicker = rememberPhotoPickerLauncher(
            config = PhotoPickerConfig(maxCount = 1, prefix = "avatar")
        ) { result ->
            result.localPaths.firstOrNull()?.let { viewModel.updateAvatar(it) }
        }
        AddContactScreen(navController = navController, onPickAvatar = { avatarPicker.launch() }, viewModel = viewModel)
    }
    composable<ContactDetailRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<ContactDetailRoute>()
        ContactDetailScreen(contactId = route.contactId, navController = navController)
    }
    composable<EditContactRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<EditContactRoute>()
        val viewModel: AddContactViewModel = hiltViewModel()
        val avatarPicker = rememberPhotoPickerLauncher(
            config = PhotoPickerConfig(maxCount = 1, prefix = "avatar")
        ) { result ->
            result.localPaths.firstOrNull()?.let { viewModel.updateAvatar(it) }
        }
        AddContactScreen(contactId = route.contactId, navController = navController, onPickAvatar = { avatarPicker.launch() }, viewModel = viewModel)
    }
}

internal fun NavGraphBuilder.settingsGraph(navController: NavHostController) {
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
    composable<AboutRoute> {
        AboutScreen(navController = navController)
    }
}
