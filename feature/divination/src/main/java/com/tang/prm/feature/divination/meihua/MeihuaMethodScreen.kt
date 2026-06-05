@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.divination.meihua

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tang.prm.feature.divination.DivinationViewModel
import com.tang.prm.feature.divination.MeihuaMethodType
import com.tang.prm.ui.navigation.DivinationRoute
import com.tang.prm.ui.navigation.ExternalOmenRoute
import com.tang.prm.ui.navigation.MeihuaResultRoute
import com.tang.prm.ui.theme.Dimens

@Composable
fun MeihuaMethodScreen(
    navController: NavController,
    viewModel: DivinationViewModel = hiltViewModel(
        viewModelStoreOwner = navController.getBackStackEntry(DivinationRoute)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showNumberDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        CenterAlignedTopAppBar(
            title = { StepIndicator(currentStep = 2, totalSteps = 3) },
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
            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "☯",
                    fontSize = 130.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "选择起卦方式",
                fontSize = 20.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 4.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "邵氏心易 · 四法起卦",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MethodCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Schedule,
                    name = "时间",
                    desc = "年月日时",
                    selected = uiState.selectedMeihuaMethod is MeihuaMethodType.Time,
                    onClick = { viewModel.selectMeihuaMethod(MeihuaMethodType.Time) }
                )
                MethodCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Pin,
                    name = "数字",
                    desc = "输入两组数",
                    selected = uiState.selectedMeihuaMethod is MeihuaMethodType.Number,
                    onClick = { viewModel.selectMeihuaMethod(MeihuaMethodType.Number) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MethodCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Shuffle,
                    name = "随机",
                    desc = "随机起卦",
                    selected = uiState.selectedMeihuaMethod is MeihuaMethodType.Random,
                    onClick = { viewModel.selectMeihuaMethod(MeihuaMethodType.Random) }
                )
                MethodCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Visibility,
                    name = "外应",
                    desc = "观象取卦",
                    selected = uiState.selectedMeihuaMethod is MeihuaMethodType.External,
                    onClick = { viewModel.selectMeihuaMethod(MeihuaMethodType.External) }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = {
                    when (uiState.selectedMeihuaMethod) {
                        is MeihuaMethodType.External -> {
                            navController.navigate(ExternalOmenRoute)
                        }
                        is MeihuaMethodType.Number -> {
                            if (uiState.numberInput.isEmpty() || uiState.numberInputB.isEmpty()) {
                                showNumberDialog = true
                            } else {
                                viewModel.generateMeihuaResult()
                                if (uiState.inputError.isNotEmpty()) {
                                    showNumberDialog = true
                                } else {
                                    navController.navigate(MeihuaResultRoute)
                                }
                            }
                        }
                        else -> {
                            viewModel.generateMeihuaResult()
                            navController.navigate(MeihuaResultRoute)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface)
            ) {
                Text(
                    text = "下一步 →",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showNumberDialog) {
        NumberInputDialog(
            currentValueA = uiState.numberInput,
            currentValueB = uiState.numberInputB,
            onValueChangeA = { viewModel.updateNumberInput(it) },
            onValueChangeB = { viewModel.updateNumberInputB(it) },
            onDismiss = { showNumberDialog = false },
            onConfirm = {
                showNumberDialog = false
                viewModel.generateMeihuaResult()
                navController.navigate(MeihuaResultRoute)
            }
        )
    }
}
