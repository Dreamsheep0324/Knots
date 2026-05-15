@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tang.prm.domain.model.Favorite
import com.tang.prm.ui.theme.*
import com.tang.prm.util.DateUtils
import com.tang.prm.ui.animation.primitives.rememberBlinkingAlpha
import com.tang.prm.ui.animation.primitives.rememberBreathingPulse

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val favorites = uiState.favorites

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedFavorite by remember { mutableStateOf<Favorite?>(null) }
    val tabs = listOf("列表", "详情", "树状", "日志")
    val pagerState = rememberPagerState(pageCount = { tabs.size })

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (selectedTab != page) selectedTab = page
        }
    }
    LaunchedEffect(selectedTab) {
        if (pagerState.currentPage != selectedTab) {
            pagerState.animateScrollToPage(selectedTab)
        }
    }

    LaunchedEffect(favorites) {
        if (selectedFavorite == null || selectedFavorite !in favorites) {
            selectedFavorite = favorites.firstOrNull()
        }
    }

    val cursorAlpha by rememberBlinkingAlpha(onDuration = 530, offDuration = 530)

    val dateFormat: (Long) -> String = { DateUtils.formatMonthDayDot(it) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(FavoriteGold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "收藏夹",
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
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回",
                                tint = TermText
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = TermText,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = TermActiveTab,
                            height = 2.dp
                        )
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                val tabColor by animateColorAsState(
                                    targetValue = if (selectedTab == index) TermActiveTab else TermInactiveTab,
                                    animationSpec = tween(250),
                                    label = "tabColor$index"
                                )
                                Text(
                                    title,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = tabColor
                                )
                            }
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 96.dp)
            ) {
                HorizontalPager(
                    state = pagerState,
                    beyondViewportPageCount = 1,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { page ->
                    when (page) {
                        0 -> Column(modifier = Modifier.fillMaxSize()) {
                            TabListContent(
                                favorites = favorites,
                                viewModel = viewModel,
                                uiState = uiState,
                                dateFormat = dateFormat,
                                selectedFavorite = selectedFavorite,
                                onFavoriteClick = { selectedFavorite = it },
                                onFavoriteNavigate = { fav ->
                                    val route = getRouteForType(fav.sourceType, fav.sourceId)
                                    if (route != null) navController.navigate(route)
                                }
                            )
                        }
                        1 -> Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(bottom = 16.dp)
                        ) {
                            TabDetailContent(
                                favorites = favorites,
                                viewModel = viewModel,
                                uiState = uiState,
                                onFavoriteClick = { fav ->
                                    val route = getRouteForType(fav.sourceType, fav.sourceId)
                                    if (route != null) navController.navigate(route)
                                }
                            )
                        }
                        2 -> Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(bottom = 16.dp)
                        ) {
                            TabTreeContent(favorites = favorites)
                        }
                        3 -> Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(bottom = 16.dp)
                        ) {
                            TabLogContent(favorites = favorites)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                TermSeparator()

                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "$ ",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TermText
                    )
                    Box(
                        modifier = Modifier
                            .width(7.dp)
                            .height(14.dp)
                            .background(TermTagBg.copy(alpha = cursorAlpha))
                    )
                }

                TermStatusBar(
                    totalItems = favorites.size,
                    isLive = selectedTab == 3
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.TabListContent(
    favorites: List<Favorite>,
    viewModel: FavoritesViewModel,
    uiState: FavoritesUiState,
    dateFormat: (Long) -> String,
    selectedFavorite: Favorite?,
    onFavoriteClick: (Favorite) -> Unit,
    onFavoriteNavigate: (Favorite) -> Unit
) {
    TermPathBar(segments = listOf("~", "favorites", "all"))

    TermCommentLine("# 总计 ${favorites.size} 项收藏 [查询耗时 0.02s]")

    TermPromptLine("ls -la")

    TermSeparator()

    TermTableHeader()

    val maxDisplayCount = 7
    val displayFavorites = favorites.take(maxDisplayCount)
    val hasMore = favorites.size > maxDisplayCount

    displayFavorites.forEach { favorite ->
        TermTableRow(
            favorite = favorite,
            dateFormat = dateFormat,
            isSelected = favorite == selectedFavorite,
            onClick = { onFavoriteClick(favorite) }
        )
    }

    if (hasMore) {
        TermCommentLine("  ... 还有 ${favorites.size - maxDisplayCount} 项未显示")
    }

    TermSeparator()

    if (favorites.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "∅",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 28.sp,
                    color = TermComment
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "暂无收藏记录",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = TermComment
                )
            }
        }
    } else {
        TermDetailPanel(
            favorite = selectedFavorite,
            onClick = {
                selectedFavorite?.let { onFavoriteNavigate(it) }
            }
        )
    }
}

@Composable
private fun TabDetailContent(
    favorites: List<Favorite>,
    viewModel: FavoritesViewModel,
    uiState: FavoritesUiState,
    onFavoriteClick: (Favorite) -> Unit
) {
    TermPathBar(segments = listOf("~", "favorites", "all"))

    TermFilterRow(
        filters = viewModel.filterLabels,
        selectedIndex = uiState.selectedFilter,
        onFilterClick = { viewModel.setFilter(it) }
    )

    TermCommentLine("# 总计 ${favorites.size} 项收藏 · 逐项展开")

    TermPromptLine("cat favorites/*")

    TermThickSeparator()

    favorites.forEachIndexed { index, favorite ->
        val type = FavoriteType.fromCode(favorite.sourceType)
        val relativeTime = getRelativeTime(favorite.createdAt)
        val shortDate = DateUtils.formatMonthDayDotTime(favorite.createdAt)

        val cardInteractionSource = remember { MutableInteractionSource() }
        val cardPressed by cardInteractionSource.collectIsPressedAsState()
        val cardScale by animateFloatAsState(
            targetValue = if (cardPressed) 0.97f else 1f,
            animationSpec = tween(100),
            label = "cardScale"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .scale(cardScale)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .border(BorderStroke(0.5.dp, TermDetailBorder), RoundedCornerShape(12.dp))
                .clickable(
                    interactionSource = cardInteractionSource,
                    indication = null,
                    onClick = { onFavoriteClick(favorite) }
                )
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(28.dp)
                        .background(type.borderColor, RoundedCornerShape(1.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    favorite.title,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TermText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .background(TermTagBg, RoundedCornerShape(2.dp))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        type.label,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(shortDate, fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TermMuted)
                Text(type.source, fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TermMuted)
                Text(relativeTime, fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TermMuted)
            }

            favorite.description?.let { desc ->
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 11.dp)
                ) {
                    Text(
                        "↳ ",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = TermComment
                    )
                    Text(
                        desc,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = TermComment,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (index < favorites.size - 1) {
            TermSeparator()
        }
    }
}

@Composable
private fun TabTreeContent(favorites: List<Favorite>) {
    TermPathBar(segments = listOf("~", "favorites", "stats"))

    TermCommentLine("# 磁盘使用 & 目录结构")

    TermPromptLine("du -h favorites/")

    TermThickSeparator()

    val typeCounts = remember(favorites) {
        FavoriteType.entries.associateWith { type ->
            favorites.count { it.sourceType == type.code }
        }
    }
    val maxCount = typeCounts.values.maxOrNull() ?: 1

    typeCounts.forEach { (type, count) ->
        val filled = if (maxCount > 0) (count.toFloat() / maxCount * 20).toInt() else 0
        val bar = "█".repeat(filled) + "░".repeat(20 - filled)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 1.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                type.shortCode,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = TermMuted,
                modifier = Modifier.width(32.dp)
            )
            Text(
                bar,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                color = TermHighlight,
                letterSpacing = (-1).sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "$count",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = TermComment
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .height(1.dp)
            .background(TermDim)
    )

    val newestDate = remember(favorites) {
        favorites.maxByOrNull { it.createdAt }?.let {
            DateUtils.formatShortDate(it.createdAt)
        } ?: "--"
    }
    val activeType = remember(typeCounts) {
        typeCounts.maxByOrNull { it.value }?.key?.shortCode ?: "--"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TermStatItem("total=", "${favorites.size}")
        TermStatItem("types=", "${typeCounts.count { it.value > 0 }}")
        TermStatItem("newest=", newestDate)
        TermStatItem("active=", activeType)
    }

    TermThickSeparator()

    TermPromptLine("tree favorites/")

    TermSeparator()

    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        Text("favorites/", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TermText)

        val typeEntries = typeCounts.entries.toList()
        typeEntries.forEachIndexed { index, (type, count) ->
            val isLast = index == typeEntries.size - 1
            val connector = if (isLast) "└──" else "├──"
            val prefix = if (isLast) "    " else "│   "

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(connector, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TermComment)
                Spacer(modifier = Modifier.width(4.dp))
                Text("${type.label}/", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TermText)
                Spacer(modifier = Modifier.width(4.dp))
                Text("($count)", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TermComment)
            }

            val typeFavorites = favorites.filter { it.sourceType == type.code }
            val showItems = typeFavorites.take(2)
            showItems.forEachIndexed { itemIndex, fav ->
                val isLastItem = itemIndex == showItems.size - 1 && count <= 2
                val itemConnector = if (isLastItem) "└──" else "├──"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("$prefix$itemConnector", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TermComment)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(fav.title, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TermHighlight, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            if (count > 2) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${prefix}└──", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TermComment)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("... +${count - 2}", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TermComment)
                }
            }
        }
    }
}

@Composable
private fun TabLogContent(favorites: List<Favorite>) {
    TermPathBar(segments = listOf("~", "var", "log", "favorites.log"))

    TermCommentLine("# 实时收藏操作日志")

    TermPromptLine("tail -f /var/log/favorites.log")

    TermThickSeparator()

    val pulseAlpha by rememberBreathingPulse(minAlpha = 0.3f, maxAlpha = 1f, cycleDuration = 3000)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(SignalGreen.copy(alpha = pulseAlpha))
        )
        Text("ADD: ", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TermText)
        Text("${favorites.size}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TermText)
        Text("·", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TermMuted)
        Text("DEL: ", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TermText)
        Text("0", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TermText)
        Text("·", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TermMuted)
        Text("NET: +${favorites.size}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TermComment)
    }

    TermSeparator()

    val groupedByDate = remember(favorites) {
        favorites
            .sortedByDescending { it.createdAt }
            .groupBy { DateUtils.formatDate(it.createdAt) }
            .mapValues { entry ->
                entry.value.map { fav ->
                    Triple(DateUtils.formatTime(fav.createdAt), fav, "ADD")
                }
            }
    }

    groupedByDate.forEach { (date, entries) ->
        Text(
            "── $date ──",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = TermComment,
            letterSpacing = 1.sp
        )

        entries.forEach { (time, fav, op) ->
            val type = FavoriteType.fromCode(fav.sourceType)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        time,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = TermComment,
                        modifier = Modifier.width(32.dp)
                    )
                    Text(
                        op,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (op == "ADD") SignalGreen else Error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .border(BorderStroke(1.dp, TermMuted), RoundedCornerShape(1.dp))
                            .padding(horizontal = 3.dp, vertical = 0.dp)
                    ) {
                        Text(
                            type.shortCode,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp,
                            color = TermMuted,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        fav.title,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = TermHighlight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "#${fav.sourceId}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        color = TermDim
                    )
                }
                Row(modifier = Modifier.padding(start = 32.dp)) {
                    Text(
                        "↳ 来自「${type.source}」",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = TermComment
                    )
                    fav.description?.let { desc ->
                        Text(
                            " · $desc",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = TermComment,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    Text(
        "-- 共 ${favorites.size} 条记录 · ${groupedByDate.size} 个日期 --",
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        color = TermComment
    )
}
