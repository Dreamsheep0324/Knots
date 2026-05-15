package com.tang.prm.ui.home

import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.animation.primitives.rememberContinuousRotation
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.model.GiftType
import com.tang.prm.ui.theme.*
import com.tang.prm.util.DateUtils
import kotlin.math.roundToInt
import com.tang.prm.ui.theme.GiftTypeStyle
import com.tang.prm.ui.theme.toStyle

// ═══════════════════════════════════════════════════════════════
//  CASSETTE TAPE CARD — 磁带卡片（参考播放器卷轴设计）
// ═══════════════════════════════════════════════════════════════

@Composable
internal fun CassetteTapeCard(
    gift: GiftRecord,
    index: Int,
    onClick: () -> Unit
) {
    val giftTypeData = GiftType.entries.find { it.name == gift.giftType } ?: GiftType.OTHER
    val dateFormat: (Long) -> String = { DateUtils.formatYearMonthDayDot(it) }
    val tapeId = String.format("NO.%03d", index)

    val reelRotation by rememberContinuousRotation(cycleDuration = 10000)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, CardBorder),
            shadowElevation = 3.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                TapeLabelInfo(
                    gift = gift,
                    giftTypeData = giftTypeData,
                    tapeId = tapeId,
                    dateFormat = dateFormat
                )

                TapeWindowWithPlayerReel(
                    gift = gift,
                    giftTypeData = giftTypeData,
                    reelRotation = reelRotation
                )
            }
        }
    }
}

// ── 磁带标签信息区 ──
@Composable
private fun TapeLabelInfo(
    gift: GiftRecord,
    giftTypeData: GiftType,
    tapeId: String,
    dateFormat: (Long) -> String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (gift.isSent) SignalAmber.copy(alpha = AnimationTokens.Alpha.subtle) else SignalGreen.copy(alpha = AnimationTokens.Alpha.subtle))
                        .border(0.5.dp, if (gift.isSent) SignalAmber.copy(alpha = 0.4f) else SignalGreen.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        if (gift.isSent) "A面 · 送出" else "B面 · 收到",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (gift.isSent) SignalAmber else SignalGreen
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    giftTypeData.displayName,
                    fontSize = 11.sp,
                    color = giftTypeData.toStyle().color,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = gift.giftName,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = gift.contactName,
                    fontSize = 13.sp,
                    color = OnSurface
                )
                Text(
                    text = " · ",
                    fontSize = 13.sp,
                    color = TextGray
                )
                Text(
                    text = dateFormat(gift.date),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TextGray
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                tapeId,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = TextGray.copy(alpha = AnimationTokens.Alpha.half)
            )
            Spacer(modifier = Modifier.height(4.dp))
            gift.amount?.let { amt ->
                Text(
                    "¥${amt.roundToInt()}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextGray
                )
            } ?: gift.occasion?.let { occ ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(SignalAmber.copy(alpha = AnimationTokens.Alpha.faint))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        occ,
                        fontSize = 10.sp,
                        color = SignalAmber
                    )
                }
            }
        }
    }
}

// ── 磁带窗口（播放器风格卷轴）──
@Composable
private fun TapeWindowWithPlayerReel(
    gift: GiftRecord,
    giftTypeData: GiftType,
    reelRotation: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.5.dp, TapeWindow, RoundedCornerShape(8.dp))
            .padding(vertical = 14.dp, horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerStyleReel(
                color = giftTypeData.toStyle().color,
                rotation = reelRotation,
                size = 44.dp
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .height(2.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    giftTypeData.toStyle().color.copy(alpha = 0.3f),
                                    giftTypeData.toStyle().color.copy(alpha = AnimationTokens.Alpha.visible),
                                    giftTypeData.toStyle().color.copy(alpha = 0.3f)
                                )
                            )
                        )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(TapeWindow)
                        .border(1.dp, TapeGearColor, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (gift.isSent) "REC" else "PLAY",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (gift.isSent) SignalCoral else SignalGreen
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .height(2.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    giftTypeData.toStyle().color.copy(alpha = 0.3f),
                                    giftTypeData.toStyle().color.copy(alpha = AnimationTokens.Alpha.visible),
                                    giftTypeData.toStyle().color.copy(alpha = 0.3f)
                                )
                            )
                        )
                )
            }

            PlayerStyleReel(
                color = giftTypeData.toStyle().color,
                rotation = reelRotation + 180f,
                size = 44.dp
            )
        }
    }
}

// ── 播放器风格卷轴（6齿 + 内圈磁带）──
@Composable
private fun PlayerStyleReel(
    color: Color,
    rotation: Float,
    size: Dp
) {
    val tapeGearColor = TapeGearColor
    val tapeGearDarkColor = TapeGearDarkColor
    val tapeWindow = TapeWindow
    Box(
        modifier = Modifier
            .size(size)
            .drawBehind {
                val s = this.size
                val cx = s.width / 2
                val cy = s.height / 2
                val r = s.width / 2 - 1

                drawCircle(
                    color = tapeGearColor.copy(alpha = 0.4f),
                    radius = r,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.5f)
                )

                rotate(rotation, pivot = Offset(cx, cy)) {
                    for (i in 0 until 6) {
                        val angle = (i * 60f) * (Math.PI / 180f)
                        val innerR = r * 0.3f
                        val outerR = r * 0.85f
                        val sx = cx + kotlin.math.cos(angle).toFloat() * innerR
                        val sy = cy + kotlin.math.sin(angle).toFloat() * innerR
                        val ex = cx + kotlin.math.cos(angle).toFloat() * outerR
                        val ey = cy + kotlin.math.sin(angle).toFloat() * outerR
                        drawLine(
                            color = color.copy(alpha = AnimationTokens.Alpha.half),
                            start = Offset(sx, sy),
                            end = Offset(ex, ey),
                            strokeWidth = 2.5f
                        )
                    }

                    drawCircle(
                        color = color.copy(alpha = 0.2f),
                        radius = r * 0.55f,
                        center = Offset(cx, cy),
                        style = Stroke(width = 5f)
                    )
                }

                drawCircle(
                    color = tapeGearDarkColor,
                    radius = r * 0.18f,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = tapeWindow,
                    radius = r * 0.08f,
                    center = Offset(cx, cy)
                )
            }
    )
}
