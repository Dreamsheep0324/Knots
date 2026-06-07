package com.tang.prm.feature.chat.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tang.prm.ui.components.AppCard
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.Primary
import com.tang.prm.ui.theme.SceneOrange
import com.tang.prm.domain.util.DateUtils

@Composable
internal fun SceneInfoCard(
    title: String,
    selectedDate: Long,
    onTitleChange: (String) -> Unit,
    onDateClick: () -> Unit,
    titleFocusRequester: FocusRequester? = null
) {
    AppCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Dimens.paddingCard)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(3.dp).height(16.dp).background(SceneOrange, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.Movie, contentDescription = null, tint = SceneOrange, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("场景设定", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth().then(if (titleFocusRequester != null) Modifier.focusRequester(titleFocusRequester) else Modifier),
                label = { Text("对话标题") },
                placeholder = { Text("如：关于项目的讨论") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = Primary.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDateClick),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("对话日期", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            DateUtils.formatYearMonthDayChineseFull(selectedDate),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
