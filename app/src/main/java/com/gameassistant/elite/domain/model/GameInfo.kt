package com.gameassistant.elite.domain.model

/**
 * 游戏信息数据模型
 */
data class GameInfo(
    val id: String,
    val name: String,
    val packageName: String,
    val iconResId: Int,
    val isInstalled: Boolean = false,
    val isSupported: Boolean = true,
    val features: List<GameFeature> = emptyList(),
    val description: String = ""
)

/**
 * 游戏功能特性
 */
data class GameFeature(
    val category: FeatureCategory,
    val name: String,
    val description: String
)

/**
 * 功能分类枚举
 */
enum class FeatureCategory {
    VISUAL_ENHANCEMENT,  // 视觉增强
    AIM_ASSIST,         // 瞄准辅助
    TACTICAL_ASSIST     // 战术辅助
}

/**
 * 卡密授权状态
 */
data class AuthStatus(
    val isAuthorized: Boolean = false,
    val cardKey: String = "",
    val expiryTime: Long = 0L,
    val remainingDays: Int = 0
)

/**
 * 游戏启动状态
 */
enum class GameLaunchStatus {
    IDLE,           // 空闲
    LOADING,        // 加载中
    RUNNING,        // 运行中
    ERROR,          // 错误
    UNAUTHORIZED    // 未授权
}