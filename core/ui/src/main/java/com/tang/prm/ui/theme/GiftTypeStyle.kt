package com.tang.prm.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.tang.prm.domain.model.GiftType

data class GiftTypeStyle(val icon: ImageVector, val color: Color)

fun GiftType.toStyle(): GiftTypeStyle = when (this) {
    GiftType.DIGITAL -> GiftTypeStyle(Icons.Default.Devices, Color(0xFF0891B2))
    GiftType.CLOTHING -> GiftTypeStyle(Icons.Default.Checkroom, Color(0xFFEC4899))
    GiftType.FOOD -> GiftTypeStyle(Icons.Default.Restaurant, Color(0xFFD97706))
    GiftType.COSMETICS -> GiftTypeStyle(Icons.Default.Face, Color(0xFF8B5CF6))
    GiftType.BOOKS -> GiftTypeStyle(Icons.Default.MenuBook, Color(0xFF78716C))
    GiftType.TOYS -> GiftTypeStyle(Icons.Default.SportsEsports, Color(0xFFEA580C))
    GiftType.TRAVEL -> GiftTypeStyle(Icons.Default.FlightTakeoff, Color(0xFF0E7490))
    GiftType.SPORTS -> GiftTypeStyle(Icons.Default.FitnessCenter, Color(0xFF16A34A))
    GiftType.HOME -> GiftTypeStyle(Icons.Default.Chair, Color(0xFF6B7280))
    GiftType.OTHER -> GiftTypeStyle(Icons.Default.CardGiftcard, Color(0xFF9CA3AF))
}
