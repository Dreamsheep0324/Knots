package com.tang.prm.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class AnniversaryIconDef(
    val key: String,
    val displayName: String,
    val icon: ImageVector,
    val backgroundColor: Color,
    val iconTint: Color
)

val commonAnniversaryIcons: List<AnniversaryIconDef> = listOf(
    AnniversaryIconDef("Cake", "生日", Icons.Default.Cake, Color(0xFFFFF3E0), Color(0xFFF57C00)),
    AnniversaryIconDef("Favorite", "爱心", Icons.Default.Favorite, Color(0xFFFCE4EC), Color(0xFFEC407A)),
    AnniversaryIconDef("Celebration", "庆祝", Icons.Default.Celebration, Color(0xFFFFFDE7), Color(0xFFFBC02D)),
    AnniversaryIconDef("Star", "星星", Icons.Default.Star, Color(0xFFFFF8E1), Color(0xFFFFA000)),
    AnniversaryIconDef("CardGiftcard", "礼物", Icons.Default.CardGiftcard, Color(0xFFF3E5F5), Color(0xFFAB47BC)),
    AnniversaryIconDef("CalendarToday", "日历", Icons.Default.CalendarToday, Color(0xFFE3F2FD), Color(0xFF1E88E5)),
    AnniversaryIconDef("HolidayVillage", "节日", Icons.Default.HolidayVillage, Color(0xFFE0F7FA), Color(0xFF00ACC1)),
    AnniversaryIconDef("FamilyRestroom", "家庭", Icons.Default.FamilyRestroom, Color(0xFFE8F5E9), Color(0xFF43A047)),
    AnniversaryIconDef("Diversity3", "朋友", Icons.Default.Diversity3, Color(0xFFFFF3E0), Color(0xFFEF6C00)),
    AnniversaryIconDef("Work", "工作", Icons.Default.Work, Color(0xFFECEFF1), Color(0xFF546E7A)),
    AnniversaryIconDef("School", "学校", Icons.Default.School, Color(0xFFEDE7F6), Color(0xFF7E57C2)),
    AnniversaryIconDef("RocketLaunch", "特殊", Icons.Default.RocketLaunch, Color(0xFFFFEBEE), Color(0xFFE53935)),
    AnniversaryIconDef("EmojiEvents", "活动", Icons.Default.EmojiEvents, Color(0xFFFFF3E0), Color(0xFFF57C00)),
    AnniversaryIconDef("AutoAwesome", "精彩", Icons.Default.AutoAwesome, Color(0xFFFCE4EC), Color(0xFFEF5350)),
    AnniversaryIconDef("NotStarted", "纪念", Icons.Default.NotStarted, Color(0xFFE8F5E9), Color(0xFF66BB6A)),
    AnniversaryIconDef("VolunteerActivism", "感恩", Icons.Default.VolunteerActivism, Color(0xFFFCE4EC), Color(0xFFEC407A)),
    AnniversaryIconDef("FavoriteBorder", "婚礼", Icons.Default.FavoriteBorder, Color(0xFFF3E5F5), Color(0xFFAB47BC)),
    AnniversaryIconDef("ChildCare", "孩子", Icons.Default.ChildCare, Color(0xFFE0F7FA), Color(0xFF00ACC1)),
    AnniversaryIconDef("Pets", "宠物", Icons.Default.Pets, Color(0xFFF5F5F5), Color(0xFF795548)),
    AnniversaryIconDef("Flight", "旅行", Icons.Default.Flight, Color(0xFFE3F2FD), Color(0xFF1E88E5)),
    AnniversaryIconDef("Home", "新居", Icons.Default.Home, Color(0xFFE8F5E9), Color(0xFF7CB342)),
    AnniversaryIconDef("LocalHospital", "健康", Icons.Default.LocalHospital, Color(0xFFFFEBEE), Color(0xFFEF5350)),
    AnniversaryIconDef("MusicNote", "音乐", Icons.Default.MusicNote, Color(0xFFF3E5F5), Color(0xFF8E24AA)),
    AnniversaryIconDef("Restaurant", "美食", Icons.Default.Restaurant, Color(0xFFFFF3E0), Color(0xFFFF7043))
)

private val anniversaryIconRegistry = IconRegistry(commonAnniversaryIcons) { it.key }

fun getAnniversaryIcon(iconName: String?): ImageVector {
    return anniversaryIconRegistry.find(iconName)?.icon ?: Icons.Default.Cake
}

fun getAnniversaryIconBackground(iconName: String?): Color {
    return anniversaryIconRegistry.find(iconName)?.backgroundColor ?: Color(0xFFFFF3E0)
}

fun getAnniversaryIconTint(iconName: String?): Color {
    return anniversaryIconRegistry.find(iconName)?.iconTint ?: Color(0xFFF57C00)
}
