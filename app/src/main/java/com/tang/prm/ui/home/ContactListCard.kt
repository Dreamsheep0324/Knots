package com.tang.prm.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CardRarity
import com.tang.prm.domain.model.getCardRarity
import com.tang.prm.ui.components.ContactAvatar
import com.tang.prm.ui.components.ContactRelationshipBadge
import com.tang.prm.ui.components.HoloCornerMarks
import com.tang.prm.ui.components.HoloDataCell
import com.tang.prm.ui.components.HoloScanLine
import com.tang.prm.ui.components.HoloScanLineTexture
import com.tang.prm.ui.animation.composites.HolographicConfig
import com.tang.prm.ui.animation.composites.HolographicCardOverlay
import com.tang.prm.ui.animation.composites.WaveformMonitor
import com.tang.prm.ui.animation.composites.WaveType
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.Error
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.toComposeColor
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.animation.core.rememberPausableInfiniteFloatLoop
import com.tang.prm.ui.animation.primitives.rememberBreathingPulse
import com.tang.prm.ui.animation.primitives.rememberScanLineOffset
import com.tang.prm.ui.animation.primitives.rememberShimmerPhase

@Composable
internal fun TerminalDossier(
    hologramCircle: HologramCircle,
    isExpanded: Boolean,
    selectedMemberId: Long?,
    flippedCardId: Long?,
    onExpand: () -> Unit,
    onSelectMember: (Long?) -> Unit,
    onFlipCard: (Long) -> Unit,
    onAddMember: () -> Unit,
    onEditCircle: () -> Unit,
    onDeleteCircle: () -> Unit,
    onRemoveMember: (Long) -> Unit,
    onContactClick: (Long) -> Unit
) {
    val circle = hologramCircle.circle
    val members = hologramCircle.members
    val memberCount = members.size
    val avgIntimacy = if (members.isEmpty()) 0 else members.map { it.intimacyScore }.average().toInt()
    val rarity = getCardRarity(avgIntimacy)
    val circleColor = MaterialTheme.colorScheme.onSurface

    Column {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpand() },
            shape = RoundedCornerShape(2.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    String.format("%03d", circle.id),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = circleColor,
                    modifier = Modifier.width(40.dp)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        circle.name,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    circle.description?.let {
                        Text(
                            it,
                            fontSize = 9.sp,
                            color = TerminalTextDim,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Text(
                    "[$memberCount]",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TerminalTextDim,
                    modifier = Modifier.width(50.dp),
                    textAlign = TextAlign.Center
                )

                Text(
                    String.format("%02d%%", avgIntimacy),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = rarity.color,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(60.dp),
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier.width(60.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val statusAlpha by rememberBreathingPulse(
                        minAlpha = 0.4f,
                        maxAlpha = 1f,
                        cycleDuration = 1200
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(circleColor.copy(alpha = statusAlpha))
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "RUN",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = circleColor
                    )
                }

                Spacer(Modifier.width(4.dp))

                val rotation by animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f,
                    animationSpec = tween(AnimationTokens.Duration.fast), label = "arrow"
                )
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotation).size(18.dp),
                    tint = circleColor.copy(alpha = 0.6f)
                )
            }
        }

        if (isExpanded) {
            TerminalExpandedContent(
                members = members,
                circleColor = circleColor,
                selectedMemberId = selectedMemberId,
                flippedCardId = flippedCardId,
                onSelectMember = onSelectMember,
                onFlipCard = onFlipCard,
                onAddMember = onAddMember,
                onEditCircle = onEditCircle,
                onDeleteCircle = onDeleteCircle,
                onRemoveMember = onRemoveMember,
                onContactClick = onContactClick
            )
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
private fun TerminalExpandedContent(
    members: List<Contact>,
    circleColor: Color,
    selectedMemberId: Long?,
    flippedCardId: Long?,
    onSelectMember: (Long?) -> Unit,
    onFlipCard: (Long) -> Unit,
    onAddMember: () -> Unit,
    onEditCircle: () -> Unit,
    onDeleteCircle: () -> Unit,
    onRemoveMember: (Long) -> Unit,
    onContactClick: (Long) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(Dimens.paddingCard)) {
            TerminalActionBar(
                circleColor = circleColor,
                onAddMember = onAddMember,
                onEditCircle = onEditCircle,
                onDeleteCircle = onDeleteCircle
            )

            Spacer(Modifier.height(12.dp))

            TerminalDivider(MaterialTheme.colorScheme.outline)

            Spacer(Modifier.height(12.dp))

            if (members.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "[空目录]",
                        fontSize = 13.sp,
                        color = TerminalTextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                TerminalMiniCardRow(
                    members = members,
                    circleColor = circleColor,
                    onSelectMember = onSelectMember
                )
            }
        }
    }
}

@Composable
private fun TerminalActionBar(
    circleColor: Color,
    onAddMember: () -> Unit,
    onEditCircle: () -> Unit,
    onDeleteCircle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TerminalActionButton(
            label = "添加",
            accentColor = circleColor,
            onClick = onAddMember
        )
        TerminalActionButton(
            label = "编辑",
            accentColor = circleColor,
            onClick = onEditCircle
        )
        TerminalActionButton(
            label = "删除",
            accentColor = Error,
            onClick = onDeleteCircle
        )
    }
}

@Composable
internal fun TerminalActionButton(label: String, onClick: () -> Unit, accentColor: Color = SignalPurple) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(2.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
    ) {
        Text(
            label,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
internal fun FullscreenCardOverlay(
    contact: Contact,
    waveformType: String,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onClose: () -> Unit,
    onRemove: () -> Unit,
    onContactClick: () -> Unit
) {
    val rarity = getCardRarity(contact.intimacyScore)
    val rarityColor = rarity.color

    HolographicCardOverlay(
        isFlipped = isFlipped,
        onFlip = onFlip,
        onClose = onClose,
        config = HolographicConfig.default.copy(
            borderColor = rarityColor,
            enableWaveform = true,
            enablePulse = true,
            enableFloat = true,
            enableScanLine = true,
            enableShimmer = true,
            scanLineColor = rarityColor,
            shimmerColor = rarityColor
        ),
        frontContent = {
            TerminalCardFrontV2(
                contact = contact,
                rarity = rarity,
                waveformType = waveformType,
                onFlip = onFlip,
                shadowElevation = 20f
            )
        },
        backContent = {
            TerminalCardBackV2(
                contact = contact,
                rarity = rarity,
                onContactClick = onContactClick,
                onRemove = onRemove,
                shadowElevation = 20f
            )
        }
    )
}

@Composable
private fun TerminalCardFrontV2(
    contact: Contact,
    rarity: CardRarity,
    waveformType: String = "sine",
    onFlip: () -> Unit,
    shadowElevation: Float = 12f
) {
    val rarityColor = rarity.color
    val cardWidth = 340.dp
    val cardHeight = 476.dp

    val floatOffset by rememberPausableInfiniteFloatLoop(
        initialValue = -2f,
        targetValue = 2f,
        durationMillis = AnimationTokens.Cycle.slow,
        easing = AnimationTokens.Easing.emphasis,
        repeatMode = androidx.compose.animation.core.RepeatMode.Reverse,
        label = "floatOffset"
    )

    val _pulseAlpha by rememberBreathingPulse(
        minAlpha = 0.2f,
        maxAlpha = 0.6f,
        cycleDuration = AnimationTokens.Cycle.normal
    )

    val scanLineOffset by rememberScanLineOffset(
        cycleDuration = AnimationTokens.Cycle.slow
    )

    val _shimmerOffset by rememberShimmerPhase(
        cycleDuration = 2500
    )

    var wavePhase by remember { mutableStateOf(0f) }
    var lastFrameTime by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { nanoTime ->
                if (lastFrameTime > 0L) {
                    val deltaSeconds = (nanoTime - lastFrameTime) / 1_000_000_000f
                    wavePhase += deltaSeconds * (2f * 3.14159f / 3f)
                }
                lastFrameTime = nanoTime
            }
        }
    }

    Box(
        modifier = Modifier
            .width(cardWidth)
            .height(cardHeight)
            .graphicsLayer { translationY = floatOffset },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(cardWidth)
                .height(cardHeight)
                .clickable { onFlip() },
            shape = RoundedCornerShape(2.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(2.dp, rarityColor.copy(alpha = 0.5f)),
            shadowElevation = shadowElevation.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Dimens.paddingCard)
                ) {
                    TerminalCardHeader(contact, rarity, rarityColor)

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(2.dp))
                                .border(1.5.dp, rarityColor.copy(alpha = 0.4f), RoundedCornerShape(2.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            ContactAvatar(contact.avatar, contact.name, 52)
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                contact.name,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                ContactRelationshipBadge(
                                    relationship = contact.relationship,
                                    bracketed = true,
                                    color = TerminalTextDim
                                )
                                val starCount = rarity.stars
                                repeat(5) { index ->
                                    Text(
                                        if (index < starCount) "★" else "☆",
                                        fontSize = 10.sp,
                                        color = if (index < starCount) rarityColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    )
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    "亲密度",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    color = TerminalTextMuted
                                )
                                Box(
                                    modifier = Modifier
                                        .width(100.dp)
                                        .height(8.dp)
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
                                Text(
                                    "${contact.intimacyScore}%",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = rarityColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    TerminalWaveformMonitor(rarityColor, wavePhase, waveformType)

                    Spacer(Modifier.height(8.dp))
                    Text(
                        "[实时波形监控]",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        color = TerminalTextMuted,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TerminalSystemParam("频率", "2.4GHz", rarityColor, modifier = Modifier.weight(1f))
                        TerminalSystemParam("响应", "15ms", rarityColor, modifier = Modifier.weight(1f))
                        TerminalSystemParam("稳定", "99.9%", rarityColor, modifier = Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "关系: ${contact.relationship ?: "未知"}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = TerminalTextDim
                        )
                        Text(
                            "最近: 2天前",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = TerminalTextDim
                        )
                    }
                }

                TerminalScanLineOverlay(scanLineOffset)
                TerminalCornerBrackets(rarityColor)
            }
        }
    }
}

@Composable
private fun TerminalCardBackV2(
    contact: Contact,
    rarity: CardRarity,
    onContactClick: () -> Unit,
    onRemove: () -> Unit,
    shadowElevation: Float = 12f
) {
    val rarityColor = rarity.color
    val cardWidth = 340.dp
    val cardHeight = 476.dp

    val scanLineOffset by rememberScanLineOffset(
        cycleDuration = 4000
    )

    Surface(
        modifier = Modifier
            .width(cardWidth)
            .height(cardHeight),
        shape = RoundedCornerShape(2.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.5.dp, rarityColor.copy(alpha = 0.25f)),
        shadowElevation = shadowElevation.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            HoloScanLineTexture(rarityColor)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Dimens.paddingPage)
            ) {
                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "PERSONNEL FILE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = rarityColor.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    )
                    Box(
                        modifier = Modifier
                            .background(rarityColor.copy(alpha = 0.05f), RoundedCornerShape(2.dp))
                            .border(1.dp, rarityColor.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "ID:${String.format("%04d", contact.id)}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = TerminalTextMuted
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        contact.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 2.sp
                    )
                    Text(
                        rarity.label,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = rarityColor
                    )
                }

                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(rarityColor.copy(alpha = 0.1f))
                )
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HoloDataCell(
                        label = "RELATION",
                        value = contact.relationship ?: "未知",
                        valueColor = rarityColor,
                        tintColor = rarityColor,
                        labelColor = TerminalTextMuted,
                        modifier = Modifier.weight(1f)
                    )
                    HoloDataCell(
                        label = "PHONE",
                        value = contact.phone ?: "—",
                        valueColor = rarityColor,
                        tintColor = rarityColor,
                        labelColor = TerminalTextMuted,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HoloDataCell(
                        label = "RARITY",
                        value = "${rarity.label} ${"★".repeat(rarity.stars)}",
                        valueColor = rarityColor,
                        tintColor = rarityColor,
                        labelColor = TerminalTextMuted,
                        modifier = Modifier.weight(1f)
                    )
                    HoloDataCell(
                        label = "STATUS",
                        value = "● ONLINE",
                        valueColor = SignalGreen,
                        tintColor = rarityColor,
                        labelColor = TerminalTextMuted,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    "INTIMACY LEVEL",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.sp,
                    color = TerminalTextMuted,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(1.dp))
                        .border(1.dp, rarityColor.copy(alpha = 0.06f), RoundedCornerShape(1.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth((contact.intimacyScore.toFloat() / 100f).coerceIn(0f, 1f))
                            .background(
                                Brush.horizontalGradient(listOf(rarityColor, rarityColor.copy(alpha = 0.4f))),
                                RoundedCornerShape(1.dp)
                            )
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "${contact.intimacyScore}%",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = rarityColor,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(rarityColor.copy(alpha = 0.02f), RoundedCornerShape(3.dp))
                        .border(1.dp, rarityColor.copy(alpha = 0.06f), RoundedCornerShape(3.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            "PROFILE",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp,
                            color = TerminalTextMuted,
                            letterSpacing = 2.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            contact.notes ?: "暂无备注信息",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 17.sp,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onContactClick,
                        shape = RoundedCornerShape(3.dp),
                        color = rarityColor.copy(alpha = AnimationTokens.Alpha.faint),
                        border = BorderStroke(1.dp, rarityColor.copy(alpha = 0.25f))
                    ) {
                        Text(
                            "查看档案",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = rarityColor,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            letterSpacing = 1.sp
                        )
                    }
                    Surface(
                        onClick = onRemove,
                        shape = RoundedCornerShape(3.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(
                            "移除",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerminalTextMuted,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            HoloScanLine(scanLineOffset, rarityColor)
            HoloCornerMarks(rarityColor)
        }
    }
}
