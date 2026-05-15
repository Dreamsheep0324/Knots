package com.tang.prm.ui.contacts

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tang.prm.domain.model.CustomCategories
import com.tang.prm.domain.model.CustomType
import com.tang.prm.ui.components.AddTypeDialog
import com.tang.prm.ui.components.AppCard
import com.tang.prm.ui.components.AppDatePicker
import com.tang.prm.ui.components.FormScreenScaffold
import com.tang.prm.ui.components.DeleteTypeDialog
import com.tang.prm.ui.components.TagSelector
import com.tang.prm.ui.components.TagSelectorMode
import com.tang.prm.domain.model.AppStrings
import com.tang.prm.ui.theme.Error
import com.tang.prm.ui.theme.IntimacyAcquaintance
import com.tang.prm.ui.theme.IntimacyClose
import com.tang.prm.ui.theme.IntimacyFamily
import com.tang.prm.ui.theme.IntimacyFriend
import com.tang.prm.ui.theme.IntimacyNew
import com.tang.prm.ui.theme.Primary
import com.tang.prm.ui.theme.TextGray
import com.tang.prm.util.DateUtils
import com.tang.prm.util.ImageCacheManager
import kotlinx.coroutines.launch
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.components.DiscardEditDialog


private val SectionIconSize = 36.dp
private val SectionIconInnerSize = 18.dp

private data class SectionStyle(val icon: ImageVector, val color: Color)

private val sectionStyles = mapOf(
    "基本信息" to SectionStyle(Icons.Default.Person, Color(0xFF42A5F5)),
    "关系" to SectionStyle(Icons.Default.FavoriteBorder, Color(0xFFE65100)),
    "重要日期" to SectionStyle(Icons.Default.CalendarToday, Color(0xFF66BB6A)),
    "联系方式" to SectionStyle(Icons.Default.Phone, Color(0xFF4DD0E1)),
    "位置信息" to SectionStyle(Icons.Default.LocationOn, Color(0xFF9575CD)),
    "个人特征" to SectionStyle(Icons.Default.EmojiEmotions, Color(0xFFF43F5E)),
    "简介" to SectionStyle(Icons.Default.EditNote, Color(0xFF64748B)),
    "亲密度" to SectionStyle(Icons.Default.Favorite, Color(0xFFF97316))
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
@Suppress("UNUSED_PARAMETER")
fun AddContactScreen(
    contactId: Long = 0L,
    navController: androidx.navigation.NavController,
    viewModel: AddContactViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showKnowingDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val nameFocusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { nameFocusRequester.requestFocus() }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            scope.launch {
                val localPath = ImageCacheManager.copyToInternalStorage(context, it, "avatar")
                viewModel.updateAvatar(localPath ?: it.toString())
            }
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
            onDateSelected = { millis ->
                viewModel.updateBirthday(DateUtils.formatDate(millis))
            }
        )
    }

    if (showKnowingDatePicker) {
        AppDatePicker(
            show = showKnowingDatePicker,
            onDismiss = { showKnowingDatePicker = false },
            onDateSelected = { millis ->
                viewModel.updateKnowingDate(DateUtils.formatDate(millis))
            }
        )
    }

    FormScreenScaffold(
        title = if (uiState.isEditing) "编辑人物" else "新建人物",
        onSaveClick = { viewModel.saveContact() },
        saveEnabled = uiState.name.isNotBlank(),
        onBackClick = { if (uiState.hasUnsavedChanges) showExitDialog = true else navController.popBackStack() },
        snackbarHostState = snackbarHostState
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ProfileHeader(
                    avatar = uiState.avatar,
                    onAvatarClick = { imagePickerLauncher.launch(arrayOf("image/*")) }
                )
            }

            item {
                FormSection("基本信息") {
                    FormField("姓名", uiState.name, viewModel::updateName, placeholder = "请输入姓名", required = true, focusRequester = nameFocusRequester)
                    Spacer(modifier = Modifier.height(12.dp))
                    FormField("昵称", uiState.nickname ?: "", viewModel::updateNickname, placeholder = "请输入昵称")
                    Spacer(modifier = Modifier.height(12.dp))
                    TagSelector(
                        mode = TagSelectorMode.SINGLE, title = "学校",
                        availableItems = uiState.educations, selectedItems = listOfNotNull(uiState.education),
                        onSelectionChange = { viewModel.updateEducation(it.firstOrNull() ?: "") },
                        onAddItem = { name, _, _ -> viewModel.addCustomType(CustomCategories.EDUCATION, name) },
                        onDeleteItem = { viewModel.deleteCustomType(it) }
                    )
                }
            }

            item {
                var showAddRelationshipDialog by remember { mutableStateOf(false) }
                FormSection(
                    title = "关系",
                    action = {
                        TextButton(onClick = { showAddRelationshipDialog = true }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("新增", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                ) {
                    TagSelector(
                        mode = TagSelectorMode.SINGLE, availableItems = uiState.relationships,
                        selectedItems = listOfNotNull(uiState.relationship),
                        onSelectionChange = { viewModel.updateRelationship(it.firstOrNull() ?: "") },
                        onAddItem = { name, _, _ -> viewModel.addCustomType(CustomCategories.RELATIONSHIP, name) },
                        onDeleteItem = { viewModel.deleteCustomType(it) },
                        emptyText = "暂无关系标签，点击新增添加",
                        showHeader = false,
                        showAddDialog = showAddRelationshipDialog,
                        onAddDialogDismiss = { showAddRelationshipDialog = false }
                    )
                }
            }

            item {
                FormSection("重要日期") {
                    DatePickerField("生日", uiState.birthday, { viewModel.updateBirthday(it) }) { showDatePicker = true }
                    Spacer(modifier = Modifier.height(12.dp))
                    DatePickerField("相识日期", uiState.knowingDate, { viewModel.updateKnowingDate(it) }) { showKnowingDatePicker = true }
                }
            }

            item {
                FormSection("联系方式") {
                    FormField("电话", uiState.phone, viewModel::updatePhone, placeholder = "请输入电话")
                    Spacer(modifier = Modifier.height(12.dp))
                    FormField("其他", uiState.email, viewModel::updateEmail, placeholder = "请输入其他联系方式")
                }
            }

            item {
                FormSection("位置信息") {
                    FormField("城市", uiState.city ?: "", viewModel::updateCity, placeholder = "请输入城市")
                    Spacer(modifier = Modifier.height(12.dp))
                    FormField("详细地址", uiState.address ?: "", viewModel::updateAddress, placeholder = "请输入详细地址", maxLines = 2)
                    Spacer(modifier = Modifier.height(12.dp))
                    FormField("公司", uiState.company ?: "", viewModel::updateCompany, placeholder = "请输入公司")
                    Spacer(modifier = Modifier.height(12.dp))
                    FormField("职务", uiState.jobTitle ?: "", viewModel::updateJobTitle, placeholder = "请输入职务")
                }
            }

            item {
                FormSection("个人特征") {
                    TagSelector(mode = TagSelectorMode.MULTI, title = "爱好", availableItems = uiState.hobbyOptions, selectedItems = uiState.hobbies, onSelectionChange = { viewModel.updateHobbies(it) }, onAddItem = { name, color, _ -> viewModel.addCustomType(CustomCategories.HOBBY, name, color) }, onDeleteItem = { viewModel.deleteCustomType(it) })
                    Spacer(modifier = Modifier.height(14.dp))
                    TagSelector(mode = TagSelectorMode.MULTI, title = "习惯", availableItems = uiState.habitOptions, selectedItems = uiState.habits, onSelectionChange = { viewModel.updateHabits(it) }, onAddItem = { name, color, _ -> viewModel.addCustomType(CustomCategories.HABIT, name, color) }, onDeleteItem = { viewModel.deleteCustomType(it) })
                    Spacer(modifier = Modifier.height(14.dp))
                    TagSelector(mode = TagSelectorMode.MULTI, title = "饮食", availableItems = uiState.dietOptions, selectedItems = uiState.diets, onSelectionChange = { viewModel.updateDiets(it) }, onAddItem = { name, color, _ -> viewModel.addCustomType(CustomCategories.DIET, name, color) }, onDeleteItem = { viewModel.deleteCustomType(it) })
                    Spacer(modifier = Modifier.height(14.dp))
                    TagSelector(mode = TagSelectorMode.MULTI, title = "技能", availableItems = uiState.skillOptions, selectedItems = uiState.skills, onSelectionChange = { viewModel.updateSkills(it) }, onAddItem = { name, color, _ -> viewModel.addCustomType(CustomCategories.SKILL, name, color) }, onDeleteItem = { viewModel.deleteCustomType(it) })
                }
            }

            item {
                FormSection("简介") {
                    NotesField(value = uiState.notes ?: "", onValueChange = { viewModel.updateNotes(it) })
                }
            }

            item {
                FormSection("亲密度") {
                    IntimacySlider(value = uiState.intimacyScore, onValueChange = { viewModel.updateIntimacyScore(it) })
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun ProfileHeader(avatar: String?, onAvatarClick: () -> Unit) {
    AppCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        val headerGradient = Brush.verticalGradient(colors = listOf(Primary.copy(alpha = 0.06f), MaterialTheme.colorScheme.surface))
        Column(
            modifier = Modifier.fillMaxWidth()
                .background(headerGradient)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(100.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape).clickable(onClick = onAvatarClick),
                contentAlignment = Alignment.Center
            ) {
                if (avatar != null) {
                    AsyncImage(model = avatar, contentDescription = "头像", modifier = Modifier.size(100.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = "添加头像", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("点击上传", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier.width(32.dp).height(3.dp)
                    .background(Primary.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
private fun FormSection(title: String, action: (@Composable () -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val style = sectionStyles[title]
    AppCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                    if (style != null) {
                        Box(modifier = Modifier.size(SectionIconSize).background(style.color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(style.icon, contentDescription = null, tint = style.color, modifier = Modifier.size(SectionIconInnerSize))
                        }
                    }
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                action?.let { it() }
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun FormField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String = "", maxLines: Int = 1, required: Boolean = false, focusRequester: FocusRequester? = null) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            if (required) { Spacer(modifier = Modifier.width(2.dp)); Text("*", color = Error, fontSize = MaterialTheme.typography.bodyMedium.fontSize) }
        }
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth().then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier),
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half)) },
            singleLine = maxLines == 1, maxLines = maxLines, shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary.copy(alpha = AnimationTokens.Alpha.visible),
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun NotesField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
        placeholder = { Text("写下你对这个人的了解...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half)) },
        maxLines = 6, shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary.copy(alpha = AnimationTokens.Alpha.visible),
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half),
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    )
}

@Composable
private fun DatePickerField(label: String, value: String?, onValueChange: (String?) -> Unit, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value ?: "", onValueChange = onValueChange, modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
            placeholder = { Text("请选择日期", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half)) },
            trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            singleLine = true, readOnly = true, enabled = false, shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half),
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half),
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
private fun IntimacySlider(value: Int, onValueChange: (Int) -> Unit) {
    val currentLevel = IntimacyLevels.find { value in it.range } ?: IntimacyLevels[0]
    var selectedLevelIndex by remember { mutableIntStateOf(IntimacyLevels.indexOf(currentLevel)) }

    LaunchedEffect(value) {
        val newIndex = IntimacyLevels.indexOfFirst { value in it.range }.coerceIn(0, IntimacyLevels.size - 1)
        if (selectedLevelIndex != newIndex) selectedLevelIndex = newIndex
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IntimacyLevels.forEachIndexed { index, level ->
                IntimacyLevelCard(level = level, isSelected = selectedLevelIndex == index, onClick = { selectedLevelIndex = index; onValueChange(level.range.first + (level.range.last - level.range.first) / 2) }, modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().background(currentLevel.color.copy(alpha = 0.06f), RoundedCornerShape(12.dp)).padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(currentLevel.icon, contentDescription = null, tint = currentLevel.color, modifier = Modifier.size(20.dp))
                    Text(currentLevel.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = currentLevel.color)
                }
                Text("${value}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = currentLevel.color)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Slider(value = value.toFloat(), onValueChange = { newValue -> onValueChange(newValue.toInt()); selectedLevelIndex = IntimacyLevels.indexOfFirst { newValue.toInt() in it.range }.coerceIn(0, IntimacyLevels.size - 1) }, valueRange = 0f..100f, colors = SliderDefaults.colors(thumbColor = currentLevel.color, activeTrackColor = currentLevel.color, inactiveTrackColor = currentLevel.color.copy(alpha = 0.15f)))
    }
}

@Composable
private fun IntimacyLevelCard(level: IntimacyLevel, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clip(RoundedCornerShape(10.dp)).background(if (isSelected) level.color else level.color.copy(alpha = AnimationTokens.Alpha.faint)).clickable(onClick = onClick).padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(level.icon, contentDescription = null, tint = if (isSelected) Color.White else level.color, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(level.name, style = MaterialTheme.typography.labelMedium, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) Color.White else level.color)
    }
}

private data class IntimacyLevel(val range: IntRange, val name: String, val color: Color, val icon: ImageVector)

private val IntimacyLevels = listOf(
    IntimacyLevel(0..20, AppStrings.Intimacy.NEW, IntimacyNew, Icons.Default.PersonAdd),
    IntimacyLevel(21..40, AppStrings.Intimacy.ACQUAINTANCE, IntimacyAcquaintance, Icons.Default.PersonOutline),
    IntimacyLevel(41..60, AppStrings.Intimacy.FRIEND, IntimacyFriend, Icons.Default.People),
    IntimacyLevel(61..80, AppStrings.Intimacy.CLOSE, IntimacyClose, Icons.Default.Favorite),
    IntimacyLevel(81..100, AppStrings.Intimacy.FAMILY, IntimacyFamily, Icons.Default.FavoriteBorder)
)


