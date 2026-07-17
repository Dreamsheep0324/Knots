package com.tang.prm.feature.divination.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.ui.components.TerminalSectionHeader
import com.tang.prm.ui.theme.SignalCoral

@Composable
internal fun RecordDetailFooter(
    aiAnalysis: String,
    onDelete: () -> Unit
) {
    Spacer(modifier = Modifier.height(24.dp))

    if (aiAnalysis.isNotBlank()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            border = BorderStroke(0.5.dp, androidx.compose.material3.MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                TerminalSectionHeader("AI解读")

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = aiAnalysis,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    OutlinedButton(
        onClick = onDelete,
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = RoundedCornerShape(10.dp),
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = SignalCoral
        ),
        border = BorderStroke(1.dp, SignalCoral.copy(alpha = 0.5f))
    ) {
        Text(
            text = "删除此记录",
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp
        )
    }

    Spacer(modifier = Modifier.height(32.dp))
}
