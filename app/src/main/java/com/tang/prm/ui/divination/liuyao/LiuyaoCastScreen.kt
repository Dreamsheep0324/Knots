@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.ui.divination.liuyao

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tang.prm.ui.divination.DivinationViewModel
import com.tang.prm.ui.divination.components.HexagramDisplay
import com.tang.prm.ui.divination.components.YaoDisplayData
import com.tang.prm.ui.navigation.Screen
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.SignalGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val YAO_LABELS = listOf("初", "二", "三", "四", "五", "上")

@Composable
fun LiuyaoCastScreen(
    navController: NavController,
    viewModel: DivinationViewModel = hiltViewModel(
        viewModelStoreOwner = navController.getBackStackEntry(Screen.Divination.route)
    )
) {
    val currentYaoIndex by viewModel.currentYaoIndex.collectAsState()
    val yaoResults by viewModel.yaoResults.collectAsState()
    val lastCoinFlips by viewModel.lastCoinFlips.collectAsState()
    val allDone = currentYaoIndex >= 6

    var isCasting by remember { mutableStateOf(false) }
    val coinScaleY = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    val infiniteTransition = rememberInfiniteTransition(label = "coin_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        CenterAlignedTopAppBar(
            title = {
                StepIndicator(currentStep = 2, totalSteps = 3)
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            },
            actions = {
                Spacer(modifier = Modifier.width(48.dp))
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent
            )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.paddingPage)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            ProgressRow(currentYaoIndex = currentYaoIndex)

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier.height(84.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (yaoResults.isEmpty()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "诚心默念所问之事",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "然后掷币起卦",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        val yaoDataList = yaoResults.mapIndexed { index, value ->
                            val isYang = value == 7 || value == 9
                            val isChanging = value == 6 || value == 9
                            YaoDisplayData(
                                position = index + 1,
                                isYang = isYang,
                                isChanging = isChanging,
                                leftLabel = YAO_LABELS[index],
                                rightInfo = ""
                            )
                        }
                        HexagramDisplay(yaoData = yaoDataList)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (allDone) "六爻已成" else if (currentYaoIndex > 0) "${YAO_LABELS[currentYaoIndex - 1]}爻" else "准备起卦",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (allDone) SignalGreen else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (yaoResults.isNotEmpty()) {
                        LastResultIndicator(
                            yaoResults = yaoResults,
                            lastCoinFlips = lastCoinFlips
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    CoinArea(
                        isCasting = isCasting,
                        coinScaleY = coinScaleY.value,
                        lastCoinFlips = lastCoinFlips,
                        glowAlpha = glowAlpha
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
            }

            if (!allDone) {
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        if (!isCasting) {
                            scope.launch {
                                isCasting = true
                                coinScaleY.animateTo(
                                    targetValue = 0f,
                                    animationSpec = tween(150, easing = FastOutLinearInEasing)
                                )
                                delay(80)
                                viewModel.castNextYao()
                                coinScaleY.animateTo(
                                    targetValue = 1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                                isCasting = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface),
                    enabled = !isCasting
                ) {
                    Text(
                        text = when {
                            isCasting -> "掷币中…"
                            yaoResults.isEmpty() -> "掷币起卦"
                            else -> "继续掷币"
                        },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.resetLiuyao()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface)
                    ) {
                        Text(
                            text = "重新起卦",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.generateLiuyaoResult()
                            navController.navigate(Screen.LiuyaoResult.route)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.onSurface,
                            contentColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface)
                    ) {
                        Text(
                            text = "查看卦象 →",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StepIndicator(currentStep: Int, totalSteps: Int = 3) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        for (step in 1..totalSteps) {
            val isActive = step == currentStep
            val isCompleted = step < currentStep
            val color = when {
                isActive -> MaterialTheme.colorScheme.onSurface
                isCompleted -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.outline
            }
            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(color))
            if (step < totalSteps) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(modifier = Modifier.width(16.dp).height(1.dp).background(
                    if (isCompleted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                ))
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}

@Composable
private fun ProgressRow(currentYaoIndex: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0..5) {
            val isDone = i < currentYaoIndex
            val isCurrent = i == currentYaoIndex && currentYaoIndex > 0

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val progressOutlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    Canvas(modifier = Modifier.matchParentSize()) {
                        drawCircle(
                            color = when {
                                isDone -> SignalGreen
                                isCurrent -> Color.Transparent
                                else -> progressOutlineColor
                            }
                        )
                    }
                    if (isCurrent) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            drawCircle(
                                color = SignalGreen.copy(alpha = 0.2f)
                            )
                        }
                        Canvas(modifier = Modifier.matchParentSize()) {
                            drawCircle(
                                color = Color.Transparent,
                                radius = size.minDimension / 2
                            )
                            drawCircle(
                                color = SignalGreen,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                            )
                        }
                    }
                    Text(
                        text = YAO_LABELS[i],
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            isDone -> Color.White
                            isCurrent -> SignalGreen
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LastResultIndicator(yaoResults: List<Int>, lastCoinFlips: List<Int>) {
    if (yaoResults.isEmpty()) return

    val lastValue = yaoResults.last()
    val symbol = when (lastValue) {
        6 -> "⚋"
        7 -> "⚊"
        8 -> "⚋"
        9 -> "⚊"
        else -> "⚊"
    }
    val label = when (lastValue) {
        6 -> "老阴 动爻"
        7 -> "少阳"
        8 -> "少阴"
        9 -> "老阳 动爻"
        else -> ""
    }
    val isChanging = lastValue == 6 || lastValue == 9

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (lastCoinFlips.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                lastCoinFlips.forEachIndexed { index, flip ->
                    val isHeads = flip == 3
                    Surface(
                        shape = CircleShape,
                        color = if (isHeads) SignalGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (isHeads) "正" else "反",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isHeads) SignalGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (index < lastCoinFlips.size - 1) {
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = symbol,
                fontSize = 16.sp,
                color = if (isChanging) SignalGreen else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = if (isChanging) SignalGreen else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CoinArea(
    isCasting: Boolean,
    coinScaleY: Float,
    lastCoinFlips: List<Int>,
    glowAlpha: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0..2) {
            val coinResult = lastCoinFlips.getOrNull(i)
            val isHeads = coinResult == 3

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .graphicsLayer {
                        scaleY = if (isCasting) coinScaleY else 1f
                        scaleX = if (isCasting) coinScaleY * 0.3f + 0.7f else 1f
                    }
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = when {
                                isCasting -> listOf(
                                    Color(0xFF8A8A8A).copy(alpha = 0.8f),
                                    Color(0xFF5A5A5A).copy(alpha = 0.8f)
                                )
                                coinResult != null && isHeads -> listOf(
                                    SignalGreen.copy(alpha = 0.6f),
                                    SignalGreen.copy(alpha = 0.3f)
                                )
                                coinResult != null -> listOf(
                                    Color(0xFF9A9A9A).copy(alpha = 0.7f),
                                    Color(0xFF6A6A6A).copy(alpha = 0.5f)
                                )
                                else -> listOf(
                                    Color(0xFF7A7A7A).copy(alpha = glowAlpha),
                                    Color(0xFF4A4A4A).copy(alpha = glowAlpha * 0.7f)
                                )
                            },
                            center = Offset(size.width * 0.35f, size.height * 0.35f),
                            radius = size.minDimension
                        )
                    )
                }
                Text(
                    text = when {
                        isCasting -> "币"
                        coinResult != null -> if (isHeads) "正" else "反"
                        else -> "币"
                    },
                    fontFamily = FontFamily.Monospace,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isCasting -> Color.White.copy(alpha = 0.5f)
                        coinResult != null && isHeads -> Color.White
                        coinResult != null -> Color.White.copy(alpha = 0.9f)
                        else -> Color.White.copy(alpha = 0.7f)
                    }
                )
            }

            if (i < 2) {
                Spacer(modifier = Modifier.width(20.dp))
            }
        }
    }
}
