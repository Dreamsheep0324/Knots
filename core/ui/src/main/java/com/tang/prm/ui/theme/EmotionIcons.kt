package com.tang.prm.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

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

private val emotionIconRegistry = IconRegistry(emotionIconDefs) { it.name }

fun getEmotionIcon(name: String): ImageVector? = emotionIconRegistry.find(name)?.icon

fun getEmotionColor(name: String): String? = emotionIconRegistry.find(name)?.color
