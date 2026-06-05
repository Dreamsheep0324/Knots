package com.tang.prm.ui.home.card

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.getCardRarity
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.animation.primitives.rememberBreathingPulse
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.Error
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.home.HologramCircle
import com.tang.prm.ui.home.TerminalDivider
import com.tang.prm.ui.home.TerminalMiniCardRow
import com.tang.prm.ui.home.TerminalTextDim
import com.tang.prm.ui.home.TerminalTextMuted
import java.util.Locale

@Composable
internal fun TerminalDossier(
    hologramCircle: HologramCircle,
    isExpanded: Boolean,
    selectedMemberId: Long?,
    flippedCardId: Long?,
    onExpand: () -> Unit,
    onSelectMember: (Long?) -> Unit,
    onFlipCard: (Long) -> Unit,
    onAddMember: () -> Unit,
    onEditCircle: () -> Unit,
    onDeleteCircle: () -> Unit,
    onRemoveMember: (Long) -> Unit,
    onContactClick: (Long) -> Unit
) {
    val circle = hologramCircle.circle
    val members = hologramCircle.members
    val memberCount = members.size
    val avgIntimacy = if (members.isEmpty()) 0 else members.map { it.intimacyScore }.average().toInt()
    val rarity = getCardRarity(avgIntimacy)
    val circleColor = MaterialTheme.colorScheme.onSurface

    Column {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpand() },
            shape = RoundedCornerShape(2.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    String.format(Locale.US, "%03d", circle.id),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = circleColor,
                    modifier = Modifier.width(40.dp)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        circle.name,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    circle.description?.let {
                        Text(
                            it,
                            fontSize = 9.sp,
                            color = TerminalTextDim,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Text(
                    "[$memberCount]",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TerminalTextDim,
                    modifier = Modifier.width(50.dp),
                    textAlign = TextAlign.Center
                )

                Text(
                    String.format(Locale.US, "%02d%%", avgIntimacy),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color(rarity.colorValue),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(60.dp),
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier.width(60.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val statusAlpha by rememberBreathingPulse(
                        minAlpha = 0.4f,
                        maxAlpha = 1f,
                        cycleDuration = 1200
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(circleColor.copy(alpha = statusAlpha))
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "RUN",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = circleColor
                    )
                }

                Spacer(Modifier.width(4.dp))

                val rotation by animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f,
                    animationSpec = tween(AnimationTokens.Duration.fast), label = "arrow"
                )
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotation).size(18.dp),
                    tint = circleColor.copy(alpha = 0.6f)
                )
            }
        }

        if (isExpanded) {
            TerminalExpandedContent(
                members = members,
                circleColor = circleColor,
                selectedMemberId = selectedMemberId,
                flippedCardId = flippedCardId,
                onSelectMember = onSelectMember,
                onFlipCard = onFlipCard,
                onAddMember = onAddMember,
                onEditCircle = onEditCircle,
                onDeleteCircle = onDeleteCircle,
                onRemoveMember = onRemoveMember,
                onContactClick = onContactClick
            )
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
private fun TerminalExpandedContent(
    members: List<Contact>,
    circleColor: Color,
    selectedMemberId: Long?,
    flippedCardId: Long?,
    onSelectMember: (Long?) -> Unit,
    onFlipCard: (Long) -> Unit,
    onAddMember: () -> Unit,
    onEditCircle: () -> Unit,
    onDeleteCircle: () -> Unit,
    onRemoveMember: (Long) -> Unit,
    onContactClick: (Long) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(Dimens.paddingCard)) {
            TerminalActionBar(
                circleColor = circleColor,
                onAddMember = onAddMember,
                onEditCircle = onEditCircle,
                onDeleteCircle = onDeleteCircle
            )

            Spacer(Modifier.height(12.dp))

            TerminalDivider(MaterialTheme.colorScheme.outline)

            Spacer(Modifier.height(12.dp))

            if (members.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "[空目录]",
                        fontSize = 13.sp,
                        color = TerminalTextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                TerminalMiniCardRow(
                    members = members,
                    circleColor = circleColor,
                    onSelectMember = onSelectMember
                )
            }
        }
    }
}

@Composable
private fun TerminalActionBar(
    circleColor: Color,
    onAddMember: () -> Unit,
    onEditCircle: () -> Unit,
    onDeleteCircle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TerminalActionButton(
            label = "添加",
            accentColor = circleColor,
            onClick = onAddMember
        )
        TerminalActionButton(
            label = "编辑",
            accentColor = circleColor,
            onClick = onEditCircle
        )
        TerminalActionButton(
            label = "删除",
            accentColor = Error,
            onClick = onDeleteCircle
        )
    }
}

@Composable
internal fun TerminalActionButton(label: String, onClick: () -> Unit, accentColor: Color = SignalPurple) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(2.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
    ) {
        Text(
            label,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
