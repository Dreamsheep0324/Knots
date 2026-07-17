@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CookingStep
import com.tang.prm.domain.model.Ingredient
import com.tang.prm.domain.model.IngredientGroupType
import com.tang.prm.domain.model.RecipeDifficulty
import com.tang.prm.domain.model.RecipeTag
import com.tang.prm.ui.components.ContactAvatar
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.SignalGold
import com.tang.prm.ui.theme.SignalPurple

// ─────────────────────────────────────────────────────────────────
// 1. 食材分组编辑器
// ─────────────────────────────────────────────────────────────────

/**
 * 食材分组编辑器：圆角容器内展示分组标题（带色点）、食材行（名称/用量/单位/删除）和「+ 添加」按钮。
 */
@Composable
fun IngredientGroupEditor(
    groupType: IngredientGroupType,
    ingredients: List<Ingredient>,
    onAdd: () -> Unit,
    onUpdate: (Int, Ingredient) -> Unit,
    onRemove: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val dotColor = when (groupType) {
        IngredientGroupType.MAIN -> SignalAmber
        IngredientGroupType.SUB -> SignalGreen
        IngredientGroupType.SEASONING -> SignalPurple
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = groupType.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${ingredients.size} 项",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            if (ingredients.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
            }
            ingredients.forEachIndexed { index, ingredient ->
                IngredientRow(
                    ingredient = ingredient,
                    onUpdate = { onUpdate(index, it) },
                    onRemove = { onRemove(index) }
                )
                if (index < ingredients.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            AddRowButton(text = "添加${groupType.displayName}", onClick = onAdd)
        }
    }
}

@Composable
private fun IngredientRow(
    ingredient: Ingredient,
    onUpdate: (Ingredient) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        OutlinedTextField(
            value = ingredient.name,
            onValueChange = { onUpdate(ingredient.copy(name = it)) },
            modifier = Modifier.weight(1.4f),
            placeholder = { Text("名称", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            colors = recipeFieldColors()
        )
        OutlinedTextField(
            value = ingredient.amount,
            onValueChange = { onUpdate(ingredient.copy(amount = it)) },
            modifier = Modifier.weight(0.8f),
            placeholder = { Text("用量", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = recipeFieldColors()
        )
        OutlinedTextField(
            value = ingredient.unit,
            onValueChange = { onUpdate(ingredient.copy(unit = it)) },
            modifier = Modifier.weight(0.6f),
            placeholder = { Text("单位", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            colors = recipeFieldColors()
        )
        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Default.Close,
                contentDescription = "删除食材",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun AddRowButton(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ─────────────────────────────────────────────────────────────────
// 2. 步骤编辑器
// ─────────────────────────────────────────────────────────────────

/**
 * 步骤编辑器：圆角容器，左侧拖动柄 + 步骤编号标签，右侧描述输入框、定时器开关、删除按钮。
 */
@Composable
fun StepEditor(
    index: Int,
    step: CookingStep,
    onUpdate: (String) -> Unit,
    onUpdateTimer: (Int?) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.DragHandle,
                contentDescription = "拖动排序",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (index + 1).toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = step.description,
                    onValueChange = onUpdate,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("描述这一步的做法...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    minLines = 2,
                    shape = RoundedCornerShape(10.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    ),
                    colors = recipeFieldColors()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TimerToggleButton(
                        timerSeconds = step.timerSeconds,
                        onToggle = {
                            onUpdateTimer(if (step.timerSeconds == null) DEFAULT_STEP_TIMER_SECONDS else null)
                        }
                    )
                    step.timerSeconds?.let { seconds ->
                        Spacer(modifier = Modifier.width(8.dp))
                        TimerValueChip(seconds = seconds) { onUpdateTimer(it) }
                    }
                }
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "删除步骤",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun TimerToggleButton(
    timerSeconds: Int?,
    onToggle: () -> Unit
) {
    val active = timerSeconds != null
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onToggle),
        shape = RoundedCornerShape(50),
        color = if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (active) Icons.Filled.Timer else Icons.Outlined.Timer,
                contentDescription = "定时器",
                tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "定时",
                style = MaterialTheme.typography.labelMedium,
                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TimerValueChip(
    seconds: Int,
    onChange: (Int?) -> Unit
) {
    var editing by remember { mutableStateOf(false) }
    var text by remember(seconds) { mutableStateOf((seconds / 60).coerceAtLeast(1).toString()) }
    val focusRequester = remember { FocusRequester() }

    if (editing) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
                .padding(start = 10.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.filter { c -> c.isDigit() }.take(3) },
                modifier = Modifier
                    .width(44.dp)
                    .focusRequester(focusRequester),
                singleLine = true,
                textStyle = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
            Text("分", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            IconButton(
                onClick = {
                    val minutes = text.toIntOrNull()?.coerceAtLeast(1) ?: 1
                    onChange(minutes * 60)
                    editing = false
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "确认",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    } else {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                .clickable { editing = true }
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Timer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = formatMinutes(seconds),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun formatMinutes(seconds: Int): String {
    val minutes = (seconds / 60).coerceAtLeast(1)
    return "${minutes}分钟"
}

// ─────────────────────────────────────────────────────────────────
// 3. 评分星标
// ─────────────────────────────────────────────────────────────────

/**
 * 5 颗星评分选择器。点击星标设置评分 1-5，再次点击同一颗星可清零。
 */
@Composable
fun StarRating(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (i in 1..5) {
            val filled = i <= rating
            IconButton(
                onClick = { onRatingChange(if (rating == i) 0 else i) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (filled) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = "$i 星",
                    tint = if (filled) SignalGold else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// 4. 菜系快捷选择 / 难度选择器
// ─────────────────────────────────────────────────────────────────

private val COMMON_CUISINES = listOf("中式", "川菜", "粤菜", "湘菜", "鲁菜", "江浙菜", "日式", "韩式", "西式", "家常")

/**
 * 常见菜系快捷选择：点击 chip 快速填入菜系。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CuisineQuickSelector(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        COMMON_CUISINES.forEach { cuisine ->
            val isSelected = cuisine == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(cuisine) },
                label = { Text(cuisine, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    selectedLabelColor = MaterialTheme.colorScheme.primary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                    enabled = true,
                    selected = isSelected
                )
            )
        }
    }
}

/**
 * 难度分段选择器：三段式等宽卡片，选中态用对应颜色（简单=绿、中等=琥珀、困难=红）+ 色点指示。
 */
@Composable
fun DifficultySelector(
    selected: RecipeDifficulty,
    onSelect: (RecipeDifficulty) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RecipeDifficulty.entries.forEach { difficulty ->
            val isSelected = difficulty == selected
            val accentColor = when (difficulty) {
                RecipeDifficulty.EASY -> SignalGreen
                RecipeDifficulty.MEDIUM -> SignalAmber
                RecipeDifficulty.HARD -> MaterialTheme.colorScheme.error
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) accentColor.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                    .border(
                        1.dp,
                        if (isSelected) accentColor
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { onSelect(difficulty) },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = difficulty.displayName,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) accentColor
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// 5. 关联人物头像选择器
// ─────────────────────────────────────────────────────────────────

/**
 * 已选人物行：横向滚动展示已选人物 chip（头像 + 姓名 + 删除按钮），末尾「+」按钮触发选择。
 * 空态显示带 PersonAdd 图标的可点击卡片。
 */
@Composable
fun PersonAvatarSelector(
    contacts: List<Contact>,
    selectedIds: Set<Long>,
    onToggle: (Long) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedContacts = contacts.filter { it.id in selectedIds }

    if (selectedContacts.isEmpty()) {
        // 空态：可点击的添加卡片
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onAdd),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "添加关联人物",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    } else {
        LazyRow(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(selectedContacts, key = { it.id }) { contact ->
                SelectedContactChip(
                    contact = contact,
                    onRemove = { onToggle(contact.id) }
                )
            }
            item {
                AddContactButton(onClick = onAdd)
            }
        }
    }
}

/**
 * 已选人物 chip：拍立得风格白底卡片（头像 + 姓名），右上角黑色半透明删除按钮。
 */
@Composable
private fun SelectedContactChip(
    contact: Contact,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(top = 6.dp)
            .width(64.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.size(50.dp)) {
                ContactAvatar(
                    avatar = contact.avatar,
                    name = contact.name,
                    size = 50
                )
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.35f))
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "移除${contact.name}",
                        tint = Color.White,
                        modifier = Modifier.size(9.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = contact.name,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 添加人物按钮：拍立得风格白底卡片，PersonAdd 图标 + "添加"文字。
 */
@Composable
private fun AddContactButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .padding(top = 6.dp)
            .width(64.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .padding(5.dp)
                .clickable(onClick = onClick),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(50.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PersonAdd,
                    contentDescription = "添加关联人物",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "添加",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// 7. 标签选择器
// ─────────────────────────────────────────────────────────────────

/**
 * 标签芯片行：可用标签为浅色 chip（长按删除），已选标签为蓝色 chip 带「×」可移除，末尾「+ 添加」用于新建。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagSelector(
    availableTags: List<RecipeTag>,
    selectedTags: List<String>,
    onToggle: (String) -> Unit,
    onAdd: (String) -> Unit,
    onDelete: (RecipeTag) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<RecipeTag?>(null) }

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        availableTags.forEach { tag ->
            if (tag.name in selectedTags) {
                SelectedTagChip(tag = tag.name, onToggle = { onToggle(tag.name) })
            } else {
                UnselectedTagChip(
                    tag = tag.name,
                    onToggle = { onToggle(tag.name) },
                    onDelete = { deleteTarget = tag }
                )
            }
        }
        // 已选但不在 availableTags 中的自定义新标签
        selectedTags.filter { name -> availableTags.none { it.name == name } }.forEach { tag ->
            SelectedTagChip(tag = tag, onToggle = { onToggle(tag) })
        }
        AddTagChip(onClick = { showAddDialog = true; newTagName = "" })
    }

    if (showAddDialog) {
        RecipeTagAddDialog(
            name = newTagName,
            onNameChange = { newTagName = it },
            onConfirm = {
                if (newTagName.isNotBlank()) {
                    onAdd(newTagName.trim())
                    newTagName = ""
                    showAddDialog = false
                }
            },
            onDismiss = { showAddDialog = false }
        )
    }

    deleteTarget?.let { tag ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("删除标签？", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "「${tag.name}」将被永久删除。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(tag)
                    deleteTarget = null
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun SelectedTagChip(tag: String, onToggle: () -> Unit) {
    AssistChip(
        onClick = onToggle,
        label = { Text(tag, fontWeight = FontWeight.SemiBold) },
        trailingIcon = {
            Icon(Icons.Default.Close, contentDescription = "移除标签", modifier = Modifier.size(14.dp))
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primary,
            labelColor = MaterialTheme.colorScheme.onPrimary,
            trailingIconContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        border = null
    )
}

@Composable
private fun UnselectedTagChip(
    tag: String,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    FilterChip(
        selected = false,
        onClick = onToggle,
        label = { Text(tag) },
        trailingIcon = {
            Icon(
                Icons.Default.Close,
                contentDescription = "删除标签",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(14.dp)
                    .clickable(onClick = onDelete)
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = MaterialTheme.colorScheme.outline,
            selectedBorderColor = MaterialTheme.colorScheme.primary,
            enabled = true,
            selected = false
        )
    )
}

@Composable
private fun AddTagChip(onClick: () -> Unit) {
    FilterChip(
        selected = false,
        onClick = onClick,
        label = { Text("添加", fontWeight = FontWeight.Medium) },
        leadingIcon = {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.primary
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
            selectedBorderColor = MaterialTheme.colorScheme.primary,
            enabled = true,
            selected = false
        )
    )
}

@Composable
private fun RecipeTagAddDialog(
    name: String,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        title = { Text("新建标签", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("标签名称") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = recipeFieldColors()
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = name.isNotBlank()) {
                Text("添加", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────
// 公共：表单字段颜色
// ─────────────────────────────────────────────────────────────────

@Composable
private fun recipeFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
)

internal const val DEFAULT_STEP_TIMER_SECONDS = 60
