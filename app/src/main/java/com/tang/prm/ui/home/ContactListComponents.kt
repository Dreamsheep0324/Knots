package com.tang.prm.ui.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.animation.core.rememberPausableInfiniteValue
import com.tang.prm.ui.animation.primitives.rememberBreathingPulse
import com.tang.prm.ui.theme.DarkOnSurfaceVariant
import com.tang.prm.ui.theme.DarkOutline
import com.tang.prm.ui.theme.SignalPurple

internal val TerminalTextDim: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkOnSurfaceVariant else Color(0xFF64748B)
internal val TerminalTextMuted: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkOnSurfaceVariant else Color(0xFF94A3B8)
private val TerminalGrid: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkOutline else Color(0xFFE2E8F0)

@Composable
internal fun TerminalPrompt() {
    val dotAlpha by rememberBreathingPulse(
        minAlpha = 0.4f, maxAlpha = 1f,
        cycleDuration = AnimationTokens.Duration.dramatic
    )
    Box(
        modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(SignalPurple.copy(alpha = dotAlpha))
    )
}

@Composable
internal fun TerminalGridBackground() {
    val gridColor = TerminalGrid.copy(alpha = 0.3f)
    Canvas(modifier = Modifier.fillMaxSize()) {
        val gridSize = 40.dp.toPx()
        val width = size.width
        val height = size.height

        for (x in 0..(width / gridSize).toInt()) {
            drawLine(
                color = gridColor,
                start = Offset(x * gridSize, 0f),
                end = Offset(x * gridSize, height),
                strokeWidth = 0.5f
            )
        }
        for (y in 0..(height / gridSize).toInt()) {
            drawLine(
                color = gridColor,
                start = Offset(0f, y * gridSize),
                end = Offset(width, y * gridSize),
                strokeWidth = 0.5f
            )
        }
    }
}

@Composable
internal fun TerminalSearchBar(query: String, onQueryChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            ">",
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            color = SignalPurple,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(8.dp))
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text("搜索圈子或成员...", fontFamily = FontFamily.Monospace, color = TerminalTextMuted, fontSize = 13.sp)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(2.dp),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedBorderColor = SignalPurple
            )
        )
    }
}

@Composable
internal fun TerminalDivider(color: Color = Color.Unspecified) {
    val dividerColor = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.outline
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(dividerColor.copy(alpha = 0.3f))
    )
}

@Composable
internal fun TerminalLoading() {
    val dotCount by rememberPausableInfiniteValue(
        initialValue = 1, targetValue = 4,
        typeConverter = androidx.compose.animation.core.TwoWayConverter({ androidx.compose.animation.core.AnimationVector1D(it.toFloat()) }, { it.value.toInt() }),
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Restart),
        label = "dots"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "> 加载中${".".repeat(dotCount)}",
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            color = TerminalTextDim
        )
    }
}


