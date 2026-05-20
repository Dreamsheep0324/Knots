@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.ui.divination.meihua

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tang.prm.ui.divination.DivinationViewModel
import com.tang.prm.ui.divination.MeihuaMethodType
import com.tang.prm.ui.navigation.Screen
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.SignalGreen

@Composable
fun MeihuaMethodScreen(
    navController: NavController,
    viewModel: DivinationViewModel = hiltViewModel(
        viewModelStoreOwner = navController.getBackStackEntry(Screen.Divination.route)
    )
) {
    val selectedMethod = viewModel.selectedMeihuaMethod
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
                selected = selectedMethod is MeihuaMethodType.Time,
                onClick = { viewModel.selectMeihuaMethod(MeihuaMethodType.Time) }
            )
            MethodCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Pin,
                name = "数字",
                desc = "输入两组数",
                selected = selectedMethod is MeihuaMethodType.Number,
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
                selected = selectedMethod is MeihuaMethodType.Random,
                onClick = { viewModel.selectMeihuaMethod(MeihuaMethodType.Random) }
            )
            MethodCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Visibility,
                name = "外应",
                desc = "观象取卦",
                selected = selectedMethod is MeihuaMethodType.External,
                onClick = { viewModel.selectMeihuaMethod(MeihuaMethodType.External) }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = {
                when (selectedMethod) {
                    is MeihuaMethodType.External -> {
                        navController.navigate(Screen.ExternalOmen.route)
                    }
                    is MeihuaMethodType.Number -> {
                        if (viewModel.numberInput.isEmpty() || viewModel.numberInputB.isEmpty()) {
                            showNumberDialog = true
                        } else {
                            viewModel.generateMeihuaResult()
                            if (viewModel.inputError.isNotEmpty()) {
                                showNumberDialog = true
                            } else {
                                navController.navigate(Screen.MeihuaResult.route)
                            }
                        }
                    }
                    else -> {
                        viewModel.generateMeihuaResult()
                        navController.navigate(Screen.MeihuaResult.route)
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
            currentValueA = viewModel.numberInput,
            currentValueB = viewModel.numberInputB,
            onValueChangeA = { viewModel.updateNumberInput(it) },
            onValueChangeB = { viewModel.updateNumberInputB(it) },
            onDismiss = { showNumberDialog = false },
            onConfirm = {
                showNumberDialog = false
                viewModel.generateMeihuaResult()
                navController.navigate(Screen.MeihuaResult.route)
            }
        )
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
private fun MethodCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    name: String,
    desc: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
    val contentColor = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                modifier = Modifier.size(28.dp),
                tint = contentColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = desc,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NumberInputDialog(
    currentValueA: String,
    currentValueB: String,
    onValueChangeA: (String) -> Unit,
    onValueChangeB: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "输入数字",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = currentValueA,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.all { it.isDigit() }) {
                            onValueChangeA(input)
                        }
                    },
                    placeholder = {
                        Text(
                            text = "A数（上卦），需大于0",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    isError = currentValueA.isNotEmpty() && currentValueA.toIntOrNull()?.let { it <= 0 } != false,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = currentValueB,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.all { it.isDigit() }) {
                            onValueChangeB(input)
                        }
                    },
                    placeholder = {
                        Text(
                            text = "B数（下卦），需大于0",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    isError = currentValueB.isNotEmpty() && currentValueB.toIntOrNull()?.let { it <= 0 } != false,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = currentValueA.isNotEmpty() && currentValueB.isNotEmpty()
                        && currentValueA.toIntOrNull()?.let { it > 0 } == true
                        && currentValueB.toIntOrNull()?.let { it > 0 } == true
            ) {
                Text(
                    text = "确定",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "取消",
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    )
}
