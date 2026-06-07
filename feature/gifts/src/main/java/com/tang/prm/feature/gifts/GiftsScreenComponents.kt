@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.gifts

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.GiftType
import com.tang.prm.ui.components.ContactAvatar
import com.tang.prm.ui.theme.*
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.animation.primitives.rememberBreathingPulse
import com.tang.prm.ui.theme.toStyle
import com.tang.prm.ui.theme.DialogDefaults
import com.tang.prm.domain.model.AppStrings
import java.util.Locale

// ═══════════════════════════════════════════════════════════════
//  RETRO CONTROL PANEL — 复古控制面板
// ═══════════════════════════════════════════════════════════════
@Composable
internal fun RetroControlPanel(
    totalCount: Int,
    sentCount: Int,
    receivedCount: Int,
    typeBreakdown: Map<String, Int>
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SignalGreen))
                    Text("播放机就绪", fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SignalGreen)
                }
                Text(
                    "CASSETTE DECK",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.sp,
                    color = TextGray.copy(alpha = AnimationTokens.Alpha.visible),
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RetroMeter(modifier = Modifier.weight(1f), label = "A面 · 送出", count = sentCount, total = totalCount.coerceAtLeast(1), color = SignalAmber)
                RetroMeter(modifier = Modifier.weight(1f), label = "B面 · 收到", count = receivedCount, total = totalCount.coerceAtLeast(1), color = SignalGreen)
            }

            Spacer(modifier = Modifier.height(8.dp))
            RetroSpectrum(typeBreakdown = typeBreakdown)
        }
    }
}

@Composable
internal fun RetroMeter(
    modifier: Modifier = Modifier,
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    val progress = (count.toFloat() / total).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(AnimationTokens.Duration.dramatic), label = "meter")

    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TextGray)
            Text(String.format(Locale.US, "%02d", count), fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(2.dp)).background(DividerLight)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color.copy(alpha = AnimationTokens.Alpha.strong))
            )
        }
    }
}

@Composable
internal fun RetroSpectrum(typeBreakdown: Map<String, Int>) {
    if (typeBreakdown.isEmpty()) return
    val maxCount = typeBreakdown.values.maxOrNull() ?: 1

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.Bottom) {
        GiftType.entries.forEach { type ->
            val count = typeBreakdown[type.name] ?: 0
            val heightFraction = (count.toFloat() / maxCount).coerceIn(0.05f, 1f)
            val animatedHeight by animateFloatAsState(targetValue = heightFraction, animationSpec = tween(800), label = "spec")
            val style = type.toStyle()

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text(if (count > 0) "$count" else "", fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = style.color, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(1.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((animatedHeight * 20).dp)
                        .clip(RoundedCornerShape(topStart = 1.dp, topEnd = 1.dp))
                        .background(style.color.copy(alpha = if (count > 0) 0.6f else 0.1f))
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(type.displayName.first().toString(), fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = if (count > 0) style.color else TextGray.copy(alpha = 0.3f))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  TYPE FILTER STRIP
// ═══════════════════════════════════════════════════════════════
@Composable
internal fun TypeFilterStrip(
    gifts: List<GiftRecord>,
    selectedType: GiftType?,
    onTypeSelect: (GiftType?) -> Unit
) {
    val usedTypes = remember(gifts) {
        val used = gifts.map { it.giftType }.distinct()
        listOf(null) + GiftType.entries.filter { it in used }
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(usedTypes, key = { it?.name ?: "all" }) { type ->
            val isSelected = selectedType == type
            val typeColor = type?.toStyle()?.color ?: SignalElectric
            val count = if (type == null) gifts.size else gifts.count { it.giftType == type }

            Surface(
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onTypeSelect(if (isSelected) null else type) },
                color = if (isSelected) typeColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
                border = if (isSelected) BorderStroke(1.dp, typeColor.copy(alpha = AnimationTokens.Alpha.half)) else BorderStroke(0.5.dp, CardBorder.copy(alpha = AnimationTokens.Alpha.half))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (type != null) {
                        Icon(type.toStyle().icon, contentDescription = null, tint = if (isSelected) typeColor else TextGray, modifier = Modifier.size(12.dp))
                    } else {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = if (isSelected) SignalElectric else TextGray, modifier = Modifier.size(12.dp))
                    }
                    Text(
                        text = if (type != null) type.displayName else AppStrings.Tabs.ALL,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) typeColor else TextGray
                    )
                    Text(
                        text = String.format(Locale.US, "%02d", count),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = if (isSelected) typeColor else TextGray.copy(alpha = AnimationTokens.Alpha.visible)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  CASSETTE TAPE RACK — 磁带架
// ═══════════════════════════════════════════════════════════════
@Composable
internal fun CassetteTapeRack(
    gifts: List<GiftRecord>,
    onTapeClick: (GiftRecord) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(gifts, key = { it.id }) { gift ->
            CassetteTapeCard(
                gift = gift,
                index = gifts.indexOf(gift) + 1,
                onClick = { onTapeClick(gift) }
            )
        }
        item { Spacer(modifier = Modifier.height(96.dp)) }
    }
}

// ═══════════════════════════════════════════════════════════════
//  FILTER & DIALOGS
// ═══════════════════════════════════════════════════════════════
@Composable
internal fun GiftFilterDialog(
    contacts: List<Contact>,
    selectedContactId: Long?,
    onContactSelect: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DialogDefaults.containerColor,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        title = { Text("筛选磁带", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) },
        text = {
            Column {
                Text(text = "按人物筛选", style = MaterialTheme.typography.labelMedium, color = TextGray, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { onContactSelect(null) },
                    color = if (selectedContactId == null) SignalElectric.copy(alpha = 0.1f) else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = if (selectedContactId == null) SignalElectric else TextGray, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "全部磁带", color = if (selectedContactId == null) SignalElectric else MaterialTheme.colorScheme.onSurface)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(contacts, key = { it.id }) { contact ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).clip(RoundedCornerShape(8.dp)).clickable { onContactSelect(contact.id) },
                            color = if (selectedContactId == contact.id) SignalElectric.copy(alpha = 0.1f) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                ContactAvatar(avatar = contact.avatar, name = contact.name, size = 24)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = contact.name, color = if (selectedContactId == contact.id) SignalElectric else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("完成", color = SignalElectric) }
        }
    )
}
