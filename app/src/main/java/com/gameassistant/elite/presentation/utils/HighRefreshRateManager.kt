package com.gameassistant.elite.presentation.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.Display
import android.view.WindowManager
import androidx.annotation.RequiresApi

/**
 * 高刷新率显示管理器
 * 负责检测和启用设备的高刷新率显示模式
 */
object HighRefreshRateManager {
    
    private const val TAG = "HighRefreshRateManager"
    private const val MIN_HIGH_REFRESH_RATE = 90f
    
    /**
     * 显示模式信息
     */
    data class DisplayModeInfo(
        val modeId: Int,
        val width: Int,
        val height: Int,
        val refreshRate: Float,
        val isHighRefreshRate: Boolean = refreshRate >= MIN_HIGH_REFRESH_RATE
    )
    
    /**
     * 刷新率设置结果
     */
    sealed class RefreshRateResult {
        object Success : RefreshRateResult()
        object NotSupported : RefreshRateResult()
        data class Error(val message: String) : RefreshRateResult()
    }
    
    /**
     * 获取设备支持的所有显示模式
     */
    fun getSupportedDisplayModes(context: Context): List<DisplayModeInfo> {
        return try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.display
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay
            }
            
            display?.supportedModes?.map { mode ->
                DisplayModeInfo(
                    modeId = mode.modeId,
                    width = mode.physicalWidth,
                    height = mode.physicalHeight,
                    refreshRate = mode.refreshRate
                )
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get supported display modes", e)
            emptyList()
        }
    }
    
    /**
     * 获取当前显示模式
     */
    fun getCurrentDisplayMode(context: Context): DisplayModeInfo? {
        return try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.display
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay
            }
            
            display?.mode?.let { mode ->
                DisplayModeInfo(
                    modeId = mode.modeId,
                    width = mode.physicalWidth,
                    height = mode.physicalHeight,
                    refreshRate = mode.refreshRate
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get current display mode", e)
            null
        }
    }
    
    /**
     * 检查设备是否支持高刷新率
     */
    fun isHighRefreshRateSupported(context: Context): Boolean {
        val supportedModes = getSupportedDisplayModes(context)
        return supportedModes.any { it.isHighRefreshRate }
    }
    
    /**
     * 获取最佳的高刷新率模式
     */
    fun getBestHighRefreshRateMode(context: Context): DisplayModeInfo? {
        val supportedModes = getSupportedDisplayModes(context)
        return supportedModes
            .filter { it.isHighRefreshRate }
            .maxByOrNull { it.refreshRate }
    }
    
    /**
     * 为Activity启用高刷新率显示
     */
    fun enableHighRefreshRate(activity: Activity): RefreshRateResult {
        return try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return RefreshRateResult.NotSupported
            }
            
            val bestMode = getBestHighRefreshRateMode(activity)
            if (bestMode == null) {
                Log.i(TAG, "No high refresh rate mode available")
                return RefreshRateResult.NotSupported
            }
            
            // 设置首选显示模式
            val layoutParams = activity.window.attributes
            layoutParams.preferredDisplayModeId = bestMode.modeId
            activity.window.attributes = layoutParams
            
            // Android 11+ 设置帧率
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                setFrameRateForAndroid11Plus(activity, bestMode.refreshRate)
            }
            
            Log.i(TAG, "High refresh rate enabled: ${bestMode.refreshRate}Hz")
            RefreshRateResult.Success
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enable high refresh rate", e)
            RefreshRateResult.Error(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Android 11+ 设置帧率
     */
    @RequiresApi(Build.VERSION_CODES.R)
    private fun setFrameRateForAndroid11Plus(activity: Activity, refreshRate: Float) {
        try {
            activity.window.decorView.rootSurfaceControl?.let { surfaceControl ->
                // 使用反射调用setFrameRate方法
                val setFrameRateMethod = surfaceControl.javaClass.getMethod(
                    "setFrameRate",
                    Float::class.java,
                    Int::class.java
                )
                // 参数：帧率，兼容性模式(1=FRAME_RATE_COMPATIBILITY_DEFAULT)
                setFrameRateMethod.invoke(surfaceControl, refreshRate, 1)
                Log.d(TAG, "Frame rate set to ${refreshRate}Hz using SurfaceControl")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to set frame rate using SurfaceControl", e)
        }
    }
    
    /**
     * 启用硬件加速
     */
    fun enableHardwareAcceleration(activity: Activity) {
        try {
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            )
            Log.d(TAG, "Hardware acceleration enabled")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enable hardware acceleration", e)
        }
    }
    
    /**
     * 获取刷新率信息摘要
     */
    fun getRefreshRateInfo(context: Context): String {
        val currentMode = getCurrentDisplayMode(context)
        val supportedModes = getSupportedDisplayModes(context)
        val highRefreshModes = supportedModes.filter { it.isHighRefreshRate }
        
        return buildString {
            appendLine("=== 刷新率信息 ===")
            appendLine("当前模式: ${currentMode?.refreshRate ?: "未知"}Hz")
            appendLine("支持的模式数量: ${supportedModes.size}")
            appendLine("高刷新率模式: ${highRefreshModes.size}")
            if (highRefreshModes.isNotEmpty()) {
                appendLine("可用高刷新率:")
                highRefreshModes.forEach { mode ->
                    appendLine("  - ${mode.refreshRate}Hz (${mode.width}x${mode.height})")
                }
            }
        }
    }
}