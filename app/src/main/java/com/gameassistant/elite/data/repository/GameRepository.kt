package com.gameassistant.elite.data.repository

import com.gameassistant.elite.domain.model.AuthStatus
import com.gameassistant.elite.domain.model.GameInfo
import com.gameassistant.elite.domain.model.GameLaunchStatus
import kotlinx.coroutines.flow.Flow

/**
 * 游戏数据仓库接口
 */
interface GameRepository {
    
    /**
     * 获取支持的游戏列表
     */
    suspend fun getSupportedGames(): List<GameInfo>
    
    /**
     * 检查游戏是否已安装
     */
    suspend fun checkGameInstallation(packageName: String): Boolean
    
    /**
     * 获取授权状态
     */
    suspend fun getAuthStatus(): AuthStatus
    
    /**
     * 保存卡密
     */
    suspend fun saveCardKey(cardKey: String): Boolean
    
    /**
     * 启动游戏辅助
     */
    suspend fun launchGameAssist(gameId: String): Boolean
    
    /**
     * 获取游戏启动状态流
     */
    fun getGameLaunchStatusFlow(): Flow<GameLaunchStatus>
    
    /**
     * 停止游戏辅助
     */
    suspend fun stopGameAssist(): Boolean
}