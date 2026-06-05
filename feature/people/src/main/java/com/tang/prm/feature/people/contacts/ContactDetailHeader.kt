package com.tang.prm.feature.people.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.AppStrings
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalSky
import com.tang.prm.domain.util.Zodiac
import com.tang.prm.domain.util.ZodiacUtils
import androidx.compose.ui.res.painterResource

@Composable
internal fun ProfileHeader(
    contact: Contact,
    relationshipTypes: List<CustomType> = emptyList(),
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val intimacyColor = getIntimacyColor(contact.intimacyScore)
    val intimacyLevel = getIntimacyLevel(contact.intimacyScore)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        intimacyColor.copy(alpha = AnimationTokens.Alpha.subtle),
                        intimacyColor.copy(alpha = AnimationTokens.Alpha.subtle),
                        intimacyColor.copy(alpha = 0.04f),
                        Color.Transparent
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "编辑", tint = MaterialTheme.colorScheme.onSurface)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.onSurface)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(3.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = contact.avatar,
                    contentDescription = contact.name,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = contact.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            contact.nickname?.takeIf { it.isNotBlank() }?.let { nickname ->
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "@$nickname",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            contact.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IntimacyBadge(score = contact.intimacyScore, color = intimacyColor, level = intimacyLevel)
                    contact.knowingDate?.let { DaysKnownBadge(knowingDate = it, color = intimacyColor) }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    contact.relationship?.takeIf { it.isNotBlank() }?.let { rel ->
                        val relColor = relationshipTypes.find { it.name == rel }?.color?.let { parseHexColor(it) } ?: SignalAmber
                        HeaderTagChip(text = rel, color = relColor, icon = Icons.Default.Link)
                    }
                    ZodiacUtils.fromBirthday(contact.birthday)?.let { zodiac ->
                        ZodiacTagChip(zodiac = zodiac)
                    }
                    contact.education?.takeIf { it.isNotBlank() }?.let {
                        HeaderTagChip(text = it, color = SignalSky, icon = Icons.Default.School)
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderTagChip(text: String, color: Color, icon: ImageVector? = null) {
    Surface(shape = RoundedCornerShape(20.dp), color = color.copy(alpha = AnimationTokens.Alpha.faint)) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ZodiacTagChip(zodiac: Zodiac) {
    val zodiacColor = Color(zodiac.colorValue)
    val tagColor = Color(zodiac.tagColorValue)
    val context = androidx.compose.ui.platform.LocalContext.current
    val iconResId = remember(zodiac.iconName) {
        context.resources.getIdentifier("ic_zodiac_${zodiac.iconName}", "drawable", context.packageName)
    }
    Surface(shape = RoundedCornerShape(20.dp), color = tagColor) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (iconResId != 0) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = zodiac.displayName,
                    tint = zodiacColor,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = zodiac.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = zodiacColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun DaysKnownBadge(knowingDate: Long, color: Color) {
    val text = remember(knowingDate) {
        val diff = System.currentTimeMillis() - knowingDate
        val days = (diff / (1000L * 60 * 60 * 24)).toInt().coerceAtLeast(0)
        val years = days / 365
        val remainDays = days % 365
        if (years > 0) "${years}年${remainDays}天" else "${days}天"
    }
    Surface(shape = RoundedCornerShape(14.dp), color = color.copy(alpha = AnimationTokens.Alpha.faint)) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Schedule,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun IntimacyBadge(score: Int, color: Color, level: IntimacyLevelInfo) {
    Surface(shape = RoundedCornerShape(14.dp), color = color.copy(alpha = AnimationTokens.Alpha.faint)) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(28.dp).background(color.copy(alpha = AnimationTokens.Alpha.subtle), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = level.icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Text(text = level.name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = color)
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(4.dp)
                    .background(color.copy(alpha = AnimationTokens.Alpha.subtle), RoundedCornerShape(2.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(score / 100f)
                        .background(color, RoundedCornerShape(2.dp))
                )
            }
            Text(text = "$score", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }
}

internal fun getIntimacyColor(score: Int): Color = when {
    score >= 81 -> Color(0xFFF43F5E)
    score >= 61 -> Color(0xFFF97316)
    score >= 41 -> Color(0xFF6366F1)
    score >= 21 -> Color(0xFF64748B)
    else -> Color(0xFF94A3B8)
}

internal fun getIntimacyLevel(score: Int): IntimacyLevelInfo = when {
    score >= 81 -> IntimacyLevelInfo(AppStrings.Intimacy.FAMILY, Icons.Default.FavoriteBorder)
    score >= 61 -> IntimacyLevelInfo(AppStrings.Intimacy.CLOSE, Icons.Default.Favorite)
    score >= 41 -> IntimacyLevelInfo(AppStrings.Intimacy.FRIEND, Icons.Default.People)
    score >= 21 -> IntimacyLevelInfo(AppStrings.Intimacy.ACQUAINTANCE, Icons.Default.PersonOutline)
    else -> IntimacyLevelInfo(AppStrings.Intimacy.NEW, Icons.Default.PersonAdd)
}

internal data class IntimacyLevelInfo(val name: String, val icon: ImageVector)

internal fun parseHexColor(hex: String): Color {
    val cleaned = hex.removePrefix("#")
    return try {
        when (cleaned.length) {
            6 -> Color(("FF$cleaned").toLong(16).toInt())
            8 -> Color(cleaned.toLong(16).toInt())
            else -> SignalAmber
        }
    } catch (_: Exception) { SignalAmber }
}
