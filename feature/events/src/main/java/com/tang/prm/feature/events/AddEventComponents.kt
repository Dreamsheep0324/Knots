package com.tang.prm.feature.events

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CustomType
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.theme.InsightPink
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.getEmotionColor
import com.tang.prm.ui.theme.getEmotionIcon
import com.tang.prm.ui.theme.getWeatherColor
import com.tang.prm.ui.theme.getWeatherIcon
import com.tang.prm.ui.theme.toComposeColor

@Composable
internal fun SectionCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shadowElevation = 3.dp) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
internal fun WeatherSelector(
    items: List<CustomType>,
    selectedItem: String,
    onSelect: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp)) {
            Icon(Icons.Default.WbSunny, contentDescription = null, tint = SignalAmber, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("今天天气", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(horizontal = 4.dp)) {
            items(items, key = { it.id }) { item ->
                val isSelected = item.name == selectedItem
                val itemColor = item.color?.let {
                    it.toComposeColor(SignalAmber)
                } ?: SignalAmber
                val icon = getWeatherIcon(item.name)
                val animatedSize by animateDpAsState(
                    targetValue = if (isSelected) 56.dp else 48.dp,
                    animationSpec = tween(200), label = "weatherSize"
                )
                val animatedBorderWidth by animateDpAsState(
                    targetValue = if (isSelected) 2.5.dp else 0.dp,
                    animationSpec = tween(200), label = "weatherBorder"
                )
                val animatedIconSize by animateDpAsState(
                    targetValue = if (isSelected) 26.dp else 22.dp,
                    animationSpec = tween(200), label = "weatherIcon"
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(64.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSelect(if (isSelected) "" else item.name) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isSelected) {
                            Canvas(modifier = Modifier.size(64.dp)) {
                                drawRoundRect(
                                    color = itemColor.copy(alpha = AnimationTokens.Alpha.faint),
                                    cornerRadius = CornerRadius(20.dp.toPx())
                                )
                            }
                        }
                        Surface(
                            modifier = Modifier.size(animatedSize),
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) itemColor.copy(alpha = AnimationTokens.Alpha.subtle) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) BorderStroke(animatedBorderWidth, itemColor.copy(alpha = AnimationTokens.Alpha.half)) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (icon != null) {
                                    Icon(icon, contentDescription = null,
                                        tint = if (isSelected) itemColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(animatedIconSize))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(item.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) itemColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
internal fun EmotionSelector(
    items: List<CustomType>,
    selectedItem: String,
    onSelect: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp)) {
            Icon(Icons.Default.Favorite, contentDescription = null, tint = InsightPink, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("此刻心情", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(horizontal = 4.dp)) {
            items(items, key = { it.id }) { item ->
                val isSelected = item.name == selectedItem
                val itemColor = item.color?.let {
                    it.toComposeColor(SignalPurple)
                } ?: SignalPurple
                val icon = getEmotionIcon(item.name)
                val animatedSize by animateDpAsState(
                    targetValue = if (isSelected) 50.dp else 44.dp,
                    animationSpec = tween(200), label = "emotionSize"
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(58.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSelect(if (isSelected) "" else item.name) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isSelected) {
                            Canvas(modifier = Modifier.size(58.dp)) {
                                drawCircle(color = itemColor.copy(alpha = AnimationTokens.Alpha.faint))
                            }
                        }
                        Surface(
                            modifier = Modifier.size(animatedSize),
                            shape = CircleShape,
                            color = if (isSelected) itemColor.copy(alpha = AnimationTokens.Alpha.subtle) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) BorderStroke(2.dp, itemColor.copy(alpha = 0.4f)) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (icon != null) {
                                    Icon(icon, contentDescription = null,
                                        tint = if (isSelected) itemColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(if (isSelected) 24.dp else 20.dp))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(item.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) itemColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
internal fun LinedPaperField(value: String, onValueChange: (String) -> Unit, placeholder: String, minLines: Int = 3) {
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half)
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(surfaceVariantColor).border(1.dp, outlineColor, RoundedCornerShape(10.dp))) {
        Canvas(modifier = Modifier.matchParentSize().padding(start = 24.dp, top = 10.dp, end = 6.dp)) {
            val lineSpacingPx = 26.dp.toPx()
            for (i in 0..8) { drawLine(surfaceVariantColor, Offset(0f, i * lineSpacingPx), Offset(size.width, i * lineSpacingPx), 1f) }
            drawLine(outlineColor, Offset(0f, 0f), Offset(0f, size.height), 1.5f)
        }
        OutlinedTextField(value = value, onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().padding(start = 28.dp, end = 6.dp),
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontStyle = FontStyle.Italic) },
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, lineHeight = 26.sp),
            minLines = minLines,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent, cursorColor = MaterialTheme.colorScheme.primary))
    }
}

@Composable
internal fun PolaroidContact(contact: Contact, onRemove: () -> Unit) {
    Surface(modifier = Modifier.width(72.dp).rotate(if (contact.id % 2 == 0L) -1.5f else 1.5f),
        shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shadowElevation = 3.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(5.dp)) {
            Box(modifier = Modifier.size(50.dp)) {
                if (contact.avatar != null) {
                    AsyncImage(model = contact.avatar, contentDescription = null, modifier = Modifier.size(50.dp).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
                } else {
                    Box(modifier = Modifier.size(50.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = AnimationTokens.Alpha.faint)), contentAlignment = Alignment.Center) {
                        Text(contact.name.firstOrNull()?.toString() ?: "?", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
                IconButton(onClick = onRemove, modifier = Modifier.align(Alignment.TopEnd).size(16.dp).background(Color.Black.copy(alpha = 0.35f), CircleShape)) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(9.dp))
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(contact.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), fontSize = 10.sp)
        }
    }
}

@Composable
internal fun PolaroidAddButton(onClick: () -> Unit) {
    Surface(modifier = Modifier.width(72.dp).clickable(onClick = onClick), shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half))) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(5.dp), verticalArrangement = Arrangement.Center) {
            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier.size(50.dp), contentAlignment = Alignment.Center) { Icon(Icons.Default.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)) }
            Spacer(modifier = Modifier.height(3.dp))
            Text("添加", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
        }
    }
}

@Composable
internal fun PolaroidPhoto(photoUri: String, rotation: Float, onRemove: () -> Unit) {
    Surface(modifier = Modifier.rotate(rotation), shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shadowElevation = 3.dp) {
        Column(modifier = Modifier.padding(5.dp, 5.dp, 5.dp, 12.dp)) {
            Box(modifier = Modifier.size(80.dp)) {
                AsyncImage(model = photoUri, contentDescription = null, modifier = Modifier.size(80.dp).clip(RoundedCornerShape(3.dp)), contentScale = ContentScale.Crop)
                IconButton(onClick = onRemove, modifier = Modifier.align(Alignment.TopEnd).size(18.dp).background(Color.Black.copy(alpha = 0.4f), CircleShape)) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                }
            }
        }
    }
}

@Composable
internal fun PolaroidPhotoAddButton(onClick: () -> Unit) {
    Surface(modifier = Modifier.clickable(onClick = onClick).rotate(1f), shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shadowElevation = 3.dp) {
        Column(modifier = Modifier.padding(5.dp, 5.dp, 5.dp, 12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) { Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp)) }
        }
    }
}
