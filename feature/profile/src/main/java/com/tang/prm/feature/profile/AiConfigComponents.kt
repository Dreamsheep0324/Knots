package com.tang.prm.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.ui.theme.Primary
import com.tang.prm.ui.theme.SceneOrange
import com.tang.prm.ui.theme.Success

internal data class ModelOption(
    val id: String,
    val name: String,
    val description: String,
    val tag: String? = null
)

internal val deepseekModels = listOf(
    ModelOption(
        id = "deepseek-v4-flash",
        name = "DeepSeek V4 Flash",
        description = "快速响应，适合日常解读",
        tag = "推荐"
    ),
    ModelOption(
        id = "deepseek-v4-pro",
        name = "DeepSeek V4 Pro",
        description = "深度推理，适合复杂断卦"
    )
)

@Composable
internal fun ApiKeySection(
    apiKeyInput: String,
    showApiKey: Boolean,
    onApiKeyInputChange: (String) -> Unit,
    onToggleShowApiKey: () -> Unit,
    onSave: () -> Unit,
    hasChanges: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "API密钥",
                style = MaterialTheme.typography.titleSmall,
                color = Primary,
                fontWeight = FontWeight.Medium
            )

            OutlinedTextField(
                value = apiKeyInput,
                onValueChange = onApiKeyInputChange,
                label = { Text("输入API Key") },
                placeholder = { Text("sk-...", fontSize = 12.sp) },
                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = onToggleShowApiKey) {
                        Icon(
                            if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showApiKey) "隐藏" else "显示",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (hasChanges) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        "保存",
                        color = Primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clickable { onSave() }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun BaseUrlSection(
    baseUrlInput: String,
    onBaseUrlInputChange: (String) -> Unit,
    onSave: () -> Unit,
    hasChanges: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "API地址",
                style = MaterialTheme.typography.titleSmall,
                color = Primary,
                fontWeight = FontWeight.Medium
            )

            OutlinedTextField(
                value = baseUrlInput,
                onValueChange = onBaseUrlInputChange,
                label = { Text("Base URL") },
                placeholder = { Text("https://api.deepseek.com", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (hasChanges) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        "保存",
                        color = Primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clickable { onSave() }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun ModelSelectionSection(
    aiModel: String,
    onModelSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "模型选择",
                style = MaterialTheme.typography.titleSmall,
                color = Primary,
                fontWeight = FontWeight.Medium
            )

            deepseekModels.forEach { model ->
                val selected = aiModel == model.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selected) Primary.copy(alpha = 0.06f) else Color.Transparent
                        )
                        .clickable { onModelSelected(model.id) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .background(
                                if (selected) Primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                model.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (model.tag != null) {
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    model.tag,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = when (model.tag) {
                                        "推荐" -> Success
                                        else -> SceneOrange
                                    },
                                    modifier = Modifier
                                        .background(
                                            when (model.tag) {
                                                "推荐" -> Success.copy(alpha = 0.1f)
                                                else -> SceneOrange.copy(alpha = 0.1f)
                                            },
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            model.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun TestConnectionSection(
    testState: TestConnectionState,
    apiKey: String,
    onTest: () -> Unit
) {
    OutlinedButton(
        onClick = onTest,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        enabled = testState !is TestConnectionState.Testing && apiKey.isNotBlank(),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Primary.copy(alpha = 0.06f),
            contentColor = Primary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = BorderStroke(1.dp, Primary.copy(alpha = if (testState !is TestConnectionState.Testing && apiKey.isNotBlank()) 0.5f else 0.2f))
    ) {
        Text(
            text = when (testState) {
                is TestConnectionState.Testing -> "测试中..."
                else -> "测试连接"
            },
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }

    when (testState) {
        is TestConnectionState.Success -> {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "✓ ${testState.message}",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = Success
            )
        }
        is TestConnectionState.Error -> {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "✗ ${testState.message}",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.error
            )
        }
        else -> {}
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        "提示：API密钥可在 platform.deepseek.com 申请，费用按token计费，单次解读约¥0.003",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}
