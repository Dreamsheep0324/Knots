package com.tang.prm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tang.prm.ui.theme.Error
import com.tang.prm.ui.theme.Primary

@Composable
fun DeleteConfirmDialog(
    title: String = "确认删除",
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AppConfirmDialog(
        title = title,
        text = message,
        confirmLabel = "删除",
        isDestructive = true,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
fun DiscardEditDialog(
    onDiscard: () -> Unit,
    onDismiss: () -> Unit
) {
    AppConfirmDialog(
        title = "放弃编辑",
        text = "你有未保存的更改，确定要退出吗？",
        confirmLabel = "放弃",
        isDestructive = true,
        onConfirm = onDiscard,
        onDismiss = onDismiss
    )
}

@Composable
fun AppConfirmDialog(
    title: String,
    text: String,
    confirmLabel: String = "确认",
    dismissLabel: String = "取消",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            border = null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 图标
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (isDestructive) Error.copy(alpha = 0.1f) else Primary.copy(alpha = 0.1f),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isDestructive) Icons.Default.Warning else Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                        tint = if (isDestructive) Error else Primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 标题
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 内容
                Text(
                    text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 按钮行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 取消按钮
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(dismissLabel, fontWeight = FontWeight.Medium)
                    }

                    // 确认按钮
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDestructive) Error else Primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(confirmLabel, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
