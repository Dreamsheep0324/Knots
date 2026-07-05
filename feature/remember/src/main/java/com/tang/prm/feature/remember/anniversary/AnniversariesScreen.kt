package com.tang.prm.feature.remember.anniversary

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tang.prm.domain.model.AppStrings
import com.tang.prm.ui.animation.primitives.staggeredAppear
import com.tang.prm.ui.components.SearchBar
import com.tang.prm.ui.navigation.AddAnniversaryRoute
import com.tang.prm.ui.navigation.AnniversaryDetailRoute
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnniversariesScreen(
    navController: NavController,
    isTabletLayout: Boolean = false,
    viewModel: AnniversariesViewModel = hiltViewModel()
) {
    // 平板模式：方案1 时光长廊（独立全屏设计，自带顶栏/搜索/Hero/时间轴/类型分组）
    if (isTabletLayout) {
        AnniversaryTabletScreen(navController = navController, viewModel = viewModel)
        return
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val dataState = uiState.data
    val tabs = listOf(AppStrings.Tabs.ALL, AppStrings.Tabs.UPCOMING, AppStrings.Tabs.PAST)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        TopAppBar(
            title = {
                Text("纪念", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            },
            actions = {
                IconButton(onClick = { navController.navigate(AddAnniversaryRoute()) }) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "新建纪念日",
                            tint = Primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        SearchBar(
            query = searchState.query,
            onQueryChange = viewModel::onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.paddingPage),
            placeholder = "搜索人物、昵称、纪念日"
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.padding(horizontal = Dimens.paddingPage),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = dataState.selectedTab == index
                Surface(
                    onClick = { viewModel.onTabSelected(index) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) Primary else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        val sortedList = dataState.displayList

        if (sortedList.isEmpty() && !dataState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(Primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Cake,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "暂无纪念日",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "记录重要的纪念日",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { navController.navigate(AddAnniversaryRoute()) },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("新建纪念日")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(sortedList, key = { _, it -> it.id }) { index, anniversary ->
                    AnniversaryCard(
                        anniversary = anniversary,
                        onClick = {
                            navController.navigate(AnniversaryDetailRoute(anniversary.id))
                        },
                        modifier = Modifier.staggeredAppear(index = minOf(index, 15))
                    )
                }
                item { Spacer(modifier = Modifier.height(100.dp).navigationBarsPadding()) }
            }
        }
    }
}
