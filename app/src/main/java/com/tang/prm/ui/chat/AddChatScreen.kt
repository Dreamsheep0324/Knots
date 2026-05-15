package com.tang.prm.ui.chat

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.tang.prm.domain.model.Contact
import com.tang.prm.ui.components.AppCard
import com.tang.prm.ui.components.ContactAvatar
import com.tang.prm.ui.components.ContactPickerDialog
import com.tang.prm.ui.components.AppDatePicker
import com.tang.prm.ui.components.FormScreenScaffold
import com.tang.prm.ui.theme.Primary
import com.tang.prm.ui.theme.TextGray
import com.tang.prm.util.DateUtils
import com.tang.prm.util.ImageCacheManager
import kotlinx.coroutines.launch
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.components.DiscardEditDialog

private val MyBubbleColor = Color(0xFF6366F1)
private val MyBubbleBgLight = Color(0xFFEEF2FF)
private val MyBubbleBgDark = Color(0xFF1A3A4A)

@Composable
fun AddChatScreen(
    contactId: Long? = null,
    eventId: Long? = null,
    navController: NavController,
    viewModel: AddChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val titleFocusRequester = remember { FocusRequester() }
    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { titleFocusRequester.requestFocus() }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            scope.launch {
                val localPath = ImageCacheManager.copyToInternalStorage(context, uri, "chat")
                viewModel.onImagePicked(localPath ?: uri.toString())
            }
        } else {
            viewModel.cancelImageRequest()
        }
    }

    LaunchedEffect(uiState.pendingImageLineId) {
        if (uiState.pendingImageLineId != null) {
            imagePickerLauncher.launch(arrayOf("image/*"))
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

@Composable
private fun ContactSelectionCard(
    selectedContact: Contact?,
    onSelectClick: () -> Unit,
    onChangeClick: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Dimens.paddingCard)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(3.dp).height(16.dp).background(Primary, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.Person, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("对话人物", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedContact != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ContactAvatar(avatar = selectedContact.avatar, name = selectedContact.name, size = 44)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(selectedContact.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        selectedContact.relationship?.takeIf { it.isNotBlank() }?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    TextButton(onClick = onChangeClick) {
                        Text("更换", color = Primary, fontSize = 13.sp)
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onSelectClick),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Primary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("选择人物", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun SceneInfoCard(
    title: String,
    selectedDate: Long,
    onTitleChange: (String) -> Unit,
    onDateClick: () -> Unit,
    titleFocusRequester: FocusRequester? = null
) {
    AppCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Dimens.paddingCard)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(3.dp).height(16.dp).background(Color(0xFFF97316), RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.Movie, contentDescription = null, tint = Color(0xFFF97316), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("场景设定", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth().then(if (titleFocusRequester != null) Modifier.focusRequester(titleFocusRequester) else Modifier),
                label = { Text("对话标题") },
                placeholder = { Text("如：关于项目的讨论") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = Primary.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDateClick),
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
                            .background(Primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("对话日期", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            DateUtils.formatYearMonthDayChineseFull(selectedDate),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun DialogueInputCard(
    dialogueLines: List<DialogueLineInput>,
    contactName: String,
    contactAvatar: String?,
    onAddMyLine: () -> Unit,
    onAddTheirLine: () -> Unit,
    onUpdateLine: (Long, String) -> Unit,
    onRemoveLineImage: (Long, String?) -> Unit,
    onRequestImage: (Long) -> Unit,
    onToggleSpeaker: (Long) -> Unit,
    onRemoveLine: (Long) -> Unit,
    onMoveLine: (Long, Int) -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Dimens.paddingCard)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(3.dp).height(16.dp).background(MyBubbleColor, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.TheaterComedy, contentDescription = null, tint = MyBubbleColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("对话剧本", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(6.dp))
                if (dialogueLines.isNotEmpty()) {
                    Text("${dialogueLines.size}条", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (dialogueLines.isEmpty()) {
                EmptyDialogueHint(contactName = contactName)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    dialogueLines.forEachIndexed { index, line ->
                        DialogueLineEditor(
                            line = line,
                            contactName = contactName,
                            contactAvatar = contactAvatar,
                            isFirst = index == 0,
                            isLast = index == dialogueLines.size - 1,
                            onUpdate = { onUpdateLine(line.id, it) },
                            onRemoveImage = { onRemoveLineImage(line.id, null) },
                            onRequestImage = { onRequestImage(line.id) },
                            onToggleSpeaker = { onToggleSpeaker(line.id) },
                            onRemove = { onRemoveLine(line.id) },
                            onMoveUp = { onMoveLine(line.id, -1) },
                            onMoveDown = { onMoveLine(line.id, 1) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onAddTheirLine,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${contactName}说了", fontSize = 13.sp)
                }

                Button(
                    onClick = onAddMyLine,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MyBubbleColor)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("我说了", fontSize = 13.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun EmptyDialogueHint(contactName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(36.dp))
        Spacer(modifier = Modifier.height(10.dp))
        Text("还没有对话内容", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text("点击下方按钮添加对话", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
    }
}

@Composable
private fun DialogueLineEditor(
    line: DialogueLineInput,
    contactName: String,
    contactAvatar: String?,
    isFirst: Boolean,
    isLast: Boolean,
    onUpdate: (String) -> Unit,
    onRemoveImage: () -> Unit,
    onRequestImage: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val bubbleBg = if (line.isMe) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val accentColor = if (line.isMe) MyBubbleColor else Primary
    val speakerLabel = if (line.isMe) "我" else contactName

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (line.isMe) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            if (!line.isMe) {
                ContactAvatar(avatar = contactAvatar, name = contactName, size = 28)
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = if (line.isMe) Alignment.End else Alignment.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        speakerLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                    Icon(
                        Icons.Default.SwapHoriz,
                        contentDescription = "切换说话人",
                        modifier = Modifier
                            .size(14.dp)
                            .clickable(onClick = onToggleSpeaker),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    shape = if (line.isMe) {
                        RoundedCornerShape(topStart = 14.dp, topEnd = 4.dp, bottomStart = 14.dp, bottomEnd = 14.dp)
                    } else {
                        RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 14.dp)
                    },
                    color = bubbleBg
                ) {
                    Column {
                        if (line.imageUri != null) {
                            Box(modifier = Modifier.padding(8.dp)) {
                                AsyncImage(
                                    model = line.imageUri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth(0.7f)
                                        .heightIn(max = 200.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(22.dp)
                                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                                        .clickable { onRemoveImage() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "删除图片", tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                        OutlinedTextField(
                            value = line.content,
                            onValueChange = onUpdate,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp, vertical = 2.dp),
                            placeholder = {
                                Text(
                                    if (line.isMe) "我说了什么..." else "${contactName}说了什么...",
                                    color = if (line.isMe) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = AnimationTokens.Alpha.half) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half),
                                    fontSize = 14.sp
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            minLines = 1,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = accentColor.copy(alpha = 0.3f),
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = if (line.isMe) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                lineHeight = 20.sp
                            ),
                            trailingIcon = {
                                if (line.imageUri == null) {
                                    Box(
                                        modifier = Modifier
                                            .padding(end = 6.dp)
                                            .size(26.dp)
                                            .background(accentColor.copy(alpha = AnimationTokens.Alpha.faint), CircleShape)
                                            .clickable { onRequestImage() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Image,
                                            contentDescription = "添加图片",
                                            tint = accentColor.copy(alpha = AnimationTokens.Alpha.half),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }

            if (line.isMe) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(MyBubbleColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MyBubbleColor, modifier = Modifier.size(14.dp))
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = if (line.isMe) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val offsetModifier = if (!line.isMe) Modifier.padding(start = 36.dp) else Modifier.padding(end = 36.dp)

            Row(offsetModifier, horizontalArrangement = Arrangement.spacedBy(0.dp), verticalAlignment = Alignment.CenterVertically) {
                if (!isFirst) {
                    IconButton(onClick = onMoveUp, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "上移", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half), modifier = Modifier.size(16.dp))
                    }
                }
                if (!isLast) {
                    IconButton(onClick = onMoveDown, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "下移", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half), modifier = Modifier.size(16.dp))
                    }
                }
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "删除", tint = Color(0xFFFF6B6B).copy(alpha = AnimationTokens.Alpha.visible), modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
private fun RemarksCard(
    remarks: String,
    onRemarksChange: (String) -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Dimens.paddingCard)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(3.dp).height(16.dp).background(Color(0xFFF97316), RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.EditNote, contentDescription = null, tint = Color(0xFFF97316), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("备注", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = remarks,
                onValueChange = onRemarksChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("给这次对话添加一些备注...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                minLines = 3,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = Color(0xFFF97316).copy(alpha = 0.3f)
                )
            )
        }
    }
}


