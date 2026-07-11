@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.people.contacts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.IntimacyTier
import com.tang.prm.ui.theme.*
import com.tang.prm.ui.components.TabletSearchBar
import com.tang.prm.ui.animation.primitives.staggeredAppear

/**
 * 平板人物列表主入口 — 全宽画廊式列表。
 *
 * 点击卡片导航到 ContactDetailTabletScreen（方案A 经典画廊精装）。
 *
 * 设计语言：
 * - Material 3 冷白基底（surface=#FFFFFF, surfaceVariant=#F1F5F9）
 * - Primary 蓝 (#2196F3) 作为唯一点缀色（搜索框/添加按钮）
 * - 12dp 圆角卡片 + 1dp outline 边框 + 2dp 阴影
 * - IntimacyTier 系统配色（UR金/SSR红/SR紫/R蓝/N灰）
 * - 按亲密度分组，组内 4 列网格，内容居中限制最大宽度
 */
@Composable
fun ContactsTabletScreen(
    onAddContact: () -> Unit,
    onContactClick: (Long) -> Unit,
    viewModel: ContactsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // ── 全宽联系人列表 ──
        ContactsListPane(
            contacts = uiState.data.contacts,
            relationships = uiState.data.relationships,
            searchQuery = searchState.query,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onAddContact = onAddContact,
            onContactClick = { contact -> onContactClick(contact.id) },
            modifier = Modifier.fillMaxSize()
        )
    }
}

// ════════════════════════════════════════════════════════════════
// 列表栏
// ════════════════════════════════════════════════════════════════

@Composable
private fun ContactsListPane(
    contacts: List<Contact>,
    relationships: List<CustomType>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onAddContact: () -> Unit,
    onContactClick: (Contact) -> Unit,
    modifier: Modifier = Modifier
) {
    val groupedContacts = remember(contacts) {
        // 亲密度从高到低：FAMILY → CLOSE → FRIEND → ACQUAINTANCE → NEW
        IntimacyTier.entries.reversed()
            .map { tier -> tier to contacts.filter { IntimacyTier.of(it.intimacyScore) == tier } }
            .filter { it.second.isNotEmpty() }
    }

    Column(modifier = modifier.statusBarsPadding()) {
        // 顶部栏：大标题 + 副标题 + 搜索 + 添加
        ContactsTabletTopBar(
            contacts = contacts,
            relationships = relationships,
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            onAddContact = onAddContact
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            thickness = 1.dp
        )

        if (contacts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.PersonSearch,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "未找到联系人",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "尝试调整搜索条件或添加新联系人",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            // 列表内容居中并限制最大宽度
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyColumn(
                    modifier = Modifier.widthIn(max = 1400.dp).fillMaxSize(),
                    contentPadding = PaddingValues(start = 48.dp, end = 48.dp, top = 12.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    groupedContacts.forEach { (tier, tierContacts) ->
                        item(key = "tier_${tier.name}") {
                            TierSection(
                                tier = tier,
                                contacts = tierContacts,
                                onContactClick = onContactClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactsTabletTopBar(
    contacts: List<Contact>,
    relationships: List<CustomType>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onAddContact: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 左侧：大标题 + 副标题
        Column {
            Text(
                text = "人物",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "共 ${contacts.size} 位 · ${relationships.size} 种关系",
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
                placeholder = "搜索姓名、电话...",
                modifier = Modifier.width(360.dp)
            )

            // 添加按钮：40dp + 12dp 圆角 + Primary 背景
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
                    .clickable(onClick = onAddContact),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "新建人物",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
// Tier 分组 + 卡片网格
// ════════════════════════════════════════════════════════════════

@Composable
private fun TierSection(
    tier: IntimacyTier,
    contacts: List<Contact>,
    onContactClick: (Contact) -> Unit
) {
    val tierColor = LocalIntimacyColors.current.forTier(tier)
    val appearKey = remember { Any() }

    Column {
        // 分组标题：圆点 + 名称 + 人数徽章 + 分隔线
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(tierColor)
            )
            Text(
                text = tier.label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            // 人数徽章：surfaceVariant + 圆角10dp
            Text(
                text = "${contacts.size}人",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            )
            // 分隔线：填充剩余空间
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            )
            // 稀有度标签
            Text(
                text = tier.cardRarity,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = tierColor,
                letterSpacing = 1.sp
            )
        }

        // 卡片网格：6 列，LazyVerticalGrid 保留 lazy 渲染优势
        val rowCount = (contacts.size + 5) / 6  // 向上取整
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = (rowCount * 172).dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            userScrollEnabled = false
        ) {
            itemsIndexed(contacts, key = { _, it -> it.id }) { index, contact ->
                ContactTabletCard(
                    contact = contact,
                    tierColor = tierColor,
                    tier = tier,
                    onClick = { onContactClick(contact) },
                    modifier = Modifier.staggeredAppear(
                        index = minOf(index, 15),
                        triggerKey = appearKey
                    )
                )
            }
        }
    }
}

@Composable
private fun ContactTabletCard(
    contact: Contact,
    tierColor: Color,
    tier: IntimacyTier,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        shadowElevation = 1.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            // 头像区：4:3 横向矩形（不再占满整宽正方形）+ 6dp 圆角 + tier 色渐变背景
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.3f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                tierColor.copy(alpha = 0.85f),
                                tierColor.copy(alpha = 0.55f)
                            )
                        )
                    )
            ) {
                if (contact.avatar != null) {
                    AsyncImage(
                        model = contact.avatar,
                        contentDescription = contact.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = contact.name.firstOrNull()?.toString() ?: "?",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White.copy(alpha = 0.95f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // 姓名 + tier 徽章
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = contact.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = tier.cardRarity,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 0.3.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(tierColor)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }

            // 关系/MBTI 副标题
            val relText = buildList {
                contact.relationship?.takeIf { it.isNotBlank() }?.let { add(it) }
                contact.mbti?.takeIf { it.isNotBlank() }?.let { add(it) }
            }.joinToString(" · ")
            Text(
                text = relText.ifEmpty { "—" },
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )

            // 亲密度星级 + tier 标签 + 分数
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(5) { i ->
                    val filled = i < tier.stars
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(if (filled) tierColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    )
                }
                Text(
                    text = tier.label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = tierColor
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${contact.intimacyScore}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = tierColor
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
// 工具函数
// ════════════════════════════════════════════════════════════════
