package com.tang.prm.feature.gifts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.GiftType
import com.tang.prm.ui.components.AppDatePicker
import com.tang.prm.ui.components.ContactPickerDialog
import com.tang.prm.ui.components.FormScreenScaffold
import com.tang.prm.ui.components.photo.PhotoPickerConfig
import com.tang.prm.ui.components.photo.rememberPhotoPickerLauncher
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGiftScreen(
    navController: NavController,
    giftId: Long = 0L,
    viewModel: GiftsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val nameFocusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val isEditing = giftId != 0L

    val editingGift by viewModel.getGiftFlow(giftId).collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(Unit) { if (!isEditing) nameFocusRequester.requestFocus() }

    var selectedContact by remember { mutableStateOf<Contact?>(null) }
    var giftName by remember { mutableStateOf("") }
    var giftType by remember { mutableStateOf(GiftType.OTHER) }
    var isSent by remember { mutableStateOf(true) }
    var occasion by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPhotos by remember { mutableStateOf<List<String>>(emptyList()) }
    var showContactPicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var hasLoadedEditData by remember { mutableStateOf(false) }

    LaunchedEffect(editingGift, uiState.data.availableContacts) {
        if (isEditing && editingGift != null && !hasLoadedEditData && uiState.data.availableContacts.isNotEmpty()) {
            editingGift?.let { g ->
                val contact = uiState.data.availableContacts.find { it.id == g.contactId }
                selectedContact = contact
                giftName = g.giftName
                giftType = g.giftType
                isSent = g.isSent
                occasion = g.occasion ?: ""
                description = g.description ?: ""
                selectedPhotos = g.photos
                selectedDate = g.date
            }
            hasLoadedEditData = true
        }
    }

    val giftPhotoPicker = rememberPhotoPickerLauncher(
        config = PhotoPickerConfig(maxCount = 9, prefix = "gift")
    ) { result ->
        selectedPhotos = selectedPhotos + result.localPaths
    }

    val canSave = selectedContact != null && giftName.isNotBlank()

    if (showContactPicker) {
        ContactPickerDialog(
            contacts = uiState.data.availableContacts,
            title = "选择人物",
            onContactSelected = { selectedContact = it; showContactPicker = false },
            onDismiss = { showContactPicker = false }
        )
    }

    if (showDatePicker) {
        AppDatePicker(
            show = showDatePicker,
            onDismiss = { showDatePicker = false },
            onDateSelected = { selectedDate = it },
            initialDate = selectedDate,
            confirmColor = MaterialTheme.colorScheme.primary,
            dismissColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    FormScreenScaffold(
        title = if (isEditing) "编辑礼物" else "新增礼物",
        snackbarHostState = snackbarHostState,
        onSaveClick = {
            selectedContact?.let { contact ->
                val giftRecord = GiftRecord(
                    gift = com.tang.prm.domain.model.Gift(
                        id = if (isEditing) giftId else 0,
                        contactId = contact.id,
                        giftName = giftName,
                        giftType = giftType,
                        date = selectedDate,
                        isSent = isSent,
                        amount = null,
                        occasion = occasion.ifBlank { null },
                        description = description.ifBlank { null },
                        location = null,
                        photos = selectedPhotos,
                        createdAt = if (isEditing) editingGift?.createdAt ?: System.currentTimeMillis() else System.currentTimeMillis()
                    ),
                    contactName = contact.name,
                    contactAvatar = contact.avatar
                )
                if (isEditing) {
                    viewModel.updateGift(giftRecord)
                } else {
                    viewModel.addGift(giftRecord)
                }
                scope.launch {
                    navController.popBackStack()
                }
            }
        },
        saveEnabled = canSave,
        onBackClick = { navController.popBackStack() }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ContactSelectionCard(
                    selectedContact = selectedContact,
                    isSent = isSent,
                    onSelectClick = { showContactPicker = true }
                )
            }

            item {
                DirectionToggleCard(isSent = isSent, onToggle = { isSent = it })
            }

            item {
                GiftNameCard(
                    giftName = giftName,
                    onNameChange = { giftName = it },
                    focusRequester = nameFocusRequester
                )
            }

            item {
                GiftTypeCard(
                    selectedType = giftType,
                    onTypeSelect = { giftType = it }
                )
            }

            item {
                DateSelectionCard(
                    selectedDate = selectedDate,
                    onClick = { showDatePicker = true }
                )
            }

            item {
                OccasionFieldCard(
                    occasion = occasion,
                    onOccasionChange = { occasion = it }
                )
            }

            item {
                DescriptionFieldCard(
                    description = description,
                    onDescriptionChange = { description = it }
                )
            }

            item {
                PhotoSelectionCard(
                    photos = selectedPhotos,
                    onAddPhoto = { giftPhotoPicker.launch() },
                    onRemovePhoto = { index -> selectedPhotos = selectedPhotos.toMutableList().apply { removeAt(index) } }
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
