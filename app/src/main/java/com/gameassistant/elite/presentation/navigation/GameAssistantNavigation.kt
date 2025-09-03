package com.gameassistant.elite.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gameassistant.elite.presentation.components.navigation.GlassmorphismBottomBar
import com.gameassistant.elite.presentation.screens.dashboard.DashboardScreen
import com.gameassistant.elite.presentation.screens.gameenhance.GameEnhanceScreen

/**
 * 应用主导航组件
 * 包含底部导航栏和页面路由管理
 */
@Composable
fun GameAssistantNavigation(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    
    Scaffold(
        modifier = modifier,
        bottomBar = {
            GlassmorphismBottomBar(
                navController = navController,
                items = NavigationProvider.items
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavigationProvider.ROUTE_DASHBOARD,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(durationMillis = 300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(durationMillis = 300)
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(durationMillis = 300)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(durationMillis = 300)
                )
            }
        ) {
            composable(NavigationProvider.ROUTE_DASHBOARD) {
                DashboardScreen()
            }
            composable(NavigationProvider.ROUTE_GAME_ENHANCE) {
                GameEnhanceScreen()
            }
        }
    }
}