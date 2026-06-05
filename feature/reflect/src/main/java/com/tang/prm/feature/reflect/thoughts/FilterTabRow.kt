package com.tang.prm.feature.reflect.thoughts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.ui.theme.SignalAmber

@Composable
internal fun FilterTabRow(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    counts: Map<String, Int> = emptyMap()
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(filterOptions, key = { it.key }) { option ->
            val count = counts[option.key]
            FilterChip(
                selected = selectedFilter == option.key,
                onClick = { onFilterSelected(option.key) },
                label = {
                    Text(
                        buildString {
                            append(option.label)
                            if (count != null) append(" $count")
                        },
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SignalAmber,
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = SignalAmber,
                    enabled = true,
                    selected = selectedFilter == option.key
                )
            )
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
}
