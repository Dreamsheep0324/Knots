package com.tang.prm.feature.events

import androidx.activity.compose.BackHandler
import com.tang.prm.ui.components.photo.PhotoPickerConfig
import com.tang.prm.ui.components.photo.rememberPhotoPickerLauncher
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tang.prm.domain.model.EventType
import com.tang.prm.ui.components.ContactPickerDialog
import com.tang.prm.ui.components.AppDatePicker
import com.tang.prm.ui.components.FormScreenScaffold
import com.tang.prm.ui.components.SectionCard
import com.tang.prm.ui.components.TagSelector
import com.tang.prm.ui.components.TagSelectorMode
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tang.prm.ui.theme.InsightPink
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.SignalSky
import com.tang.prm.ui.theme.getEmotionIcon
import com.tang.prm.ui.theme.getGenericIcon
import com.tang.prm.ui.theme.getWeatherIcon
import com.tang.prm.domain.util.DateUtils

import java.util.Calendar
import com.tang.prm.ui.components.DiscardEditDialog
import com.tang.prm.ui.theme.DialogDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    contactId: Long? = null, eventId: Long? = null,
    navController: NavController, viewModel: AddEventViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val titleFocusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { titleFocusRequester.requestFocus() }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val isEditMode = eventId != null && eventId > 0

    val photoPicker = rememberPhotoPickerLauncher(
        config = PhotoPickerConfig(maxCount = 10, prefix = "event")
    ) { result ->
        result.localPaths.forEach { viewModel.addPhoto(it) }
    }

    var showExitDialog by remember { mutableStateOf(false) }
    BackHandler(enabled = uiState.hasUnsavedChanges) { showExitDialog = true }

    if (showExitDialog) {
        DiscardEditDialog(onDiscard = { showExitDialog = false; navController.popBackStack() }, onDismiss = { showExitDialog = false })
    }

    LaunchedEffect(eventId) { if (isEditMode && eventId != null) viewModel.loadEvent(eventId) }
    LaunchedEffect(contactId) { if (!isEditMode) contactId?.let { if (it > 0) viewModel.addParticipantById(it) } }
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            navController.popBackStack()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.errorMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    if (showDatePicker) {
        AppDatePicker(
            show = showDatePicker,
            onDismiss = { showDatePicker = false },
            confirmText = "下一步",
            onDateSelected = {
                viewModel.updateTime(mergeDateAndTime(it, uiState.time))
                showTimePicker = true
            }
        )
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState()
        AlertDialog(onDismissRequest = { showTimePicker = false }, containerColor = DialogDefaults.containerColor,
            confirmButton = { TextButton(onClick = {
                val cal = Calendar.getInstance().apply { timeInMillis = uiState.time }
                cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour); cal.set(Calendar.MINUTE, timePickerState.minute)
                viewModel.updateTime(cal.timeInMillis); showTimePicker = false
            }) { Text("确定") } },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("取消") } },
            title = { Text("选择时间") },
            text = { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { TimePicker(state = timePickerState) } })
    }

    if (uiState.showContactPicker) {
        ContactPickerDialog(contacts = uiState.availableContacts, multiSelect = true, selectedContacts = uiState.participants,
            onContactSelected = { viewModel.addParticipant(it) }, onDismiss = { viewModel.hideContactPicker() })
    }

    FormScreenScaffold(
        title = if (isEditMode) "编辑事件" else "新建事件",
        onSaveClick = { viewModel.saveEvent() },
        saveEnabled = uiState.title.isNotBlank() && uiState.type.isNotBlank(),
        onBackClick = { if (uiState.hasUnsavedChanges) showExitDialog = true else navController.popBackStack() },
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHostState = snackbarHostState
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.surface),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            item { EventHeaderCard(uiState, viewModel, titleFocusRequester) }

            item {
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shadowElevation = 3.dp) {
                    Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
                        WeatherSelector(items = uiState.weathers, selectedItem = uiState.weather, onSelect = { viewModel.updateWeather(it) })
                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(20.dp))
                        EmotionSelector(items = uiState.emotions, selectedItem = uiState.emotion, onSelect = { viewModel.updateEmotion(it) })
                    }
                }
            }

            item { EventFormSections(uiState, viewModel, photoPicker, onShowDatePicker = { showDatePicker = it }) }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun EventHeaderCard(
    uiState: AddEventUiState,
    viewModel: AddEventViewModel,
    titleFocusRequester: FocusRequester
) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shadowElevation = 3.dp) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
            OutlinedTextField(value = uiState.title, onValueChange = viewModel::updateTitle, modifier = Modifier.fillMaxWidth().focusRequester(titleFocusRequester),
                placeholder = { Text("记录今天的故事...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent, cursorColor = MaterialTheme.colorScheme.primary))
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text(DateUtils.formatMonthDayWeekday(uiState.time),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (uiState.weather.isNotBlank()) {
                    Spacer(modifier = Modifier.width(10.dp))
                    val wIcon = getWeatherIcon(uiState.weather)
                    val wColor = resolveWeatherColor(uiState.weather)
                    if (wIcon != null) Icon(wIcon, contentDescription = null, tint = wColor, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(uiState.weather, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (uiState.emotion.isNotBlank()) {
                    Spacer(modifier = Modifier.width(10.dp))
                    val eIcon = getEmotionIcon(uiState.emotion)
                    val eColor = resolveEmotionColor(uiState.emotion)
                    if (eIcon != null) Icon(eIcon, contentDescription = null, tint = eColor, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(uiState.emotion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun EventFormSections(
    uiState: AddEventUiState,
    viewModel: AddEventViewModel,
    photoPicker: com.tang.prm.ui.components.photo.ManagedPhotoPickerLauncher,
    onShowDatePicker: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionCard(title = "事件类型", icon = Icons.AutoMirrored.Filled.Label, iconTint = SignalPurple) {
            TagSelector(mode = TagSelectorMode.SINGLE, title = null, showHeader = false, showAddButton = true,
                availableItems = uiState.eventTypes, selectedItems = listOf(uiState.type),
                onSelectionChange = { viewModel.updateType(it.firstOrNull() ?: EventType.MEETUP.name) },
                onAddItem = { name, color, icon -> viewModel.addEventType(name, color, icon) },
                onDeleteItem = { viewModel.deleteEventType(it) },
                iconResolver = { name -> uiState.eventTypes.find { it.name == name }?.icon?.let { getGenericIcon(it) } },
                showIconPicker = true)
        }

        SectionCard(title = "时间和地点", icon = Icons.Default.CalendarToday, iconTint = MaterialTheme.colorScheme.primary) {
            Row(modifier = Modifier.fillMaxWidth().clickable { onShowDatePicker(true) }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(DateUtils.formatMonthDayWeekdayTime(uiState.time),
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp, modifier = Modifier.padding(vertical = 6.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(value = uiState.location, onValueChange = viewModel::updateLocation, modifier = Modifier.weight(1f),
                    placeholder = { Text("添加地点...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface), singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent, cursorColor = MaterialTheme.colorScheme.primary))
            }
        }

        SectionCard(title = "参与人物", icon = Icons.Default.People, iconTint = SignalGreen) {
            if (uiState.participants.isEmpty()) {
                Surface(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable { viewModel.showContactPicker() },
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = com.tang.prm.ui.animation.core.AnimationTokens.Alpha.half))) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("添加参与人物", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(uiState.participants, key = { it.id }) { contact -> PolaroidContact(contact = contact, onRemove = { viewModel.removeParticipant(contact) }) }
                    item { PolaroidAddButton(onClick = { viewModel.showContactPicker() }) }
                }
            }
        }

        if (uiState.type == EventType.CONVERSATION.name) {
            SectionCard(title = "对话摘要", icon = Icons.AutoMirrored.Filled.Chat, iconTint = SignalSky) {
                LinedPaperField(value = uiState.conversationSummary, onValueChange = viewModel::updateConversationSummary, placeholder = "记录本次对话的重要内容...", minLines = 3)
            }
        }

        SectionCard(title = "文字描述", icon = Icons.Default.Edit, iconTint = MaterialTheme.colorScheme.primary) {
            LinedPaperField(value = uiState.description, onValueChange = viewModel::updateDescription, placeholder = "描述这次事件...", minLines = 4)
        }

        SectionCard(title = "照片", icon = Icons.Default.AddPhotoAlternate, iconTint = SignalAmber) {
            if (uiState.photos.isEmpty()) {
                Surface(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable { photoPicker.launch() },
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = com.tang.prm.ui.animation.core.AnimationTokens.Alpha.half))) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("添加事件照片", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(uiState.photos, key = { idx, _ -> idx }) { idx, photo ->
                        EditablePolaroidPhoto(
                            photoUri = photo,
                            rotation = if (idx % 2 == 0) -1.5f else 1.5f,
                            onRemove = { viewModel.removePhotoAt(idx) }
                        )
                    }
                    item { PolaroidPhotoAddButton(onClick = { photoPicker.launch() }) }
                }
            }
        }

        SectionCard(title = "个人感悟", icon = Icons.Default.AutoAwesome, iconTint = InsightPink) {
            LinedPaperField(value = uiState.remarks, onValueChange = viewModel::updateRemarks, placeholder = "写下你的感受...", minLines = 3)
        }
    }
}

/**
 * 合并日期和时间：取 [dateMillis] 的年月日 + [timeMillis] 的时分秒，返回合并后的时间戳。
 * 用于日期选择器：用户选中新日期后，保留原有时间的时分。
 */
private fun mergeDateAndTime(dateMillis: Long, timeMillis: Long): Long {
    val timeCal = Calendar.getInstance().apply { timeInMillis = timeMillis }
    return Calendar.getInstance().apply {
        timeInMillis = dateMillis
        set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
        set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
    }.timeInMillis
}
