@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.tang.prm.ui.navigation.SettingsRoute
import com.tang.prm.ui.navigation.GiftsRoute
import com.tang.prm.ui.navigation.ContactListRoute
import com.tang.prm.ui.navigation.PhotoAlbumRoute
import com.tang.prm.ui.navigation.FootprintsRoute
import com.tang.prm.ui.navigation.ThoughtsRoute
import com.tang.prm.ui.navigation.FavoritesRoute
import com.tang.prm.ui.navigation.DivinationRoute
import com.tang.prm.ui.navigation.SubscriptionsRoute
import com.tang.prm.ui.navigation.RecipesRoute
import com.tang.prm.ui.theme.*
import com.tang.prm.domain.util.DateUtils

private val TerminalGreen = SignalGreen

@Stable
internal data class ChannelDef(
    val name: String,
    val color: Color,
    val route: Any,
    val desc: String,
    val icon: ImageVector? = null,
    val textIcon: String? = null,
)

internal val channels = listOf(
    ChannelDef("礼物", SignalCoral, GiftsRoute, "收送记录与心愿单", Icons.Default.CardGiftcard),
    ChannelDef("圈子", SignalPurple, ContactListRoute, "社交分组与关系管理", Icons.Default.Hub),
    ChannelDef("相册", SignalSky, PhotoAlbumRoute.default(), "共享回忆与时光轴", Icons.Default.Image),
    ChannelDef("足迹", SignalGreen, FootprintsRoute, "共同地点与旅行轨迹", Icons.Default.Map),
    ChannelDef("想法", SignalAmber, ThoughtsRoute, "灵感笔记与待办事项", Icons.Default.Lightbulb),
    ChannelDef("收藏", SignalGold, FavoritesRoute, "珍藏回忆与重要内容", Icons.Default.Star),
    ChannelDef("占卜", SignalElectric, DivinationRoute, "梅花易数 · 六爻纳甲", textIcon = "☯"),
    ChannelDef("订阅", SignalSky, SubscriptionsRoute, "会员订阅与到期提醒", Icons.Default.CreditCard),
    ChannelDef("菜谱", SignalElectric, RecipesRoute, "一起做过的菜与味道", Icons.Default.Restaurant),
)

@Composable
fun HomeScreen(
    navController: NavController,
    isTabletLayout: Boolean = false,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // dateStr 仅在跨日时才变化：每分钟检查一次，仅当格式化日期不同时更新 state，避免无谓重组
    var dateStr by remember { mutableStateOf(DateUtils.formatYearMonthDay(System.currentTimeMillis())) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60_000L)
            val newDateStr = DateUtils.formatYearMonthDay(System.currentTimeMillis())
            if (newDateStr != dateStr) dateStr = newDateStr
        }
    }

    val signalStrengths = remember(uiState) {
        mapOf(
            GiftsRoute to uiState.giftCount,
            ContactListRoute to uiState.circleCount,
            PhotoAlbumRoute.default() to uiState.photoCount,
            FootprintsRoute to uiState.footprintCount,
            ThoughtsRoute to uiState.thoughtCount,
            FavoritesRoute to uiState.favoriteCount,
            DivinationRoute to 0,
            SubscriptionsRoute to uiState.subscriptionCount,
            RecipesRoute to uiState.recipeCount
        )
    }
    val onChannelClick = remember(navController) { { route: Any -> navController.navigate(route) } }
    val onSettingsClick = remember(navController) { { navController.navigate(SettingsRoute) } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (isTabletLayout) {
            HomeTopBar(
                showSettings = false,
                onSettingsClick = onSettingsClick
            )

            JournalTabletHome(
                uiState = uiState,
                channels = channels,
                signalStrengths = signalStrengths,
                onChannelClick = onChannelClick,
                onDecorPhotoPathChange = viewModel::setDecorPhotoPath
            )
        } else {
            HomeTopBar(
                showSettings = true,
                onSettingsClick = onSettingsClick
            )
            PhoneHomeContent(
                dateStr = dateStr,
                uiState = uiState,
                channels = channels,
                signalStrengths = signalStrengths,
                onChannelClick = onChannelClick
            )
        }
    }
}

@Composable
private fun HomeTopBar(
    showSettings: Boolean = true,
    onSettingsClick: () -> Unit
) {
    // 秒级时钟状态下沉到 TopBar 内部，避免每秒触发整个 HomeScreen 重组
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }
    val timeStr = DateUtils.formatTimeWithSeconds(currentTime)

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(TerminalGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "SYS://结绳",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    timeStr,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        actions = {
            if (showSettings) {
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "设置",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
private fun PhoneHomeContent(
    dateStr: String,
    uiState: HomeUiState,
    channels: List<ChannelDef>,
    signalStrengths: Map<Any, Int>,
    onChannelClick: (Any) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item(key = "signal_card", contentType = "signal") {
            IncomingSignalCard(
                dateStr = dateStr,
                contactCount = uiState.contactCount,
                eventCount = uiState.eventCount,
                giftCount = uiState.giftCount,
                anniversaryCount = uiState.anniversaryCount,
                conversationCount = uiState.conversationCount
            )
        }

        item(key = "orbital_calendar", contentType = "calendar") {
            OrbitalCalendar(
                anniversaries = uiState.upcomingAnniversaries,
                events = uiState.recentEvents
            )
        }

        item(key = "channel_grid", contentType = "grid") {
            ChannelGrid(
                channels = channels,
                signalStrengths = signalStrengths,
                onChannelClick = onChannelClick
            )
        }
    }
}
