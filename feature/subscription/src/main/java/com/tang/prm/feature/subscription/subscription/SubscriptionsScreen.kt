package com.tang.prm.feature.subscription.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.tang.prm.ui.components.SearchBar
import com.tang.prm.ui.navigation.AddSubscriptionRoute
import com.tang.prm.ui.navigation.SubscriptionDetailRoute
import com.tang.prm.ui.navigation.SubscriptionStatsRoute
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.Primary
import com.tang.prm.ui.theme.SignalSky

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionsScreen(
    navController: NavHostController,
    viewModel: SubscriptionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SignalSky)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "订阅",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(22.dp))
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(AddSubscriptionRoute) }) {
                        Icon(Icons.Default.Add, contentDescription = "添加", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(22.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            SearchBar(
                query = searchState.query,
                onQueryChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.paddingPage),
                placeholder = "搜索订阅..."
            )

            Spacer(modifier = Modifier.height(6.dp))

            val stats = uiState.data.stats
            if (stats != null) {
                StatsSummaryCard(
                    monthlyTotal = stats.monthlyTotal,
                    yearlyTotal = stats.yearlyTotal,
                    expiringSoonCount = stats.expiringSoon.size,
                    activeCount = stats.activeCount,
                    onViewStats = { navController.navigate(SubscriptionStatsRoute) },
                    modifier = Modifier.padding(horizontal = Dimens.paddingPage)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            FilterTabs(
                selectedTab = uiState.data.selectedTab,
                onTabSelected = viewModel::onTabSelected,
                modifier = Modifier.padding(horizontal = Dimens.paddingPage)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.data.groupedByCategory.isEmpty() && !uiState.data.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(Primary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "暂无订阅",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "添加你的第一个订阅服务",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    uiState.data.groupedByCategory.forEach { (category, subscriptions) ->
                        item(key = "group_$category") {
                            CategoryGroupCard(
                                category = category,
                                subscriptions = subscriptions,
                                onItemClick = { sub ->
                                    navController.navigate(SubscriptionDetailRoute(sub.id))
                                }
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(100.dp).navigationBarsPadding()) }
                }
            }
        }
    }
}
