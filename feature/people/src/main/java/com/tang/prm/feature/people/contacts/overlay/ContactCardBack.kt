package com.tang.prm.feature.people.contacts.overlay

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.model.IntimacyTier
import com.tang.prm.domain.model.Contact
import com.tang.prm.ui.components.HoloCornerMarks
import com.tang.prm.ui.components.HoloDataCell
import com.tang.prm.ui.components.HoloScanLine
import com.tang.prm.ui.components.HoloScanLineTexture
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.animation.primitives.rememberScanLineOffset
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.LocalIntimacyColors
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.feature.people.contacts.IntimacyProgressBar
import com.tang.prm.feature.people.contacts.formattedId

@Composable
internal fun ContactCardBack(
    contact: Contact,
    rarity: IntimacyTier,
    onContactClick: () -> Unit,
    onClose: () -> Unit,
    shadowElevation: Float = 12f
) {
    val rarityColor = LocalIntimacyColors.current.forTier(rarity)
    val cardWidth = 340.dp
    val cardHeight = 476.dp

    val scanLineOffset by rememberScanLineOffset(
        cycleDuration = AnimationTokens.Cycle.slow
    )

    Surface(
        modifier = Modifier
            .width(cardWidth)
            .height(cardHeight),
        shape = RoundedCornerShape(2.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.5.dp, rarityColor.copy(alpha = 0.25f)),
        shadowElevation = shadowElevation.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            HoloScanLineTexture(rarityColor)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Dimens.paddingPage)
            ) {
                Spacer(Modifier.height(14.dp))
                CardBackHeader(contact = contact, rarity = rarity, rarityColor = rarityColor)
                Spacer(Modifier.height(6.dp))
                CardBackIdentity(contact = contact, rarity = rarity, rarityColor = rarityColor)
                Spacer(Modifier.height(12.dp))
                CardBackIntimacy(contact = contact, rarityColor = rarityColor)
                Spacer(Modifier.height(12.dp))
                CardBackProfile(contact = contact, rarityColor = rarityColor)
                Spacer(Modifier.weight(1f))
                CardBackActions(
                    rarityColor = rarityColor,
                    onContactClick = onContactClick,
                    onClose = onClose
                )
            }

            HoloScanLine(scanLineOffset, rarityColor)
            HoloCornerMarks(rarityColor)
        }
    }
}

/** 顶部状态栏：PERSONNEL FILE 标题 + 联系人 ID 徽章 */
@Composable
private fun CardBackHeader(
    contact: Contact,
    rarity: IntimacyTier,
    rarityColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "PERSONNEL FILE",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = rarityColor.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp
        )
        Box(
            modifier = Modifier
                .background(rarityColor.copy(alpha = 0.05f), RoundedCornerShape(2.dp))
                .border(1.dp, rarityColor.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                "ID:${contact.formattedId}",
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** 中部身份信息：姓名 + 稀有度 + 分隔线 + 数据单元（关系/电话/稀有度/状态） */
@Composable
private fun CardBackIdentity(
    contact: Contact,
    rarity: IntimacyTier,
    rarityColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            contact.name,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = 2.sp
        )
        Text(
            rarity.cardRarity,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = rarityColor
        )
    }

    Spacer(Modifier.height(10.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(rarityColor.copy(alpha = 0.1f))
    )
    Spacer(Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HoloDataCell(
            label = "RELATION",
            value = contact.relationship ?: "未知",
            valueColor = rarityColor,
            tintColor = rarityColor,
            modifier = Modifier.weight(1f)
        )
        HoloDataCell(
            label = "PHONE",
            value = contact.phone ?: "—",
            valueColor = rarityColor,
            tintColor = rarityColor,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HoloDataCell(
            label = "RARITY",
            value = "${rarity.cardRarity} ${"★".repeat(rarity.stars)}",
            valueColor = rarityColor,
            tintColor = rarityColor,
            modifier = Modifier.weight(1f)
        )
        HoloDataCell(
            label = "STATUS",
            value = "● ONLINE",
            valueColor = SignalGreen,
            tintColor = rarityColor,
            modifier = Modifier.weight(1f)
        )
    }
}

/** 亲密度区：标题 + 渐变进度条 + 百分比 */
@Composable
private fun CardBackIntimacy(contact: Contact, rarityColor: Color) {
    Text(
        "INTIMACY LEVEL",
        fontFamily = FontFamily.Monospace,
        fontSize = 8.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 2.sp
    )
    Spacer(Modifier.height(6.dp))
    IntimacyProgressBar(
        score = contact.intimacyScore,
        fillBrush = Brush.horizontalGradient(listOf(rarityColor, rarityColor.copy(alpha = 0.4f))),
        modifier = Modifier.fillMaxWidth(),
        height = 6.dp,
        cornerRadius = 1.dp,
        border = BorderStroke(1.dp, rarityColor.copy(alpha = 0.06f))
    )
    Spacer(Modifier.height(4.dp))
    Text(
        "${contact.intimacyScore}%",
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        color = rarityColor,
        textAlign = TextAlign.End,
        modifier = Modifier.fillMaxWidth()
    )
}

/** 备注档案区：PROFILE 标题 + 备注/暂无信息文案 */
@Composable
private fun CardBackProfile(contact: Contact, rarityColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(rarityColor.copy(alpha = 0.02f), RoundedCornerShape(3.dp))
            .border(1.dp, rarityColor.copy(alpha = 0.06f), RoundedCornerShape(3.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(
                "PROFILE",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                contact.notes ?: "暂无备注信息",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** 底部按钮组：查看档案 / 关闭 */
@Composable
private fun CardBackActions(
    rarityColor: Color,
    onContactClick: () -> Unit,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            onClick = onContactClick,
            shape = RoundedCornerShape(3.dp),
            color = rarityColor.copy(alpha = AnimationTokens.Alpha.faint),
            border = BorderStroke(1.dp, rarityColor.copy(alpha = 0.25f))
        ) {
            Text(
                "查看档案",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = rarityColor,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                letterSpacing = 1.sp
            )
        }
        Surface(
            onClick = onClose,
            shape = RoundedCornerShape(3.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Text(
                "关闭",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                letterSpacing = 1.sp
            )
        }
    }
}
