@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.divination.liuyao

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tang.prm.feature.divination.DivinationViewModel
import com.tang.prm.ui.navigation.LiuyaoResultRoute
import com.tang.prm.ui.navigation.DivinationRoute
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.SignalGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LiuyaoCastScreen(
    navController: NavController,
    viewModel: DivinationViewModel = hiltViewModel(
        viewModelStoreOwner = navController.getBackStackEntry(DivinationRoute)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allDone = uiState.currentYaoIndex >= 6

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

            ProgressRow(currentYaoIndex = uiState.currentYaoIndex)

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(1f))

                CastDisplaySection(
                    yaoResults = uiState.yaoResults,
                    lastCoinFlips = uiState.lastCoinFlips,
                    currentYaoIndex = uiState.currentYaoIndex,
                    allDone = allDone,
                    coinScaleY = coinScaleY.value,
                    glowAlpha = glowAlpha,
                    isCasting = isCasting
                )

                Spacer(modifier = Modifier.weight(1f))
            }

            CastActionsSection(
                allDone = allDone,
                isCasting = isCasting,
                yaoResults = uiState.yaoResults,
                onCast = {
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
                },
                onReset = { viewModel.resetLiuyao() },
                onViewResult = {
                    viewModel.generateLiuyaoResult()
                    navController.navigate(LiuyaoResultRoute)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
