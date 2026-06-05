package com.tang.prm.feature.encounter.chat

import androidx.activity.compose.BackHandler
import com.tang.prm.ui.components.photo.PhotoPickerConfig
import com.tang.prm.ui.components.photo.rememberPhotoPickerLauncher
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tang.prm.ui.components.AppDatePicker
import com.tang.prm.ui.components.ContactPickerDialog
import com.tang.prm.ui.components.DiscardEditDialog
import com.tang.prm.ui.components.FormScreenScaffold
import com.tang.prm.feature.encounter.chat.components.ContactSelectionCard
import com.tang.prm.feature.encounter.chat.components.DialogueInputCard
import com.tang.prm.feature.encounter.chat.components.RemarksCard
import com.tang.prm.feature.encounter.chat.components.SceneInfoCard
import com.tang.prm.ui.theme.Dimens

@Composable
fun AddChatScreen(
    contactId: Long? = null,
    eventId: Long? = null,
    navController: NavController,
    viewModel: AddChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val titleFocusRequester = remember { FocusRequester() }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { titleFocusRequester.requestFocus() }

    val chatPhotoPicker = rememberPhotoPickerLauncher(
        config = PhotoPickerConfig(maxCount = 1, prefix = "chat")
    ) { result ->
        result.localPaths.firstOrNull()?.let { viewModel.onImagePicked(it) }
    }

    LaunchedEffect(uiState.pendingImageLineId) {
        if (uiState.pendingImageLineId != null) {
            chatPhotoPicker.launch()
        }
    }

    var showExitDialog by remember { mutableStateOf(false) }
    BackHandler(enabled = uiState.hasUnsavedChanges) { showExitDialog = true }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            navController.popBackStack()
        }
    }

    if (showExitDialog) {
        DiscardEditDialog(onDiscard = { showExitDialog = false; navController.popBackStack() }, onDismiss = { showExitDialog = false })
    }

    if (showDatePicker) {
        AppDatePicker(
            show = showDatePicker,
            onDismiss = { showDatePicker = false },
            onDateSelected = { viewModel.updateDate(it) },
            initialDate = uiState.selectedDate
        )
    }

    if (uiState.showContactPicker) {
        ContactPickerDialog(
            contacts = uiState.contacts,
            title = "选择人物",
            subtitle = "选择与谁进行了对话",
            onContactSelected = { viewModel.selectContact(it) },
            onDismiss = { viewModel.hideContactPicker() }
        )
    }

    FormScreenScaffold(
        title = if (uiState.editingEventId != null) "编辑对话" else "新建对话",
        onSaveClick = { viewModel.saveChat() },
        saveEnabled = uiState.selectedContact != null,
        onBackClick = { if (uiState.hasUnsavedChanges) showExitDialog = true else navController.popBackStack() },
        snackbarHostState = snackbarHostState
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Dimens.paddingPage),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
        ) {
            item {
                ContactSelectionCard(
                    selectedContact = uiState.selectedContact,
                    onSelectClick = { viewModel.showContactPicker() },
                    onChangeClick = { viewModel.showContactPicker() }
                )
            }

            item {
                SceneInfoCard(
                    title = uiState.title,
                    selectedDate = uiState.selectedDate,
                    onTitleChange = viewModel::updateTitle,
                    onDateClick = { showDatePicker = true },
                    titleFocusRequester = titleFocusRequester
                )
            }

            item {
                DialogueInputCard(
                    dialogueLines = uiState.dialogueLines,
                    contactName = uiState.selectedContact?.name ?: "对方",
                    contactAvatar = uiState.selectedContact?.avatar,
                    onAddMyLine = { viewModel.addDialogueLine(isMe = true) },
                    onAddTheirLine = { viewModel.addDialogueLine(isMe = false) },
                    onUpdateLine = viewModel::updateDialogueLine,
                    onRemoveLineImage = viewModel::updateDialogueLineImage,
                    onRequestImage = viewModel::requestImageForLine,
                    onToggleSpeaker = viewModel::toggleDialogueSpeaker,
                    onRemoveLine = viewModel::removeDialogueLine,
                    onMoveLine = viewModel::moveDialogueLine
                )
            }

            item {
                RemarksCard(
                    remarks = uiState.remarks ?: "",
                    onRemarksChange = viewModel::updateRemarks
                )
            }
        }
    }
}
