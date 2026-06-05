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

private val iconMap = commonAnniversaryIcons.associateBy { it.key }

fun getAnniversaryIcon(iconName: String?): ImageVector {
    return iconMap[iconName]?.icon ?: Icons.Default.Cake
}

fun getAnniversaryIconBackground(iconName: String?): Color {
    return iconMap[iconName]?.backgroundColor ?: Color(0xFFFFF3E0)
}

fun getAnniversaryIconTint(iconName: String?): Color {
    return iconMap[iconName]?.iconTint ?: Color(0xFFF57C00)
}

data class WeatherIconDef(
    val name: String,
    val icon: ImageVector,
    val color: String
)

val weatherIconDefs: List<WeatherIconDef> = listOf(
    WeatherIconDef("晴天", Icons.Default.WbSunny, "#F59E0B"),
    WeatherIconDef("多云", Icons.Default.Cloud, "#94A3B8"),
    WeatherIconDef("阴天", Icons.Default.FilterDrama, "#64748B"),
    WeatherIconDef("小雨", Icons.Default.WaterDrop, "#3B82F6"),
    WeatherIconDef("大雨", Icons.Default.Thunderstorm, "#6366F1"),
    WeatherIconDef("雪", Icons.Default.AcUnit, "#06B6D4"),
    WeatherIconDef("风", Icons.Default.Air, "#14B8A6"),
    WeatherIconDef("雾", Icons.Default.CloudQueue, "#9CA3AF")
)

private val weatherIconMap = weatherIconDefs.associateBy { it.name }

fun getWeatherIcon(name: String): ImageVector? = weatherIconMap[name]?.icon

fun getWeatherColor(name: String): String? = weatherIconMap[name]?.color

data class EmotionIconDef(
    val name: String,
    val icon: ImageVector,
    val color: String
)

val emotionIconDefs: List<EmotionIconDef> = listOf(
    EmotionIconDef("开心", Icons.Default.SentimentVerySatisfied, "#F59E0B"),
    EmotionIconDef("平静", Icons.Default.SelfImprovement, "#14B8A6"),
    EmotionIconDef("兴奋", Icons.Default.EmojiEmotions, "#F97316"),
    EmotionIconDef("感动", Icons.Default.Favorite, "#EC4899"),
    EmotionIconDef("焦虑", Icons.Default.Psychology, "#8B5CF6"),
    EmotionIconDef("难过", Icons.Default.SentimentVeryDissatisfied, "#6B7280"),
    EmotionIconDef("愤怒", Icons.Default.MoodBad, "#EF4444"),
    EmotionIconDef("疲惫", Icons.Default.Bedtime, "#6366F1"),
    EmotionIconDef("惊喜", Icons.Default.AutoAwesome, "#EAB308"),
    EmotionIconDef("感恩", Icons.Default.VolunteerActivism, "#EC4899"),
    EmotionIconDef("期待", Icons.Default.Star, "#F59E0B"),
    EmotionIconDef("思念", Icons.Default.FavoriteBorder, "#8B5CF6")
)

private val emotionIconMap = emotionIconDefs.associateBy { it.name }

fun getEmotionIcon(name: String): ImageVector? = emotionIconMap[name]?.icon

fun getEmotionColor(name: String): String? = emotionIconMap[name]?.color

private val genericIconMap: Map<String, ImageVector> = mapOf(
    "People" to Icons.Default.People, "Person" to Icons.Default.Person,
    "Group" to Icons.Default.Group, "Restaurant" to Icons.Default.Restaurant,
    "LocalCafe" to Icons.Default.LocalCafe, "Flight" to Icons.Default.Flight,
    "DirectionsCar" to Icons.Default.DirectionsCar, "Hotel" to Icons.Default.Hotel,
    "Phone" to Icons.Default.Phone, "Message" to Icons.Default.Message,
    "Videocam" to Icons.Default.Videocam, "Email" to Icons.Default.Email,
    "Work" to Icons.Default.Work, "School" to Icons.Default.School,
    "Home" to Icons.Default.Home, "Event" to Icons.Default.Event,
    "Favorite" to Icons.Default.Favorite, "Star" to Icons.Default.Star,
    "AutoAwesome" to Icons.Default.AutoAwesome, "Celebration" to Icons.Default.Celebration,
    "CardGiftcard" to Icons.Default.CardGiftcard, "Cake" to Icons.Default.Cake,
    "MusicNote" to Icons.Default.MusicNote, "SportsSoccer" to Icons.Default.SportsSoccer,
    "FitnessCenter" to Icons.Default.FitnessCenter, "ShoppingBag" to Icons.Default.ShoppingBag,
    "Pets" to Icons.Default.Pets, "LocalHospital" to Icons.Default.LocalHospital,
    "CalendarToday" to Icons.Default.CalendarToday, "HolidayVillage" to Icons.Default.HolidayVillage,
    "FamilyRestroom" to Icons.Default.FamilyRestroom, "RocketLaunch" to Icons.Default.RocketLaunch,
    "EmojiEvents" to Icons.Default.EmojiEvents, "VolunteerActivism" to Icons.Default.VolunteerActivism,
    "FavoriteBorder" to Icons.Default.FavoriteBorder, "ChildCare" to Icons.Default.ChildCare
)

fun getGenericIcon(name: String?): ImageVector? = name?.let { genericIconMap[it] }
