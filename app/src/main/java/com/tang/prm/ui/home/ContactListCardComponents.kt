package com.tang.prm.ui.home

import com.tang.prm.ui.animation.primitives.rememberBreathingPulse
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.model.CardRarity
import com.tang.prm.domain.model.Contact
import com.tang.prm.ui.theme.SignalGreen


@Composable
internal fun TerminalCardHeader(contact: Contact, rarity: CardRarity, rarityColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val blinkAlpha by rememberBreathingPulse(
                minAlpha = 0.2f, maxAlpha = 1f, cycleDuration = 800
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(SignalGreen.copy(alpha = blinkAlpha), CircleShape)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "ONLINE",
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                color = SignalGreen,
                fontWeight = FontWeight.Bold
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                rarity.label,
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
                color = TerminalTextMuted
            )
        }
    }
}

@Composable
internal fun TerminalScanLineOverlay(scanLineOffset: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val lineY = size.height * scanLineOffset
        drawLine(
            color = Color(0xFF00FF00).copy(alpha = 0.06f),
            start = Offset(0f, lineY),
            end = Offset(size.width, lineY),
            strokeWidth = 2f
        )

        for (y in 0 until size.height.toInt() step 4) {
            drawLine(
                color = Color.Black.copy(alpha = 0.02f),
                start = Offset(0f, y.toFloat()),
                end = Offset(size.width, y.toFloat()),
                strokeWidth = 1f
            )
        }
    }
}

@Composable
internal fun TerminalCornerBrackets(color: Color) {
    val bracketLength = 16.dp
    val bracketThickness = 2.dp

    Canvas(modifier = Modifier.fillMaxSize()) {
        val len = bracketLength.toPx()
        val thick = bracketThickness.toPx()
        val w = size.width
        val h = size.height

        drawLine(color, Offset(0f, 0f), Offset(len, 0f), thick)
        drawLine(color, Offset(0f, 0f), Offset(0f, len), thick)
        drawLine(color, Offset(w, 0f), Offset(w - len, 0f), thick)
        drawLine(color, Offset(w, 0f), Offset(w, len), thick)
        drawLine(color, Offset(0f, h), Offset(len, h), thick)
        drawLine(color, Offset(0f, h), Offset(0f, h - len), thick)
        drawLine(color, Offset(w, h), Offset(w - len, h), thick)
        drawLine(color, Offset(w, h), Offset(w, h - len), thick)
    }
}
