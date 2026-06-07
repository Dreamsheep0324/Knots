package com.tang.prm.feature.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tang.prm.ui.components.AppCard
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.SceneOrange

@Composable
internal fun RemarksCard(
    remarks: String,
    onRemarksChange: (String) -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Dimens.paddingCard)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(3.dp).height(16.dp).background(SceneOrange, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.EditNote, contentDescription = null, tint = SceneOrange, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("备注", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = remarks,
                onValueChange = onRemarksChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("给这次对话添加一些备注...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                minLines = 3,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = SceneOrange.copy(alpha = 0.3f)
                )
            )
        }
    }
}
