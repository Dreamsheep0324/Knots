package com.tang.prm.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

private val genericIconMap: Map<String, ImageVector> = mapOf(
    "People" to Icons.Default.People, "Person" to Icons.Default.Person,
    "Group" to Icons.Default.Group, "Restaurant" to Icons.Default.Restaurant,
    "LocalCafe" to Icons.Default.LocalCafe, "Flight" to Icons.Default.Flight,
    "DirectionsCar" to Icons.Default.DirectionsCar, "Hotel" to Icons.Default.Hotel,
    "Phone" to Icons.Default.Phone, "Message" to Icons.AutoMirrored.Filled.Message,
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
    "FavoriteBorder" to Icons.Default.FavoriteBorder, "ChildCare" to Icons.Default.ChildCare,
    "Cloud" to Icons.Default.Cloud, "CloudQueue" to Icons.Default.CloudQueue,
    "Storage" to Icons.Default.Storage, "Security" to Icons.Default.Security,
    "Language" to Icons.Default.Language, "SmartDisplay" to Icons.Default.SmartDisplay,
    "Headphones" to Icons.Default.Headphones,
    "Palette" to Icons.Default.Palette, "Brush" to Icons.Default.Brush,
    "AutoStories" to Icons.Default.AutoStories, "MenuBook" to Icons.AutoMirrored.Filled.MenuBook,
    "Podcasts" to Icons.Default.Podcasts, "SportsEsports" to Icons.Default.SportsEsports,
    "HealthAndSafety" to Icons.Default.HealthAndSafety,
    "AccountBalanceWallet" to Icons.Default.AccountBalanceWallet, "CreditCard" to Icons.Default.CreditCard,
    "Receipt" to Icons.Default.Receipt, "ShoppingCart" to Icons.Default.ShoppingCart,
    "Subscriptions" to Icons.Default.Subscriptions, "Devices" to Icons.Default.Devices,
    "Wifi" to Icons.Default.Wifi, "VpnKey" to Icons.Default.VpnKey,
    "Backup" to Icons.Default.Backup, "Terminal" to Icons.Default.Terminal
)

fun getGenericIcon(name: String?): ImageVector? = name?.let { genericIconMap[it] }
