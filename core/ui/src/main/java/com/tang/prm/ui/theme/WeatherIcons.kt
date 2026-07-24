package com.tang.prm.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

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

private val weatherIconRegistry = IconRegistry(weatherIconDefs) { it.name }

fun getWeatherIcon(name: String): ImageVector? = weatherIconRegistry.find(name)?.icon

fun getWeatherColor(name: String): String? = weatherIconRegistry.find(name)?.color
