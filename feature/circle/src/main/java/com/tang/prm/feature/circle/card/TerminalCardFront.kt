package com.tang.prm.feature.circle.card

import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.model.IntimacyTier
import com.tang.prm.domain.model.Contact
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.animation.core.rememberPausableInfiniteFloatLoop
import com.tang.prm.ui.animation.primitives.rememberScanLineOffset
import com.tang.prm.ui.components.ContactAvatar
import com.tang.prm.ui.components.ContactRelationshipBadge
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.LocalIntimacyColors
import com.tang.prm.feature.circle.TerminalCardHeader
import com.tang.prm.feature.circle.TerminalCornerBrackets
import com.tang.prm.feature.circle.TerminalScanLineOverlay
import com.tang.prm.feature.circle.TerminalSystemParam
import com.tang.prm.feature.circle.TerminalTextDim
import com.tang.prm.feature.circle.TerminalTextMuted
import com.tang.prm.feature.circle.TerminalWaveformMonitor

@Composable
internal fun TerminalCardFrontV2(
    contact: Contact,
    rarity: IntimacyTier,
    waveformType: String = "sine",
    onFlip: () -> Unit,
    shadowElevation: Float = 12f
) {
    val rarityColor = LocalIntimacyColors.current.forTier(rarity)
    val cardWidth = 340.dp
    val cardHeight = 476.dp

    val floatOffset by rememberPausableInfiniteFloatLoop(
        initialValue = -2f,
        targetValue = 2f,
        durationMillis = AnimationTokens.Cycle.slow,
        easing = AnimationTokens.Easing.emphasis,
        repeatMode = RepeatMode.Reverse,
        label = "floatOffset"
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
