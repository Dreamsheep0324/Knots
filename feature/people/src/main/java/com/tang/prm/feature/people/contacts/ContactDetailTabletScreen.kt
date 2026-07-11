@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.people.contacts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.IntimacyTier
import com.tang.prm.domain.util.DateUtils
import com.tang.prm.ui.components.DeleteConfirmDialog
import com.tang.prm.ui.navigation.EditContactRoute
import com.tang.prm.ui.navigation.EventDetailRoute
import com.tang.prm.ui.navigation.AnniversaryDetailRoute
import com.tang.prm.ui.navigation.GiftDetailRoute
import com.tang.prm.ui.navigation.ChatDetailRoute
import com.tang.prm.ui.theme.*
import com.tang.prm.ui.theme.toComposeColor

// ═══════════════════════════════════════════════════════════════
// 平板人物详情 — 方案A 经典画廊精装
// ═══════════════════════════════════════════════════════════════

@Composable
fun ContactDetailTabletScreen(
    contactId: Long,
    navController: NavController,
    viewModel: ContactDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val contact = uiState.data.contact
    var detailThoughtId by remember { mutableStateOf<Long?>(null) }
    val detailThought = detailThoughtId?.let { id -> uiState.data.thoughts.find { it.id == id } }

    LaunchedEffect(contactId) {
        viewModel.setContactId(contactId)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ContactDetailEvent.ContactDeleted -> navController.popBackStack()
            }
        }
    }

    if (uiState.dialog.showDeleteDialog) {
        DeleteConfirmDialog(
            title = "删除人物",
            message = "确定要删除 \"${contact?.name ?: ""}\" 吗？此操作不可撤销。",
            onConfirm = { viewModel.deleteContact() },
            onDismiss = { viewModel.hideDeleteDialog() }
        )
    }

    detailThought?.let { thought ->
        ContactThoughtDetailDialog(
            thought = thought,
            contactName = contact?.name,
            contactAvatar = contact?.avatar,
            isFavorite = thought.id in uiState.data.favoriteIds,
            onDismiss = { detailThoughtId = null },
            onToggleFavorite = { viewModel.toggleFavorite(thought.id, thought.content) },
            onToggleTodo = { viewModel.toggleTodoDone(thought) },
            onDelete = { viewModel.deleteThought(thought.id); detailThoughtId = null }
        )
    }

    if (contact == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.data.isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 3.dp)
            }
        }
        return
    }

    val tier = IntimacyTier.of(contact.intimacyScore)
    val tierColor = Color(tier.colorValue)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .systemBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // ── 顶部栏 ──
            item {
                GalleryTopBar(
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(EditContactRoute(contactId)) },
                    onDelete = { viewModel.showDeleteDialog() }
                )
            }

            // ── Hero 区 ──
            item {
                GalleryHero(
                    contact = contact,
                    tier = tier,
                    tierColor = tierColor,
                    relationshipTypes = uiState.data.relationshipTypes
                )
            }

            // ── 四栏内容区 ──
            item {
                GalleryBody(
                    contact = contact,
                    tierColor = tierColor,
                    events = uiState.data.events,
                    conversations = uiState.data.conversations,
                    anniversaries = uiState.data.anniversaries,
                    gifts = uiState.data.gifts,
                    thoughts = uiState.data.thoughts,
                    eventTypes = uiState.data.eventTypes,
                    onEventClick = { id -> navController.navigate(EventDetailRoute(id)) },
                    onAnniversaryClick = { id -> navController.navigate(AnniversaryDetailRoute(id)) },
                    onGiftClick = { id -> navController.navigate(GiftDetailRoute(id)) },
                    onThoughtClick = { id -> detailThoughtId = id },
                    onConversationClick = { id -> navController.navigate(ChatDetailRoute(id)) }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 顶部栏
// ═══════════════════════════════════════════════════════════════

@Composable
private fun GalleryTopBar(
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 56.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = "人物档案",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    .clickable(onClick = onEdit),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "编辑",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, SignalCoral.copy(alpha = 0.3f), CircleShape)
                    .clickable(onClick = onDelete),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = SignalCoral,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Hero 区 — 双层画框 + 衬线大姓名 + 亲密度角标
// ═══════════════════════════════════════════════════════════════

@Composable
private fun GalleryHero(
    contact: Contact,
    tier: IntimacyTier,
    tierColor: Color,
    relationshipTypes: List<CustomType>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 56.dp)
            .padding(bottom = 32.dp)
    ) {
        // 暖色光晕背景
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .clip(RoundedCornerShape(0.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            tierColor.copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        radius = 1000f
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .padding(top = 40.dp),
            horizontalArrangement = Arrangement.spacedBy(40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── 画框人像 ──
            GalleryPortraitFrame(contact = contact, tierColor = tierColor)

            // ── 中间信息 ──
            GalleryHeroInfo(
                contact = contact,
                tier = tier,
                tierColor = tierColor,
                relationshipTypes = relationshipTypes,
                modifier = Modifier.weight(1f)
            )

            // ── 亲密度角标卡 ──
            GalleryIntimacyCard(score = contact.intimacyScore, tier = tier, tierColor = tierColor)
        }
    }
}

@Composable
private fun GalleryPortraitFrame(contact: Contact, tierColor: Color) {
    Box(
        modifier = Modifier
            .size(width = 260.dp, height = 320.dp)
    ) {
        // 外层细线画框（双层）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
        )

        // 主画框
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(6.dp))
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(6.dp),
                    ambientColor = tierColor.copy(alpha = 0.25f),
                    spotColor = tierColor.copy(alpha = 0.25f)
                )
        ) {
            // 渐变背景作为兜底
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(tierColor, tierColor.copy(alpha = 0.7f))
                        )
                    )
            )

            if (contact.avatar != null) {
                AsyncImage(
                    model = contact.avatar,
                    contentDescription = contact.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // 无头像：衬线大首字母
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contact.name.firstOrNull()?.toString() ?: "?",
                        fontSize = 200.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White.copy(alpha = 0.3f),
                        fontFamily = FontFamily.Serif
                    )
                }
            }
        }
    }
}

@Composable
private fun GalleryHeroInfo(
    contact: Contact,
    tier: IntimacyTier,
    tierColor: Color,
    relationshipTypes: List<CustomType>,
    modifier: Modifier = Modifier
) {
    val relationshipLabel = relationshipTypes.find { it.name == contact.relationship }?.name
        ?: contact.relationship
        ?: "未分类"

    // 认识时长：精确到年+天
    val knownDuration = contact.knowingDate?.let {
        val diffMillis = System.currentTimeMillis() - it
        if (diffMillis > 0) {
            val totalDays = (diffMillis / (24L * 60 * 60 * 1000)).toInt()
            val years = totalDays / 365
            val days = totalDays % 365
            if (years > 0) "${years}年${days}天" else "${days}天"
        } else null
    }
    val age = contact.birthday?.let {
        val years = ((System.currentTimeMillis() - it) / (365L * 24 * 60 * 60 * 1000)).toInt()
        if (years in 0..150) years else null
    }
    val genderLabel = when (contact.gender) {
        com.tang.prm.domain.model.Gender.MALE -> "男"
        com.tang.prm.domain.model.Gender.FEMALE -> "女"
        else -> null
    }

    Column(modifier = modifier) {
        // 分类标签
        Text(
            text = "— ${relationshipLabel.uppercase()} · ${tier.label} —",
            fontSize = 13.sp,
            color = tierColor,
            letterSpacing = 4.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 衬线大姓名
        Text(
            text = contact.name,
            fontSize = 64.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = (-1.5).sp,
            lineHeight = 66.sp,
            fontFamily = FontFamily.Serif,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        // 昵称（如有）
        contact.nickname?.takeIf { it.isNotBlank() }?.let { nick ->
            Text(
                text = "“$nick”",
                fontSize = 16.sp,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Serif,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // 引文（用 notes 作为引文，没有则用默认）
        Text(
            text = contact.notes?.takeIf { it.isNotBlank() }
                ?: "认识一个人，是认识一整个世界。",
            fontSize = 17.sp,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Serif,
            lineHeight = 26.sp,
            modifier = Modifier.padding(bottom = 24.dp, end = 40.dp)
        )

        // 上分隔线
        Box(
            modifier = Modifier
                .padding(top = 16.dp, bottom = 18.dp)
                .width(60.dp)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline)
        )

        // 元数据行：动态展示真实存在的字段
        Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            GalleryHeroMetaItem(label = "RELATION", value = relationshipLabel)
            genderLabel?.let { GalleryHeroMetaItem(label = "GENDER", value = it) }
            age?.let { GalleryHeroMetaItem(label = "AGE", value = "$it 岁") }
            contact.mbti?.takeIf { it.isNotBlank() }?.let {
                GalleryHeroMetaItem(label = "MBTI", value = it)
            }
            contact.education?.takeIf { it.isNotBlank() }?.let {
                GalleryHeroMetaItem(label = "EDU", value = it)
            }
            knownDuration?.let {
                GalleryHeroMetaItem(label = "KNOWN", value = it)
            }
        }
    }
}

@Composable
private fun GalleryHeroMetaItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun GalleryIntimacyCard(
    score: Int,
    tier: IntimacyTier,
    tierColor: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 3.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "$score",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = tierColor,
                fontFamily = FontFamily.Serif,
                lineHeight = 42.sp
            )
            Text(
                text = "INTIMACY",
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Medium
            )
            // 5颗星
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(5) { i ->
                    val filled = i < tier.stars
                    Text(
                        text = "★",
                        fontSize = 13.sp,
                        color = if (filled) SignalGold else MaterialTheme.colorScheme.outline
                    )
                }
            }
            // 分隔线
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline)
            )
            Text(
                text = "${tier.label} ${tier.cardRarity}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 三栏内容区 — 档案 / 时光 / 心绪
// ═══════════════════════════════════════════════════════════════

@Composable
private fun GalleryBody(
    contact: Contact,
    tierColor: Color,
    events: List<Event>,
    conversations: List<Event>,
    anniversaries: List<com.tang.prm.domain.model.Anniversary>,
    gifts: List<com.tang.prm.domain.model.Gift>,
    thoughts: List<com.tang.prm.domain.model.Thought>,
    eventTypes: List<CustomType>,
    onEventClick: (Long) -> Unit,
    onAnniversaryClick: (Long) -> Unit,
    onGiftClick: (Long) -> Unit,
    onThoughtClick: (Long) -> Unit,
    onConversationClick: (Long) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 56.dp)
            .padding(bottom = 80.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        verticalAlignment = Alignment.Top
    ) {
        // ── 左栏：档案（完整字段）──
        Column(modifier = Modifier.weight(1f)) {
            GallerySectionTitle(title = "PROFILE · 档案", accentColor = tierColor)

            // 联系方式
            if (hasAny(contact.phone, contact.email, contact.city, contact.address)) {
                GalleryInfoCard(title = "CONTACT · 联系方式") {
                    contact.phone?.takeIf { it.isNotBlank() }?.let { GalleryInfoRow(key = "电话", value = it) }
                    contact.email?.takeIf { it.isNotBlank() }?.let { GalleryInfoRow(key = "邮箱", value = it) }
                    contact.city?.takeIf { it.isNotBlank() }?.let { GalleryInfoRow(key = "城市", value = it) }
                    contact.address?.takeIf { it.isNotBlank() }?.let { GalleryInfoRow(key = "地址", value = it, maxLines = 2) }
                }
            }

            // 重要日期
            if (contact.birthday != null || contact.knowingDate != null || contact.lastInteractionTime != null) {
                GalleryInfoCard(title = "DATES · 重要日期") {
                    contact.birthday?.let {
                        GalleryInfoRow(key = "生日", value = buildString {
                            append(DateUtils.formatYearMonthDayChineseFull(it))
                            if (contact.isLunarBirthday) append(" · 农历")
                            if (contact.isLeapMonthBirthday) append(" · 闰月")
                        })
                    }
                    contact.knowingDate?.let {
                        GalleryInfoRow(key = "认识日", value = DateUtils.formatYearMonthDayChineseFull(it))
                    }
                    contact.lastInteractionTime?.let {
                        GalleryInfoRow(key = "最后互动", value = DateUtils.formatRelativeTime(it))
                    }
                }
            }

            // 职业信息
            if (hasAny(contact.company, contact.jobTitle, contact.industry)) {
                GalleryInfoCard(title = "OCCUPATION · 职业") {
                    contact.company?.takeIf { it.isNotBlank() }?.let { GalleryInfoRow(key = "公司", value = it) }
                    contact.jobTitle?.takeIf { it.isNotBlank() }?.let { GalleryInfoRow(key = "职位", value = it) }
                    contact.industry?.takeIf { it.isNotBlank() }?.let { GalleryInfoRow(key = "行业", value = it) }
                }
            }

            // 个人特征（hobby/habit/diet/skill 是 JSON 数组字符串，需解析）
            if (hasAny(contact.mbti, contact.hobby, contact.habit, contact.diet, contact.skill)) {
                GalleryInfoCard(title = "PERSONAL · 个性") {
                    contact.mbti?.takeIf { it.isNotBlank() }?.let { GalleryInfoRow(key = "MBTI", value = it) }
                    parseListField(contact.hobby).takeIf { it.isNotEmpty() }?.let {
                        GalleryInfoRow(key = "爱好", value = it.joinToString("、"))
                    }
                    parseListField(contact.habit).takeIf { it.isNotEmpty() }?.let {
                        GalleryInfoRow(key = "习惯", value = it.joinToString("、"))
                    }
                    parseListField(contact.diet).takeIf { it.isNotEmpty() }?.let {
                        GalleryInfoRow(key = "饮食", value = it.joinToString("、"))
                    }
                    parseListField(contact.skill).takeIf { it.isNotEmpty() }?.let {
                        GalleryInfoRow(key = "技能", value = it.joinToString("、"))
                    }
                }
            }

            // 家庭社交
            if (hasAny(contact.spouseName, contact.childrenNames, contact.introducer) || contact.childrenCount > 0) {
                GalleryInfoCard(title = "FAMILY · 家庭社交") {
                    contact.spouseName?.takeIf { it.isNotBlank() }?.let { GalleryInfoRow(key = "配偶", value = it) }
                    if (contact.childrenCount > 0) {
                        GalleryInfoRow(key = "子女", value = "${contact.childrenCount}人")
                    }
                    contact.childrenNames?.takeIf { it.isNotBlank() }?.let { GalleryInfoRow(key = "子女姓名", value = it, maxLines = 2) }
                    contact.introducer?.takeIf { it.isNotBlank() }?.let { GalleryInfoRow(key = "介绍人", value = it) }
                }
            }
        }

        // ── 中左栏：时光（事件画廊）──
        Column(modifier = Modifier.weight(1.25f)) {
            GallerySectionTitle(title = "MOMENTS · 时光", accentColor = tierColor)
            if (events.isEmpty()) {
                GalleryEmptyHint(text = "暂无事件记录")
            } else {
                val rows = events.take(6).chunked(2)
                rows.forEach { rowEvents ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowEvents.forEach { event ->
                            GalleryEventCard(
                                event = event,
                                eventTypes = eventTypes,
                                onClick = { onEventClick(event.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowEvents.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // ── 中右栏：纪念 + 礼物 ──
        Column(modifier = Modifier.weight(1.1f)) {
            GallerySectionTitle(title = "MILESTONES · 纪念", accentColor = tierColor)
            if (anniversaries.isEmpty()) {
                GalleryEmptyHint(text = "暂无纪念日")
            } else {
                anniversaries.take(4).forEach { a ->
                    GalleryAnniversaryCard(
                        anniversary = a,
                        onClick = { onAnniversaryClick(a.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            GallerySectionTitle(title = "GIFTS · 礼物", accentColor = tierColor)
            if (gifts.isEmpty()) {
                GalleryEmptyHint(text = "暂无礼物记录")
            } else {
                gifts.take(4).forEach { g ->
                    GalleryGiftCard(
                        gift = g,
                        onClick = { onGiftClick(g.id) }
                    )
                }
            }
        }

        // ── 右栏：心绪（想法笔记）+ 对话 ──
        Column(modifier = Modifier.weight(0.95f)) {
            GallerySectionTitle(title = "THOUGHTS · 心绪", accentColor = tierColor)
            if (thoughts.isEmpty()) {
                GalleryEmptyHint(text = "暂无想法记录")
            } else {
                thoughts.take(5).forEach { thought ->
                    GalleryNoteCard(
                        date = DateUtils.formatYearMonthDay(thought.createdAt),
                        content = thought.content,
                        onClick = { onThoughtClick(thought.id) }
                    )
                }
            }

            // 对话记录
            Spacer(modifier = Modifier.height(24.dp))
            GallerySectionTitle(title = "DIALOGUES · 对话", accentColor = tierColor)
            if (conversations.isEmpty()) {
                GalleryEmptyHint(text = "暂无对话记录")
            } else {
                conversations.take(4).forEach { c ->
                    GalleryConversationCard(
                        conversation = c,
                        onClick = { onConversationClick(c.id) }
                    )
                }
            }
        }
    }
}

private fun hasAny(vararg values: String?): Boolean = values.any { !it.isNullOrBlank() }

/** 解析 JSON 数组字符串或逗号分隔字符串为列表，如 ["读书","运动"] → [读书, 运动] */
private fun parseListField(value: String?): List<String> {
    if (value.isNullOrBlank()) return emptyList()
    return try {
        val arr = org.json.JSONArray(value)
        (0 until arr.length()).map { arr.getString(it) }.filter { it.isNotBlank() }
    } catch (e: Exception) {
        value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
}

@Composable
private fun GallerySectionTitle(title: String, accentColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(2.dp)
                .background(MaterialTheme.colorScheme.onSurface)
        )
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 3.sp
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline)
        )
    }
}

@Composable
private fun GalleryInfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

@Composable
private fun GalleryInfoRow(key: String, value: String?, maxLines: Int = 1) {
    if (value.isNullOrBlank()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = key,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f, fill = false).padding(start = 16.dp)
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    )
}

@Composable
private fun GalleryEventCard(
    event: Event,
    eventTypes: List<CustomType>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typeLabel = event.customTypeName ?: getEventTypeLabel(event.type)
    val customType = if (event.type != com.tang.prm.domain.model.EventType.OTHER) {
        eventTypes.find { it.key == event.type.name } ?: eventTypes.find { it.name == event.type.name }
    } else {
        event.customTypeName?.let { ctn -> eventTypes.find { it.name == ctn } }
    }
    val accentColor = customType?.color?.let { it.toComposeColor(SignalPurple) }
        ?: getEventTypeStyle(event.type).accentColor

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation = 0.dp
    ) {
        Column {
            // 顶部彩色图区
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(accentColor, accentColor.copy(alpha = 0.6f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                val icon = customType?.icon?.let { getGenericIcon(it) }
                    ?: getEventTypeStyle(event.type).icon
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(56.dp)
                )
            }
            // 信息区
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = event.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${DateUtils.formatMonthDay(event.time)} · ${event.location ?: "—"}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun GalleryConversationCard(
    conversation: Event,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SignalPurple.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Chat,
                    contentDescription = null,
                    tint = SignalPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conversation.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = DateUtils.formatRelativeTime(conversation.time),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    conversation.emotion?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            fontSize = 11.sp,
                            color = SignalPurple,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GalleryNoteCard(date: String, content: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
        shape = RoundedCornerShape(12.dp),
        color = SignalAmber.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, SignalAmber.copy(alpha = 0.2f)),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = date,
                fontSize = 11.sp,
                color = SignalAmber,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = content,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp,
                fontStyle = FontStyle.Italic,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun GalleryAnniversaryCard(
    anniversary: com.tang.prm.domain.model.Anniversary,
    onClick: () -> Unit
) {
    val accentColor = when (anniversary.type) {
        com.tang.prm.domain.model.AnniversaryType.BIRTHDAY -> AnniversaryBirthday
        com.tang.prm.domain.model.AnniversaryType.ANNIVERSARY -> SignalPurple
        com.tang.prm.domain.model.AnniversaryType.HOLIDAY -> SignalAmber
    }
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Cake,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = anniversary.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = DateUtils.formatMonthDay(anniversary.date),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (anniversary.isRepeat) {
                        Icon(
                            Icons.Default.Repeat,
                            contentDescription = "每年",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(11.dp)
                        )
                    }
                    if (anniversary.isLunar) {
                        Text(
                            text = "农历",
                            fontSize = 10.sp,
                            color = accentColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            // 类型徽章
            Text(
                text = anniversary.type.displayName,
                fontSize = 10.sp,
                color = accentColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(accentColor.copy(alpha = 0.1f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun GalleryGiftCard(
    gift: com.tang.prm.domain.model.Gift,
    onClick: () -> Unit
) {
    val directionColor = if (gift.isSent) SignalGreen else SignalSky
    val directionIcon = if (gift.isSent) Icons.Default.NorthEast else Icons.Default.SouthWest
    val directionLabel = if (gift.isSent) "送出" else "收到"

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SignalAmber.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CardGiftcard,
                    contentDescription = null,
                    tint = SignalAmber,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = gift.giftName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = DateUtils.formatMonthDay(gift.date),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    gift.occasion?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            // 方向徽章 + 金额
            Column(horizontalAlignment = Alignment.End) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(directionIcon, contentDescription = null, tint = directionColor, modifier = Modifier.size(12.dp))
                    Text(
                        text = directionLabel,
                        fontSize = 10.sp,
                        color = directionColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                gift.amount?.let {
                    Text(
                        text = "¥${String.format(java.util.Locale.US, "%.0f", it)}",
                        fontSize = 11.sp,
                        color = SignalAmber,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GalleryEmptyHint(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            fontStyle = FontStyle.Italic
        )
    }
}
