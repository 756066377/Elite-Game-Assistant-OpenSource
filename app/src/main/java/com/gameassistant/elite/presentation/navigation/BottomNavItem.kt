package com.gameassistant.elite.presentation.navigation

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 底部导航项的数据类
 * @param title 标题
 * @param selectedIcon 选中状态的图标
 * @param unselectedIcon 未选中状态的图标
 * @param route 导航路由
 */
data class BottomNavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)