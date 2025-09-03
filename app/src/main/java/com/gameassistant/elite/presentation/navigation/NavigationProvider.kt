package com.gameassistant.elite.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Games

/**
 * 提供底部导航项的列表和路由常量
 */
object NavigationProvider {
    /**
     * 导航项列表
     */
    val items = listOf(
        BottomNavItem(
            title = "仪表盘",
            selectedIcon = Icons.Filled.Dashboard,
            unselectedIcon = Icons.Outlined.Dashboard,
            route = "dashboard"
        ),
        BottomNavItem(
            title = "游戏增强",
            selectedIcon = Icons.Filled.Games,
            unselectedIcon = Icons.Outlined.Games,
            route = "game_enhance"
        )
    )

    // 定义路由常量
    const val ROUTE_DASHBOARD = "dashboard"
    const val ROUTE_GAME_ENHANCE = "game_enhance"
}