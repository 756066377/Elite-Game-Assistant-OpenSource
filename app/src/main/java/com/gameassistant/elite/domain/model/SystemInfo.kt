package com.gameassistant.elite.domain.model

/**
 * 系统信息数据模型
 */
data class SystemInfo(
    val kernelVersion: String = "",
    val buildFingerprint: String = "",
    val androidVersion: String = "",
    val selinuxStatus: SELinuxStatus = SELinuxStatus.UNKNOWN,
    val isRooted: Boolean = false
)

/**
 * SELinux状态枚举
 */
enum class SELinuxStatus {
    ENFORCING,
    PERMISSIVE,
    DISABLED,
    UNKNOWN
}

/**
 * CPU信息数据模型
 */
data class CpuInfo(
    val coreCount: Int = 0,
    val currentFrequencies: List<Long> = emptyList(),
    val maxFrequencies: List<Long> = emptyList(),
    val usagePercentages: List<Float> = emptyList(),
    val temperature: Float = 0f
)

/**
 * GPU信息数据模型
 */
data class GpuInfo(
    val renderer: String = "",
    val vendor: String = "",
    val currentFrequency: Long = 0L,
    val maxFrequency: Long = 0L,
    val usage: Float = 0f
)

/**
 * 内存信息数据模型
 */
data class MemoryInfo(
    val totalRam: Long = 0L,
    val availableRam: Long = 0L,
    val usedRam: Long = 0L,
    val usagePercentage: Float = 0f
)

/**
 * 电池信息数据模型
 */
data class BatteryInfo(
    val level: Int = 0,
    val temperature: Float = 0f,
    val voltage: Int = 0,
    val health: BatteryHealth = BatteryHealth.UNKNOWN,
    val isCharging: Boolean = false,
    val chargingType: ChargingType = ChargingType.NONE
)

/**
 * 电池健康状态枚举
 */
enum class BatteryHealth {
    GOOD,
    OVERHEAT,
    DEAD,
    OVER_VOLTAGE,
    COLD,
    UNKNOWN
}

/**
 * 充电类型枚举
 */
enum class ChargingType {
    NONE,
    AC,
    USB,
    WIRELESS
}