package com.gameassistant.elite.data.repository

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.gameassistant.elite.data.datasource.LibsuSystemInfoDataSource
import com.gameassistant.elite.data.source.SystemInfoDataSource
import com.gameassistant.elite.domain.model.BatteryHealth
import com.gameassistant.elite.domain.model.BatteryInfo
import com.gameassistant.elite.domain.model.ChargingType
import com.gameassistant.elite.domain.model.CpuInfo
import com.gameassistant.elite.domain.model.GpuInfo
import com.gameassistant.elite.domain.model.MemoryInfo
import com.gameassistant.elite.domain.model.SystemInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 系统监控数据仓库实现类
 */
@Singleton
class SystemMonitorRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val systemInfoDataSource: SystemInfoDataSource,
    private val libsuSystemInfoDataSource: LibsuSystemInfoDataSource
) : SystemMonitorRepository {
    
    override suspend fun getSystemInfo(): SystemInfo {
        return try {
            // 优先使用 libsu 获取更准确的系统信息
            libsuSystemInfoDataSource.getSystemInfo()
        } catch (e: Exception) {
            // 如果 libsu 失败，回退到原有方法
            systemInfoDataSource.getSystemInfo()
        }
    }
    
    override fun getCpuInfoFlow(): Flow<CpuInfo> = flow {
        while (true) {
            emit(systemInfoDataSource.getCpuInfo())
            delay(1000) // 每秒更新一次
        }
    }
    
    override fun getGpuInfoFlow(): Flow<GpuInfo> = flow {
        while (true) {
            emit(systemInfoDataSource.getGpuInfo())
            delay(2000) // 每2秒更新一次
        }
    }
    
    override fun getMemoryInfoFlow(): Flow<MemoryInfo> = flow {
        while (true) {
            emit(systemInfoDataSource.getMemoryInfo())
            delay(1000) // 每秒更新一次
        }
    }
    
    override fun getBatteryInfoFlow(): Flow<BatteryInfo> = flow {
        while (true) {
            emit(getBatteryInfo())
            delay(5000) // 每5秒更新一次
        }
    }
    
    /**
     * 获取电池信息
     */
    private fun getBatteryInfo(): BatteryInfo {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        
        return if (batteryIntent != null) {
            val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val temperature = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
            val voltage = batteryIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
            val health = batteryIntent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
            val status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
            val plugged = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
            
            val batteryLevel = if (level != -1 && scale != -1) {
                (level * 100 / scale.toFloat()).toInt()
            } else 0
            
            BatteryInfo(
                level = batteryLevel,
                temperature = temperature,
                voltage = voltage,
                health = mapBatteryHealth(health),
                isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING,
                chargingType = mapChargingType(plugged)
            )
        } else {
            BatteryInfo()
        }
    }
    
    /**
     * 映射电池健康状态
     */
    private fun mapBatteryHealth(health: Int): BatteryHealth {
        return when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> BatteryHealth.GOOD
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> BatteryHealth.OVERHEAT
            BatteryManager.BATTERY_HEALTH_DEAD -> BatteryHealth.DEAD
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> BatteryHealth.OVER_VOLTAGE
            BatteryManager.BATTERY_HEALTH_COLD -> BatteryHealth.COLD
            else -> BatteryHealth.UNKNOWN
        }
    }
    
    /**
     * 映射充电类型
     */
    private fun mapChargingType(plugged: Int): ChargingType {
        return when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> ChargingType.AC
            BatteryManager.BATTERY_PLUGGED_USB -> ChargingType.USB
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> ChargingType.WIRELESS
            else -> ChargingType.NONE
        }
    }
    

    
    override suspend fun writeCardKey(cardKey: String, gameType: String): Boolean {
        return libsuSystemInfoDataSource.writeCardKey(cardKey, gameType)
    }
    
    override suspend fun doesCardKeyFileExist(gameType: String): Boolean {
        return libsuSystemInfoDataSource.doesCardKeyFileExist(gameType)
    }
}