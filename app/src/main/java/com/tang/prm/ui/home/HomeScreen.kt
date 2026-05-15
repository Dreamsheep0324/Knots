@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.navigation.NavController
import com.tang.prm.ui.components.AppCard
import com.tang.prm.ui.navigation.Screen
import com.tang.prm.ui.theme.*
import com.tang.prm.util.DateUtils
import com.tang.prm.ui.animation.core.rememberIsResumed
import com.tang.prm.ui.animation.core.AnimationTokens

private val TerminalGreen = SignalGreen

internal data class ChannelDef(
    val name: String,
    val color: Color,
    val route: String,
    val icon: ImageVector,
    val desc: String,
)

internal val channels = listOf(
    ChannelDef("礼物", SignalCoral, "gifts", Icons.Default.CardGiftcard, "收送记录与心愿单"),
    ChannelDef("圈子", SignalPurple, "contact_list", Icons.Default.Hub, "社交分组与关系管理"),
    ChannelDef("相册", SignalSky, "photo_album", Icons.Default.Image, "共享回忆与时光轴"),
    ChannelDef("足迹", SignalGreen, "footprints", Icons.Default.Map, "共同地点与旅行轨迹"),
    ChannelDef("想法", SignalAmber, "thoughts", Icons.Default.Lightbulb, "灵感笔记与待办事项"),
    ChannelDef("收藏", SignalGold, "favorites", Icons.Default.Star, "珍藏回忆与重要内容"),
)

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    val isResumed by rememberIsResumed()

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            if (isResumed) {
                currentTime = System.currentTimeMillis()
            }
        }
    }

    val timeStr = DateUtils.formatTimeWithSeconds(currentTime)
    val dateStr = DateUtils.formatYearMonthDay(currentTime)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
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
                IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "设置",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                IncomingSignalCard(
                    dateStr = dateStr,
                    contactCount = uiState.contactCount,
                    eventCount = uiState.eventCount,
                    giftCount = uiState.giftCount,
                    anniversaryCount = uiState.anniversaryCount,
                    conversationCount = uiState.conversationCount
                )
            }

            item {
                OrbitalCalendar(
                    anniversaries = uiState.allAnniversaries,
                    events = uiState.allEvents
                )
            }

            item {
                ChannelGrid(
                    channels = channels,
                    signalStrengths = mapOf(
                        "gifts" to uiState.giftCount,
                        "contact_list" to uiState.circleCount,
                        "photo_album" to uiState.photoCount,
                        "footprints" to uiState.footprintCount,
                        "thoughts" to uiState.thoughtCount,
                        "favorites" to uiState.favoriteCount
                    ),
                    onChannelClick = { route ->
                        when (route) {
                            "gifts" -> navController.navigate(Screen.Gifts.route)
                            "contact_list" -> navController.navigate(Screen.ContactList.route)
                            "photo_album" -> navController.navigate(Screen.PhotoAlbum.route)
                            "footprints" -> navController.navigate(Screen.Footprints.route)
                            "thoughts" -> navController.navigate(Screen.Thoughts.route)
                            "favorites" -> navController.navigate(Screen.Favorites.route)
                        }
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
