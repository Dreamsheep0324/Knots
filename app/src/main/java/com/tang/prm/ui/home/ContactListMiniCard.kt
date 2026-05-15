package com.tang.prm.ui.home

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.getCardRarity
import com.tang.prm.ui.components.ContactAvatar
import com.tang.prm.ui.components.ContactRelationshipBadge
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.animation.primitives.rememberBreathingPulse
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.SignalPurple
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TerminalMiniCardRow(
    members: List<Contact>,
    circleColor: Color,
    onSelectMember: (Long) -> Unit
) {
    val listState = rememberLazyListState()
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val currentIndex: Int by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isNotEmpty()) {
                val center = layoutInfo.viewportStartOffset + (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
                visibleItems.minByOrNull { item ->
                    abs((item.offset + item.size / 2) - center)
                }?.index ?: 0
            } else 0
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        TerminalTapeTrack(
            currentIndex = currentIndex,
            totalCount = members.size,
            label = "DATA_TAPE",
            trackColor = circleColor
        )

        Spacer(Modifier.height(8.dp))

        LazyRow(
            state = listState,
            flingBehavior = snapFlingBehavior,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(members.withIndex().toList()) { (index, contact) ->
                TerminalMiniCard(
                    contact = contact,
                    onClick = { onSelectMember(contact.id) },
                    modifier = Modifier.fillParentMaxWidth(),
                    nodeIndex = index,
                    totalNodes = members.size
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        TerminalPageIndicator(
            currentIndex = currentIndex,
            totalCount = members.size,
            indicatorColor = circleColor
        )
    }
}

@Composable
private fun TerminalTapeTrack(
    currentIndex: Int,
    totalCount: Int,
    label: String,
    trackColor: Color = SignalPurple
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "[$label]",
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            color = trackColor.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
        ) {
            if (totalCount > 0) {
                val progress = (currentIndex + 1).toFloat() / totalCount.toFloat()
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(trackColor.copy(alpha = 0.6f))
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        Text(
            String.format("%02d/%02d", currentIndex + 1, totalCount),
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            color = trackColor.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TerminalPageIndicator(
    currentIndex: Int,
    totalCount: Int,
    indicatorColor: Color = SignalPurple
) {
    if (totalCount <= 1) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalCount) { index ->
            val isActive = index == currentIndex
            val size by animateDpAsState(
                targetValue = if (isActive) 8.dp else 4.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "dot_size"
            )
            val alpha by animateFloatAsState(
                targetValue = if (isActive) 1f else 0.3f,
                animationSpec = tween(AnimationTokens.Duration.fast),
                label = "dot_alpha"
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(size)
                    .clip(CircleShape)
                    .background(
                        if (isActive) indicatorColor.copy(alpha = alpha)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                    )
            )
        }
    }
}

@Composable
private fun TerminalMiniCard(
    contact: Contact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    nodeIndex: Int = 0,
    totalNodes: Int = 1
) {
    val rarity = getCardRarity(contact.intimacyScore)
    val rarityColor = rarity.color

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Box(
        modifier = modifier
            .height(100.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.5.dp, rarityColor.copy(alpha = 0.5f)),
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "[NODE_${String.format("%02d", nodeIndex + 1)}]",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = TerminalTextMuted,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                contact.name,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            "${contact.intimacyScore}%",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = rarityColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(2.dp))
                                .border(1.dp, rarityColor.copy(alpha = 0.4f), RoundedCornerShape(2.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            ContactAvatar(contact.avatar, contact.name, 36)
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(1.dp))
                                    .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(1.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth((contact.intimacyScore.toFloat() / 100f).coerceIn(0f, 1f))
                                        .background(rarityColor.copy(alpha = 0.8f), RoundedCornerShape(1.dp))
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    ContactRelationshipBadge(
                                        relationship = contact.relationship,
                                        color = TerminalTextDim,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    val statusAlpha by rememberBreathingPulse(
                                        minAlpha = 0.4f, maxAlpha = 1f,
                                        cycleDuration = 1200
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(SignalGreen.copy(alpha = statusAlpha))
                                    )
                                    Text(
                                        "在线",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 8.sp,
                                        color = SignalGreen
                                    )
                                }

                                Text(
                                    "[${rarity.label}]",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    color = rarityColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    NodeLinkIndicator(
                        currentIndex = nodeIndex,
                        totalCount = totalNodes,
                        activeColor = rarityColor
                    )
                }

                MiniCardCornerBrackets(rarityColor.copy(alpha = 0.4f))
            }
        }
    }
}

@Composable
private fun NodeLinkIndicator(
    currentIndex: Int,
    totalCount: Int,
    activeColor: Color
) {
    if (totalCount <= 1) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalCount) { index ->
            val isActive = index == currentIndex
            val isNear = abs(index - currentIndex) == 1

            val nodeColor = when {
                isActive -> activeColor
                isNear -> activeColor.copy(alpha = 0.4f)
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            }

            val nodeSize by animateDpAsState(
                targetValue = if (isActive) 6.dp else 4.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "node_size"
            )

            Box(
                modifier = Modifier
                    .size(nodeSize)
                    .clip(CircleShape)
                    .background(nodeColor)
            )

            if (index < totalCount - 1) {
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
                )
            }
        }
    }
}

@Composable
internal fun MiniCardCornerBrackets(color: Color) {
    val length = 8.dp
    val thickness = 1.5f
    Canvas(modifier = Modifier.fillMaxSize()) {
        val len = length.toPx()
        val w = size.width
        val h = size.height
        drawLine(color, Offset(0f, 0f), Offset(len, 0f), thickness)
        drawLine(color, Offset(0f, 0f), Offset(0f, len), thickness)
        drawLine(color, Offset(w, 0f), Offset(w - len, 0f), thickness)
        drawLine(color, Offset(w, 0f), Offset(w, len), thickness)
        drawLine(color, Offset(0f, h), Offset(len, h), thickness)
        drawLine(color, Offset(0f, h), Offset(0f, h - len), thickness)
        drawLine(color, Offset(w, h), Offset(w - len, h), thickness)
        drawLine(color, Offset(w, h), Offset(w, h - len), thickness)
    }
}
