package com.tang.prm.feature.people.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tang.prm.domain.model.AppStrings
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.components.AppCard
import com.tang.prm.ui.theme.Error
import com.tang.prm.ui.theme.SceneOrange

internal val SectionIconSize = 36.dp
internal val SectionIconInnerSize = 18.dp

internal data class SectionStyle(val icon: ImageVector, val color: Color)

internal val sectionStyles = mapOf(
    "基本信息" to SectionStyle(Icons.Default.Person, Color(0xFF42A5F5)),
    "关系" to SectionStyle(Icons.Default.FavoriteBorder, Color(0xFFE65100)),
    "重要日期" to SectionStyle(Icons.Default.CalendarToday, Color(0xFF66BB6A)),
    "联系方式" to SectionStyle(Icons.Default.Phone, Color(0xFF4DD0E1)),
    "位置信息" to SectionStyle(Icons.Default.LocationOn, Color(0xFF9575CD)),
    "个人特征" to SectionStyle(Icons.Default.EmojiEmotions, Color(0xFFF43F5E)),
    "简介" to SectionStyle(Icons.Default.EditNote, Color(0xFF64748B)),
    "亲密度" to SectionStyle(Icons.Default.Favorite, SceneOrange)
)

internal data class IntimacyLevel(val range: IntRange, val name: String, val color: Color, val icon: ImageVector)

internal val IntimacyLevels = com.tang.prm.domain.model.IntimacyTier.entries.map { tier ->
    val color = Color(tier.colorValue)
    val icon = when (tier) {
        com.tang.prm.domain.model.IntimacyTier.NEW -> Icons.Default.PersonAdd
        com.tang.prm.domain.model.IntimacyTier.ACQUAINTANCE -> Icons.Default.PersonOutline
        com.tang.prm.domain.model.IntimacyTier.FRIEND -> Icons.Default.People
        com.tang.prm.domain.model.IntimacyTier.CLOSE -> Icons.Default.Favorite
        com.tang.prm.domain.model.IntimacyTier.FAMILY -> Icons.Default.FavoriteBorder
    }
    IntimacyLevel(tier.minScore..tier.maxScore, tier.label, color, icon)
}

@Composable
internal fun AddContactProfileHeader(avatar: String?, onAvatarClick: () -> Unit) {
    AppCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        val headerGradient = Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f), MaterialTheme.colorScheme.surface))
        Column(
            modifier = Modifier.fillMaxWidth()
                .background(headerGradient)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(100.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape).clickable(onClick = onAvatarClick),
                contentAlignment = Alignment.Center
            ) {
                if (avatar != null) {
                    AsyncImage(model = avatar, contentDescription = "头像", modifier = Modifier.size(100.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = "添加头像", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("点击上传", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier.width(32.dp).height(3.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
internal fun FormSection(title: String, action: (@Composable () -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val style = sectionStyles[title]
    AppCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                    if (style != null) {
                        Box(modifier = Modifier.size(SectionIconSize).background(style.color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(style.icon, contentDescription = null, tint = style.color, modifier = Modifier.size(SectionIconInnerSize))
                        }
                    }
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                action?.let { it() }
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
internal fun FormField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String = "", maxLines: Int = 1, required: Boolean = false, focusRequester: FocusRequester? = null) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            if (required) { Spacer(modifier = Modifier.width(2.dp)); Text("*", color = Error, fontSize = MaterialTheme.typography.bodyMedium.fontSize) }
        }
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth().then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier),
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half)) },
            singleLine = maxLines == 1, maxLines = maxLines, shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = AnimationTokens.Alpha.visible),
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
internal fun NotesField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
        placeholder = { Text("写下你对这个人的了解...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half)) },
        maxLines = 6, shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = AnimationTokens.Alpha.visible),
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half),
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    )
}

@Composable
internal fun DatePickerField(label: String, value: String?, onValueChange: (String?) -> Unit, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value ?: "", onValueChange = onValueChange, modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
            placeholder = { Text("请选择日期", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half)) },
            trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            singleLine = true, readOnly = true, enabled = false, shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half),
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half),
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
internal fun IntimacySlider(value: Int, onValueChange: (Int) -> Unit) {
    val currentLevel = IntimacyLevels.find { value in it.range } ?: IntimacyLevels[0]
    var selectedLevelIndex by remember { mutableIntStateOf(IntimacyLevels.indexOf(currentLevel)) }

    LaunchedEffect(value) {
        val newIndex = IntimacyLevels.indexOfFirst { value in it.range }.coerceIn(0, IntimacyLevels.size - 1)
        if (selectedLevelIndex != newIndex) selectedLevelIndex = newIndex
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IntimacyLevels.forEachIndexed { index, level ->
                IntimacyLevelCard(level = level, isSelected = selectedLevelIndex == index, onClick = { selectedLevelIndex = index; onValueChange(level.range.first + (level.range.last - level.range.first) / 2) }, modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().background(currentLevel.color.copy(alpha = 0.06f), RoundedCornerShape(12.dp)).padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(currentLevel.icon, contentDescription = null, tint = currentLevel.color, modifier = Modifier.size(20.dp))
                    Text(currentLevel.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = currentLevel.color)
                }
                Text("${value}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = currentLevel.color)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Slider(value = value.toFloat(), onValueChange = { newValue -> onValueChange(newValue.toInt()); selectedLevelIndex = IntimacyLevels.indexOfFirst { newValue.toInt() in it.range }.coerceIn(0, IntimacyLevels.size - 1) }, valueRange = 0f..100f, colors = SliderDefaults.colors(thumbColor = currentLevel.color, activeTrackColor = currentLevel.color, inactiveTrackColor = currentLevel.color.copy(alpha = 0.15f)))
    }
}

@Composable
internal fun IntimacyLevelCard(level: IntimacyLevel, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clip(RoundedCornerShape(10.dp)).background(if (isSelected) level.color else level.color.copy(alpha = AnimationTokens.Alpha.faint)).clickable(onClick = onClick).padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(level.icon, contentDescription = null, tint = if (isSelected) Color.White else level.color, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(level.name, style = MaterialTheme.typography.labelMedium, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) Color.White else level.color)
    }
}
