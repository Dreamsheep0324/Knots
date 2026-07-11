@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.divination.meihua

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.engine.divination.model.MeihuaData
import com.tang.prm.ui.components.AppDatePicker
import com.tang.prm.feature.divination.AiDeepState
import com.tang.prm.feature.divination.AiViewModel
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.domain.util.DateUtils

@Composable
fun MeihuaAiDeepSection(
    meihuaData: MeihuaData,
    viewModel: AiViewModel,
    onQuestionChange: (String) -> Unit = {},
    onAnalysisComplete: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val apiKeyConfigured by viewModel.apiKeyConfigured.collectAsStateWithLifecycle()
    val savedGender by viewModel.savedGender.collectAsStateWithLifecycle()
    val savedBirthDate by viewModel.savedBirthDate.collectAsStateWithLifecycle()

    var gender by remember { mutableStateOf(savedGender) }
    var birthDate by remember { mutableStateOf(savedBirthDate) }
    var question by remember { mutableStateOf("") }
    var genderExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(savedGender) { gender = savedGender }
    LaunchedEffect(savedBirthDate) { birthDate = savedBirthDate }
    LaunchedEffect(Unit) { viewModel.refreshApiKeyStatus() }
    LaunchedEffect(Unit) {
        viewModel.analysisCompleteEvent.collect { content -> onAnalysisComplete(content) }
    }

    val initialDateMillis = remember(birthDate) {
        if (birthDate.isNotBlank()) {
            try {
                val parts = birthDate.split("-")
                if (parts.size == 3) {
                    java.util.Calendar.getInstance().apply {
                        set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt(), 0, 0, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                    }.timeInMillis
                } else null
            } catch (_: Exception) { null }
        } else null
    }

    if (showDatePicker) {
        AppDatePicker(
            show = showDatePicker,
            onDismiss = { showDatePicker = false },
            onDateSelected = { millis ->
                val date = DateUtils.formatDate(millis)
                birthDate = date
                viewModel.saveBirthDate(date)
            },
            initialDate = initialDateMillis
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        when (state) {
            is AiDeepState.Idle, is AiDeepState.Error -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "补充信息",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ExposedDropdownMenuBox(
                                expanded = genderExpanded,
                                onExpandedChange = { genderExpanded = it },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = gender,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("性别", fontSize = 10.sp) },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded)
                                    },
                                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                    textStyle = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = genderExpanded,
                                    onDismissRequest = { genderExpanded = false },
                                    containerColor = MaterialTheme.colorScheme.surface
                                ) {
                                    listOf("男", "女").forEach { option ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    option,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = if (gender == option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                )
                                            },
                                            onClick = {
                                                gender = option
                                                viewModel.saveGender(option)
                                                genderExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1.5f)
                                    .clickable { showDatePicker = true }
                            ) {
                                OutlinedTextField(
                                    value = birthDate,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("出生日期", fontSize = 10.sp) },
                                    placeholder = { Text("点击选择", fontSize = 10.sp) },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    enabled = false,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        disabledContainerColor = Color.Transparent,
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = question,
                            onValueChange = {
                                question = it
                                onQuestionChange(it)
                            },
                            label = { Text("请输入你想问的问题", fontSize = 10.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 4,
                            minLines = 2,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )

                        if (state is AiDeepState.Error) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = (state as AiDeepState.Error).message,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                if (question.isNotBlank()) {
                                    viewModel.startMeihuaAnalysis(meihuaData, gender, birthDate, question)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            enabled = question.isNotBlank(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (question.isNotBlank()) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = if (question.isNotBlank()) 0.6f else 0.2f))
                        ) {
                            Text(
                                text = "✦ 开始解读",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (!apiKeyConfigured) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "提示：需先在设置中配置AI API密钥",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            is AiDeepState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AI解读中...",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = SignalGreen
                    )
                }
            }

            is AiDeepState.Streaming -> {
                val content = (state as AiDeepState.Streaming).content
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "AI解读中...",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SignalGreen
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = content + "▌",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            is AiDeepState.Result -> {
                val content = (state as AiDeepState.Result).content
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "AI解读",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SignalGreen
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = content,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { viewModel.reset() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "重新提问",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
