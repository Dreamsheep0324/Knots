package com.tang.prm.feature.subscription.subscription

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tang.prm.domain.model.SubscriptionCycle
import com.tang.prm.domain.util.DateUtils
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.components.AppCard
import com.tang.prm.ui.components.AppDatePicker
import com.tang.prm.ui.components.DiscardEditDialog
import com.tang.prm.ui.components.FormSectionLabel
import com.tang.prm.ui.components.FormScreenScaffold
import com.tang.prm.ui.components.TagSelector
import com.tang.prm.ui.components.TagSelectorMode
import com.tang.prm.ui.theme.SignalSky

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddSubscriptionScreen(
    subscriptionId: Long,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddSubscriptionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showExitDialog by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(subscriptionId) {
        if (subscriptionId > 0) viewModel.initForEdit(subscriptionId)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }

    BackHandler(enabled = uiState.hasUnsavedChanges) { showExitDialog = true }

    if (showExitDialog) {
        DiscardEditDialog(
            onDiscard = { showExitDialog = false; onBack() },
            onDismiss = { showExitDialog = false }
        )
    }

    if (showStartDatePicker) {
        AppDatePicker(
            show = true,
            onDismiss = { showStartDatePicker = false },
            onDateSelected = { viewModel.updateStartDate(it); showStartDatePicker = false },
            initialDate = uiState.startDate
        )
    }

    FormScreenScaffold(
        title = if (uiState.isEdit) "编辑订阅" else "新建订阅",
        onSaveClick = { viewModel.saveSubscription() },
        saveEnabled = uiState.name.isNotBlank() && uiState.price.isNotBlank(),
        onBackClick = { if (uiState.hasUnsavedChanges) showExitDialog = true else onBack() }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 名称
            item {
                SubscriptionFormSection(
                    icon = Icons.Default.CreditCard,
                    iconColor = Color(0xFF42A5F5),
                    title = "名称"
                ) {
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = viewModel::updateName,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("订阅名称", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors()
                    )
                }
            }

            // 分类
            item {
                SubscriptionFormSection(
                    icon = Icons.Default.Style,
                    iconColor = Color(0xFF8B5CF6),
                    title = "分类"
                ) {
                    TagSelector(
                        mode = TagSelectorMode.SINGLE,
                        availableItems = uiState.categoryOptions,
                        selectedItems = listOfNotNull(uiState.category),
                        onSelectionChange = { viewModel.updateCategory(it.firstOrNull()) },
                        onAddItem = { name, color, icon -> viewModel.addCustomType(name, color, icon) },
                        onDeleteItem = { viewModel.deleteCustomType(it) },
                        showHeader = false,
                        showIconPicker = false,
                        emptyText = "暂无分类，点击新增"
                    )
                }
            }

            // 价格与周期
            item {
                SubscriptionFormSection(
                    icon = Icons.Default.Payments,
                    iconColor = Color(0xFF22C55E),
                    title = "价格与周期"
                ) {
                    // 价格输入
                    OutlinedTextField(
                        value = uiState.price,
                        onValueChange = viewModel::updatePrice,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("金额", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = {
                            Text(
                                when (uiState.currency) {
                                    "CNY" -> "¥"; "USD" -> "$"; "EUR" -> "€"; "GBP" -> "£"; else -> uiState.currency
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    // 周期选择
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SubscriptionCycle.entries.forEach { cycle ->
                            FilterChip(
                                selected = uiState.cycle == cycle,
                                onClick = { viewModel.updateCycle(cycle) },
                                label = { Text(cycle.displayName) }
                            )
                        }
                    }
                }
            }

            // 日期
            item {
                SubscriptionFormSection(
                    icon = Icons.Default.CalendarToday,
                    iconColor = Color(0xFFF97316),
                    title = "日期"
                ) {
                    // 开始日期 - 可点击选择
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .clickable { showStartDatePicker = true }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("开始日期", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(formatTimestamp(uiState.startDate), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                        Text("选择", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // 下次扣费 - 自动计算，只读展示
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("下次扣费", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(formatTimestamp(uiState.nextBillingDate), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                        Text("自动计算", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    }
                }
            }

            // 备注
            item {
                SubscriptionFormSection(
                    icon = Icons.Default.EditNote,
                    iconColor = Color(0xFF64748B),
                    title = "备注"
                ) {
                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = viewModel::updateNotes,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                        placeholder = { Text("添加备注...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half)) },
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors()
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun SubscriptionFormSection(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    content: @Composable () -> Unit
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            FormSectionLabel(icon = icon, label = title, color = iconColor)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = AnimationTokens.Alpha.visible),
    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half),
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
)

private fun formatTimestamp(timestamp: Long): String {
    if (timestamp <= 0L) return "—"
    return DateUtils.formatYearMonthDayChineseFull(timestamp)
}
