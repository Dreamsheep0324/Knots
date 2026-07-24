package com.tang.prm.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import com.tang.prm.domain.model.GiftType

/** 礼物类型专用调色板（领域特定，与品牌信号色 Signal* 独立）。 */
private object GiftTypeColors {
    val digital = Color(0xFF0891B2)
    val clothing = Color(0xFFEC4899)
    val food = Color(0xFFD97706)
    val cosmetics = Color(0xFF8B5CF6)
    val books = Color(0xFF78716C)
    val toys = Color(0xFFEA580C)
    val travel = Color(0xFF0E7490)
    val sports = Color(0xFF16A34A)
    val home = Color(0xFF6B7280)
    val other = Color(0xFF9CA3AF)
}

data class GiftTypeStyle(val icon: ImageVector, val color: Color)

fun GiftType.toStyle(): GiftTypeStyle = when (this) {
    GiftType.DIGITAL -> GiftTypeStyle(Icons.Default.Devices, GiftTypeColors.digital)
    GiftType.CLOTHING -> GiftTypeStyle(Icons.Default.Checkroom, GiftTypeColors.clothing)
    GiftType.FOOD -> GiftTypeStyle(Icons.Default.Restaurant, GiftTypeColors.food)
    GiftType.COSMETICS -> GiftTypeStyle(Icons.Default.Face, GiftTypeColors.cosmetics)
    GiftType.BOOKS -> GiftTypeStyle(Icons.AutoMirrored.Filled.MenuBook, GiftTypeColors.books)
    GiftType.TOYS -> GiftTypeStyle(Icons.Default.SportsEsports, GiftTypeColors.toys)
    GiftType.TRAVEL -> GiftTypeStyle(Icons.Default.FlightTakeoff, GiftTypeColors.travel)
    GiftType.SPORTS -> GiftTypeStyle(Icons.Default.FitnessCenter, GiftTypeColors.sports)
    GiftType.HOME -> GiftTypeStyle(Icons.Default.Chair, GiftTypeColors.home)
    GiftType.OTHER -> GiftTypeStyle(Icons.Default.CardGiftcard, GiftTypeColors.other)
}
