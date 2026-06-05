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
import com.tang.prm.ui.theme.Primary
import com.tang.prm.ui.theme.TextGray
import com.tang.prm.ui.theme.DialogDefaults
import com.tang.prm.ui.theme.getGenericIcon
import com.tang.prm.ui.theme.toComposeColor
import com.tang.prm.ui.animation.core.AnimationTokens

enum class TagSelectorMode { SINGLE, MULTI }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagSelector(
    mode: TagSelectorMode,
    availableItems: List<CustomType>,
    selectedItems: List<String>,
    onSelectionChange: (List<String>) -> Unit,
    onAddItem: (String, String?, String?) -> Unit,
    onDeleteItem: (CustomType) -> Unit,
    title: String? = null,
    emptyText: String = "暂无标签，点击新增添加",
    showHeader: Boolean = true,
    showAddButton: Boolean = true,
    showAddDialog: Boolean = false,
    onAddDialogDismiss: () -> Unit = {},
    iconResolver: ((String) -> ImageVector?)? = null,
    showIconPicker: Boolean = false
) {
    var internalShowAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var typeToDelete by remember { mutableStateOf<CustomType?>(null) }

    val isAddDialogVisible = showAddDialog || internalShowAddDialog

    Column(modifier = Modifier.fillMaxWidth()) {
        if (showHeader && title != null) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (availableItems.isEmpty() && !showAddButton) {
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
                availableItems.forEach { item ->
                    val isSelected = selectedItems.contains(item.name)
                    val itemColor = item.color?.let {
                        it.toComposeColor(Primary)
                    } ?: Primary
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
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "删除",
                                    modifier = Modifier.size(10.dp).clickable { typeToDelete = item; showDeleteDialog = true },
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
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
                        onClick = { internalShowAddDialog = true },
                        label = { Text("新增", fontWeight = FontWeight.Medium) },
                        leadingIcon = {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = Primary)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = DialogDefaults.containerColor,
                            labelColor = Primary,
                            iconColor = Primary
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

    if (isAddDialogVisible) {
        AddTypeDialog(
            title = "新增${title?.let { "$it" } ?: ""}标签",
            onDismiss = { internalShowAddDialog = false; onAddDialogDismiss() },
            onConfirm = { name, color, icon -> onAddItem(name, color, icon); internalShowAddDialog = false; onAddDialogDismiss() },
            showIconPicker = showIconPicker
        )
    }
    if (showDeleteDialog && typeToDelete != null) {
        DeleteTypeDialog(
            typeName = typeToDelete!!.name,
            onDismiss = { showDeleteDialog = false; typeToDelete = null },
            onConfirm = { onDeleteItem(typeToDelete!!); showDeleteDialog = false; typeToDelete = null }
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
    val colorOptions = listOf(
        "#F43F5E", "#EF4444", "#F97316",
        "#EAB308", "#22C55E", "#10B981",
        "#14B8A6", "#06B6D4", "#3B82F6",
        "#6366F1", "#8B5CF6", "#EC4899"
    )
    val iconOptions = listOf(
        "People", "Person", "Group", "Restaurant", "LocalCafe", "Flight",
        "DirectionsCar", "Hotel", "Phone", "Message", "Videocam", "Email",
        "Work", "School", "Home", "Event", "Favorite", "Star",
        "AutoAwesome", "Celebration", "CardGiftcard", "Cake", "MusicNote", "SportsSoccer",
        "FitnessCenter", "ShoppingBag", "Pets", "LocalHospital", "ChildCare", "RocketLaunch"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = DialogDefaults.containerColor, tonalElevation = 0.dp) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("标签名称") }, singleLine = true,
                    shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half),
                        focusedContainerColor = DialogDefaults.containerColor, unfocusedContainerColor = DialogDefaults.containerColor
                    )
                )
                if (name.isNotBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("预览", style = MaterialTheme.typography.labelMedium, color = TextGray)
                        val previewColor = selectedColor?.let { colorHex ->
                            colorHex.toComposeColor(Primary)
                        } ?: Primary
                        Surface(shape = RoundedCornerShape(8.dp), color = previewColor.copy(alpha = AnimationTokens.Alpha.subtle)) {
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
                GridRow(items = colorOptions, columns = 6) { colorHex ->
                    val isSelected = selectedColor == colorHex
                    val color = colorHex.toComposeColor(Primary)
                    Box(
                        modifier = Modifier.size(36.dp).background(color, CircleShape)
                            .then(if (isSelected) Modifier.border(2.dp, Color.White, CircleShape).border(3.dp, color, CircleShape) else Modifier)
                            .clickable { selectedColor = if (selectedColor == colorHex) null else colorHex },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                if (showIconPicker) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("选择图标", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(10.dp))
                    GridRow(items = iconOptions, columns = 6) { iconName ->
                        val isSelected = selectedIcon == iconName
                        val icon = getGenericIcon(iconName)
                        if (icon != null) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Primary.copy(alpha = AnimationTokens.Alpha.subtle) else DialogDefaults.containerColor,
                                border = if (isSelected) BorderStroke(2.dp, Primary.copy(alpha = 0.4f)) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half))
                            ) {
                                Box(modifier = Modifier.fillMaxSize().clickable { selectedIcon = if (selectedIcon == iconName) null else iconName },
                                    contentAlignment = Alignment.Center) {
                                    Icon(imageVector = icon, contentDescription = null, tint = if (isSelected) Primary else TextGray, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onDismiss) { Text("取消", color = TextGray) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { if (name.isNotBlank()) onConfirm(name, selectedColor, selectedIcon) },
                        enabled = name.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, disabledContainerColor = Primary.copy(alpha = 0.4f))
                    ) { Text("添加", fontWeight = FontWeight.SemiBold) }
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
        Surface(shape = RoundedCornerShape(24.dp), color = DialogDefaults.containerColor, tonalElevation = 0.dp) {
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
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGray)
                    ) { Text("取消") }
                    Button(
                        onClick = onConfirm, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) { Text("删除", fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}
