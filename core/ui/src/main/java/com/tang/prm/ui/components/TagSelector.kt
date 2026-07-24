package com.tang.prm.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.tang.prm.domain.model.CustomType
import com.tang.prm.ui.theme.TextGray
import com.tang.prm.ui.theme.DialogDefaults
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.getGenericIcon
import com.tang.prm.ui.theme.toComposeColor
import com.tang.prm.ui.animation.core.AnimationTokens

enum class TagSelectorMode { SINGLE, MULTI }

data class TagSelectorConfig(
    val mode: TagSelectorMode,
    val title: String? = null,
    val emptyText: String = "暂无标签，点击新增添加",
    val showHeader: Boolean = true,
    val showAddButton: Boolean = true,
    val showIconPicker: Boolean = false
)

data class TagSelectorCallbacks(
    val onSelectionChange: (List<String>) -> Unit,
    val onAddItem: (String, String?, String?) -> Unit,
    val onDeleteItem: (CustomType) -> Unit,
    val onAddDialogDismiss: () -> Unit = {},
    val iconResolver: ((String) -> ImageVector?)? = null
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagSelector(
    config: TagSelectorConfig,
    availableItems: List<CustomType>,
    selectedItems: List<String>,
    callbacks: TagSelectorCallbacks,
    showAddDialog: Boolean = false
) {
    var internalShowAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var typeToDelete by remember { mutableStateOf<CustomType?>(null) }

    val isAddDialogVisible = showAddDialog || internalShowAddDialog

    Column(modifier = Modifier.fillMaxWidth()) {
        if (config.showHeader && config.title != null) {
            Text(
                config.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        TagChipList(
            items = availableItems,
            selectedItems = selectedItems,
            mode = config.mode,
            emptyText = config.emptyText,
            showAddButton = config.showAddButton,
            iconResolver = callbacks.iconResolver,
            onSelectionChange = callbacks.onSelectionChange,
            onAddClick = { internalShowAddDialog = true },
            onDeleteRequest = { typeToDelete = it; showDeleteDialog = true }
        )
    }

    TagSelectorDialogs(
        isAddVisible = isAddDialogVisible,
        addDialogTitle = "新增${config.title?.let { "$it" } ?: ""}标签",
        showIconPicker = config.showIconPicker,
        onAddDismiss = { internalShowAddDialog = false; callbacks.onAddDialogDismiss() },
        onAddConfirm = { name, color, icon -> callbacks.onAddItem(name, color, icon); internalShowAddDialog = false; callbacks.onAddDialogDismiss() },
        deleteTarget = if (showDeleteDialog) typeToDelete else null,
        onDeleteDismiss = { showDeleteDialog = false; typeToDelete = null },
        onDeleteConfirm = {
            typeToDelete?.let { callbacks.onDeleteItem(it) }
            showDeleteDialog = false
            typeToDelete = null
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagChipList(
    items: List<CustomType>,
    selectedItems: List<String>,
    mode: TagSelectorMode,
    emptyText: String,
    showAddButton: Boolean,
    iconResolver: ((String) -> ImageVector?)?,
    onSelectionChange: (List<String>) -> Unit,
    onAddClick: () -> Unit,
    onDeleteRequest: (CustomType) -> Unit
) {
    if (items.isEmpty() && !showAddButton) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                emptyText,
                style = MaterialTheme.typography.bodyMedium,
                color = TextGray.copy(alpha = 0.6f)
            )
        }
    } else {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.forEach { item ->
                val isSelected = selectedItems.contains(item.name)
                val itemColor = item.color?.let {
                    it.toComposeColor(MaterialTheme.colorScheme.primary)
                } ?: MaterialTheme.colorScheme.primary
                val resolvedIcon = iconResolver?.invoke(item.name)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        when (mode) {
                            TagSelectorMode.SINGLE -> onSelectionChange(if (isSelected) emptyList() else listOf(item.name))
                            TagSelectorMode.MULTI -> onSelectionChange(if (isSelected) selectedItems.toMutableList().apply { remove(item.name) } else selectedItems + item.name)
                        }
                    },
                    label = { Text(item.name, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal) },
                    leadingIcon = if (resolvedIcon != null) {
                        {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            } else {
                                Icon(resolvedIcon, contentDescription = null, modifier = Modifier.size(14.dp), tint = itemColor)
                            }
                        }
                    } else if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    } else if (item.color != null) {
                        { Box(modifier = Modifier.size(8.dp).background(itemColor, CircleShape)) }
                    } else null,
                    trailingIcon = if (!item.isDefault && showAddButton) {
                        {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable { onDeleteRequest(item) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "删除",
                                    modifier = Modifier.size(10.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = itemColor.copy(alpha = AnimationTokens.Alpha.subtle),
                        selectedLabelColor = itemColor,
                        selectedLeadingIconColor = itemColor
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = MaterialTheme.colorScheme.outline,
                        selectedBorderColor = itemColor.copy(alpha = 0.4f),
                        enabled = true,
                        selected = isSelected
                    )
                )
            }
            if (showAddButton) {
                FilterChip(
                    selected = false,
                    onClick = onAddClick,
                    label = { Text("新增", fontWeight = FontWeight.Medium) },
                    leadingIcon = {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = DialogDefaults.containerColor,
                        labelColor = MaterialTheme.colorScheme.primary,
                        iconColor = MaterialTheme.colorScheme.primary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half),
                        enabled = true,
                        selected = false
                    )
                )
            }
        }
    }
}

@Composable
private fun TagSelectorDialogs(
    isAddVisible: Boolean,
    addDialogTitle: String,
    showIconPicker: Boolean,
    onAddDismiss: () -> Unit,
    onAddConfirm: (String, String?, String?) -> Unit,
    deleteTarget: CustomType?,
    onDeleteDismiss: () -> Unit,
    onDeleteConfirm: () -> Unit
) {
    if (isAddVisible) {
        AddTypeDialog(
            title = addDialogTitle,
            onDismiss = onAddDismiss,
            onConfirm = onAddConfirm,
            showIconPicker = showIconPicker
        )
    }
    deleteTarget?.let { toDelete ->
        DeleteTypeDialog(
            typeName = toDelete.name,
            onDismiss = onDeleteDismiss,
            onConfirm = onDeleteConfirm
        )
    }
}

@Composable
private fun GridRow(items: List<String>, columns: Int, content: @Composable (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(columns).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                row.forEach { item -> content(item) }
                if (row.size < columns) {
                    repeat(columns - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
        }
    }
}

private val AddTypeColorOptions = listOf(
    "#F43F5E", "#EF4444", "#F97316",
    "#EAB308", "#22C55E", "#10B981",
    "#14B8A6", "#06B6D4", "#3B82F6",
    "#6366F1", "#8B5CF6", "#EC4899"
)

private val AddTypeIconOptionsWithPicker = listOf(
    "People", "Person", "Group", "Restaurant", "LocalCafe", "Flight",
    "DirectionsCar", "Hotel", "Phone", "Message", "Videocam", "Email",
    "Work", "School", "Home", "Event", "Favorite", "Star",
    "AutoAwesome", "Celebration", "CardGiftcard", "Cake", "MusicNote", "SportsSoccer",
    "FitnessCenter", "ShoppingBag", "Pets", "LocalHospital", "ChildCare", "RocketLaunch"
)

private val AddTypeIconOptionsDefault = listOf(
    "Cloud", "Storage", "Security", "Language", "SmartDisplay", "Headphones",
    "Videocam", "Palette", "Brush", "AutoStories", "MenuBook", "Podcasts",
    "SportsEsports", "FitnessCenter", "HealthAndSafety", "CreditCard", "Receipt", "ShoppingCart",
    "Subscriptions", "Devices", "Wifi", "VpnKey", "Backup", "Terminal",
    "Work", "School", "Home", "MusicNote", "ShoppingBag", "Pets"
)

@Composable
fun AddTypeDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String?, String?) -> Unit,
    showIconPicker: Boolean = false
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf<String?>(null) }
    var selectedIcon by remember { mutableStateOf<String?>(null) }
    val iconOptions = if (showIconPicker) AddTypeIconOptionsWithPicker else AddTypeIconOptionsDefault

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(Dimens.cornerXxl), color = DialogDefaults.containerColor, tonalElevation = 0.dp) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("标签名称") }, singleLine = true,
                    shape = RoundedCornerShape(Dimens.cornerMedium), modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half),
                        focusedContainerColor = DialogDefaults.containerColor, unfocusedContainerColor = DialogDefaults.containerColor
                    )
                )
                if (name.isNotBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("预览", style = MaterialTheme.typography.labelMedium, color = TextGray)
                        val previewColor = selectedColor?.let { colorHex ->
                            colorHex.toComposeColor(MaterialTheme.colorScheme.primary)
                        } ?: MaterialTheme.colorScheme.primary
                        Surface(shape = RoundedCornerShape(Dimens.cornerSmall), color = previewColor.copy(alpha = AnimationTokens.Alpha.subtle)) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val previewIcon = selectedIcon?.let { iconName -> getGenericIcon(iconName) }
                                if (previewIcon != null) {
                                    Icon(imageVector = previewIcon, contentDescription = null, tint = previewColor, modifier = Modifier.size(14.dp))
                                } else if (selectedColor != null) {
                                    Box(modifier = Modifier.size(8.dp).background(previewColor, CircleShape))
                                }
                                Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = previewColor)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text("选择颜色", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(10.dp))
                ColorPickerGrid(
                    options = AddTypeColorOptions,
                    selected = selectedColor,
                    onSelect = { selectedColor = it }
                )

                if (showIconPicker) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("选择图标", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(10.dp))
                    IconPickerGrid(
                        options = iconOptions,
                        selected = selectedIcon,
                        onSelect = { selectedIcon = it }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onDismiss) { Text("取消", color = TextGray) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { if (name.isNotBlank()) onConfirm(name, selectedColor, selectedIcon) },
                        enabled = name.isNotBlank(),
                        shape = RoundedCornerShape(Dimens.cornerMedium),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    ) { Text("添加", fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}

@Composable
private fun ColorPickerGrid(
    options: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit
) {
    GridRow(items = options, columns = 6) { colorHex ->
        val isSelected = selected == colorHex
        val color = colorHex.toComposeColor(MaterialTheme.colorScheme.primary)
        Box(
            modifier = Modifier.size(36.dp).background(color, CircleShape)
                .then(if (isSelected) Modifier.border(2.dp, Color.White, CircleShape).border(3.dp, color, CircleShape) else Modifier)
                .clickable { onSelect(if (selected == colorHex) null else colorHex) },
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun IconPickerGrid(
    options: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit
) {
    GridRow(items = options, columns = 6) { iconName ->
        val isSelected = selected == iconName
        val icon = getGenericIcon(iconName)
        if (icon != null) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = AnimationTokens.Alpha.subtle) else DialogDefaults.containerColor,
                border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half))
            ) {
                Box(modifier = Modifier.fillMaxSize().clickable { onSelect(if (selected == iconName) null else iconName) },
                    contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = if (isSelected) MaterialTheme.colorScheme.primary else TextGray, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun DeleteTypeDialog(
    typeName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(Dimens.cornerXxl), color = DialogDefaults.containerColor, tonalElevation = 0.dp) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(56.dp).background(Color(0xFFFEE2E2), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("删除标签", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(8.dp))
                Text("确定要删除「$typeName」吗？删除后无法恢复。", style = MaterialTheme.typography.bodyMedium, color = TextGray, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onDismiss, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(Dimens.cornerMedium),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGray)
                    ) { Text("取消") }
                    Button(
                        onClick = onConfirm, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(Dimens.cornerMedium),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) { Text("删除", fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}
