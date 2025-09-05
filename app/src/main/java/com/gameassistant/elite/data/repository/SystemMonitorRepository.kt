package com.gameassistant.elite.data.repository

import com.gameassistant.elite.domain.model.BatteryInfo
import com.gameassistant.elite.domain.model.CpuInfo
import com.gameassistant.elite.domain.model.GpuInfo
import com.gameassistant.elite.domain.model.MemoryInfo
import com.gameassistant.elite.domain.model.SystemInfo
import kotlinx.coroutines.flow.Flow

/**
 * 系统监控数据仓库接口
 */
interface SystemMonitorRepository {
    
    /**
     * 获取系统基本信息
     */
    suspend fun getSystemInfo(): SystemInfo
    
    /**
     * 获取CPU信息流
     */
    fun getCpuInfoFlow(): Flow<CpuInfo>
    
    /**
     * 获取GPU信息流
     */
    fun getGpuInfoFlow(): Flow<GpuInfo>
    
    /**
     * 获取内存信息流
     */
    fun getMemoryInfoFlow(): Flow<MemoryInfo>
    
    /**
     * 获取电池信息流
     */
    fun getBatteryInfoFlow(): Flow<BatteryInfo>
    

    
    /**
     * 写入卡密到系统文件
     * @param cardKey 卡密内容
     * @param gameType 游戏类型
     */
    suspend fun writeCardKey(cardKey: String, gameType: String = "default"): Boolean
    
    /**
     * 检查卡密文件是否存在
     * @param gameType 游戏类型
     */
    suspend fun doesCardKeyFileExist(gameType: String = "default"): Boolean
}