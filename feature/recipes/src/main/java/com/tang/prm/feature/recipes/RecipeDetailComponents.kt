package com.tang.prm.feature.recipes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.model.CookingStep
import com.tang.prm.domain.model.Ingredient
import com.tang.prm.domain.model.IngredientGroupType
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.SignalPurple

/**
 * 份数调节器：胶囊形容器，左右各一个加减按钮，中间显示当前人份。
 */
@Composable
fun PortionScaler(
    currentServings: Int,
    onAdjust: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScalerButton(
                icon = Icons.Default.Remove,
                contentDescription = "减少份数",
                onClick = { onAdjust(-1) }
            )
            Text(
                text = "$currentServings 人份",
                style = MaterialTheme.typography.labelLarge,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(48.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            ScalerButton(
                icon = Icons.Default.Add,
                contentDescription = "增加份数",
                onClick = { onAdjust(1) }
            )
        }
    }
}

@Composable
private fun ScalerButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.size(26.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(26.dp)) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

/**
 * 食材分组区块：组名标题（带色点）+ 食材条目列表。
 * 当 [isScaled] 为 true 且食材可缩放时，用量以主题 primary 蓝色高亮显示。
 */
@Composable
fun IngredientGroupSection(
    groupName: String,
    groupType: IngredientGroupType,
    ingredients: List<Ingredient>,
    isScaled: Boolean,
    modifier: Modifier = Modifier
) {
    val dotColor = when (groupType) {
        IngredientGroupType.MAIN -> SignalAmber
        IngredientGroupType.SUB -> SignalGreen
        IngredientGroupType.SEASONING -> SignalPurple
    }
    val groupTint = when (groupType) {
        IngredientGroupType.MAIN -> SignalAmber
        IngredientGroupType.SUB -> SignalGreen
        IngredientGroupType.SEASONING -> SignalPurple
    }.copy(alpha = 0.06f)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = groupTint,
        border = BorderStroke(1.dp, dotColor.copy(alpha = 0.18f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = groupName,
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = "${ingredients.size} 项",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            if (ingredients.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
            }
            ingredients.forEachIndexed { index, ingredient ->
                IngredientRow(
                    ingredient = ingredient,
                    dotColor = dotColor,
                    isScaled = isScaled
                )
                if (index < ingredients.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp)
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    )
                }
            }
        }
    }
}

@Composable
private fun IngredientRow(
    ingredient: Ingredient,
    dotColor: Color,
    isScaled: Boolean
) {
    val scaled = isScaled && ingredient.isScalable
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor.copy(alpha = 0.5f))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = ingredient.name,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        val amountText = formatIngredientAmount(ingredient)
        if (scaled) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Text(
                    text = amountText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        } else {
            Text(
                text = amountText,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatIngredientAmount(ingredient: Ingredient): String {
    val amount = ingredient.amount.ifBlank { "—" }
    val unit = ingredient.unit
    return if (unit.isBlank()) amount else "$amount $unit"
}

/**
 * 烹饪步骤卡片：圆角容器内展示步骤描述，可选定时器 chip。
 * 配合时间线使用时，编号由时间线节点呈现。
 */
@Composable
fun StepCard(
    step: CookingStep,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        shadowElevation = 1.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // 左侧主题色强调条
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .align(Alignment.CenterStart)
                    .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
            )
            Column(modifier = Modifier.padding(start = 14.dp, top = 12.dp, end = 12.dp, bottom = 12.dp)) {
                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 24.sp
                )
                step.timerSeconds?.takeIf { it > 0 }?.let { seconds ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formatTimer(seconds),
                                style = MaterialTheme.typography.labelMedium,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTimer(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return when {
        minutes == 0 -> "${secs}秒"
        secs == 0 -> "${minutes}分钟"
        else -> "${minutes}分${secs}秒"
    }
}

/**
 * 关联人物 chip：圆形头像 + 姓名。
 */
@Composable
fun PersonChip(
    name: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(start = 6.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.firstOrNull()?.toString() ?: "?",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 区块标题：图标 + 标签 + 可选尾部内容。
 */
@Composable
fun DetailSectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 0.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        trailing?.invoke()
    }
}

/**
 * 根据菜系返回对应的食物 emoji。
 */
fun FoodEmoji(cuisine: String?): String {
    if (cuisine == null) return "🍽️"
    return when {
        cuisine.contains("中") || cuisine.contains("川") ||
            cuisine.contains("粤") || cuisine.contains("鲁") -> "🍚"
        cuisine.contains("日") || cuisine.contains("和") -> "🍣"
        cuisine.contains("韩") -> "🍲"
        cuisine.contains("意") || cuisine.contains("西") -> "🍝"
        cuisine.contains("烤") || cuisine.contains("烧") -> "🥩"
        cuisine.contains("面") || cuisine.contains("汤") -> "🍜"
        cuisine.contains("甜") || cuisine.contains("烘焙") || cuisine.contains("蛋糕") -> "🍰"
        else -> "🍽️"
    }
}
