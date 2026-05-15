package com.tang.prm.ui.home

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.GiftType
import com.tang.prm.ui.components.AppCard
import com.tang.prm.ui.components.AppDatePicker
import com.tang.prm.ui.components.ContactAvatar
import com.tang.prm.ui.components.ContactPickerDialog
import com.tang.prm.ui.components.FormScreenScaffold
import com.tang.prm.ui.components.FormSectionLabel
import com.tang.prm.ui.theme.*
import com.tang.prm.util.DateUtils
import com.tang.prm.util.ImageCacheManager
import com.tang.prm.ui.animation.core.AnimationTokens
import kotlinx.coroutines.launch
import com.tang.prm.ui.theme.GiftTypeStyle
import com.tang.prm.ui.theme.toStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGiftScreen(
    navController: NavController,
    giftId: Long = 0L,
    viewModel: GiftsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val nameFocusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val isEditing = giftId != 0L

    val editingGift by viewModel.getGiftFlow(giftId).collectAsState(initial = null)

    LaunchedEffect(Unit) { if (!isEditing) nameFocusRequester.requestFocus() }

    var selectedContact by remember { mutableStateOf<Contact?>(null) }
    var giftName by remember { mutableStateOf("") }
    var giftType by remember { mutableStateOf(GiftType.OTHER) }
    var isSent by remember { mutableStateOf(true) }
    var occasion by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPhotos by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showContactPicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var hasLoadedEditData by remember { mutableStateOf(false) }

    LaunchedEffect(editingGift, uiState.availableContacts) {
        if (isEditing && editingGift != null && !hasLoadedEditData && uiState.availableContacts.isNotEmpty()) {
            val g = editingGift!!
            val contact = uiState.availableContacts.find { it.id == g.contactId }
            selectedContact = contact
            giftName = g.giftName
            giftType = GiftType.entries.find { it.name == g.giftType } ?: GiftType.OTHER
            isSent = g.isSent
            occasion = g.occasion ?: ""
            description = g.description ?: ""
            selectedPhotos = g.photos.map { Uri.parse(it) }
            selectedDate = g.date
            hasLoadedEditData = true
        }
    }

    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        scope.launch {
            val cachedUris = uris.mapNotNull { uri ->
                val localPath = ImageCacheManager.copyToInternalStorage(context, uri, "gift")
                localPath?.let { Uri.fromFile(java.io.File(it)) } ?: uri
            }
            selectedPhotos = selectedPhotos + cachedUris
        }
    }

    val canSave = selectedContact != null && giftName.isNotBlank()

    if (showContactPicker) {
        ContactPickerDialog(
            contacts = uiState.availableContacts,
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
            confirmColor = Primary,
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
                        giftType = giftType.name,
                        date = selectedDate,
                        isSent = isSent,
                        amount = null,
                        occasion = occasion.ifBlank { null },
                        description = description.ifBlank { null },
                        location = null,
                        photos = selectedPhotos.map { it.toString() },
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
                    onAddPhoto = { photoPickerLauncher.launch("image/*") },
                    onRemovePhoto = { idx ->
                        selectedPhotos = selectedPhotos.filterIndexed { i, _ -> i != idx }
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ContactSelectionCard(
    selectedContact: Contact?,
    isSent: Boolean,
    onSelectClick: () -> Unit
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            FormSectionLabel(icon = Icons.Default.Person, label = "关联人物", color = Primary)
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onSelectClick),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedContact != null) {
                        ContactAvatar(avatar = selectedContact.avatar, name = selectedContact.name, size = 44)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isSent) "送给" else "来自",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = selectedContact.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Primary.copy(alpha = AnimationTokens.Alpha.faint), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Primary, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("选择人物", style = MaterialTheme.typography.titleMedium, color = Primary, fontWeight = FontWeight.Medium)
                            Text("点击选择关联对象", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun DirectionToggleCard(isSent: Boolean, onToggle: (Boolean) -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            FormSectionLabel(icon = Icons.Default.SwapHoriz, label = "收送方向", color = if (isSent) SignalAmber else SignalGreen)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DirectionButton(
                    modifier = Modifier.weight(1f),
                    label = "送出",
                    icon = Icons.AutoMirrored.Filled.Send,
                    color = SignalAmber,
                    isSelected = isSent,
                    onClick = { onToggle(true) }
                )
                DirectionButton(
                    modifier = Modifier.weight(1f),
                    label = "收到",
                    icon = Icons.Default.Download,
                    color = SignalGreen,
                    isSelected = !isSent,
                    onClick = { onToggle(false) }
                )
            }
        }
    }
}

@Composable
private fun DirectionButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick),
        color = if (isSelected) color.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(1.5.dp, color.copy(alpha = AnimationTokens.Alpha.half)) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half))
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun GiftNameCard(giftName: String, onNameChange: (String) -> Unit, focusRequester: FocusRequester? = null) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            FormSectionLabel(icon = Icons.Default.CardGiftcard, label = "礼物名称", color = SignalCoral)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = giftName,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth().then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier),
                placeholder = { Text("如：机械键盘、护肤品", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary.copy(alpha = AnimationTokens.Alpha.visible),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
private fun GiftTypeCard(selectedType: GiftType, onTypeSelect: (GiftType) -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            FormSectionLabel(icon = Icons.Default.Category, label = "礼物分类", color = SignalPurple)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(GiftType.entries, key = { it }) { type ->
                    val isSelected = selectedType == type
                    val style = type.toStyle()
                    Surface(
                        modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable { onTypeSelect(type) },
                        color = if (isSelected) style.color.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        border = if (isSelected) BorderStroke(1.5.dp, style.color.copy(alpha = AnimationTokens.Alpha.half)) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = style.icon,
                                contentDescription = null,
                                tint = if (isSelected) style.color else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = type.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) style.color else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateSelectionCard(selectedDate: Long, onClick: () -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            FormSectionLabel(icon = Icons.Default.CalendarToday, label = "日期", color = Primary)
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onClick),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Primary.copy(alpha = AnimationTokens.Alpha.faint), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("选择日期", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = DateUtils.formatYearMonthDayChineseFull(selectedDate),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun OccasionFieldCard(occasion: String, onOccasionChange: (String) -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            FormSectionLabel(icon = Icons.Default.Event, label = "场合（选填）", color = SignalAmber)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = occasion,
                onValueChange = onOccasionChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("如：生日、纪念日", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary.copy(alpha = AnimationTokens.Alpha.visible),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
private fun DescriptionFieldCard(description: String, onDescriptionChange: (String) -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            FormSectionLabel(icon = Icons.Default.EditNote, label = "备注（选填）", color = TextSlate)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                placeholder = { Text("写下关于这份礼物的描述...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half)) },
                maxLines = 4,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary.copy(alpha = AnimationTokens.Alpha.visible),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
private fun PhotoSelectionCard(
    photos: List<Uri>,
    onAddPhoto: () -> Unit,
    onRemovePhoto: (Int) -> Unit
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            FormSectionLabel(icon = Icons.Default.AddPhotoAlternate, label = "照片（选填）", color = SignalSky)
            Spacer(modifier = Modifier.height(12.dp))
            if (photos.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(photos.size, key = { photos[it] }) { idx ->
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            AsyncImage(
                                model = photos[idx],
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { onRemovePhoto(idx) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "删除",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                    item {
                        Surface(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onAddPhoto),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half))
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Add, contentDescription = "添加照片", tint = Primary, modifier = Modifier.size(28.dp))
                            }
                        }
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onAddPhoto),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half))
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("添加礼物照片", style = MaterialTheme.typography.bodyMedium, color = Primary)
                    }
                }
            }
        }
    }
}


