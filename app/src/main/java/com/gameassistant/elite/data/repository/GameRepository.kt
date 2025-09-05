package com.gameassistant.elite.data.repository


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
     * 检查卡密文件是否存在
     * @param gameType 游戏类型
     */
    suspend fun isCardKeyFilePresent(gameType: String = "default"): Boolean
    
    /**
     * 保存卡密
     * @param cardKey 卡密内容
     * @param gameType 游戏类型
     */
    suspend fun saveCardKey(cardKey: String, gameType: String = "default"): Boolean
    
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