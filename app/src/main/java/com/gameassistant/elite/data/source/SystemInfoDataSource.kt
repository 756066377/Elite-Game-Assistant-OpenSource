package com.gameassistant.elite.data.source

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import com.gameassistant.elite.domain.model.CpuInfo
import com.gameassistant.elite.domain.model.GpuInfo
import com.gameassistant.elite.domain.model.MemoryInfo
import com.gameassistant.elite.domain.model.SELinuxStatus
import com.gameassistant.elite.domain.model.SystemInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 系统信息数据源
 * 负责从系统获取硬件和内核信息
 */
@Singleton
class SystemInfoDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * 获取系统基本信息
     */
    fun getSystemInfo(): SystemInfo {
        return SystemInfo(
            kernelVersion = getKernelVersion(),
            buildFingerprint = Build.FINGERPRINT,
            androidVersion = "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            selinuxStatus = getSELinuxStatus()
        )
    }
    
    /**
     * 获取CPU信息
     */
    fun getCpuInfo(): CpuInfo {
        val coreCount = Runtime.getRuntime().availableProcessors()
        val frequencies = getCpuFrequencies()
        val usages = getCpuUsages()
        val temperature = getCpuTemperature()
        
        return CpuInfo(
            coreCount = coreCount,
            currentFrequencies = frequencies.first,
            maxFrequencies = frequencies.second,
            usagePercentages = usages,
            temperature = temperature
        )
    }
    
    /**
     * 获取GPU信息
     */
    fun getGpuInfo(): GpuInfo {
        // 注意：GPU信息获取在Android中比较受限，这里提供基础实现
        return GpuInfo(
            renderer = getGpuRenderer(),
            vendor = getGpuVendor(),
            currentFrequency = 0L, // 需要root权限或特殊API
            maxFrequency = 0L,
            usage = 0f
        )
    }
    
    /**
     * 获取内存信息
     */
    fun getMemoryInfo(): MemoryInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        
        val totalRam = memInfo.totalMem
        val availableRam = memInfo.availMem
        val usedRam = totalRam - availableRam
        val usagePercentage = (usedRam.toFloat() / totalRam * 100)
        
        return MemoryInfo(
            totalRam = totalRam,
            availableRam = availableRam,
            usedRam = usedRam,
            usagePercentage = usagePercentage
        )
    }
    
    /**
     * 获取内核版本
     */
    private fun getKernelVersion(): String {
        return try {
            System.getProperty("os.version") ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    /**
     * 获取SELinux状态
     */
    private fun getSELinuxStatus(): SELinuxStatus {
        try {
            val process = Runtime.getRuntime().exec("getenforce")
            val reader = BufferedReader(process.inputStream.reader())
            val status = reader.readLine()?.trim()
            reader.close()
            process.waitFor()

            return when (status?.lowercase()) {
                "enforcing" -> SELinuxStatus.ENFORCING
                "permissive" -> SELinuxStatus.PERMISSIVE
                "disabled" -> SELinuxStatus.DISABLED
                else -> SELinuxStatus.UNKNOWN
            }
        } catch (e: IOException) {
            return SELinuxStatus.UNKNOWN
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt() // 恢复中断状态
            return SELinuxStatus.UNKNOWN
        }
    }
    
    /**
     * 获取CPU频率信息
     * @return Pair<当前频率列表, 最大频率列表>
     */
    private fun getCpuFrequencies(): Pair<List<Long>, List<Long>> {
        val currentFreqs = mutableListOf<Long>()
        val maxFreqs = mutableListOf<Long>()
        
        try {
            val coreCount = Runtime.getRuntime().availableProcessors()
            for (i in 0 until coreCount) {
                // 当前频率
                val currentFreqFile = File("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq")
                if (currentFreqFile.exists()) {
                    val freq = currentFreqFile.readText().trim().toLongOrNull() ?: 0L
                    currentFreqs.add(freq)
                } else {
                    currentFreqs.add(0L)
                }
                
                // 最大频率
                val maxFreqFile = File("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq")
                if (maxFreqFile.exists()) {
                    val freq = maxFreqFile.readText().trim().toLongOrNull() ?: 0L
                    maxFreqs.add(freq)
                } else {
                    maxFreqs.add(0L)
                }
            }
        } catch (e: Exception) {
            // 如果无法读取，返回空列表
        }
        
        return Pair(currentFreqs, maxFreqs)
    }
    
    /**
     * 获取CPU使用率
     */
    private fun getCpuUsages(): List<Float> {
        // 简化实现，实际需要读取/proc/stat并计算
        val coreCount = Runtime.getRuntime().availableProcessors()
        return List(coreCount) { (Math.random() * 100).toFloat() } // 模拟数据
    }
    
    /**
     * 获取CPU温度
     */
    private fun getCpuTemperature(): Float {
        return try {
            val thermalFiles = listOf(
                "/sys/class/thermal/thermal_zone0/temp",
                "/sys/class/thermal/thermal_zone1/temp",
                "/sys/devices/system/cpu/cpu0/cpufreq/cpu_temp"
            )
            
            for (path in thermalFiles) {
                val file = File(path)
                if (file.exists()) {
                    val temp = file.readText().trim().toFloatOrNull()
                    if (temp != null) {
                        return if (temp > 1000) temp / 1000f else temp
                    }
                }
            }
            0f
        } catch (e: Exception) {
            0f
        }
    }
    
    /**
     * 获取GPU渲染器信息
     */
    private fun getGpuRenderer(): String {
        return try {
            // 这需要OpenGL上下文，这里返回基础信息
            Build.HARDWARE
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    /**
     * 获取GPU厂商信息
     */
    private fun getGpuVendor(): String {
        return try {
            // 基于硬件信息推测GPU厂商
            val hardware = Build.HARDWARE.lowercase()
            when {
                hardware.contains("qcom") || hardware.contains("msm") -> "Qualcomm Adreno"
                hardware.contains("exynos") -> "ARM Mali"
                hardware.contains("kirin") -> "ARM Mali"
                hardware.contains("mtk") -> "PowerVR/Mali"
                else -> "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }
}