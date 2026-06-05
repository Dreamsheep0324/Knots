@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.reflect.favorites

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tang.prm.domain.model.Favorite
import com.tang.prm.ui.theme.*
import com.tang.prm.domain.util.DateUtils
import com.tang.prm.ui.animation.primitives.rememberBlinkingAlpha

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
