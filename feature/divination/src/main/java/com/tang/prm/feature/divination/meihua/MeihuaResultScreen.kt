@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.divination.meihua

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tang.prm.ui.components.EmptyState
import com.tang.prm.feature.divination.DivinationViewModel
import com.tang.prm.feature.divination.AiViewModel
import com.tang.prm.feature.divination.components.ResultHexagramDisplay
import com.tang.prm.feature.divination.components.ShallowDeepTabSwitcher
import com.tang.prm.ui.navigation.DivinationRoute
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.SignalGreen

@Composable
fun MeihuaResultScreen(
    navController: NavController,
    viewModel: DivinationViewModel = hiltViewModel(
        viewModelStoreOwner = navController.getBackStackEntry(DivinationRoute)
    ),
    aiViewModel: AiViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val data = uiState.meihuaData

    if (data == null) {
        Column(modifier = Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = { StepIndicator(currentStep = 3, totalSteps = 3) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack(DivinationRoute, inclusive = false) }) {
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
            EmptyState(
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                title = "暂无梅花结果",
                actionLabel = "返回",
                onAction = { navController.popBackStack(DivinationRoute, inclusive = false) }
            )
        }
        return
    }

    val meihua = checkNotNull(data) { "梅花数据不应为空" }

    LaunchedEffect(meihua) {
        viewModel.saveResult()
    }

    var showDeep by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        CenterAlignedTopAppBar(
            title = { StepIndicator(currentStep = 3, totalSteps = 3) },
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
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                val yaoDisplayData = buildMeihuaYaoDisplayData(meihua)
                val changingNote = "${meihua.movingYao.yaoName} 动 → ${meihua.changedName}"

                ResultHexagramDisplay(
                    yaoData = yaoDisplayData,
                    changingNote = changingNote
                )

                Spacer(modifier = Modifier.height(20.dp))

                MeihuaHexagramInfoSection(data = meihua)

                Spacer(modifier = Modifier.height(20.dp))

                MeihuaSectionHeader(title = "六爻详情")

                Spacer(modifier = Modifier.height(8.dp))

                YaoDetailTable(meihua.yaosDetail, meihua)

                Spacer(modifier = Modifier.height(20.dp))

                MeihuaSectionHeader(title = "事之始终")

                Spacer(modifier = Modifier.height(8.dp))

                RelationMap(meihua)

                Spacer(modifier = Modifier.height(20.dp))

                ShallowDeepTabSwitcher(
                    showDeep = showDeep,
                    onTabChange = { showDeep = it },
                    accentColor = SignalGreen
                )

                if (showDeep) {
                    MeihuaAiDeepSection(
                        meihuaData = meihua,
                        viewModel = aiViewModel,
                        onQuestionChange = { viewModel.updateQuestion(it) },
                        onAnalysisComplete = { viewModel.saveAiAnalysis(it) }
                    )
                } else {
                    InterpretationSection(meihua)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            BottomNav(
                onReset = {
                    viewModel.resetMeihua()
                    navController.popBackStack(DivinationRoute, inclusive = false)
                },
                onShare = {
                    val activity = navController.context as? android.app.Activity
                    if (activity != null) {
                        shareScreenCapture(activity)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
