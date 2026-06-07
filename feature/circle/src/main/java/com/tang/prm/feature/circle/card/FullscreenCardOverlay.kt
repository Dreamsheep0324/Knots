package com.tang.prm.feature.circle.card

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.tang.prm.domain.model.CardRarity
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.getCardRarity
import com.tang.prm.ui.animation.composites.HolographicCardOverlay
import com.tang.prm.ui.animation.composites.HolographicConfig

@Composable
internal fun FullscreenCardOverlay(
    contact: Contact,
    waveformType: String,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onClose: () -> Unit,
    onRemove: () -> Unit,
    onContactClick: () -> Unit
) {
    val rarity = getCardRarity(contact.intimacyScore)
    val rarityColor = Color(rarity.colorValue)

    HolographicCardOverlay(
        isFlipped = isFlipped,
        onFlip = onFlip,
        onClose = onClose,
        config = HolographicConfig.default.copy(
            borderColor = rarityColor,
            enableWaveform = true,
            enablePulse = true,
            enableFloat = true,
            enableScanLine = true,
            enableShimmer = true,
            scanLineColor = rarityColor,
            shimmerColor = rarityColor
        ),
        frontContent = {
            TerminalCardFrontV2(
                contact = contact,
                rarity = rarity,
                waveformType = waveformType,
                onFlip = onFlip,
                shadowElevation = 20f
            )
        },
        backContent = {
            TerminalCardBackV2(
                contact = contact,
                rarity = rarity,
                onContactClick = onContactClick,
                onRemove = onRemove,
                shadowElevation = 20f
            )
        }
    )
}
