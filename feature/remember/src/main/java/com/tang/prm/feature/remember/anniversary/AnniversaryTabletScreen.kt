@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.remember.anniversary

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.tang.prm.domain.model.Anniversary
import com.tang.prm.domain.model.AnniversaryType
import com.tang.prm.domain.util.DateCalcUtils
import com.tang.prm.ui.animation.primitives.staggeredAppear
import com.tang.prm.ui.components.TabletSearchBar
import com.tang.prm.ui.navigation.AddAnniversaryRoute
import com.tang.prm.ui.theme.SignalSky
import com.tang.prm.ui.theme.getAnniversaryIcon
import java.time.Instant
import java.time.ZoneId

// ═══════════════════════════════════════════════════════════════
// 平板纪念主界面 — 方案1 时光长廊 Time Gallery
// ═══════════════════════════════════════════════════════════════
// 设计语言：
// - Material 3 冷白基底（与人物界面一致）
// - AnniversaryType 三色：BIRTHDAY 橙 / ANNIVERSARY 粉 / HOLIDAY 蓝
// - 三大区块：① Hero 区（即将到来大卡片+统计带） ② 横向全年时间轴 ③ 按类型3列分组
// ═══════════════════════════════════════════════════════════════

@Composable
fun AnniversaryTabletScreen(
    navController: NavController,
    viewModel: AnniversariesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val dataState = uiState.data
    // 每次进入界面生成新的触发键，让 staggeredAppear 重新播放
    val appearKey = remember { Any() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // ── 顶部栏（与人物界面一致） ──
            item {
                GalleryTopBar(
                    totalCount = dataState.allAnniversaries.size,
                    upcomingCount = dataState.upcomingAnniversaries.size,
                    pastCount = dataState.pastAnniversaries.size,
                    searchQuery = searchState.query,
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    onAddClick = { navController.navigate(AddAnniversaryRoute()) },
                    modifier = Modifier.staggeredAppear(index = 0, triggerKey = appearKey)
                )
            }

            // ── Hero 区 ──
            item {
                GalleryHero(
                    upcoming = dataState.upcomingAnniversaries,
                    modifier = Modifier.staggeredAppear(index = 1, triggerKey = appearKey)
                )
            }

            // ── 全年时间轴 ──
            item {
                YearTimelineSection(
                    anniversaries = dataState.allAnniversaries,
                    modifier = Modifier.staggeredAppear(index = 2, triggerKey = appearKey)
                )
            }

            // ── 按类型分组 ──
            item {
                ByTypeSection(
                    anniversaries = dataState.allAnniversaries,
                    modifier = Modifier.staggeredAppear(index = 3, triggerKey = appearKey)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 顶部栏 — 与人物界面一致的简洁样式
// ═══════════════════════════════════════════════════════════════

@Composable
private fun GalleryTopBar(
    totalCount: Int,
    upcomingCount: Int,
    pastCount: Int,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 48.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 左侧：大标题 + 副标题
        Column {
            Text(
                text = "纪念",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "共 $totalCount 个 · $upcomingCount 个即将到来 · $pastCount 个已过",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // 右侧：搜索框 + 添加按钮
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TabletSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                placeholder = "搜索人物、纪念日...",
                modifier = Modifier.width(360.dp)
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .shadow(
                        elevation = 3.dp,
                        shape = RoundedCornerShape(12.dp),
                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onAddClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "新建纪念",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Hero 区 — 即将到来大卡片 + 横向统计带
// ═══════════════════════════════════════════════════════════════

@Composable
private fun GalleryHero(
    upcoming: List<Anniversary>,
    modifier: Modifier = Modifier
) {
    val next = upcoming.firstOrNull()
    val typeColor = next?.let { getTypeColor(it.type) } ?: SignalSky

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── 即将到来大卡片 ──
        next?.let { anniversary ->
            UpcomingHeroCard(
                anniversary = anniversary,
                typeColor = typeColor
            )
        } ?: EmptyHeroCard()
    }
}

@Composable
private fun UpcomingHeroCard(
    anniversary: Anniversary,
    typeColor: Color
) {
    val daysInfo = remember(anniversary.id, anniversary.date) {
        DateCalcUtils.calculateDaysInfo(anniversary.date)
    }
    val date = remember(anniversary.date) {
        Instant.ofEpochMilli(anniversary.date).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, typeColor.copy(alpha = 0.2f)),
        shadowElevation = 4.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .padding(32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // ── 左：即将到来详情 ──
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 类型徽章
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = typeColor.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                getAnniversaryIcon(anniversary.icon),
                                contentDescription = null,
                                tint = typeColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = anniversary.type.displayName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = typeColor
                            )
                        }
                    }
                    Text(
                        text = "即将到来 · NEXT",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 名称
                Text(
                    text = anniversary.name,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // 联系人 + 日期信息
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    anniversary.contactName?.let { name ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = name,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.outline)
                        )
                    }
                    Text(
                        text = buildString {
                            append("${date.monthValue}月${date.dayOfMonth}日")
                            if (anniversary.isRepeat) append(" · 每年")
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── 中：分隔线 ──
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(180.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            )

            // ── 右：大倒数 ──
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.width(260.dp)
            ) {
                Text(
                    text = "COUNTDOWN",
                    fontSize = 11.sp,
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (daysInfo.daysUntil == 0) "0" else "${daysInfo.daysUntil}",
                        fontSize = 120.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Serif,
                        color = typeColor,
                        lineHeight = 100.sp
                    )
                    Text(
                        text = "天",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                Text(
                    text = if (daysInfo.daysUntil == 0) "就是今天" else "后到来",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyHeroCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Cake,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "暂无即将到来的纪念",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "点击右上角新建一个纪念日",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 全年时间轴 — 横向12月长廊
// ═══════════════════════════════════════════════════════════════

@Composable
private fun YearTimelineSection(
    anniversaries: List<Anniversary>,
    modifier: Modifier = Modifier
) {
    val byMonth = remember(anniversaries) {
        (1..12).associateWith { month ->
            anniversaries.filter { a ->
                Instant.ofEpochMilli(a.date).atZone(ZoneId.systemDefault()).toLocalDate().monthValue == month
            }.sortedBy { a ->
                Instant.ofEpochMilli(a.date).atZone(ZoneId.systemDefault()).toLocalDate().dayOfMonth
            }
        }
    }
    val currentMonth = remember {
        java.time.LocalDate.now().monthValue
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp)
            .padding(bottom = 32.dp)
    ) {
        // 章节标题
        SectionHead(
            title = "全年时间轴",
            enTitle = "YEAR TIMELINE",
            count = "12 个月"
        )
        Spacer(Modifier.height(20.dp))

        // 横向卷轴 — 12 月柱状卡片
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            items((1..12).toList()) { month ->
                TimelineMonthColumn(
                    month = month,
                    anniversaries = byMonth[month].orEmpty(),
                    isCurrent = month == currentMonth,
                    modifier = Modifier.width(180.dp)
                )
            }
        }
    }
}

@Composable
private fun TimelineMonthColumn(
    month: Int,
    anniversaries: List<Anniversary>,
    isCurrent: Boolean,
    modifier: Modifier = Modifier
) {
    val monthName = remember(month) {
        java.time.Month.of(month).getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH).uppercase()
    }
    val hasEvents = anniversaries.isNotEmpty()
    val accentColor = anniversaries.firstOrNull()?.let { getTypeColor(it.type) }
        ?: MaterialTheme.colorScheme.outline

    Surface(
        modifier = modifier.height(220.dp),
        shape = RoundedCornerShape(14.dp),
        color = if (isCurrent) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isCurrent) 2.dp else 1.dp,
            color = if (isCurrent) MaterialTheme.colorScheme.primary else if (hasEvents) accentColor.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // 月份头
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = monthName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Serif,
                        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${month}月",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = if (hasEvents) accentColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "${anniversaries.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasEvents) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // 月份连接线
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(if (hasEvents) accentColor.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            )

            Spacer(Modifier.height(10.dp))

            // 事件列表 / 空状态 — 固定高度区域，居中显示
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.TopStart
            ) {
                if (anniversaries.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                            )
                            Text(
                                text = "无纪念",
                                fontSize = 11.sp,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        anniversaries.take(3).forEach { anniversary ->
                            TimelineEventChip(
                                anniversary = anniversary
                            )
                        }
                        if (anniversaries.size > 3) {
                            Text(
                                text = "+ ${anniversaries.size - 3} 个",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineEventChip(
    anniversary: Anniversary
) {
    val date = remember(anniversary.date) {
        Instant.ofEpochMilli(anniversary.date).atZone(ZoneId.systemDefault()).toLocalDate()
    }
    val typeColor = getTypeColor(anniversary.type)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 色条
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(typeColor)
        )
        // 日期
        Text(
            text = "${date.dayOfMonth}日",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = typeColor
        )
        // 类型 + 联系人（单行）
        Text(
            text = buildString {
                append(anniversary.type.displayName)
                anniversary.contactName?.let { if (it.isNotBlank()) append(" · $it") }
            },
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// 按类型分组 — 3 列（生日 / 纪念日 / 节日）
// ═══════════════════════════════════════════════════════════════

@Composable
private fun ByTypeSection(
    anniversaries: List<Anniversary>,
    modifier: Modifier = Modifier
) {
    val grouped = remember(anniversaries) {
        AnniversaryType.entries.associateWith { type ->
            anniversaries.filter { it.type == type }.sortedBy { a ->
                val d = Instant.ofEpochMilli(a.date).atZone(ZoneId.systemDefault()).toLocalDate()
                d.monthValue * 100 + d.dayOfMonth
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp)
            .padding(bottom = 80.dp)
    ) {
        SectionHead(
            title = "按类型分组",
            enTitle = "BY TYPE",
            count = "${anniversaries.size} 个"
        )
        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnniversaryType.entries.forEach { type ->
                TypeColumn(
                    type = type,
                    anniversaries = grouped[type].orEmpty(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TypeColumn(
    type: AnniversaryType,
    anniversaries: List<Anniversary>,
    modifier: Modifier = Modifier
) {
    val typeColor = getTypeColor(type)
    val typeIcon = when (type) {
        AnniversaryType.BIRTHDAY -> Icons.Default.Cake
        AnniversaryType.ANNIVERSARY -> Icons.Default.Favorite
        AnniversaryType.HOLIDAY -> Icons.Default.Celebration
    }

    Column(modifier = modifier) {
        // 列头
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(typeColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    typeIcon,
                    contentDescription = null,
                    tint = typeColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = type.displayName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.weight(1f))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "${anniversaries.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(typeColor.copy(alpha = 0.6f))
        )
        Spacer(Modifier.height(12.dp))

        if (anniversaries.isEmpty()) {
            Text(
                text = "暂无${type.displayName}",
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                anniversaries.forEach { anniversary ->
                    TypeCard(
                        anniversary = anniversary,
                        typeColor = typeColor
                    )
                }
            }
        }
    }
}

@Composable
private fun TypeCard(
    anniversary: Anniversary,
    typeColor: Color
) {
    val daysInfo = remember(anniversary.id, anniversary.date) {
        DateCalcUtils.calculateDaysInfo(anniversary.date)
    }
    val date = remember(anniversary.date) {
        Instant.ofEpochMilli(anniversary.date).atZone(ZoneId.systemDefault()).toLocalDate()
    }
    val isSoon = daysInfo.daysUntil in 0..30

    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 日期块（中文）
            Column(
                modifier = Modifier.width(52.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${date.dayOfMonth}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    color = typeColor,
                    lineHeight = 24.sp
                )
                Text(
                    text = "${date.monthValue}月",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // 信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = anniversary.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    anniversary.contactName?.let { name ->
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = name,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Box(
                            modifier = Modifier
                                .size(2.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.outline)
                        )
                    }
                    Text(
                        text = buildString {
                            if (anniversary.isRepeat) {
                                append("每年")
                            } else {
                                append("单次")
                            }
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 倒数徽章
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isSoon) typeColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = if (daysInfo.daysUntil == 0) "今天" else "${daysInfo.daysUntil}天",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = if (isSoon) typeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 通用组件
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SectionHead(title: String, enTitle: String, count: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = enTitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Box(
            modifier = Modifier
                .weight(2f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        )
        Text(
            text = count,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// 工具函数 — getTypeColor 已在 AnniversariesComponents.kt 中定义为 internal
// ═══════════════════════════════════════════════════════════════
