package com.tang.prm.ui.anniversary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tang.prm.domain.model.AnniversaryType
import com.tang.prm.ui.components.AppCard
import com.tang.prm.ui.components.ContactPickerDialog
import com.tang.prm.ui.components.AppDatePicker
import com.tang.prm.ui.components.FormScreenScaffold
import com.tang.prm.ui.theme.*
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.components.DiscardEditDialog
import com.tang.prm.ui.theme.DialogDefaults

data class AnniversaryTypeOption(
    val type: AnniversaryType,
    val icon: ImageVector,
    val iconTint: Color,
    val lightBg: Color,
    val selectedBg: Color,
    val description: String
)

private val anniversaryTypeOptions = listOf(
    AnniversaryTypeOption(
        type = AnniversaryType.BIRTHDAY,
        icon = Icons.Default.Cake,
        iconTint = Color(0xFFE8720C),
        lightBg = Color(0xFFFFF8F0),
        selectedBg = Color(0xFFFFF0E6),
        description = "记录生日，自动倒数"
    ),
    AnniversaryTypeOption(
        type = AnniversaryType.ANNIVERSARY,
        icon = Icons.Default.Favorite,
        iconTint = Color(0xFFD81B60),
        lightBg = Color(0xFFFFF5F7),
        selectedBg = Color(0xFFFDE8EC),
        description = "恋爱、结婚等重要日子"
    ),
    AnniversaryTypeOption(
        type = AnniversaryType.HOLIDAY,
        icon = Icons.Default.HolidayVillage,
        iconTint = Color(0xFF00838F),
        lightBg = Color(0xFFF0FAFA),
        selectedBg = Color(0xFFE0F5F5),
        description = "传统节日、公共假期"
    )
)

@Composable
fun AddAnniversaryScreen(
    contactId: Long? = null,
    anniversaryId: Long? = null,
    navController: NavController,
    viewModel: AddAnniversaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
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
        AlertDialog(
            onDismissRequest = { showIconPicker = false },
            containerColor = DialogDefaults.containerColor,
            title = {
                Text("选择图标", fontWeight = FontWeight.Bold)
            },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(commonAnniversaryIcons, key = { it.key }) { iconDef ->
                        val isSelected = uiState.selectedIcon == iconDef.key
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                viewModel.updateSelectedIcon(iconDef.key)
                                showIconPicker = false
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(iconDef.backgroundColor)
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(
                                                2.5.dp,
                                                iconDef.iconTint,
                                                CircleShape
                                            )
                                        } else {
                                            Modifier
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    iconDef.icon,
                                    contentDescription = null,
                                    tint = iconDef.iconTint,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = iconDef.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) iconDef.iconTint else Color(0xFF6B7280),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showIconPicker = false }) { Text("关闭") }
            }
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
                AppCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(getAnniversaryIconBackground(uiState.selectedIcon))
                                    .clickable { showIconPicker = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    getAnniversaryIcon(uiState.selectedIcon),
                                    contentDescription = null,
                                    tint = getAnniversaryIconTint(uiState.selectedIcon),
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "点击更换图标",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextGray,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = uiState.name,
                            onValueChange = viewModel::updateName,
                            modifier = Modifier.fillMaxWidth().focusRequester(nameFocusRequester),
                            label = { Text("纪念日名称 *") },
                            placeholder = { Text("如：张三的生日") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                        Text(
                            "纪念日类型",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            anniversaryTypeOptions.forEachIndexed { index, option ->
                                val isSelected = uiState.selectedType == option.type
                                val shape = when (index) {
                                    0 -> RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp)
                                    anniversaryTypeOptions.lastIndex -> RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp)
                                    else -> RoundedCornerShape(0.dp)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            color = if (isSelected) option.iconTint else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            shape = shape
                                        )
                                        .clickable { viewModel.updateSelectedType(option.type) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            option.icon,
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.visible),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = option.type.displayName,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.visible)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                AppCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("关联联系人", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.contactName ?: "",
                            onValueChange = { },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showContactSelector = true },
                            readOnly = true,
                            enabled = false,
                            placeholder = { Text("点击选择联系人（可选）") },
                            trailingIcon = {
                                Icon(Icons.Default.Person, contentDescription = "选择", tint = TextGray)
                            },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("日期", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.dateText,
                            onValueChange = { },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDatePicker = true },
                            readOnly = true,
                            enabled = false,
                            trailingIcon = {
                                Icon(Icons.Default.CalendarToday, contentDescription = "选择日期", tint = TextGray)
                            },
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("农历日期", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = uiState.isLunar,
                                onCheckedChange = { viewModel.updateIsLunar(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = OnPrimary,
                                    checkedTrackColor = Primary,
                                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    uncheckedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("每年重复", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = uiState.isRepeat,
                                onCheckedChange = { viewModel.updateIsRepeat(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = OnPrimary,
                                    checkedTrackColor = Primary,
                                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    uncheckedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }
            }

            item {
                AppCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        OutlinedTextField(
                            value = uiState.remarks,
                            onValueChange = viewModel::updateRemarks,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("备注") },
                            placeholder = { Text("添加备注信息...") },
                            minLines = 3,
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }
        }
    }
}
