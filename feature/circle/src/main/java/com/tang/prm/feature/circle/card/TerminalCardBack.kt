package com.tang.prm.feature.circle.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.model.CardRarity
import com.tang.prm.domain.model.Contact
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.animation.primitives.rememberScanLineOffset
import com.tang.prm.ui.components.HoloCornerMarks
import com.tang.prm.ui.components.HoloDataCell
import com.tang.prm.ui.components.HoloScanLine
import com.tang.prm.ui.components.HoloScanLineTexture
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.feature.circle.TerminalTextMuted
import java.util.Locale

@Composable
internal fun TerminalCardBackV2(
    contact: Contact,
    rarity: CardRarity,
    onContactClick: () -> Unit,
    onRemove: () -> Unit,
    shadowElevation: Float = 12f
) {
    val rarityColor = Color(rarity.colorValue)
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
                            "ID:${String.format(Locale.US, "%04d", contact.id)}",
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
