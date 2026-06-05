package com.tang.prm.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

data class ThoughtTypeStyle(
    val accentColor: Color,
    val bgColor: Color,
    val icon: ImageVector
)

@Composable
fun getThoughtTypeStyle(type: String): ThoughtTypeStyle = when (type) {
    "FRIEND" -> ThoughtTypeStyle(SemanticBlueText, SemanticBlueBg, Icons.Default.People)
    "PLAN" -> ThoughtTypeStyle(SemanticAmberText, SemanticAmberBg, Icons.Default.Flag)
    "MURMUR" -> ThoughtTypeStyle(SemanticPurpleText, SemanticPurpleBg, Icons.Default.AutoAwesome)
    else -> ThoughtTypeStyle(SignalElectric, EventLightIndigo, Icons.Default.Lightbulb)
}
