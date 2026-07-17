@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.tang.prm.feature.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.tang.prm.ui.components.EmptyState
import com.tang.prm.ui.components.SegmentedOption
import com.tang.prm.ui.components.SegmentedToggleButton
import com.tang.prm.ui.navigation.AddRecipeRoute
import com.tang.prm.ui.navigation.RecipeDetailRoute
import com.tang.prm.ui.theme.OnSurface
import com.tang.prm.ui.theme.SignalElectric
import com.tang.prm.ui.theme.SignalGold

@Composable
fun RecipesScreen(
    navController: NavController,
    viewModel: RecipesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val data = uiState.data
    val dialog = uiState.dialog
    var viewMode by remember { mutableStateOf("list") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SignalElectric)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "菜谱",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = OnSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                actions = {
                    val viewModeOptions = listOf(
                        SegmentedOption("list", Icons.AutoMirrored.Filled.ViewList, "清单"),
                        SegmentedOption("grid", Icons.Default.GridView, "画廊"),
                        SegmentedOption("gallery", Icons.Default.PhotoLibrary, "相册")
                    )
                    SegmentedToggleButton(
                        options = viewModeOptions,
                        selectedKey = viewMode,
                        onSelectionChange = { viewMode = it }
                    )
                    IconButton(onClick = { navController.navigate(AddRecipeRoute) }) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "添加菜谱",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            RecipeSearchBar(
                query = searchState.query,
                onQueryChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            )

            if (data.availableTags.isNotEmpty()) {
                FilterChipRow(
                    availableTags = data.availableTags,
                    selectedTag = data.selectedTag,
                    onSelectTag = viewModel::selectTag,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (data.displayList.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Restaurant,
                    title = "还没有菜谱",
                    description = "记录下第一道想保存的菜，它可以关联重要的人。",
                    actionLabel = "添加菜谱",
                    onAction = { navController.navigate(AddRecipeRoute) }
                )
            } else {
                when (viewMode) {
                    "grid" -> RecipeGridView(
                        items = data.displayList,
                        onItemClick = { id -> navController.navigate(RecipeDetailRoute(id)) }
                    )
                    "gallery" -> RecipeGalleryView(
                        items = data.displayList,
                        onItemClick = { id -> navController.navigate(RecipeDetailRoute(id)) },
                        onItemDelete = { id -> viewModel.showDeleteConfirm(id) }
                    )
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = data.displayList,
                            key = { it.id }
                        ) { item ->
                            RecipeCard(
                                item = item,
                                searchQuery = searchState.query,
                                onClick = { navController.navigate(RecipeDetailRoute(item.id)) },
                                onDelete = { viewModel.showDeleteConfirm(item.id) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }

    if (dialog.showDeleteConfirm) {
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
                TextButton(onClick = { viewModel.deleteRecipe() }) {
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
}

// ─────────────────────────────────────────────────────────────────
// 视图模式切换器（右上角下拉菜单）
// ─────────────────────────────────────────────────────────────────

@Composable
private fun RecipeSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val bgColor = if (isFocused) MaterialTheme.colorScheme.surface
    else MaterialTheme.colorScheme.surfaceVariant
    val borderColor = if (isFocused) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    else Color.Transparent

    Surface(
        modifier = modifier
            .height(46.dp)
            .then(
                if (isFocused) Modifier.shadow(
                    elevation = 1.dp,
                    shape = CircleShape,
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                ) else Modifier
            )
            .border(2.dp, borderColor, CircleShape),
        shape = CircleShape,
        color = bgColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (query.isEmpty()) {
                    Text(
                        text = "搜索菜名、食材或人物",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    interactionSource = interactionSource,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                )
            }
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "清除",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChipRow(
    availableTags: List<String>,
    selectedTag: String?,
    onSelectTag: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "all") {
            RecipeFilterChip(
                text = "全部",
                selected = selectedTag == null,
                onClick = { onSelectTag(null) }
            )
        }
        items(
            items = availableTags,
            key = { it }
        ) { tag ->
            RecipeFilterChip(
                text = tag,
                selected = selectedTag == tag,
                onClick = { onSelectTag(if (selectedTag == tag) null else tag) }
            )
        }
    }
}

@Composable
private fun RecipeFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .then(
                if (selected) Modifier.shadow(
                    elevation = 1.dp,
                    shape = CircleShape,
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                ) else Modifier
            ),
        shape = CircleShape,
        color = containerColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
private fun RecipeCard(
    item: RecipeListItem,
    searchQuery: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val cardShape = RoundedCornerShape(20.dp)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .clickable(onClick = onClick),
        shape = cardShape,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 1.dp
    ) {
        Box {
            Row(modifier = Modifier.padding(16.dp)) {
                // 左侧缩略图：有照片用照片，否则用 emoji 占位
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    val firstPhoto = item.photos.firstOrNull()
                    if (firstPhoto != null) {
                        AsyncImage(
                            model = firstPhoto,
                            contentDescription = item.title,
                            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = cuisineEmoji(item.cuisine),
                            fontSize = 28.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 右侧信息体
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rememberHighlightedText(
                            text = item.title,
                            query = searchQuery
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Meta 行：时间 · dot · 份数
                    if (item.cookingTime != null || item.servings != null) {
                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item.cookingTime?.let {
                                Text(
                                    text = "${it}分钟",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (item.cookingTime != null && item.servings != null) {
                                Box(
                                    modifier = Modifier
                                        .size(3.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.outline)
                                )
                            }
                            item.servings?.let {
                                Text(
                                    text = "${it}人份",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // 描述（2 行，搜索高亮）
                    item.description?.takeIf { it.isNotEmpty() }?.let { desc ->
                        Text(
                            text = rememberHighlightedText(
                                text = desc,
                                query = searchQuery
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // 标签行：分类标签 = 蓝色，人物标签 = 灰色
                    if (item.tags.isNotEmpty() || item.contactNames.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            item.tags.forEach { tag ->
                                CategoryTag(tag)
                            }
                            item.contactNames.forEach { name ->
                                PersonTag(name)
                            }
                        }
                    }
                }
            }

            // 右上角圆形菜单按钮（绝对定位）
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable { showMenu = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "更多",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("删除", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryTag(text: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PersonTag(text: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun rememberHighlightedText(
    text: String,
    query: String
): AnnotatedString {
    val highlightBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    val highlightTextColor = MaterialTheme.colorScheme.primary
    return remember(text, query, highlightBg, highlightTextColor) {
        buildAnnotatedString {
            if (query.isBlank() || text.isEmpty()) {
                append(text)
                return@buildAnnotatedString
            }
            val lowerText = text.lowercase()
            val lowerQuery = query.lowercase()
            var startIndex = 0
            while (startIndex < text.length) {
                val matchIndex = lowerText.indexOf(lowerQuery, startIndex)
                if (matchIndex == -1) {
                    append(text.substring(startIndex))
                    break
                }
                if (matchIndex > startIndex) {
                    append(text.substring(startIndex, matchIndex))
                }
                withStyle(
                    SpanStyle(
                        background = highlightBg,
                        color = highlightTextColor,
                        fontWeight = FontWeight.SemiBold
                    )
                ) {
                    append(text.substring(matchIndex, matchIndex + query.length))
                }
                startIndex = matchIndex + query.length
            }
        }
    }
}

private fun cuisineEmoji(cuisine: String?): String {
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

// ─────────────────────────────────────────────────────────────────
// 画廊视图（grid）：美食画报风格大卡片，照片占满，底部信息浮层叠加
// ─────────────────────────────────────────────────────────────────

@Composable
private fun RecipeGridView(
    items: List<RecipeListItem>,
    onItemClick: (Long) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = items,
            key = { it.id }
        ) { item ->
            RecipeGridCard(
                item = item,
                onClick = { onItemClick(item.id) }
            )
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun RecipeGridCard(
    item: RecipeListItem,
    onClick: () -> Unit
) {
    val cardShape = RoundedCornerShape(16.dp)
    val firstPhoto = item.photos.firstOrNull()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .clickable(onClick = onClick),
        shape = cardShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            // 背景照片
            if (firstPhoto != null) {
                AsyncImage(
                    model = firstPhoto,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // 无照片时：居中 emoji + 渐变底
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = cuisineEmoji(item.cuisine), fontSize = 52.sp)
                }
            }
            // 底部渐变遮罩 + 信息浮层
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colorStops = arrayOf(
                                0f to Color.Transparent,
                                0.55f to Color.Transparent,
                                1f to Color.Black.copy(alpha = 0.65f)
                            )
                        )
                    )
            )
            // 顶部菜系角标
            item.cuisine?.takeIf { it.isNotEmpty() }?.let { cuisine ->
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = cuisine,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            // 顶部评分角标
            if (item.rating > 0) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.9f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.StarRate,
                            contentDescription = null,
                            tint = SignalGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = item.rating.toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            }
            // 底部信息
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Meta 行
                Row(
                    modifier = Modifier.padding(top = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item.cookingTime?.let {
                        Text(
                            text = "${it}分",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    if (item.cookingTime != null && item.servings != null) {
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.5f))
                        )
                    }
                    item.servings?.let {
                        Text(
                            text = "${it}人份",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    if (item.contactNames.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.5f))
                        )
                        Text(
                            text = "${item.contactNames.size}人喜欢",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// 相册视图（gallery）：菜谱卡风格，照片为主，下方信息块
// ─────────────────────────────────────────────────────────────────

@Composable
private fun RecipeGalleryView(
    items: List<RecipeListItem>,
    onItemClick: (Long) -> Unit,
    onItemDelete: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            items = items,
            key = { it.id }
        ) { item ->
            RecipeGalleryCard(
                item = item,
                onClick = { onItemClick(item.id) },
                onDelete = { onItemDelete(item.id) }
            )
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun RecipeGalleryCard(
    item: RecipeListItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val cardShape = RoundedCornerShape(16.dp)
    val firstPhoto = item.photos.firstOrNull()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .clickable(onClick = onClick),
        shape = cardShape,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.height(120.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左：方形照片
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                if (firstPhoto != null) {
                    AsyncImage(
                        model = firstPhoto,
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(text = cuisineEmoji(item.cuisine), fontSize = 40.sp)
                }
            }
            // 右：信息区
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 顶部行：菜系 + 评分 + 删除
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item.cuisine?.takeIf { it.isNotEmpty() }?.let {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = it,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    if (item.rating > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.StarRate,
                                contentDescription = null,
                                tint = SignalGold,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = item.rating.toString(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SignalGold,
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable(onClick = onDelete)
                    )
                }
                // 标题
                Text(
                    text = item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // 底部 meta
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item.cookingTime?.let {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "${it}分",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 3.dp)
                            )
                        }
                    }
                    if (item.cookingTime != null && item.servings != null) {
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.outline)
                        )
                    }
                    item.servings?.let {
                        Text(
                            text = "${it}人份",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // 底部：喜欢人数
                if (item.contactNames.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = item.contactNames.joinToString("、").takeIf { it.isNotEmpty() }
                                ?: "${item.contactNames.size}人喜欢",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 3.dp)
                        )
                    }
                }
            }
        }
    }
}
