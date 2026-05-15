package com.tang.prm.ui.events

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.tang.prm.domain.model.Contact
import com.tang.prm.ui.components.ContactPickerDialog
import com.tang.prm.ui.components.AppDatePicker
import com.tang.prm.ui.components.FormScreenScaffold
import com.tang.prm.domain.model.EventTypes
import com.tang.prm.domain.model.CustomType
import com.tang.prm.ui.components.TagSelector
import com.tang.prm.ui.components.TagSelectorMode
import com.tang.prm.ui.theme.Primary
import com.tang.prm.ui.theme.toComposeColor
import com.tang.prm.ui.theme.getEmotionColor
import com.tang.prm.ui.theme.getEmotionIcon
import com.tang.prm.ui.theme.getWeatherColor
import com.tang.prm.ui.theme.getGenericIcon
import com.tang.prm.ui.theme.getWeatherIcon
import com.tang.prm.util.DateUtils
import com.tang.prm.util.ImageCacheManager
import kotlinx.coroutines.launch
import java.util.Calendar
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.components.DiscardEditDialog
import com.tang.prm.ui.theme.DialogDefaults
import androidx.compose.material3.SnackbarHostState



@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shadowElevation = 3.dp) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun WeatherSelector(
    items: List<CustomType>,
    selectedItem: String,
    onSelect: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp)) {
            Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("今天天气", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(horizontal = 4.dp)) {
            items(items, key = { it.id }) { item ->
                val isSelected = item.name == selectedItem
                val itemColor = item.color?.let {
                    it.toComposeColor(Color(0xFFF59E0B))
                } ?: Color(0xFFF59E0B)
                val icon = getWeatherIcon(item.name)
                val animatedSize by animateDpAsState(
                    targetValue = if (isSelected) 56.dp else 48.dp,
                    animationSpec = tween(200), label = "weatherSize"
                )
                val animatedBorderWidth by animateDpAsState(
                    targetValue = if (isSelected) 2.5.dp else 0.dp,
                    animationSpec = tween(200), label = "weatherBorder"
                )
                val animatedIconSize by animateDpAsState(
                    targetValue = if (isSelected) 26.dp else 22.dp,
                    animationSpec = tween(200), label = "weatherIcon"
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(64.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSelect(if (isSelected) "" else item.name) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isSelected) {
                            Canvas(modifier = Modifier.size(64.dp)) {
                                drawRoundRect(
                                    color = itemColor.copy(alpha = AnimationTokens.Alpha.faint),
                                    cornerRadius = CornerRadius(20.dp.toPx())
                                )
                            }
                        }
                        Surface(
                            modifier = Modifier.size(animatedSize),
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) itemColor.copy(alpha = AnimationTokens.Alpha.subtle) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) BorderStroke(animatedBorderWidth, itemColor.copy(alpha = AnimationTokens.Alpha.half)) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (icon != null) {
                                    Icon(icon, contentDescription = null,
                                        tint = if (isSelected) itemColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(animatedIconSize))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(item.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) itemColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun EmotionSelector(
    items: List<CustomType>,
    selectedItem: String,
    onSelect: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp)) {
            Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("此刻心情", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(horizontal = 4.dp)) {
            items(items, key = { it.id }) { item ->
                val isSelected = item.name == selectedItem
                val itemColor = item.color?.let {
                    it.toComposeColor(Color(0xFF8B5CF6))
                } ?: Color(0xFF8B5CF6)
                val icon = getEmotionIcon(item.name)
                val animatedSize by animateDpAsState(
                    targetValue = if (isSelected) 50.dp else 44.dp,
                    animationSpec = tween(200), label = "emotionSize"
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(58.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSelect(if (isSelected) "" else item.name) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isSelected) {
                            Canvas(modifier = Modifier.size(58.dp)) {
                                drawCircle(color = itemColor.copy(alpha = AnimationTokens.Alpha.faint))
                            }
                        }
                        Surface(
                            modifier = Modifier.size(animatedSize),
                            shape = CircleShape,
                            color = if (isSelected) itemColor.copy(alpha = AnimationTokens.Alpha.subtle) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) BorderStroke(2.dp, itemColor.copy(alpha = 0.4f)) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (icon != null) {
                                    Icon(icon, contentDescription = null,
                                        tint = if (isSelected) itemColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(if (isSelected) 24.dp else 20.dp))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(item.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) itemColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    contactId: Long? = null, eventId: Long? = null,
    navController: NavController, viewModel: AddEventViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val titleFocusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { titleFocusRequester.requestFocus() }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val isEditMode = eventId != null && eventId > 0
    val scope = rememberCoroutineScope()

    val imagePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            scope.launch {
                val localPath = ImageCacheManager.copyToInternalStorage(context, it, "event")
                viewModel.addPhoto(localPath ?: it.toString())
            }
        }
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

    if (showDatePicker) {
        AppDatePicker(
            show = showDatePicker,
            onDismiss = { showDatePicker = false },
            confirmText = "下一步",
            onDateSelected = {
                val cal = Calendar.getInstance(); val cur = Calendar.getInstance().apply { timeInMillis = uiState.time }
                cal.timeInMillis = it; cal.set(Calendar.HOUR_OF_DAY, cur.get(Calendar.HOUR_OF_DAY)); cal.set(Calendar.MINUTE, cur.get(Calendar.MINUTE))
                viewModel.updateTime(cal.timeInMillis)
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

            item {
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shadowElevation = 3.dp) {
                    Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
                        OutlinedTextField(value = uiState.title, onValueChange = viewModel::updateTitle, modifier = Modifier.fillMaxWidth().focusRequester(titleFocusRequester),
                            placeholder = { Text("记录今天的故事...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                            textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent, cursorColor = Primary))
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Primary, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(DateUtils.formatMonthDayWeekday(uiState.time),
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (uiState.weather.isNotBlank()) {
                                Spacer(modifier = Modifier.width(10.dp))
                                val wIcon = getWeatherIcon(uiState.weather)
                                val wColor = getWeatherColor(uiState.weather)?.let { it.toComposeColor(Color(0xFFF59E0B)) } ?: Color(0xFFF59E0B)
                                if (wIcon != null) Icon(wIcon, contentDescription = null, tint = wColor, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(uiState.weather, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (uiState.emotion.isNotBlank()) {
                                Spacer(modifier = Modifier.width(10.dp))
                                val eIcon = getEmotionIcon(uiState.emotion)
                                val eColor = getEmotionColor(uiState.emotion)?.let { it.toComposeColor(Color(0xFF8B5CF6)) } ?: Color(0xFF8B5CF6)
                                if (eIcon != null) Icon(eIcon, contentDescription = null, tint = eColor, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(uiState.emotion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

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

            item {
                SectionCard(title = "事件类型", icon = Icons.AutoMirrored.Filled.Label, iconTint = Color(0xFF8B5CF6)) {
                    TagSelector(mode = TagSelectorMode.SINGLE, title = null, showHeader = false, showAddButton = true,
                        availableItems = uiState.eventTypes, selectedItems = listOf(uiState.type),
                        onSelectionChange = { viewModel.updateType(it.firstOrNull() ?: EventTypes.MEETUP) },
                        onAddItem = { name, color, icon -> viewModel.addEventType(name, color, icon) },
                        onDeleteItem = { viewModel.deleteEventType(it) },
                        iconResolver = { name -> uiState.eventTypes.find { it.name == name }?.icon?.let { getGenericIcon(it) } },
                        showIconPicker = true)
                }
            }

            item {
                SectionCard(title = "时间和地点", icon = Icons.Default.CalendarToday, iconTint = Primary) {
                    Row(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
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
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent, cursorColor = Primary))
                    }
                }
            }

            item {
                SectionCard(title = "参与人物", icon = Icons.Default.People, iconTint = Color(0xFF10B981)) {
                    if (uiState.participants.isEmpty()) {
                        Surface(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable { viewModel.showContactPicker() },
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half))) {
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
            }

            if (uiState.type == EventTypes.CONVERSATION) {
                item {
                    SectionCard(title = "对话摘要", icon = Icons.AutoMirrored.Filled.Chat, iconTint = Color(0xFF06B6D4)) {
                        LinedPaperField(value = uiState.conversationSummary, onValueChange = viewModel::updateConversationSummary, placeholder = "记录本次对话的重要内容...", minLines = 3)
                    }
                }
            }

            item {
                SectionCard(title = "文字描述", icon = Icons.Default.Edit, iconTint = Primary) {
                    LinedPaperField(value = uiState.description, onValueChange = viewModel::updateDescription, placeholder = "描述这次事件...", minLines = 4)
                }
            }

            item {
                SectionCard(title = "照片", icon = Icons.Default.AddPhotoAlternate, iconTint = Color(0xFFF59E0B)) {
                    if (uiState.photos.isEmpty()) {
                        Surface(modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(10.dp)).clickable { imagePickerLauncher.launch(arrayOf("image/*")) },
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half))) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("添加照片", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            itemsIndexed(uiState.photos) { index, photo ->
                                PolaroidPhoto(photoUri = photo, rotation = if (index % 3 == 0) -1.5f else if (index % 3 == 1) 0f else 1.5f, onRemove = { viewModel.removePhoto(photo) })
                            }
                            item { PolaroidPhotoAddButton(onClick = { imagePickerLauncher.launch(arrayOf("image/*")) }) }
                        }
                    }
                }
            }

            item {
                SectionCard(title = "个人感悟", icon = Icons.Default.AutoAwesome, iconTint = Color(0xFFEC4899)) {
                    LinedPaperField(value = uiState.remarks, onValueChange = viewModel::updateRemarks, placeholder = "写下你的感受...", minLines = 3)
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun LinedPaperField(value: String, onValueChange: (String) -> Unit, placeholder: String, minLines: Int = 3) {
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half)
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(surfaceVariantColor).border(1.dp, outlineColor, RoundedCornerShape(10.dp))) {
        Canvas(modifier = Modifier.matchParentSize().padding(start = 24.dp, top = 10.dp, end = 6.dp)) {
            val lineSpacingPx = 26.dp.toPx()
            for (i in 0..8) { drawLine(surfaceVariantColor, Offset(0f, i * lineSpacingPx), Offset(size.width, i * lineSpacingPx), 1f) }
            drawLine(outlineColor, Offset(0f, 0f), Offset(0f, size.height), 1.5f)
        }
        OutlinedTextField(value = value, onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().padding(start = 28.dp, end = 6.dp),
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontStyle = FontStyle.Italic) },
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, lineHeight = 26.sp),
            minLines = minLines,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent, cursorColor = Primary))
    }
}

@Composable
private fun PolaroidContact(contact: Contact, onRemove: () -> Unit) {
    Surface(modifier = Modifier.width(72.dp).rotate(if (contact.id % 2 == 0L) -1.5f else 1.5f),
        shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shadowElevation = 3.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(5.dp)) {
            Box(modifier = Modifier.size(50.dp)) {
                if (contact.avatar != null) {
                    AsyncImage(model = contact.avatar, contentDescription = null, modifier = Modifier.size(50.dp).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
                } else {
                    Box(modifier = Modifier.size(50.dp).clip(RoundedCornerShape(4.dp)).background(Primary.copy(alpha = AnimationTokens.Alpha.faint)), contentAlignment = Alignment.Center) {
                        Text(contact.name.firstOrNull()?.toString() ?: "?", style = MaterialTheme.typography.titleSmall, color = Primary, fontWeight = FontWeight.Bold)
                    }
                }
                IconButton(onClick = onRemove, modifier = Modifier.align(Alignment.TopEnd).size(16.dp).background(Color.Black.copy(alpha = 0.35f), CircleShape)) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(9.dp))
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(contact.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), fontSize = 10.sp)
        }
    }
}

@Composable
private fun PolaroidAddButton(onClick: () -> Unit) {
    Surface(modifier = Modifier.width(72.dp).clickable(onClick = onClick), shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half))) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(5.dp), verticalArrangement = Arrangement.Center) {
            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier.size(50.dp), contentAlignment = Alignment.Center) { Icon(Icons.Default.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)) }
            Spacer(modifier = Modifier.height(3.dp))
            Text("添加", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
        }
    }
}

@Composable
private fun PolaroidPhoto(photoUri: String, rotation: Float, onRemove: () -> Unit) {
    Surface(modifier = Modifier.rotate(rotation), shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shadowElevation = 3.dp) {
        Column(modifier = Modifier.padding(5.dp, 5.dp, 5.dp, 12.dp)) {
            Box(modifier = Modifier.size(80.dp)) {
                AsyncImage(model = photoUri, contentDescription = null, modifier = Modifier.size(80.dp).clip(RoundedCornerShape(3.dp)), contentScale = ContentScale.Crop)
                IconButton(onClick = onRemove, modifier = Modifier.align(Alignment.TopEnd).size(18.dp).background(Color.Black.copy(alpha = 0.4f), CircleShape)) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                }
            }
        }
    }
}

@Composable
private fun PolaroidPhotoAddButton(onClick: () -> Unit) {
    Surface(modifier = Modifier.clickable(onClick = onClick).rotate(1f), shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shadowElevation = 3.dp) {
        Column(modifier = Modifier.padding(5.dp, 5.dp, 5.dp, 12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) { Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp)) }
        }
    }
}


