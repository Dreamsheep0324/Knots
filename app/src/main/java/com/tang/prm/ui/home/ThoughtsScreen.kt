@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.tang.prm.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tang.prm.domain.model.Thought
import com.tang.prm.domain.model.ThoughtType
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.components.AppCard
import com.tang.prm.ui.components.AppDatePicker
import com.tang.prm.ui.components.ContactAvatar
import com.tang.prm.ui.components.ContactPickerDialog
import com.tang.prm.ui.components.FormSectionLabel
import com.tang.prm.ui.theme.DialogDefaults
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalCoral
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.SignalSky
import com.tang.prm.util.DateUtils

private val ThoughtTypeColor = mapOf(
    ThoughtType.FRIEND to SignalAmber,
    ThoughtType.PLAN to SignalSky,
    ThoughtType.MURMUR to SignalPurple
)

private val ThoughtTypeBg = mapOf(
    ThoughtType.FRIEND to SignalAmber.copy(alpha = AnimationTokens.Alpha.faint),
    ThoughtType.PLAN to SignalSky.copy(alpha = AnimationTokens.Alpha.faint),
    ThoughtType.MURMUR to SignalPurple.copy(alpha = AnimationTokens.Alpha.faint)
)

private val ThoughtTypeIcon = mapOf(
    ThoughtType.FRIEND to Icons.Default.Group,
    ThoughtType.PLAN to Icons.Default.TaskAlt,
    ThoughtType.MURMUR to Icons.Default.Lightbulb
)

private val ThoughtTypeLabel = mapOf(
    ThoughtType.FRIEND to "伙伴",
    ThoughtType.PLAN to "计划",
    ThoughtType.MURMUR to "碎碎念"
)

private data class FilterOption(val key: String, val label: String, val icon: ImageVector)

private val filterOptions = listOf(
    FilterOption("all", "全部", Icons.Default.FilterList),
    FilterOption("friend", "伙伴", Icons.Default.Group),
    FilterOption("plan", "计划", Icons.Default.TaskAlt),
    FilterOption("murmur", "碎碎念", Icons.Default.Lightbulb),
    FilterOption("todo", "待办", Icons.Default.CheckBox)
)

@Composable
fun ThoughtsScreen(
    onBack: () -> Unit,
    viewModel: ThoughtsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var detailThoughtId by remember { mutableStateOf<Long?>(null) }
    val detailThought = detailThoughtId?.let { id -> uiState.filteredThoughts.find { it.id == id } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SignalAmber)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "想法",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showAddDialog(ThoughtType.MURMUR) }) {
                        Icon(Icons.Default.Add, contentDescription = "添加")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ThoughtLevelBanner(uiState = uiState)

            if (uiState.contactThoughts.isNotEmpty()) {
                SectionHeader(
                    icon = Icons.Default.Person,
                    iconColor = SignalAmber,
                    title = "关联人物",
                    action = null,
                    onActionClick = null,
                    modifier = Modifier.padding(start = 24.dp)
                )
                ContactStoriesRow(
                    contactThoughts = uiState.contactThoughts,
                    selectedContactId = uiState.selectedContactId,
                    onContactClick = { contactId ->
                        viewModel.onContactFilterSelected(contactId)
                    }
                )
            }

            FilterTabRow(
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = { viewModel.onFilterSelected(it) },
                counts = mapOf(
                    "all" to uiState.totalCount,
                    "friend" to uiState.friendCount,
                    "plan" to uiState.planCount,
                    "murmur" to uiState.murmurCount,
                    "todo" to uiState.todoTotalCount
                )
            )

            if (uiState.filteredThoughts.isEmpty()) {
                EmptyThoughtsState(
                    filter = uiState.selectedFilter
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp
                    )
                ) {
                    items(uiState.filteredThoughts, key = { it.id }) { thought ->
                        ThoughtFeedCard(
                            thought = thought,
                            contactName = viewModel.getContactName(thought.contactId),
                            contactAvatar = viewModel.getContactAvatar(thought.contactId),
                            isFavorite = thought.id in uiState.favoriteIds,
                            exp = uiState.thoughtExp(thought),
                            onToggleTodo = { viewModel.toggleTodoDone(thought) },
                            onEdit = { viewModel.showEditDialog(thought) },
                            onDelete = { viewModel.deleteThought(thought.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(thought.id, thought.content) },
                            onCardClick = { detailThoughtId = thought.id }
                        )
                    }
                }
            }
        }
    }

    detailThought?.let { thought ->
        ThoughtDetailDialog(
            thought = thought,
            contactName = viewModel.getContactName(thought.contactId),
            contactAvatar = viewModel.getContactAvatar(thought.contactId),
            isFavorite = thought.id in uiState.favoriteIds,
            onDismiss = { detailThoughtId = null },
            onToggleFavorite = { viewModel.toggleFavorite(thought.id, thought.content) },
            onToggleTodo = { viewModel.toggleTodoDone(thought) },
            onEdit = { viewModel.showEditDialog(thought); detailThoughtId = null },
            onDelete = { viewModel.deleteThought(thought.id); detailThoughtId = null }
        )
    }

    if (uiState.showDialog) {
        ThoughtDialog(
            thought = uiState.editingThought,
            dialogType = uiState.dialogType,
            contacts = uiState.contacts,
            onDismiss = { viewModel.dismissDialog() },
            onConfirm = { content, type, contactId, isPrivate, isTodo, dueDate ->
                val editing = uiState.editingThought
                if (editing == null) {
                    viewModel.insertThought(content, type, contactId, isPrivate, isTodo, dueDate)
                } else {
                    viewModel.updateThought(
                        editing.copy(
                            content = content,
                            type = type,
                            contactId = contactId,
                            isPrivate = isPrivate,
                            isTodo = isTodo,
                            dueDate = dueDate
                        )
                    )
                }
            }
        )
    }
}

@Composable
private fun ThoughtLevelBanner(uiState: ThoughtsUiState) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = SignalAmber.copy(alpha = AnimationTokens.Alpha.faint),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = SignalAmber,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Lv.${uiState.currentLevel}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "EXP ${uiState.currentExp}/${expForLevel(uiState.nextLevel)} · 距离 Lv.${uiState.nextLevel} 还需 ${expForLevel(uiState.nextLevel) - uiState.currentExp} EXP",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { uiState.levelProgress },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = SignalAmber,
                        trackColor = SignalAmber.copy(alpha = AnimationTokens.Alpha.faint),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "${(uiState.levelProgress * 100).toInt()}%",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SignalAmber.copy(alpha = AnimationTokens.Alpha.faint)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "${uiState.streak}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SignalAmber
                    )
                    Text(
                        "STREAK",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactStoriesRow(
    contactThoughts: List<ContactThoughts>,
    selectedContactId: Long?,
    onContactClick: (Long) -> Unit = {}
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(contactThoughts.take(8)) { ct ->
            val isSelected = ct.contact.id == selectedContactId
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(52.dp)
                    .clickable { onContactClick(ct.contact.id) }
            ) {
                Surface(
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp),
                    border = BorderStroke(
                        width = if (isSelected) 2.5.dp else 2.dp,
                        color = if (isSelected) SignalAmber else SignalAmber.copy(alpha = AnimationTokens.Alpha.subtle)
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        ContactAvatar(
                            avatar = ct.contact.avatar,
                            name = ct.contact.name,
                            size = 44
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    ct.contact.name,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) SignalAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun FilterTabRow(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    counts: Map<String, Int> = emptyMap()
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(filterOptions) { option ->
            val count = counts[option.key]
            FilterChip(
                selected = selectedFilter == option.key,
                onClick = { onFilterSelected(option.key) },
                label = {
                    Text(
                        buildString {
                            append(option.label)
                            if (count != null) append(" $count")
                        },
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SignalAmber,
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = SignalAmber,
                    enabled = true,
                    selected = selectedFilter == option.key
                )
            )
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun ThoughtFeedCard(
    thought: Thought,
    contactName: String?,
    contactAvatar: String?,
    isFavorite: Boolean,
    exp: Int,
    onToggleTodo: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCardClick: () -> Unit = {}
) {
    val typeColor = ThoughtTypeColor[thought.type] ?: SignalAmber
    val typeBg = ThoughtTypeBg[thought.type] ?: SignalAmber.copy(alpha = AnimationTokens.Alpha.faint)
    val typeIcon = ThoughtTypeIcon[thought.type] ?: Icons.Default.Lightbulb
    val typeLabel = ThoughtTypeLabel[thought.type] ?: "想法"

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = typeBg,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            typeIcon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = typeColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        typeLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 1.dp)
                    ) {
                        Text(
                            DateUtils.formatRelativeTime(thought.createdAt),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (thought.isPrivate) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(3.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.outline)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "私密",
                                modifier = Modifier.size(10.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half)
                            )
                        }
                        if (thought.isTodo && thought.dueDate != null) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(3.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.outline)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "截止 ${DateUtils.formatMonthDayChinese(thought.dueDate)}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = SignalGreen
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "编辑",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        modifier = Modifier.size(16.dp),
                        tint = SignalCoral.copy(alpha = AnimationTokens.Alpha.half)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                thought.content,
                style = MaterialTheme.typography.bodyMedium,
                color = if (thought.isTodo && thought.isDone) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half)
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                lineHeight = 24.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (thought.content.length > 80) {
                Text(
                    "查看详情",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = SignalAmber,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (contactName != null) {
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half))
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 6.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ContactAvatar(
                                avatar = contactAvatar,
                                name = contactName,
                                size = 18
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                contactName,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "取消收藏" else "收藏",
                        tint = if (isFavorite) SignalCoral else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                if (thought.isTodo) {
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onToggleTodo,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (thought.isDone) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                            contentDescription = if (thought.isDone) "已完成" else "待办",
                            tint = if (thought.isDone) SignalGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SignalAmber.copy(alpha = AnimationTokens.Alpha.faint)
                ) {
                    Text(
                        "+$exp EXP",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SignalAmber,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ThoughtDialog(
    thought: Thought?,
    dialogType: ThoughtType,
    contacts: List<com.tang.prm.domain.model.Contact>,
    onDismiss: () -> Unit,
    onConfirm: (content: String, type: ThoughtType, contactId: Long?, isPrivate: Boolean, isTodo: Boolean, dueDate: Long?) -> Unit
) {
    var content by remember { mutableStateOf(thought?.content ?: "") }
    var selectedType by remember { mutableStateOf(thought?.type ?: dialogType) }
    var selectedContactId by remember { mutableStateOf(thought?.contactId) }
    var isPrivate by remember { mutableStateOf(thought?.isPrivate ?: false) }
    var isTodo by remember { mutableStateOf(thought?.isTodo ?: false) }
    var dueDate by remember { mutableStateOf(thought?.dueDate) }
    var showContactPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val selectedContact = contacts.find { it.id == selectedContactId }

    if (showContactPicker) {
        ContactPickerDialog(
            contacts = contacts,
            title = "关联人物",
            onContactSelected = { selectedContactId = it.id; showContactPicker = false },
            onDismiss = { showContactPicker = false }
        )
    }

    if (showDatePicker) {
        AppDatePicker(
            show = showDatePicker,
            onDismiss = { showDatePicker = false },
            onDateSelected = { dueDate = it },
            initialDate = dueDate
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DialogDefaults.containerColor,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = (ThoughtTypeBg[selectedType] ?: SignalAmber.copy(alpha = AnimationTokens.Alpha.faint)),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            ThoughtTypeIcon[selectedType] ?: Icons.Default.Lightbulb,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = ThoughtTypeColor[selectedType] ?: SignalAmber
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(if (thought == null) "新想法" else "编辑想法", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("写下你的想法...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(12.dp)
                )

                FormSectionLabel(
                    icon = ThoughtTypeIcon[selectedType] ?: Icons.Default.Lightbulb,
                    label = "类型",
                    color = ThoughtTypeColor[selectedType] ?: SignalAmber
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ThoughtType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(ThoughtTypeLabel[type] ?: type.key, fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    ThoughtTypeIcon[type] ?: Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = (ThoughtTypeColor[type] ?: SignalAmber).copy(alpha = AnimationTokens.Alpha.subtle),
                                selectedLabelColor = ThoughtTypeColor[type] ?: SignalAmber,
                                selectedLeadingIconColor = ThoughtTypeColor[type] ?: SignalAmber
                            )
                        )
                    }
                }

                FormSectionLabel(
                    icon = Icons.Default.Person,
                    label = "关联人物",
                    color = SignalAmber
                )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showContactPicker = true },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedContact != null) {
                            ContactAvatar(avatar = selectedContact.avatar, name = selectedContact.name, size = 36)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "关联",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    selectedContact.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = { selectedContactId = null }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "移除", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(SignalAmber.copy(alpha = AnimationTokens.Alpha.faint), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = SignalAmber, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "选择人物",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SignalAmber,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { isPrivate = !isPrivate },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isPrivate) SignalAmber.copy(alpha = AnimationTokens.Alpha.faint) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(
                            1.dp,
                            if (isPrivate) SignalAmber.copy(alpha = AnimationTokens.Alpha.subtle) else MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isPrivate) SignalAmber else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (isPrivate) "私密" else "公开",
                                fontSize = 12.sp,
                                fontWeight = if (isPrivate) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isPrivate) SignalAmber else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { isTodo = !isTodo },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isTodo) SignalGreen.copy(alpha = AnimationTokens.Alpha.faint) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(
                            1.dp,
                            if (isTodo) SignalGreen.copy(alpha = AnimationTokens.Alpha.subtle) else MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                if (isTodo) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isTodo) SignalGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (isTodo) "待办" else "非待办",
                                fontSize = 12.sp,
                                fontWeight = if (isTodo) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isTodo) SignalGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (isTodo) {
                    AnimatedVisibility(visible = isTodo) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showDatePicker = true },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = SignalGreen
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (dueDate != null) "截止 ${DateUtils.formatMonthDayChinese(dueDate!!)}" else "设置截止日期",
                                    fontSize = 12.sp,
                                    color = if (dueDate != null) SignalGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (dueDate != null) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    IconButton(onClick = { dueDate = null }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Close, contentDescription = "清除", modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (content.isNotBlank()) {
                        onConfirm(content.trim(), selectedType, selectedContactId, isPrivate, isTodo, dueDate)
                    }
                },
                enabled = content.isNotBlank()
            ) {
                Text(if (thought == null) "添加" else "保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun EmptyThoughtsState(
    filter: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Lightbulb,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = SignalAmber.copy(alpha = AnimationTokens.Alpha.half)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                when (filter) {
                    "friend" -> "还没有关于伙伴的想法"
                    "plan" -> "还没有计划"
                    "murmur" -> "还没有碎碎念"
                    "todo" -> "没有待办事项"
                    else -> "还没有想法"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "点击 + 记录你的第一个想法",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half)
            )
        }
    }
}

@Composable
private fun ThoughtDetailDialog(
    thought: Thought,
    contactName: String?,
    contactAvatar: String?,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleTodo: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val typeColor = ThoughtTypeColor[thought.type] ?: SignalAmber
    val typeBg = ThoughtTypeBg[thought.type] ?: SignalAmber.copy(alpha = AnimationTokens.Alpha.faint)
    val typeIcon = ThoughtTypeIcon[thought.type] ?: Icons.Default.Lightbulb
    val typeLabel = ThoughtTypeLabel[thought.type] ?: "想法"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DialogDefaults.containerColor,
            tonalElevation = 0.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = typeBg,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                typeIcon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = typeColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            typeLabel,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 1.dp)
                        ) {
                            Text(
                                DateUtils.formatRelativeTime(thought.createdAt),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (thought.isPrivate) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "私密",
                                    modifier = Modifier.size(10.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half)
                                )
                            }
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "关闭",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        thought.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (thought.isTodo && thought.isDone) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        lineHeight = 22.sp
                    )

                    if (thought.isTodo) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (thought.isDone) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (thought.isDone) SignalGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (thought.isDone) "已完成" else "待办中",
                                fontSize = 12.sp,
                                color = if (thought.isDone) SignalGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (thought.dueDate != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "· 截止 ${DateUtils.formatMonthDayChinese(thought.dueDate)}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = SignalGreen
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 12.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(36.dp)) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "取消收藏" else "收藏",
                            tint = if (isFavorite) SignalCoral else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (thought.isTodo) {
                        IconButton(onClick = onToggleTodo, modifier = Modifier.size(36.dp)) {
                            Icon(
                                if (thought.isDone) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                contentDescription = if (thought.isDone) "已完成" else "待办",
                                tint = if (thought.isDone) SignalGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (contactName != null) {
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half))
                        ) {
                            Row(
                                modifier = Modifier.padding(start = 6.dp, end = 10.dp, top = 5.dp, bottom = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ContactAvatar(avatar = contactAvatar, name = contactName, size = 20)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    contactName,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp), tint = SignalCoral)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("删除", color = SignalCoral, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    TextButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = typeColor)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("编辑", color = typeColor, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
