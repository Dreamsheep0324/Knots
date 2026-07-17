package com.tang.prm.feature.people.contacts

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.tang.prm.domain.model.Contact
import com.tang.prm.ui.components.AppCard
import com.tang.prm.ui.components.ContactAvatar
import com.tang.prm.ui.animation.primitives.staggeredAppear
import com.tang.prm.ui.theme.*
import com.tang.prm.domain.model.AppStrings
import java.util.Locale

/**
 * 联系人 ID 的统一格式化（4 位补零，例如 42 → "0042"）。
 * 用于 Holographic 卡牌、卡片视图等多处显示。
 */
internal val Contact.formattedId: String
    get() = String.format(Locale.US, "%04d", id)

/**
 * 统一的亲密度进度条组件。
 *
 * 用于详情页徽章、列表卡片、画廊卡片、全息卡正/背面等多处场景。
 * 通过 `fillBrush` 参数支持纯色或渐变填充，通过 `border` 参数可选边框。
 *
 * @param score 亲密度分数（0-100），内部会 coerceIn 防止溢出
 * @param fillBrush 进度条填充画刷（纯色用 SolidColor，渐变用 Brush.horizontalGradient 等）
 * @param modifier 用于设置宽度（如 Modifier.width(64.dp) 或 Modifier.fillMaxWidth()）
 * @param height 进度条高度
 * @param cornerRadius 圆角半径
 * @param background 进度条轨道背景色
 * @param border 可选边框（全息卡系列使用）
 */
@Composable
internal fun IntimacyProgressBar(
    score: Int,
    fillBrush: Brush,
    modifier: Modifier = Modifier,
    height: Dp = 4.dp,
    cornerRadius: Dp = 2.dp,
    background: Color = MaterialTheme.colorScheme.surfaceVariant,
    border: BorderStroke? = null
) {
    val shape = RoundedCornerShape(cornerRadius)
    val trackModifier = if (border != null) {
        modifier.height(height).background(background, shape).border(border, shape)
    } else {
        modifier.height(height).background(background, shape)
    }
    Box(modifier = trackModifier) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth((score / 100f).coerceIn(0f, 1f))
                .background(fillBrush, shape)
        )
    }
}

/**
 * 格式化"认识时长"：精确到年+天（例如 "3年45天" 或 "12天"）。
 * 时间戳为 0 或未来时间返回 null。
 * 用于详情页头部 DaysKnownBadge 与平板 Hero 信息栏。
 */
internal fun formatKnownDuration(timestamp: Long): String? {
    val diffMillis = System.currentTimeMillis() - timestamp
    if (diffMillis <= 0) return null
    val totalDays = (diffMillis / (24L * 60 * 60 * 1000)).toInt().coerceAtLeast(0)
    val years = totalDays / 365
    val days = totalDays % 365
    return if (years > 0) "${years}年${days}天" else "${days}天"
}

@Composable
internal fun RelationshipFilterChips(
    relationships: List<com.tang.prm.domain.model.CustomType>,
    selectedRelationship: String?,
    onRelationshipSelected: (String?) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChipItem(
                label = "全部",
                isSelected = selectedRelationship == null,
                onClick = { onRelationshipSelected(null) }
            )
        }
        items(relationships, key = { it.id }) { rel ->
            FilterChipItem(
                label = rel.name,
                isSelected = selectedRelationship == rel.name,
                onClick = { onRelationshipSelected(rel.name) }
            )
        }
    }
}

@Composable
internal fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun ContactsContent(
    contacts: List<Contact>,
    viewMode: Int,
    isReorderMode: Boolean,
    onContactClick: (Contact) -> Unit,
    onCardSelect: (Long) -> Unit = {},
    onToggleReorder: () -> Unit = {},
    onMoveContact: (Int, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    when (viewMode) {
        0 -> ContactsGrid(contacts = contacts, onContactClick = onContactClick, modifier = modifier)
        1 -> ContactsList(
            contacts = contacts,
            onContactClick = onContactClick,
            isReorderMode = isReorderMode,
            onToggleReorder = onToggleReorder,
            onMoveContact = onMoveContact,
            modifier = modifier
        )
        2 -> ContactsCardView(contacts = contacts, onCardSelect = onCardSelect, modifier = modifier)
    }
}

@Composable
internal fun ContactsGrid(
    contacts: List<Contact>,
    onContactClick: (Contact) -> Unit,
    modifier: Modifier = Modifier
) {
    val appearKey = remember { Any() }
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.navigationBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 100.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(contacts, key = { _, it -> it.id }) { index, contact ->
            ContactGridCard(
                contact = contact,
                onClick = { onContactClick(contact) },
                modifier = Modifier.staggeredAppear(index = minOf(index, 15), triggerKey = appearKey)
            )
        }
    }
}

@Composable
internal fun ContactsList(
    contacts: List<Contact>,
    onContactClick: (Contact) -> Unit,
    isReorderMode: Boolean,
    onToggleReorder: () -> Unit,
    onMoveContact: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var dragIndex by remember { mutableIntStateOf(-1) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    // 卡片高度 80dp 转 px，作为拖拽交换阈值（跨密度一致体验）
    val itemHeightPx = with(density) { 80.dp.toPx() }
    val swapThreshold = itemHeightPx / 2f

    Box(modifier = modifier) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isReorderMode) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.SwapVert,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                "拖拽调整顺序",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        TextButton(onClick = onToggleReorder) {
                            Text("完成", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            itemsIndexed(contacts, key = { _, it -> it.id }) { index, contact ->
                val isDragging = index == dragIndex
                val elevation by animateDpAsState(
                    targetValue = if (isDragging) 12.dp else 3.dp,
                    label = "elevate_${contact.id}"
                )
                val scale by animateFloatAsState(
                    targetValue = if (isDragging) 1.03f else 1f,
                    label = "scale_${contact.id}"
                )

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            translationY = if (isDragging) dragOffsetY else 0f
                            scaleX = scale
                            scaleY = scale
                        }
                        .zIndex(if (isDragging) 1f else 0f)
                        .then(
                            if (isReorderMode) {
                                Modifier.pointerInput(contact.id) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            dragIndex = index
                                            dragOffsetY = 0f
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffsetY += dragAmount.y

                                            if (dragOffsetY > swapThreshold && index < contacts.size - 1) {
                                                onMoveContact(index, index + 1)
                                                dragIndex = index + 1
                                                dragOffsetY = 0f
                                            } else if (dragOffsetY < -swapThreshold && index > 0) {
                                                onMoveContact(index, index - 1)
                                                dragIndex = index - 1
                                                dragOffsetY = 0f
                                            }
                                        },
                                        onDragEnd = {
                                            dragIndex = -1
                                            dragOffsetY = 0f
                                        },
                                        onDragCancel = {
                                            dragIndex = -1
                                            dragOffsetY = 0f
                                        }
                                    )
                                }
                            } else {
                                Modifier.pointerInput(contact.id) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { onToggleReorder() },
                                        onDrag = { _, _ -> },
                                        onDragEnd = {},
                                        onDragCancel = {}
                                    )
                                }
                            }
                        )
                ) {
                    ContactListCard(
                        contact = contact,
                        onClick = { onContactClick(contact) },
                        isReorderMode = isReorderMode,
                        elevation = elevation
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(100.dp).navigationBarsPadding()) }
        }
    }
}

@Composable
internal fun ContactGridCard(
    contact: Contact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ContactAvatar(
                avatar = contact.avatar,
                name = contact.name,
                size = 50
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = contact.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

internal fun getContactListIntimacyLevel(score: Int): String =
    com.tang.prm.domain.model.IntimacyTier.of(score).label

@Composable
internal fun ContactListCard(
    contact: Contact,
    onClick: () -> Unit,
    isReorderMode: Boolean = false,
    elevation: Dp = 3.dp,
    modifier: Modifier = Modifier
) {
    val intimacyColor = getIntimacyColor(contact.intimacyScore)
    val intimacyLevel = getContactListIntimacyLevel(contact.intimacyScore)

    Surface(
        onClick = if (isReorderMode) ({}) else onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = elevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isReorderMode) {
                Icon(
                    Icons.Default.DragHandle,
                    contentDescription = "拖拽",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }

            ContactAvatar(
                avatar = contact.avatar,
                name = contact.name,
                size = 48
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    contact.relationship?.let { relationship ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = intimacyColor.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = relationship,
                                style = MaterialTheme.typography.labelSmall,
                                color = intimacyColor,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IntimacyProgressBar(
                        score = contact.intimacyScore,
                        fillBrush = SolidColor(intimacyColor),
                        modifier = Modifier.width(64.dp),
                        height = 4.dp,
                        cornerRadius = 2.dp
                    )
                    Text(
                        text = "$intimacyLevel ${contact.intimacyScore}",
                        style = MaterialTheme.typography.labelSmall,
                        color = intimacyColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (!isReorderMode) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFFD1D5DB),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
