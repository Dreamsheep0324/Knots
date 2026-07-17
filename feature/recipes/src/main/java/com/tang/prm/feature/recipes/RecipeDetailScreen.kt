@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.recipes

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BreakfastDining
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CookingStep
import com.tang.prm.domain.model.Ingredient
import com.tang.prm.domain.model.IngredientGroupType
import com.tang.prm.domain.model.Recipe
import com.tang.prm.domain.model.RecipeDifficulty
import com.tang.prm.domain.util.DateUtils
import com.tang.prm.ui.components.ContactAvatar
import com.tang.prm.ui.navigation.EditRecipeRoute
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalElectric
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.SignalGold

/**
 * 食谱详情页所有用户操作回调，封装为单一 data class 以缩短 Composable 参数列表，
 * 并通过 Stable 合约帮助子 Composable 跳过不必要的重组。
 */
data class RecipeDetailActions(
    val onBack: () -> Unit,
    val onEdit: () -> Unit,
    val onToggleFavorite: () -> Unit,
    val onAdjustServings: (Int) -> Unit,
    val onDelete: () -> Unit
)

@Composable
fun RecipeDetailScreen(
    recipeId: Long,
    navController: NavController,
    viewModel: RecipeDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val data = uiState.data
    val recipe = data.recipe

    LaunchedEffect(recipeId) {
        viewModel.loadRecipe(recipeId)
    }

    if (uiState.dialog.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteConfirm() },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            title = { Text("删除这道菜谱？", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "删除后将无法恢复，关联的人物记录也会解除。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRecipe { navController.popBackStack() }
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteConfirm() }) {
                    Text("取消")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        when {
            data.isLoading && recipe == null -> LoadingState()
            recipe != null -> RecipeDetailContent(
                recipe = recipe,
                currentServings = data.currentServings,
                originalServings = data.originalServings,
                scaledIngredients = data.scaledIngredients,
                contacts = data.contacts,
                actions = RecipeDetailActions(
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(EditRecipeRoute(recipeId)) },
                    onToggleFavorite = viewModel::toggleFavorite,
                    onAdjustServings = viewModel::adjustServings,
                    onDelete = viewModel::showDeleteConfirm
                )
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun RecipeDetailContent(
    recipe: Recipe,
    currentServings: Int,
    originalServings: Int,
    scaledIngredients: List<Ingredient>,
    contacts: List<Contact>,
    actions: RecipeDetailActions
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        item(key = "hero") {
            HeroSection(
                recipe = recipe,
                onBack = actions.onBack,
                onToggleFavorite = actions.onToggleFavorite,
                onEdit = actions.onEdit
            )
        }
        item(key = "body") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                    TitleRow(recipe = recipe)
                    // 标签
                    if (recipe.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        TagsSection(tags = recipe.tags)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    // 基本信息卡片：时间 / 难度 / 份数
                    BasicInfoCard(
                        recipe = recipe,
                        currentServings = currentServings,
                        onAdjustServings = actions.onAdjustServings
                    )
                    // 关联人物
                    if (contacts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        RelatedPeopleSection(contacts = contacts)
                    }
                    // 食材清单
                    if (scaledIngredients.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        IngredientsSection(
                            ingredients = scaledIngredients,
                            currentServings = currentServings,
                            originalServings = originalServings
                        )
                    }
                    // 烹饪步骤
                    if (recipe.steps.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        StepsSection(steps = recipe.steps)
                    }
                    // 成品照片（多张时展示缩略图横滚）
                    if (recipe.photos.size > 1) {
                        Spacer(modifier = Modifier.height(20.dp))
                        PhotosGallerySection(photos = recipe.photos)
                    }
                    // 备忘笔记
                    val notes = recipe.notes
                    if (!notes.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        NotesSection(notes = notes)
                    }
                    // 简介
                    val description = recipe.description
                    if (!description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        DescriptionSection(description = description)
                    }
                    DeleteButton(onDelete = actions.onDelete)
                }
            }
        }
    }
}

@Composable
private fun HeroSection(
    recipe: Recipe,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit
) {
    val coverPhoto = recipe.photos.firstOrNull()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 12.dp)
    ) {
        // 操作栏放在照片上方，让图片与状态栏拉开距离
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassIconButton(
                onClick = onBack,
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回"
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassIconButton(
                    onClick = onToggleFavorite,
                    icon = if (recipe.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = "收藏",
                    tint = if (recipe.isFavorite) SignalGold else MaterialTheme.colorScheme.onSurface,
                    backgroundColor = if (recipe.isFavorite) Color(0xFFFFF8E1).copy(alpha = 0.95f)
                    else Color.White.copy(alpha = 0.88f)
                )
                GlassIconButton(
                    onClick = onEdit,
                    icon = Icons.Filled.Edit,
                    contentDescription = "编辑"
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (coverPhoto != null) {
                AsyncImage(
                    model = coverPhoto,
                    contentDescription = recipe.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                ) {
                    Text(
                        text = FoodEmoji(recipe.cuisine),
                        fontSize = 80.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // 底部渐变遮罩，托起左下角信息胶囊
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .align(Alignment.BottomStart)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.45f)
                            )
                        )
                    )
            )

            // 左下角菜系与耗时胶囊
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomStart),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                recipe.cuisine?.takeIf { it.isNotEmpty() }?.let { cuisine ->
                    InfoCapsule(text = cuisine, tint = SignalElectric)
                }
                recipe.cookingTime?.let { time ->
                    InfoCapsule(
                        text = "$time 分钟",
                        tint = SignalGreen,
                        leadingIcon = Icons.Default.Schedule
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    backgroundColor: Color = Color.White.copy(alpha = 0.88f),
    size: Int = 38
) {
    Surface(
        modifier = modifier.size(size.dp),
        shape = CircleShape,
        color = backgroundColor,
        shadowElevation = 2.dp
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(size.dp)) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size((size / 2).dp)
            )
        }
    }
}

@Composable
private fun InfoCapsule(
    text: String,
    tint: Color,
    leadingIcon: ImageVector? = null
) {
    Surface(
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.92f),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon?.let {
                Icon(
                    it,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = tint
            )
        }
    }
}

@Composable
private fun TitleRow(recipe: Recipe) {
    Column {
        Text(
            text = recipe.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = (-0.4).sp,
            lineHeight = 36.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = DateUtils.formatYearMonthDayDot(recipe.createdAt),
                style = MaterialTheme.typography.bodySmall,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (recipe.rating > 0) {
                Spacer(modifier = Modifier.width(10.dp))
                RatingBadge(rating = recipe.rating)
            }
        }
    }
}

@Composable
private fun RatingBadge(rating: Int) {
    Surface(
        shape = CircleShape,
        color = SignalGold.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = SignalGold,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "$rating",
                style = MaterialTheme.typography.labelMedium,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SignalAmber
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// 基本信息：玻璃质感横向统计卡
// ─────────────────────────────────────────────────────────────────

@Composable
private fun BasicInfoCard(
    recipe: Recipe,
    currentServings: Int,
    onAdjustServings: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InfoStat(
                icon = Icons.Default.Schedule,
                iconTint = SignalGreen,
                label = "时间",
                value = recipe.cookingTime?.let { "${it}分钟" } ?: "—",
                modifier = Modifier.weight(1f)
            )
            StatDivider()
            InfoStat(
                icon = Icons.Default.Whatshot,
                iconTint = when (recipe.difficulty) {
                    RecipeDifficulty.EASY -> SignalGreen
                    RecipeDifficulty.MEDIUM -> SignalAmber
                    RecipeDifficulty.HARD -> MaterialTheme.colorScheme.error
                },
                label = "难度",
                value = recipe.difficulty.displayName,
                modifier = Modifier.weight(1f)
            )
            StatDivider()
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "份数",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                PortionScaler(
                    currentServings = currentServings,
                    onAdjust = onAdjustServings
                )
            }
        }
    }
}

@Composable
private fun InfoStat(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(44.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    )
}

// ─────────────────────────────────────────────────────────────────
// 标签
// ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsSection(tags: List<String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { tag ->
            Surface(
                shape = CircleShape,
                color = SignalPurple.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, SignalPurple.copy(alpha = 0.22f))
            ) {
                Text(
                    text = tag,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = 12.sp,
                    color = SignalPurple,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// 关联人物
// ─────────────────────────────────────────────────────────────────

@Composable
private fun RelatedPeopleSection(contacts: List<Contact>) {
    DetailSectionLabel("关联人物", icon = Icons.Default.Group, iconTint = SignalGreen)
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(contacts, key = { it.id }) { contact ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(60.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.5.dp, SignalGreen.copy(alpha = 0.3f)),
                    shadowElevation = 1.dp
                ) {
                    ContactAvatar(
                        avatar = contact.avatar,
                        name = contact.name,
                        size = 52,
                        modifier = Modifier.padding(2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// 食材清单
// ─────────────────────────────────────────────────────────────────

@Composable
private fun IngredientsSection(
    ingredients: List<Ingredient>,
    currentServings: Int,
    originalServings: Int
) {
    val isScaled = originalServings > 0 && currentServings != originalServings
    DetailSectionLabel(
        text = "食材清单",
        icon = Icons.Default.BreakfastDining,
        iconTint = SignalAmber
    ) {
        if (isScaled) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            ) {
                Text(
                    text = "已按 $currentServings 人份换算",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)) {
            IngredientGroupType.entries.forEachIndexed { index, groupType ->
                val groupIngredients = ingredients.filter { it.groupType == groupType }
                if (groupIngredients.isNotEmpty()) {
                    if (index > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    IngredientGroupSection(
                        groupName = groupType.displayName,
                        groupType = groupType,
                        ingredients = groupIngredients,
                        isScaled = isScaled
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// 烹饪步骤
// ─────────────────────────────────────────────────────────────────

@Composable
private fun StepsSection(steps: List<CookingStep>) {
    DetailSectionLabel("烹饪步骤", icon = Icons.Default.RestaurantMenu, iconTint = SignalGreen)
    Column {
        steps.forEachIndexed { index, step ->
            val isLast = index == steps.lastIndex
            TimelineStep(
                step = step,
                isLast = isLast
            )
        }
    }
}

@Composable
private fun TimelineStep(
    step: CookingStep,
    isLast: Boolean
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = step.order.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .padding(vertical = 4.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        StepCard(
            step = step,
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 12.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────
// 成品照片画廊
// ─────────────────────────────────────────────────────────────────

@Composable
private fun PhotosGallerySection(photos: List<String>) {
    DetailSectionLabel("成品照片", icon = Icons.Default.Image, iconTint = SignalAmber)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(photos, key = { it }) { photo ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 2.dp
            ) {
                AsyncImage(
                    model = photo,
                    contentDescription = "成品照片",
                    modifier = Modifier
                        .size(112.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// 备忘笔记
// ─────────────────────────────────────────────────────────────────

@Composable
private fun NotesSection(notes: String) {
    DetailSectionLabel("备忘笔记", icon = Icons.AutoMirrored.Filled.Notes, iconTint = SignalPurple)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = SignalPurple.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, SignalPurple.copy(alpha = 0.18f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(SignalPurple.copy(alpha = 0.5f))
            )
            Text(
                text = notes,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// 简介
// ─────────────────────────────────────────────────────────────────

@Composable
private fun DescriptionSection(description: String) {
    DetailSectionLabel("简介", icon = Icons.Default.AutoAwesome, iconTint = SignalElectric)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        shadowElevation = 1.dp
    ) {
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun DeleteButton(onDelete: () -> Unit) {
    Spacer(modifier = Modifier.height(28.dp))
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f))
    ) {
        TextButton(
            onClick = onDelete,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Filled.DeleteOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "删除菜谱",
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}
