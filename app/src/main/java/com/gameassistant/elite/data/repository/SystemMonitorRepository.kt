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
     * 检查 Root 权限状态
     */
    suspend fun checkRootStatus(): Boolean
    
    /**
     * 写入卡密到系统文件
     */
    suspend fun writeCardKey(cardKey: String): Boolean
    
    /**
     * 读取卡密文件
     */
    suspend fun readCardKey(): String?
}