package com.tang.prm.feature.remember.anniversary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.tang.prm.ui.components.AppDatePicker
import com.tang.prm.ui.components.ContactPickerDialog
import com.tang.prm.ui.components.FormScreenScaffold
import com.tang.prm.ui.components.DiscardEditDialog

@Composable
fun AddAnniversaryScreen(
    contactId: Long? = null,
    anniversaryId: Long? = null,
    navController: NavController,
    viewModel: AddAnniversaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val nameFocusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }
    var showContactSelector by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showIconPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { nameFocusRequester.requestFocus() }

    LaunchedEffect(contactId) {
        contactId?.let { id ->
            val contact = uiState.contacts.firstOrNull { it.id == id }
            contact?.let { viewModel.updateContact(it) }
        }
    }

    var showExitDialog by remember { mutableStateOf(false) }
    BackHandler(enabled = uiState.hasUnsavedChanges) { showExitDialog = true }

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
            initialDate = uiState.date
        )
    }

    if (showContactSelector) {
        ContactPickerDialog(
            contacts = uiState.contacts,
            onContactSelected = {
                viewModel.updateContact(it)
                showContactSelector = false
            },
            onDismiss = { showContactSelector = false }
        )
    }

    if (showIconPicker) {
        IconPickerDialog(
            selectedIcon = uiState.selectedIcon,
            onIconSelected = {
                viewModel.updateSelectedIcon(it)
                showIconPicker = false
            },
            onDismiss = { showIconPicker = false }
        )
    }

    FormScreenScaffold(
        title = if (uiState.isEditing) "编辑纪念日" else "新建纪念日",
        onSaveClick = { viewModel.saveAnniversary() },
        saveEnabled = uiState.name.isNotBlank(),
        onBackClick = { if (uiState.hasUnsavedChanges) showExitDialog = true else navController.popBackStack() },
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHostState = snackbarHostState
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                AnniversaryNameSection(
                    selectedIcon = uiState.selectedIcon,
                    name = uiState.name,
                    onNameChange = viewModel::updateName,
                    onIconClick = { showIconPicker = true },
                    nameFocusRequester = nameFocusRequester
                )
            }

            item {
                AnniversaryTypeSection(
                    selectedType = uiState.selectedType,
                    onTypeSelected = viewModel::updateSelectedType
                )
            }

            item {
                AnniversaryContactSection(
                    contactName = uiState.contactName,
                    onClick = { showContactSelector = true }
                )
            }

            item {
                AnniversaryDateSection(
                    dateText = uiState.dateText,
                    isLunar = uiState.isLunar,
                    isLeapMonth = uiState.isLeapMonth,
                    isRepeat = uiState.isRepeat,
                    onDateClick = { showDatePicker = true },
                    onLunarChange = { viewModel.updateIsLunar(it) },
                    onLeapMonthChange = { viewModel.updateIsLeapMonth(it) },
                    onRepeatChange = { viewModel.updateIsRepeat(it) }
                )
            }

            item {
                AnniversaryRemarksSection(
                    remarks = uiState.remarks,
                    onRemarksChange = viewModel::updateRemarks
                )
            }
        }
    }
}
