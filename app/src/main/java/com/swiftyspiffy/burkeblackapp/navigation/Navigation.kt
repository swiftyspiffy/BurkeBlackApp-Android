package com.swiftyspiffy.burkeblackapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Account : Screen("account")
    data object Socials : Screen("socials")
    data object Soundbytes : Screen("soundbytes")
    data object Giveaways : Screen("giveaways")
    data object Bits : Screen("bits")
    data object Donations : Screen("donations")
    data object Feedback : Screen("feedback")
    data object ModPanel : Screen("mod_panel")
    data object About : Screen("about")
    data object Crew : Screen("crew")
    data object Scrolls : Screen("scrolls")
    data object Studio : Screen("studio")
    data object Faq : Screen("faq")
    data object ClipVoting : Screen("clip_voting")
    data object CommunityServers : Screen("community_servers")
    data object ServerDetail : Screen("server_detail")
    data object Advanced : Screen("advanced")
    data object NotificationSettings : Screen("notification_settings")
    data object Rigging : Screen("rigging")
    data object Tidings : Screen("tidings")
    data object NewTiding : Screen("new_tiding")
    data object EditTiding : Screen("edit_tiding")
    data object DeletedTidings : Screen("deleted_tidings")
    data object CaptainsDispatch : Screen("captains_dispatch")
    data object CrewDispatch : Screen("crew_dispatch")
    data object EmotesGallery : Screen("emotes_gallery")
    data object GiveawaySettings : Screen("giveaway_settings")
    data object LateShift : Screen("late_shift")
    data object Appearance : Screen("appearance")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Helm", Icons.Default.Home),
    BottomNavItem(Screen.Crew, "Crew", Icons.Default.Groups),
    BottomNavItem(Screen.Tidings, "Tidings", Icons.Default.Article),
    BottomNavItem(Screen.Socials, "Ports", Icons.Default.DirectionsBoat),
    BottomNavItem(Screen.Rigging, "Rigging", Icons.Default.Settings),
)
