package com.tang.prm.ui.contacts

import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.animation.core.rememberPausableInfiniteFloatLoop
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.tang.prm.domain.model.CardRarity
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.getCardRarity
import com.tang.prm.ui.components.CardCornerBrackets
import com.tang.prm.ui.components.CardScanLineOverlay
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
import com.tang.prm.ui.animation.primitives.rememberBreathingPulse
import com.tang.prm.ui.animation.primitives.rememberScanLineOffset
import com.tang.prm.ui.theme.Dimens
import kotlin.math.sin

@Composable
internal fun ContactCardOverlay(
    contact: Contact,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onClose: () -> Unit,
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
            borderAlpha = AnimationTokens.Alpha.half,
            enableWaveform = true,
            enablePulse = true,
            enableFloat = true,
            enableScanLine = true,
            enableShimmer = false,
            scanLineColor = rarityColor,
            shimmerColor = rarityColor
        ),
        frontContent = {
            ContactCardFront(
                contact = contact,
                rarity = rarity,
                onFlip = onFlip,
                shadowElevation = 20f
            )
        },
        backContent = {
            ContactCardBack(
                contact = contact,
                rarity = rarity,
                onContactClick = onContactClick,
                onClose = onClose,
                shadowElevation = 20f
            )
        }
    )
}

@Composable
private fun ContactCardFront(
    contact: Contact,
    rarity: CardRarity,
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

    val pulseAlpha by rememberBreathingPulse(
        minAlpha = 0.2f,
        maxAlpha = 0.6f,
        cycleDuration = AnimationTokens.Cycle.normal
    )

    val scanLineOffset by rememberScanLineOffset(
        cycleDuration = AnimationTokens.Cycle.slow
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
            border = BorderStroke(2.dp, rarityColor.copy(alpha = AnimationTokens.Alpha.half)),
            shadowElevation = shadowElevation.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Dimens.paddingCard)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(CardGreen.copy(alpha = pulseAlpha), CircleShape)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "ONLINE",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = CardGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                rarity.shortLabel,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = rarityColor,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "ID:${String.format("%04d", contact.id)}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(2.dp))
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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Box(
                                    modifier = Modifier
                                        .width(100.dp)
                                        .height(8.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(1.dp))
                                        .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(1.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth((contact.intimacyScore.toFloat() / 100f).coerceIn(0f, 1f))
                                            .background(rarityColor.copy(alpha = AnimationTokens.Alpha.strong), RoundedCornerShape(1.dp))
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

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(2.dp))
                            .border(1.dp, rarityColor.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.06f)
                        val midLineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val width = size.width
                            val height = size.height
                            val centerY = height / 2

                            for (i in 0..4) {
                                val y = (height / 4) * i
                                drawLine(gridColor, Offset(0f, y), Offset(width, y), 0.5f)
                            }
                            for (i in 0..10) {
                                val x = (width / 10) * i
                                drawLine(gridColor, Offset(x, 0f), Offset(x, height), 0.5f)
                            }

                            drawLine(
                                midLineColor,
                                Offset(0f, centerY),
                                Offset(width, centerY),
                                0.8f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 3f), 0f)
                            )

                            val path = Path()
                            for (x in 0..width.toInt()) {
                                val t = x / width
                                val y = centerY + sin(t * 6f * Math.PI.toFloat() + wavePhase) * (height * 0.3f)
                                if (x == 0) path.moveTo(x.toFloat(), y)
                                else path.lineTo(x.toFloat(), y)
                            }
                            drawPath(path, rarityColor.copy(alpha = AnimationTokens.Alpha.subtle), style = Stroke(width = 6f))
                            drawPath(path, rarityColor.copy(alpha = 0.85f), style = Stroke(width = 2f))
                        }

                        Text(
                            "正弦波",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 7.sp,
                            color = rarityColor.copy(alpha = AnimationTokens.Alpha.half),
                            modifier = Modifier.align(Alignment.TopEnd)
                        )
                    }
                    Text(
                        "[实时波形监控]",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("频率" to "2.4GHz", "响应" to "15ms", "稳定" to "99.9%").forEach { (label, value) ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(2.dp))
                                    .border(1.dp, rarityColor.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(label, fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(2.dp))
                                Text(value, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = rarityColor, fontWeight = FontWeight.Bold)
                            }
                        }
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "最近: 2天前",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                CardScanLineOverlay(scanLineOffset)
                CardCornerBrackets(rarityColor)
            }
        }
    }
}

@Composable
private fun ContactCardBack(
    contact: Contact,
    rarity: CardRarity,
    onContactClick: () -> Unit,
    onClose: () -> Unit,
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        rarity.shortLabel,
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
                        modifier = Modifier.weight(1f)
                    )
                    HoloDataCell(
                        label = "PHONE",
                        value = contact.phone ?: "—",
                        valueColor = rarityColor,
                        tintColor = rarityColor,
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
                        value = "${rarity.shortLabel} ${"★".repeat(rarity.stars)}",
                        valueColor = rarityColor,
                        tintColor = rarityColor,
                        modifier = Modifier.weight(1f)
                    )
                    HoloDataCell(
                        label = "STATUS",
                        value = "● ONLINE",
                        valueColor = CardGreen,
                        tintColor = rarityColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    "INTIMACY LEVEL",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        onClick = onClose,
                        shape = RoundedCornerShape(3.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(
                            "关闭",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
