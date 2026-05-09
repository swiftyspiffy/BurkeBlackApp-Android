package com.swiftyspiffy.burkeblackapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swiftyspiffy.burkeblackapp.data.websocket.GiveawayWebSocketManager
import com.swiftyspiffy.burkeblackapp.push.PushNotificationManager
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.swiftyspiffy.burkeblackapp.auth.TwitchAuthManager
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import com.swiftyspiffy.burkeblackapp.navigation.Screen
import com.swiftyspiffy.burkeblackapp.navigation.bottomNavItems
import com.swiftyspiffy.burkeblackapp.ui.screens.AboutScreen
import com.swiftyspiffy.burkeblackapp.ui.screens.AdvancedScreen
import com.swiftyspiffy.burkeblackapp.ui.screens.AccountScreen
import com.swiftyspiffy.burkeblackapp.ui.screens.AccountViewModel
import com.swiftyspiffy.burkeblackapp.ui.screens.AppearanceScreen
import com.swiftyspiffy.burkeblackapp.ui.screens.BitsScreen
import com.swiftyspiffy.burkeblackapp.ui.screens.DispatchMode
import com.swiftyspiffy.burkeblackapp.ui.screens.DispatchScreen
import com.swiftyspiffy.burkeblackapp.ui.screens.DonationsScreen
import com.swiftyspiffy.burkeblackapp.ui.screens.EmotesGalleryScreen
import com.swiftyspiffy.burkeblackapp.ui.screens.FeedbackScreen
import com.swiftyspiffy.burkeblackapp.ui.screens.GiveawayPopupOverlay
import com.swiftyspiffy.burkeblackapp.ui.screens.GiveawaySettingsScreen
import com.swiftyspiffy.burkeblackapp.ui.screens.NotificationSettingsScreen
import com.swiftyspiffy.burkeblackapp.ui.screens.PushPermissionPrompt
import com.swiftyspiffy.burkeblackapp.ui.screens.RiggingScreen
import com.swiftyspiffy.burkeblackapp.ui.screens.TidingsScreen
import com.swiftyspiffy.burkeblackapp.data.models.CommunityServer
import com.swiftyspiffy.burkeblackapp.data.models.NewsArticle
import com.swiftyspiffy.burkeblackapp.ui.screens.CommunityServersScreen
import com.swiftyspiffy.burkeblackapp.ui.screens.CommunityServersViewModel
import com.swiftyspiffy.burkeblackapp.ui.screens.ClipVotingScreen
import com.swiftyspiffy.burkeblackapp.ui.screens.ClipVotingViewModel
import com.swiftyspiffy.burkeblackapp.ui.screens.CrewScreen
import com.swiftyspiffy.burkeblackapp.ui.screens.ServerDetailScreen
import com.swiftyspiffy.burkeblackapp.ui.screens.FaqScreen
import com.swiftyspiffy.burkeblackapp.ui.screens.GiveawaysScreen
import com.swiftyspiffy.burkeblackapp.ui.screens.HomeScreen
import com.swiftyspiffy.burkeblackapp.ui.screens.DeletedTidingsScreen
import com.swiftyspiffy.burkeblackapp.ui.screens.LateShiftScreen
import com.swiftyspiffy.burkeblackapp.ui.screens.NewTidingScreen
import com.swiftyspiffy.burkeblackapp.ui.screens.ScrollsScreen
import com.swiftyspiffy.burkeblackapp.ui.screens.StudioScreen
import com.swiftyspiffy.burkeblackapp.ui.screens.SocialsScreen
import com.swiftyspiffy.burkeblackapp.ui.screens.SoundbytesScreen
import com.swiftyspiffy.burkeblackapp.ui.screens.SoundbytesViewModel
import com.swiftyspiffy.burkeblackapp.ui.screens.mod.ModPanelScreen
import com.swiftyspiffy.burkeblackapp.ui.screens.mod.ModPanelViewModel
import com.swiftyspiffy.burkeblackapp.ui.theme.BurkeBlackAppTheme
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme

class MainActivity : ComponentActivity() {
    private val authUri = androidx.compose.runtime.mutableStateOf<android.net.Uri?>(null)
    private val deepLinkUri = androidx.compose.runtime.mutableStateOf<android.net.Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        intent?.data?.let { uri -> routeIncomingUri(uri) }
        setContent {
            BurkeBlackAppTheme {
                val accountViewModel: AccountViewModel = viewModel()
                MainApp(accountViewModel, authUri, deepLinkUri)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.data?.let { uri -> routeIncomingUri(uri) }
    }

    private fun routeIncomingUri(uri: android.net.Uri) {
        if (uri.scheme == "burkeblackapp") {
            // Auth callback URIs have token/username params and no host
            // Deep link URIs have a host (e.g. burkeblackapp://tidings/42)
            val hasAuthParams = uri.getQueryParameter("token") != null
            if (hasAuthParams) {
                authUri.value = uri
            } else {
                deepLinkUri.value = uri
            }
        }
    }
}

@Composable
private fun MainApp(
    accountViewModel: AccountViewModel,
    authUri: androidx.compose.runtime.MutableState<android.net.Uri?>,
    deepLinkUri: androidx.compose.runtime.MutableState<android.net.Uri?>
) {
    val navController = rememberNavController()
    val uri by authUri

    // Handle auth callback
    LaunchedEffect(uri) {
        uri?.let { callbackUri ->
            AppLogger.logSensitive("Auth", "callback received: ${callbackUri.scheme}://${callbackUri.host}")
            val callbackData = TwitchAuthManager.parseCallbackUri(callbackUri)
            if (callbackData != null) {
                accountViewModel.handleAuthCallback(callbackData)
                navController.navigate(Screen.Account.route) {
                    popUpTo(Screen.Home.route)
                }
            }
            authUri.value = null
        }
    }

    // Handle deep links (e.g. burkeblackapp://tidings/42)
    val deepLink by deepLinkUri
    var pendingTidingsArticleId by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(deepLink) {
        deepLink?.let { dlUri ->
            AppLogger.log("DeepLink: ${dlUri.scheme}://${dlUri.host}${dlUri.path}")
            when (dlUri.host) {
                "tidings" -> {
                    pendingTidingsArticleId = dlUri.pathSegments?.firstOrNull()?.toIntOrNull()
                    navController.navigate(Screen.Tidings.route)
                }
            }
            deepLinkUri.value = null
        }
    }

    // Reconnect WebSocket and refresh push token when app resumes from background
    val isLoggedIn by accountViewModel.isLoggedIn.collectAsState()
    val token by accountViewModel.sessionManager.token.collectAsState(initial = null)
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    LifecycleResumeEffect(isLoggedIn) {
        if (isLoggedIn) {
            val wsManager = GiveawayWebSocketManager.instance
            if (!wsManager.isConnected.value) {
                AppLogger.log("WebSocket: reconnecting on app resume")
                wsManager.reconnectIfNeeded()
            }
            // Refresh push notification token registration with backend
            coroutineScope.launch {
                PushNotificationManager.refreshTokenRegistration(context, token)
            }
        }
        onPauseOrDispose { }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            AppNavHost(
                navController = navController,
                accountViewModel = accountViewModel,
                pendingTidingsArticleId = pendingTidingsArticleId,
                onTidingsArticleConsumed = { pendingTidingsArticleId = null }
            )

            // Giveaway popup overlay (floats on top)
            GiveawayPopupOverlay(isAuthenticated = isLoggedIn)

            // Push permission prompt
            PushPermissionPrompt(isLoggedIn = isLoggedIn, token = token)
        }
    }
}

@Composable
private fun BottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Only show bottom bar on main tabs
    val mainRoutes = bottomNavItems.map { it.screen.route }
    if (currentRoute !in mainRoutes && currentRoute != null) return

    val accent = PirateTheme.accentColor
    val font = PirateTheme.fontFamily

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = Color.White,
        windowInsets = WindowInsets(0),
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .height(56.dp)
    ) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, fontFamily = font) },
                selected = currentRoute == item.screen.route,
                onClick = {
                    if (currentRoute != item.screen.route) {
                        AppLogger.log("Navigate: ${item.screen.route}")
                        navController.navigate(item.screen.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = accent,
                    selectedTextColor = accent,
                    unselectedIconColor = Color.White.copy(alpha = 0.5f),
                    unselectedTextColor = Color.White.copy(alpha = 0.5f),
                    indicatorColor = accent.copy(alpha = 0.15f)
                )
            )
        }
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    accountViewModel: AccountViewModel,
    pendingTidingsArticleId: Int? = null,
    onTidingsArticleConsumed: () -> Unit = {}
) {
    val token by accountViewModel.sessionManager.token.collectAsState(initial = null)
    val username by accountViewModel.username.collectAsState()
    val isModerator by accountViewModel.isModerator.collectAsState()
    var selectedServer by remember { mutableStateOf<CommunityServer?>(null) }
    var editingTiding by remember { mutableStateOf<NewsArticle?>(null) }
    val communityServersViewModel = remember(token) { CommunityServersViewModel(token) }

    // Presentation mode & Captain's Dispatch visibility
    val context = androidx.compose.ui.platform.LocalContext.current
    val appSettings = (context.applicationContext as BurkeBlackApplication).appSettings
    val presentationMode by appSettings.presentationModeFlow.collectAsState(initial = false)
    val showCaptainsDispatch by appSettings.showCaptainsDispatchFlow.collectAsState(initial = false)

    val isBurke = username.equals("burkeblack", ignoreCase = true)
    val isSwifty = username.equals("swiftyspiffy", ignoreCase = true)
    val showCaptainButton = isBurke || (isSwifty && showCaptainsDispatch)
    val showCrewDispatchButton = !presentationMode && isModerator
    val showModPanel = !presentationMode && isModerator

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAccount = { navController.navigate(Screen.Account.route) }
            )
        }

        composable(Screen.Crew.route) {
            CrewScreen(
                username = username,
                onNavigateToClipVoting = { navController.navigate(Screen.ClipVoting.route) },
                onNavigateToCommunityServers = { navController.navigate(Screen.CommunityServers.route) },
                onNavigateToEmotes = { navController.navigate(Screen.EmotesGallery.route) },
                onNavigateToStudio = { navController.navigate(Screen.Studio.route) },
                onNavigateToFaq = { navController.navigate(Screen.Faq.route) },
                onNavigateToLateShift = { navController.navigate(Screen.LateShift.route) }
            )
        }

        composable(Screen.ClipVoting.route) {
            val clipVotingViewModel = remember(token) { ClipVotingViewModel(token) }
            ClipVotingScreen(
                viewModel = clipVotingViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CommunityServers.route) {
            CommunityServersScreen(
                viewModel = communityServersViewModel,
                onBack = { navController.popBackStack() },
                onServerClick = { server ->
                    selectedServer = server
                    navController.navigate(Screen.ServerDetail.route)
                }
            )
        }

        composable(Screen.ServerDetail.route) {
            val csIsLoading by communityServersViewModel.isLoading.collectAsState()
            selectedServer?.let { server ->
                ServerDetailScreen(
                    server = server,
                    onBack = { navController.popBackStack() },
                    isRefreshing = csIsLoading,
                    onRefresh = { communityServersViewModel.fetchServers() }
                )
            }
        }

        composable(Screen.Scrolls.route) {
            ScrollsScreen(
                onNavigateToStudio = { navController.navigate(Screen.Studio.route) },
                onNavigateToFaq = { navController.navigate(Screen.Faq.route) }
            )
        }

        composable(Screen.Studio.route) {
            StudioScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Faq.route) {
            FaqScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Account.route) {
            AccountScreen(
                viewModel = accountViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToSoundbytes = {
                    if (token != null) navController.navigate(Screen.Soundbytes.route)
                },
                onNavigateToGiveaways = {
                    if (token != null) navController.navigate(Screen.Giveaways.route)
                },
                onNavigateToBits = {
                    if (token != null) navController.navigate(Screen.Bits.route)
                },
                onNavigateToDonations = {
                    if (token != null) navController.navigate(Screen.Donations.route)
                },
                onNavigateToFeedback = {
                    if (token != null) navController.navigate(Screen.Feedback.route)
                },
                onNavigateToModPanel = {
                    if (token != null && showModPanel) navController.navigate(Screen.ModPanel.route)
                },
                onNavigateToCaptainsDispatch = if (showCaptainButton) {
                    { token?.let { navController.navigate(Screen.CaptainsDispatch.route) } }
                } else null,
                onNavigateToCrewDispatch = if (showCrewDispatchButton) {
                    { token?.let { navController.navigate(Screen.CrewDispatch.route) } }
                } else null
            )
        }

        composable(Screen.Socials.route) {
            SocialsScreen()
        }

        composable(Screen.Soundbytes.route) {
            token?.let { t ->
                val soundbytesViewModel = remember(t) { SoundbytesViewModel(t) }
                SoundbytesScreen(
                    viewModel = soundbytesViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.Giveaways.route) {
            token?.let { t ->
                GiveawaysScreen(token = t, onBack = { navController.popBackStack() })
            }
        }

        composable(Screen.Bits.route) {
            token?.let { t ->
                BitsScreen(token = t, onBack = { navController.popBackStack() })
            }
        }

        composable(Screen.Donations.route) {
            token?.let { t ->
                DonationsScreen(token = t, onBack = { navController.popBackStack() })
            }
        }

        composable(Screen.Feedback.route) {
            FeedbackScreen(
                token = token ?: "",
                username = username,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ModPanel.route) {
            token?.let { t ->
                val modViewModel = remember(t) { ModPanelViewModel(t) }
                ModPanelScreen(
                    viewModel = modViewModel,
                    username = username,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.About.route) {
            AboutScreen(
                onBack = { navController.popBackStack() },
                onNavigateToFeedback = { navController.navigate(Screen.Feedback.route) },
                onNavigateToAdvanced = { navController.navigate(Screen.Advanced.route) }
            )
        }

        composable(Screen.Advanced.route) {
            AdvancedScreen(
                token = token,
                username = username,
                onBack = { navController.popBackStack() }
            )
        }

        // New screens
        composable(Screen.Rigging.route) {
            RiggingScreen(
                onBack = { navController.popBackStack() },
                onNavigateToNotificationSettings = { navController.navigate(Screen.NotificationSettings.route) },
                onNavigateToGiveawaySettings = { navController.navigate(Screen.GiveawaySettings.route) },
                onNavigateToAppearance = { navController.navigate(Screen.Appearance.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) }
            )
        }

        composable(Screen.NotificationSettings.route) {
            NotificationSettingsScreen(
                token = token,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.GiveawaySettings.route) {
            GiveawaySettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Appearance.route) {
            AppearanceScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Tidings.route) {
            var tidingsRefreshKey by remember { mutableStateOf(0) }
            // Bump refresh key when nav stack returns here (after a successful create).
            val backStackEntry by navController.currentBackStackEntryAsState()
            LaunchedEffect(backStackEntry?.destination?.route) {
                if (backStackEntry?.destination?.route == Screen.Tidings.route) {
                    tidingsRefreshKey++
                }
            }
            TidingsScreen(
                initialArticleId = pendingTidingsArticleId,
                token = token,
                onNewTiding = if (token != null) {
                    { navController.navigate(Screen.NewTiding.route) }
                } else null,
                onShowDeleted = if (token != null) {
                    { navController.navigate(Screen.DeletedTidings.route) }
                } else null,
                onEditTiding = if (token != null) {
                    { article ->
                        editingTiding = article
                        navController.navigate(Screen.EditTiding.route)
                    }
                } else null,
                refreshKey = tidingsRefreshKey
            )
            LaunchedEffect(Unit) { onTidingsArticleConsumed() }
        }

        composable(Screen.NewTiding.route) {
            token?.let { t ->
                NewTidingScreen(
                    token = t,
                    onBack = { navController.popBackStack() },
                    onCreated = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.EditTiding.route) {
            val article = editingTiding
            if (token != null && article != null) {
                NewTidingScreen(
                    token = token!!,
                    onBack = { navController.popBackStack() },
                    onCreated = {
                        editingTiding = null
                        navController.popBackStack()
                    },
                    existing = article,
                    existingVisibleIos = article.isVisibleOnIos == 1,
                    existingVisibleAndroid = article.isVisibleOnAndroid == 1,
                    existingVisibleWebsite = article.isVisibleOnWebsite == 1
                )
            }
        }

        composable(Screen.DeletedTidings.route) {
            token?.let { t ->
                DeletedTidingsScreen(
                    token = t,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.CaptainsDispatch.route) {
            token?.let { t ->
                DispatchScreen(
                    token = t,
                    mode = DispatchMode.CAPTAIN,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.CrewDispatch.route) {
            token?.let { t ->
                DispatchScreen(
                    token = t,
                    mode = DispatchMode.CREW,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.LateShift.route) {
            LateShiftScreen(token = token, onBack = { navController.popBackStack() })
        }

        composable(Screen.EmotesGallery.route) {
            EmotesGalleryScreen(
                token = token,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
