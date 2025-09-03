package com.gameassistant.elite.presentation.navigation

import androidx.annotation.DrawableRes
import com.gameassistant.elite.R

/**
 * 导航项目定义
 * 定义应用中的主要导航页面
 */
sealed class NavigationItems(
    val route: String,
    val title: String,
    @DrawableRes val icon: Int
) {
    object Dashboard : NavigationItems(
        route = "dashboard",
        title = "仪表盘",
        icon = R.drawable.ic_dashboard
    )
    
    object GameEnhance : NavigationItems(
        route = "game_enhance",
        title = "游戏增强",
        icon = R.drawable.ic_game
    )
    
    companion object {
        val items = listOf(Dashboard, GameEnhance)
    }
}