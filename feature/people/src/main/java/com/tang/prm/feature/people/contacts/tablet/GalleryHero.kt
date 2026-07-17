@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.people.contacts.tablet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.IntimacyTier
import com.tang.prm.feature.people.contacts.formatKnownDuration
import com.tang.prm.ui.theme.SignalGold

// ═══════════════════════════════════════════════════════════════
// Hero 区 — 双层画框 + 衬线大姓名 + 亲密度角标
// ═══════════════════════════════════════════════════════════════

@Composable
internal fun GalleryHero(
    contact: Contact,
    tier: IntimacyTier,
    tierColor: Color,
    relationshipTypes: List<CustomType>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 56.dp)
            .padding(bottom = 32.dp)
    ) {
        // 暖色光晕背景
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .clip(RoundedCornerShape(0.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            tierColor.copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        radius = 1000f
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .padding(top = 40.dp),
            horizontalArrangement = Arrangement.spacedBy(40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── 画框人像 ──
            GalleryPortraitFrame(contact = contact, tierColor = tierColor)

            // ── 中间信息 ──
            GalleryHeroInfo(
                contact = contact,
                tier = tier,
                tierColor = tierColor,
                relationshipTypes = relationshipTypes,
                modifier = Modifier.weight(1f)
            )

            // ── 亲密度角标卡 ──
            GalleryIntimacyCard(score = contact.intimacyScore, tier = tier, tierColor = tierColor)
        }
    }
}

@Composable
internal fun GalleryPortraitFrame(contact: Contact, tierColor: Color) {
    Box(
        modifier = Modifier
            .size(width = 260.dp, height = 320.dp)
    ) {
        // 外层细线画框（双层）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
        )

        // 主画框
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(6.dp))
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(6.dp),
                    ambientColor = tierColor.copy(alpha = 0.25f),
                    spotColor = tierColor.copy(alpha = 0.25f)
                )
        ) {
            // 渐变背景作为兜底
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(tierColor, tierColor.copy(alpha = 0.7f))
                        )
                    )
            )

            if (contact.avatar != null) {
                AsyncImage(
                    model = contact.avatar,
                    contentDescription = contact.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // 无头像：衬线大首字母
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contact.name.firstOrNull()?.toString() ?: "?",
                        fontSize = 200.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White.copy(alpha = 0.3f),
                        fontFamily = FontFamily.Serif
                    )
                }
            }
        }
    }
}

@Composable
internal fun GalleryHeroInfo(
    contact: Contact,
    tier: IntimacyTier,
    tierColor: Color,
    relationshipTypes: List<CustomType>,
    modifier: Modifier = Modifier
) {
    val relationshipLabel = relationshipTypes.find { it.name == contact.relationship }?.name
        ?: contact.relationship
        ?: "未分类"

    // 认识时长：精确到年+天
    val knownDuration = contact.knowingDate?.let { formatKnownDuration(it) }
    val age = contact.birthday?.let {
        val years = ((System.currentTimeMillis() - it) / (365L * 24 * 60 * 60 * 1000)).toInt()
        if (years in 0..150) years else null
    }
    val genderLabel = when (contact.gender) {
        com.tang.prm.domain.model.Gender.MALE -> "男"
        com.tang.prm.domain.model.Gender.FEMALE -> "女"
        else -> null
    }

    Column(modifier = modifier) {
        // 分类标签
        Text(
            text = "— ${relationshipLabel.uppercase()} · ${tier.label} —",
            fontSize = 13.sp,
            color = tierColor,
            letterSpacing = 4.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 衬线大姓名
        Text(
            text = contact.name,
            fontSize = 64.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = (-1.5).sp,
            lineHeight = 66.sp,
            fontFamily = FontFamily.Serif,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        // 昵称（如有）
        contact.nickname?.takeIf { it.isNotBlank() }?.let { nick ->
            Text(
                text = "“$nick”",
                fontSize = 16.sp,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Serif,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // 引文（用 notes 作为引文，没有则用默认）
        Text(
            text = contact.notes?.takeIf { it.isNotBlank() }
                ?: "认识一个人，是认识一整个世界。",
            fontSize = 17.sp,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Serif,
            lineHeight = 26.sp,
            modifier = Modifier.padding(bottom = 24.dp, end = 40.dp)
        )

        // 上分隔线
        Box(
            modifier = Modifier
                .padding(top = 16.dp, bottom = 18.dp)
                .width(60.dp)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline)
        )

        // 元数据行：动态展示真实存在的字段
        Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            GalleryHeroMetaItem(label = "RELATION", value = relationshipLabel)
            genderLabel?.let { GalleryHeroMetaItem(label = "GENDER", value = it) }
            age?.let { GalleryHeroMetaItem(label = "AGE", value = "$it 岁") }
            contact.mbti?.takeIf { it.isNotBlank() }?.let {
                GalleryHeroMetaItem(label = "MBTI", value = it)
            }
            contact.education?.takeIf { it.isNotBlank() }?.let {
                GalleryHeroMetaItem(label = "EDU", value = it)
            }
            knownDuration?.let {
                GalleryHeroMetaItem(label = "KNOWN", value = it)
            }
        }
    }
}

@Composable
internal fun GalleryHeroMetaItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
internal fun GalleryIntimacyCard(
    score: Int,
    tier: IntimacyTier,
    tierColor: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 3.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "$score",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = tierColor,
                fontFamily = FontFamily.Serif,
                lineHeight = 42.sp
            )
            Text(
                text = "INTIMACY",
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Medium
            )
            // 5颗星
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(5) { i ->
                    val filled = i < tier.stars
                    Text(
                        text = "★",
                        fontSize = 13.sp,
                        color = if (filled) SignalGold else MaterialTheme.colorScheme.outline
                    )
                }
            }
            // 分隔线
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline)
            )
            Text(
                text = "${tier.label} ${tier.cardRarity}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
