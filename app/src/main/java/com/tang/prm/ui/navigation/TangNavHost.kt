package com.tang.prm.ui.navigation

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SystemUpdateAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tang.prm.domain.model.UpdateResult
import com.tang.prm.feature.profile.UpdateViewModel
import com.tang.prm.feature.chat.navigation.chatGraph
import com.tang.prm.feature.circle.navigation.circleGraph
import com.tang.prm.feature.divination.navigation.divinationGraph
import com.tang.prm.feature.events.navigation.eventsGraph
import com.tang.prm.feature.gifts.navigation.giftsGraph
import com.tang.prm.feature.home.navigation.homeGraph
import com.tang.prm.feature.people.navigation.peopleGraph
import com.tang.prm.feature.profile.navigation.profileGraph
import com.tang.prm.feature.recipes.navigation.recipesGraph
import com.tang.prm.feature.reflect.navigation.reflectGraph
import com.tang.prm.feature.remember.navigation.rememberGraph
import com.tang.prm.feature.subscription.navigation.subscriptionGraph
import com.tang.prm.ui.components.GlassBottomBar
import com.tang.prm.ui.components.GlassSideBar
import com.tang.prm.ui.navigation.SettingsRoute
import com.tang.prm.ui.theme.DialogDefaults

@Composable
fun TangNavHost(
    tabletModeEnabled: Boolean = false,
    navController: NavHostController = rememberNavController(),
    updateViewModel: UpdateViewModel = hiltViewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

    val bottomRoutes = bottomNavItems.map { it.route }.toSet()

    var overlayVisible by remember { mutableStateOf(false) }

    val transitions = remember(bottomRoutes) { navTransitions(bottomRoutes) }

    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var updateInfo by remember { mutableStateOf<UpdateResult.HasUpdate?>(null) }

    val configuration = LocalConfiguration.current
    val isTabletLayout = tabletModeEnabled && configuration.screenWidthDp >= 600

    LaunchedEffect(Unit) {
        val currentVersion = runCatching {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        }.getOrDefault("1.0.0")

        updateViewModel.checkForUpdate(currentVersion) { result ->
            if (result is UpdateResult.HasUpdate) {
                updateInfo = result
            }
        }
    }

    val info = updateInfo
    if (info != null) {
        AlertDialog(
            onDismissRequest = { updateInfo = null },
            containerColor = DialogDefaults.containerColor,
            icon = {
                Icon(
                    Icons.Outlined.SystemUpdateAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    "发现新版本",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    "结绳 v${info.latestVersion} 已发布，是否前往下载更新？",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val url = info.releaseUrl
                    updateInfo = null
                    try {
                        uriHandler.openUri(url)
                    } catch (e: Exception) {
                        Log.w("TangNavHost", "打开更新链接失败: $url", e)
                    }
                }) {
                    Text("立即更新", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { updateInfo = null }) {
                    Text("稍后再说")
                }
            }
        )
    }

    val navigateTo: (Any) -> Unit = { routeObject ->
        navController.navigate(routeObject) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    if (isTabletLayout) {
        Row(modifier = Modifier.fillMaxSize()) {
            // 侧边栏使用水平展开/收起动画，避免详情页时瞬间消失的割裂感
            AnimatedVisibility(
                visible = showBottomBar && !overlayVisible,
                enter = expandHorizontally(
                    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                ) + fadeIn(tween(220)),
                exit = shrinkHorizontally(
                    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                ) + fadeOut(tween(180))
            ) {
                GlassSideBar(
                    items = bottomNavItems,
                    currentDestination = currentDestination,
                    onNavigate = navigateTo,
                    onSettingsClick = { navController.navigate(SettingsRoute) }
                )
            }

            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = HomeRoute,
                    modifier = Modifier.fillMaxSize(),
                    enterTransition = transitions.enterTransition,
                    exitTransition = transitions.exitTransition,
                    popEnterTransition = transitions.popEnterTransition,
                    popExitTransition = transitions.popExitTransition
                ) {
                    homeGraph(navController, isTabletLayout)
                    giftsGraph(navController)
                    reflectGraph(navController, isTabletLayout)
                    circleGraph(navController)
                    divinationGraph(navController)
                    subscriptionGraph(navController)
                    peopleGraph(navController, { overlayVisible = it }, isTabletLayout)
                    rememberGraph(navController, isTabletLayout)
                    profileGraph(navController)
                    eventsGraph(navController, isTabletLayout)
                    chatGraph(navController, isTabletLayout)
                    recipesGraph(navController)
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = HomeRoute,
                modifier = Modifier.fillMaxSize(),
                enterTransition = transitions.enterTransition,
                exitTransition = transitions.exitTransition,
                popEnterTransition = transitions.popEnterTransition,
                popExitTransition = transitions.popExitTransition
            ) {
                homeGraph(navController, isTabletLayout)
                giftsGraph(navController)
                reflectGraph(navController)
                circleGraph(navController)
                divinationGraph(navController)
                subscriptionGraph(navController)
                peopleGraph(navController, { overlayVisible = it })
                rememberGraph(navController)
                profileGraph(navController)
                eventsGraph(navController)
                chatGraph(navController)
                recipesGraph(navController)
            }

            if (showBottomBar && !overlayVisible) {
                GlassBottomBar(
                    items = bottomNavItems,
                    currentDestination = currentDestination,
                    onNavigate = navigateTo,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
