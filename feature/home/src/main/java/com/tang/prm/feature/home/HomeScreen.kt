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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.navigation.NavController
import com.tang.prm.domain.model.HomeOrbitalMode
import com.tang.prm.ui.navigation.SettingsRoute
import com.tang.prm.ui.navigation.ContactDetailRoute
import com.tang.prm.ui.navigation.ContactListRoute
import com.tang.prm.ui.theme.*
import com.tang.prm.domain.util.DateUtils
import com.tang.prm.ui.animation.core.rememberIsResumed

@Composable
fun HomeScreen(
    navController: NavController,
    isTabletLayout: Boolean = false,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // P-6 修复：dateStr 跨日检查用 rememberIsResumed + pausableDelay，App 切后台时暂停轮询，
    // 不再每分钟做无意义的 DateUtils.format 调用
    // N-1 修复：isResumed 以 lambda 传入，确保 pausableDelay 执行期间切后台能立即暂停
    var dateStr by remember { mutableStateOf(DateUtils.formatYearMonthDay(System.currentTimeMillis())) }
    val isResumed by rememberIsResumed()
    LaunchedEffect(Unit) {
        while (true) {
            pausableDelay(60_000L) { isResumed }
            val newDateStr = DateUtils.formatYearMonthDay(System.currentTimeMillis())
            if (newDateStr != dateStr) dateStr = newDateStr
        }
    }

    // C-7 修复：signalStrengths 直接从 uiState.data.stats 派生，channels 与 stats 通过
    // ChannelDef.signalProvider 关联，新增频道只需改 HomeConstants.kt 一处
    // N-5 修复：计算下沉到手机分支内，平板模式不再为 JournalTabletHome 计算无人读取的 map
    val onChannelClick = remember(navController) { { route: Any -> navController.navigate(route) } }
    val onSettingsClick = remember(navController) { { navController.navigate(SettingsRoute) } }
    // 首页力导向图预览：点击联系人节点跳转人物详情
    val onContactClick = remember(navController) { { contactId: Long -> navController.navigate(ContactDetailRoute(contactId)) } }
    // Q-14/P-4 修复：方法引用用 remember 包裹，避免每重组创建新对象引发下游不必要重组
    val onDecorPhotoPathChange = remember(viewModel) { { path: String? -> viewModel.setDecorPhotoPath(path) } }

    // B-7 修复：transientError 触发 Snackbar，显示后立即 consume
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.dialog.transientError) {
        uiState.dialog.transientError?.let {
            snackbarHostState.showSnackbar(
                message = "操作失败，请稍后重试",
                withDismissAction = true
            )
            viewModel.consumeTransientError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // N-7 + U-10 修复：平板模式不显示秒级时间，避免与 JournalDateBanner 日级时间语义冲突
        HomeTopBar(
            showSettings = true,
            showTime = !isTabletLayout,
            isResumedProvider = { isResumed },
            onSettingsClick = onSettingsClick
        )

        when {
            // U-1 修复：加载中显示骨架屏，而非全 0 空数据
            uiState.data.isLoading -> HomeLoadingSkeleton()
            // U-2 修复：错误状态显示重试入口，而非卡死在 loading
            uiState.data.error != null -> HomeErrorState(
                onRetry = { viewModel.retry() }
            )
            // U-3 修复：全新用户（无联系人无事件无纪念日）显示引导卡片，而非全 0 空首页
            uiState.data.stats.contactCount == 0 && uiState.data.stats.eventCount == 0 && uiState.data.stats.anniversaryCount == 0 -> {
                HomeEmptyGuide(onChannelClick = onChannelClick)
            }
            isTabletLayout -> JournalTabletHome(
                uiState = uiState,
                channels = channels,
                onChannelClick = onChannelClick,
                onDecorPhotoPathChange = onDecorPhotoPathChange
            )
            else -> {
                // N-5 修复：signalStrengths 仅在手机分支内计算，平板分支不浪费 CPU 重建 map
                val signalStrengths = remember(uiState.data.stats) {
                    channels.associate { it.route to it.signalProvider(uiState.data.stats) }
                }
                PhoneHomeContent(
                    dateStr = dateStr,
                    uiState = uiState,
                    channels = channels,
                    signalStrengths = signalStrengths,
                    onChannelClick = onChannelClick,
                    onContactClick = onContactClick
                )
            }
        }
    }

    // B-7 修复：Snackbar 宿主
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.fillMaxWidth()
    )
}

// U-1 修复：加载骨架屏——用淡色占位块替代全 0 空数据，避免"加载中显示空首页"的误导
@Composable
private fun HomeLoadingSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 问候语占位
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        // 信号卡占位
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        // 频道卡片占位
        // N-11 修复：占位数与 channels 列表长度一致（9），避免加载完成后从 3 个跳到 9 个的 layout shift
        repeat(channels.size) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}

// U-2 修复：错误状态——显示错误提示与重试按钮，而非卡死在 loading
@Composable
private fun HomeErrorState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "加载失败",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "首页数据暂时无法加载，请检查后重试",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        FilledTonalButton(onClick = onRetry) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("重试")
        }
    }
}

// U-3 修复：全新用户引导卡片——替代"全 0 空首页"，引导用户去添加第一个人/事件
@Composable
private fun HomeEmptyGuide(onChannelClick: (Any) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "开始记录你的第一份记忆",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "添加联系人、事件或纪念日，让这里亮起信号",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        FilledTonalButton(onClick = { onChannelClick(ContactListRoute) }) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("添加联系人")
        }
    }
}

@Composable
private fun HomeTopBar(
    showSettings: Boolean = true,
    // N-7 + U-10 修复：平板模式传 false 不显示秒级时间，交给 JournalDateBanner 统一呈现日级时间
    showTime: Boolean = true,
    // N-4 修复：isResumed 通过 lambda 传入，让 pausableDelay 每秒读取最新 State.value
    isResumedProvider: () -> Boolean = { true },
    onSettingsClick: () -> Unit
) {
    // 秒级时钟状态下沉到 TopBar 内部，避免每秒触发整个 HomeScreen 重组
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    // N-4 修复：用 pausableDelay 替代裸 delay(1000)，App 切后台时暂停秒级 tick，
    // 与 HomeScreen 的 dateStr 60s 轮询和 HomeSignalCard 打字机保持一致暂停语义
    LaunchedEffect(Unit) {
        while (true) {
            pausableDelay(1000L, isResumedProvider)
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
                        .background(SignalGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "SYS://结绳",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (showTime) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        timeStr,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
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
    onChannelClick: (Any) -> Unit,
    onContactClick: (Long) -> Unit
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
                stats = uiState.data.stats
            )
        }

        item(key = "orbital_calendar", contentType = "calendar") {
            when (uiState.data.homeOrbitalMode) {
                HomeOrbitalMode.ORBITAL -> OrbitalCalendar(
                    anniversaries = uiState.data.upcomingAnniversaries,
                    events = uiState.data.recentEvents,
                    // N-3 修复：dateStr 作为 todayDateKey，跨日时 OrbitalCalendar 内部 todayCal 失效重建
                    todayDateKey = dateStr
                )
                HomeOrbitalMode.FORCE_GRAPH -> HomeForceGraphPreview(
                    onContactClick = onContactClick
                )
            }
        }

        item(key = "channel_grid", contentType = "grid") {
            // Q-1 修复：函数从 ChannelGrid 重命名为 ChannelList，反映其单列纵向列表实现
            ChannelList(
                channels = channels,
                signalStrengths = signalStrengths,
                onChannelClick = onChannelClick
            )
        }
    }
}
