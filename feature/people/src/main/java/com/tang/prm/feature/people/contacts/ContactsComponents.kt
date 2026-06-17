package com.tang.prm.feature.people.contacts

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
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
        color = if (isSelected) Primary else MaterialTheme.colorScheme.surfaceVariant
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
                modifier = Modifier.staggeredAppear(index = minOf(index, 15))
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
                                tint = Primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                "拖拽调整顺序",
                                fontSize = 13.sp,
                                color = Primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        TextButton(onClick = onToggleReorder) {
                            Text("完成", color = Primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            itemsIndexed(contacts, key = { _, it -> it.id }) { index, contact ->
                val isDragging = index == dragIndex
                val elevation by animateDpAsState(
                    targetValue = if (isDragging) 12.dp else 3.dp,
                    label = "elevate_$index"
                )
                val scale by animateFloatAsState(
                    targetValue = if (isDragging) 1.03f else 1f,
                    label = "scale_$index"
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
                                Modifier.pointerInput(index) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            dragIndex = index
                                            dragOffsetY = 0f
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffsetY += dragAmount.y

                                            val itemHeight = 80f
                                            val threshold = itemHeight / 2
                                            if (dragOffsetY > threshold && index < contacts.size - 1) {
                                                onMoveContact(index, index + 1)
                                                dragIndex = index + 1
                                                dragOffsetY = 0f
                                            } else if (dragOffsetY < -threshold && index > 0) {
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
                                Modifier.pointerInput(index) {
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
                        elevation = elevation,
                        modifier = Modifier.staggeredAppear(index = minOf(index, 15))
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

internal fun getContactListIntimacyColor(score: Int): Color =
    Color(com.tang.prm.domain.model.IntimacyTier.of(score).colorValue)

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
    val intimacyColor = getContactListIntimacyColor(contact.intimacyScore)
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
                    Box(
                        modifier = Modifier
                            .width(64.dp)
                            .height(4.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(2.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(contact.intimacyScore / 100f)
                                .background(intimacyColor, RoundedCornerShape(2.dp))
                        )
                    }
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
