package com.tang.prm.feature.people.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.tang.prm.feature.people.contacts.AddContactScreen
import com.tang.prm.feature.people.contacts.AddContactViewModel
import com.tang.prm.feature.people.contacts.ContactDetailScreen
import com.tang.prm.feature.people.contacts.ContactDetailTabletScreen
import com.tang.prm.feature.people.contacts.ContactsScreen
import com.tang.prm.ui.components.photo.AvatarCropDialog
import com.tang.prm.ui.components.photo.PhotoPickerConfig
import com.tang.prm.ui.components.photo.rememberPhotoPickerLauncher
import com.tang.prm.ui.navigation.AddContactRoute
import com.tang.prm.ui.navigation.ContactDetailRoute
import com.tang.prm.ui.navigation.ContactsRoute
import com.tang.prm.ui.navigation.EditContactRoute

fun NavGraphBuilder.peopleGraph(
    navController: NavHostController,
    onOverlayVisibleChange: (Boolean) -> Unit,
    isTabletLayout: Boolean = false
) {
    composable<ContactsRoute> {
        ContactsScreen(
            navController = navController,
            onOverlayVisibleChange = onOverlayVisibleChange,
            isTabletLayout = isTabletLayout
        )
    }
    composable<AddContactRoute> {
        val viewModel: AddContactViewModel = hiltViewModel()
        var pendingCropPath by remember { mutableStateOf<String?>(null) }
        val avatarPicker = rememberPhotoPickerLauncher(
            config = PhotoPickerConfig(maxCount = 1, prefix = "avatar")
        ) { result ->
            result.localPaths.firstOrNull()?.let { pendingCropPath = it }
        }
        pendingCropPath?.let { path ->
            AvatarCropDialog(
                imagePath = path,
                onCropComplete = { croppedPath ->
                    viewModel.updateAvatar(croppedPath)
                    pendingCropPath = null
                },
                onDismiss = { pendingCropPath = null }
            )
        }
        AddContactScreen(navController = navController, onPickAvatar = { avatarPicker.launch() }, viewModel = viewModel)
    }
    composable<ContactDetailRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<ContactDetailRoute>()
        if (isTabletLayout) {
            ContactDetailTabletScreen(contactId = route.contactId, navController = navController)
        } else {
            ContactDetailScreen(contactId = route.contactId, navController = navController)
        }
    }
    composable<EditContactRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<EditContactRoute>()
        val viewModel: AddContactViewModel = hiltViewModel()
        var pendingCropPath by remember { mutableStateOf<String?>(null) }
        val avatarPicker = rememberPhotoPickerLauncher(
            config = PhotoPickerConfig(maxCount = 1, prefix = "avatar")
        ) { result ->
            result.localPaths.firstOrNull()?.let { pendingCropPath = it }
        }
        pendingCropPath?.let { path ->
            AvatarCropDialog(
                imagePath = path,
                onCropComplete = { croppedPath ->
                    viewModel.updateAvatar(croppedPath)
                    pendingCropPath = null
                },
                onDismiss = { pendingCropPath = null }
            )
        }
        AddContactScreen(navController = navController, onPickAvatar = { avatarPicker.launch() }, viewModel = viewModel)
    }
}
