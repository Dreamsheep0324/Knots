package com.tang.prm.feature.profile

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.model.BackupFileInfo
import com.tang.prm.domain.util.DateUtils

@Composable
internal fun BackupFileItem(
    fileInfo: BackupFileInfo,
    isDeleting: Boolean,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = shape
            )
            .clickable(enabled = !isDeleting) { onRestore() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 文件信息
        Column(modifier = Modifier.weight(1f)) {
            Text(
                DateUtils.formatDateTimeHyphen(fileInfo.timestamp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.size(2.dp))
            Text(
                formatBackupFileSize(fileInfo.fileSize),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 删除按钮
        IconButton(
            onClick = onDelete,
            enabled = !isDeleting,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.DeleteOutline,
                contentDescription = "删除",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
internal fun SuccessTip(message: String) {
    val successColor = MaterialTheme.colorScheme.primary
    val successContainerColor = MaterialTheme.colorScheme.primaryContainer
    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = successContainerColor
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = successColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(message, color = successColor, fontSize = 12.sp)
        }
    }
}

@Composable
internal fun ErrorTip(message: String?) {
    if (message == null) return
    val errorColor = MaterialTheme.colorScheme.error
    val errorContainerColor = MaterialTheme.colorScheme.errorContainer
    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = errorContainerColor
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = errorColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(message, color = errorColor, fontSize = 12.sp)
        }
    }
}

internal fun formatBackupFileSize(size: Long): String {
    if (size <= 0) return "0 B"
    if (size < 1024) return "$size B"
    if (size < 1024 * 1024) return "%.1f KB".format(size / 1024.0)
    if (size < 1024L * 1024 * 1024) return "%.1f MB".format(size / (1024.0 * 1024))
    return "%.1f GB".format(size / (1024.0 * 1024 * 1024))
}
