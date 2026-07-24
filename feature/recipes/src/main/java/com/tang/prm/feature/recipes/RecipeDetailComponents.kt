package com.tang.prm.feature.recipes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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

private data class IngredientGroupStyle(
    val accentColor: Color,
    val amountTextColor: Color,
    val englishLabel: String
)

private fun ingredientGroupStyleFor(groupType: IngredientGroupType): IngredientGroupStyle =
    when (groupType) {
        IngredientGroupType.MAIN -> IngredientGroupStyle(
            accentColor = SignalAmber,
            amountTextColor = Color(0xFFB07000),
            englishLabel = "MAIN"
        )
        IngredientGroupType.SUB -> IngredientGroupStyle(
            accentColor = SignalGreen,
            amountTextColor = Color(0xFF0A7A52),
            englishLabel = "SUB"
        )
        IngredientGroupType.SEASONING -> IngredientGroupStyle(
            accentColor = SignalPurple,
            amountTextColor = Color(0xFF4250D4),
            englishLabel = "SEASONING"
        )
    }

/**
 * 食材分组区块：胶囊分栏版式（方案 C 精致版）。
 * 组标题由「色条 + 中文名 + 英文小标 + 数量徽章 + 渐变装饰线」组成；
 * 食材行为双栏底色胶囊——左栏淡灰底承载食材名（前置序号点），
 * 右栏组色淡底承载用量，两栏拼接成完整胶囊。
 * 当 [isScaled] 为 true 且食材可缩放时，用量右栏改用主题 primary 色高亮。
 */
@Composable
fun IngredientGroupSection(
    groupName: String,
    groupType: IngredientGroupType,
    ingredients: List<Ingredient>,
    isScaled: Boolean,
    modifier: Modifier = Modifier
) {
    val groupStyle = ingredientGroupStyleFor(groupType)
    val accentColor = groupStyle.accentColor
    val amountTextColor = groupStyle.amountTextColor
    val englishLabel = groupStyle.englishLabel

    Column(modifier = modifier.fillMaxWidth()) {
        // 组标题行：色条 + 中文名 + 英文小标 + 数量徽章
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(bottom = 10.dp)
                .padding(start = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = groupName,
                style = MaterialTheme.typography.labelLarge,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = englishLabel,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor.copy(alpha = 0.7f),
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${ingredients.size} 项",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.1f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
        // 组标题下渐变装饰线
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, bottom = 4.dp)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(accentColor.copy(alpha = 0.35f), Color.Transparent)
                    )
                )
        )
        // 食材胶囊列表
        ingredients.forEach { ingredient ->
            IngredientRow(
                ingredient = ingredient,
                accentColor = accentColor,
                amountTextColor = amountTextColor,
                isScaled = isScaled
            )
        }
    }
}

@Suppress("FunctionNaming")
@Composable
private fun IngredientRow(
    ingredient: Ingredient,
    accentColor: Color,
    amountTextColor: Color,
    isScaled: Boolean
) {
    val scaled = isScaled && ingredient.isScalable
    val amountText = formatIngredientAmount(ingredient)
    val pillColor = if (scaled) MaterialTheme.colorScheme.primary else accentColor
    val pillTextColor = if (scaled) MaterialTheme.colorScheme.primary else amountTextColor

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
    ) {
        // 左栏：食材名（淡灰底，左圆角，前置序号点）
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.028f)),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.55f))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = ingredient.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        // 右栏：用量（组色淡底，右圆角）
        Box(
            modifier = Modifier
                .width(96.dp)
                .fillMaxHeight()
                .background(pillColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = amountText,
                style = MaterialTheme.typography.labelMedium,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = pillTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
