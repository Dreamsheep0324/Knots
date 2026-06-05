package com.tang.prm.feature.people.contacts.overlay

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.getCardRarity
import com.tang.prm.ui.animation.composites.HolographicCardOverlay
import com.tang.prm.ui.animation.composites.HolographicConfig
import com.tang.prm.ui.animation.core.AnimationTokens

@Composable
internal fun ContactCardOverlay(
    contact: Contact,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onClose: () -> Unit,
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
            borderAlpha = AnimationTokens.Alpha.half,
            enableWaveform = true,
            enablePulse = true,
            enableFloat = true,
            enableScanLine = true,
            enableShimmer = false,
            scanLineColor = rarityColor,
            shimmerColor = rarityColor
        ),
        frontContent = {
            ContactCardFront(
                contact = contact,
                rarity = rarity,
                onFlip = onFlip,
                shadowElevation = 20f
            )
        },
        backContent = {
            ContactCardBack(
                contact = contact,
                rarity = rarity,
                onContactClick = onContactClick,
                onClose = onClose,
                shadowElevation = 20f
            )
        }
    )
}
