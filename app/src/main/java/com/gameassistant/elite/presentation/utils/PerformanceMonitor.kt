package com.gameassistant.elite.presentation.utils

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.roundToInt

/**
 * 性能监控工具
 * 提供实时帧率监控、滑动性能分析和系统资源监控
 */
object PerformanceMonitor {
    
    /**
     * 帧率监控器
     */
    @Stable
    class FrameRateMonitor {
        private var frameCount = 0
        private var lastTime = System.currentTimeMillis()
        private var currentFps = 0f
        private val fpsHistory = mutableListOf<Float>()
        
        fun onFrame() {
            frameCount++
            val currentTime = System.currentTimeMillis()
            val deltaTime = currentTime - lastTime
            
            if (deltaTime >= 1000) { // 每秒更新一次
                currentFps = frameCount * 1000f / deltaTime
                fpsHistory.add(currentFps)
                
                // 保持历史记录在合理范围内
                if (fpsHistory.size > 60) {
                    fpsHistory.removeAt(0)
                }
                
                frameCount = 0
                lastTime = currentTime
            }
        }
        
        fun getCurrentFps(): Float = currentFps
        
        fun getAverageFps(): Float {
            return if (fpsHistory.isNotEmpty()) {
                fpsHistory.average().toFloat()
            } else 0f
        }
        
        fun getMinFps(): Float = fpsHistory.minOrNull() ?: 0f
        fun getMaxFps(): Float = fpsHistory.maxOrNull() ?: 0f
        
        fun reset() {
            frameCount = 0
            currentFps = 0f
            fpsHistory.clear()
            lastTime = System.currentTimeMillis()
        }
    }
    
    /**
     * 滑动性能监控器
     */
    @Stable
    class ScrollPerformanceMonitor {
        private var scrollStartTime = 0L
        private var totalScrollDistance = 0f
        private var scrollEventCount = 0
        private val scrollSpeeds = mutableListOf<Float>()
        
        fun onScrollStart() {
            scrollStartTime = System.currentTimeMillis()
            totalScrollDistance = 0f
            scrollEventCount = 0
            scrollSpeeds.clear()
        }
        
        fun onScrollEvent(distance: Float) {
            totalScrollDistance += kotlin.math.abs(distance)
            scrollEventCount++
            
            val currentTime = System.currentTimeMillis()
            if (scrollStartTime > 0) {
                val deltaTime = currentTime - scrollStartTime
                if (deltaTime > 0) {
                    val speed = totalScrollDistance / deltaTime * 1000 // px/s
                    scrollSpeeds.add(speed)
                }
            }
        }
        
        fun onScrollEnd(): ScrollMetrics {
            val endTime = System.currentTimeMillis()
            val totalTime = endTime - scrollStartTime
            
            return ScrollMetrics(
                totalTime = totalTime,
                totalDistance = totalScrollDistance,
                eventCount = scrollEventCount,
                averageSpeed = scrollSpeeds.average().toFloat(),
                maxSpeed = scrollSpeeds.maxOrNull() ?: 0f
            )
        }
    }
    
    /**
     * 滑动指标
     */
    @Stable
    data class ScrollMetrics(
        val totalTime: Long,
        val totalDistance: Float,
        val eventCount: Int,
        val averageSpeed: Float,
        val maxSpeed: Float
    )
    
    /**
     * 系统性能指标
     */
    @Stable
    data class SystemPerformanceMetrics(
        val currentFps: Float,
        val averageFps: Float,
        val minFps: Float,
        val maxFps: Float,
        val memoryUsage: Long,
        val cpuUsage: Float,
        val batteryLevel: Int,
        val thermalState: String
    )
}

/**
 * 创建帧率监控器
 */
@Composable
fun rememberFrameRateMonitor(): PerformanceMonitor.FrameRateMonitor {
    return remember { PerformanceMonitor.FrameRateMonitor() }
}

/**
 * 创建滑动性能监控器
 */
@Composable
fun rememberScrollPerformanceMonitor(): PerformanceMonitor.ScrollPerformanceMonitor {
    return remember { PerformanceMonitor.ScrollPerformanceMonitor() }
}

/**
 * 实时性能监控组件
 */
@Composable
fun PerformanceOverlay(
    modifier: Modifier = Modifier,
    showFps: Boolean = true,
    showMemory: Boolean = true,
    showScrollMetrics: Boolean = false,
    lazyListState: LazyListState? = null
) {
    val frameRateMonitor = rememberFrameRateMonitor()
    val scrollMonitor = rememberScrollPerformanceMonitor()
    
    // 实时性能数据
    var performanceMetrics by remember { 
        mutableStateOf(
            PerformanceMonitor.SystemPerformanceMetrics(
                currentFps = 0f,
                averageFps = 0f,
                minFps = 0f,
                maxFps = 0f,
                memoryUsage = 0L,
                cpuUsage = 0f,
                batteryLevel = 100,
                thermalState = "Normal"
            )
        )
    }
    
    // 定期更新性能指标
    LaunchedEffect(Unit) {
        while (true) {
            frameRateMonitor.onFrame()
            
            // 获取内存使用情况
            val runtime = Runtime.getRuntime()
            val memoryUsage = runtime.totalMemory() - runtime.freeMemory()
            
            performanceMetrics = performanceMetrics.copy(
                currentFps = frameRateMonitor.getCurrentFps(),
                averageFps = frameRateMonitor.getAverageFps(),
                minFps = frameRateMonitor.getMinFps(),
                maxFps = frameRateMonitor.getMaxFps(),
                memoryUsage = memoryUsage
            )
            
            delay(100) // 每100ms更新一次
        }
    }
    
    // 监控滑动性能
    lazyListState?.let { listState ->
        LaunchedEffect(listState.isScrollInProgress) {
            if (listState.isScrollInProgress) {
                scrollMonitor.onScrollStart()
            } else {
                val metrics = scrollMonitor.onScrollEnd()
                if (showScrollMetrics) {
                    println("📊 滑动性能: 总时间${metrics.totalTime}ms, 距离${metrics.totalDistance.roundToInt()}px, 平均速度${metrics.averageSpeed.roundToInt()}px/s")
                }
            }
        }
    }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (showFps) {
                FpsIndicator(
                    currentFps = performanceMetrics.currentFps,
                    averageFps = performanceMetrics.averageFps
                )
            }
            
            if (showMemory) {
                MemoryIndicator(
                    memoryUsage = performanceMetrics.memoryUsage
                )
            }
        }
    }
}

/**
 * FPS指示器
 */
@Composable
private fun FpsIndicator(
    currentFps: Float,
    averageFps: Float
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "FPS:",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "${currentFps.roundToInt()}",
            color = when {
                currentFps >= 55 -> Color.Green
                currentFps >= 30 -> Color.Yellow
                else -> Color.Red
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "(avg: ${averageFps.roundToInt()})",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 10.sp
        )
    }
}

/**
 * 内存指示器
 */
@Composable
private fun MemoryIndicator(
    memoryUsage: Long
) {
    val memoryMB = memoryUsage / (1024 * 1024)
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "MEM:",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "${memoryMB}MB",
            color = when {
                memoryMB < 100 -> Color.Green
                memoryMB < 200 -> Color.Yellow
                else -> Color.Red
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 性能分析工具
 */
@Composable
fun PerformanceAnalyzer(
    enabled: Boolean = false,
    content: @Composable () -> Unit
) {
    if (enabled) {
        Box {
            content()
            
            PerformanceOverlay(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                showFps = true,
                showMemory = true
            )
        }
    } else {
        content()
    }
}

/**
 * 滑动性能分析修饰符
 */
fun Modifier.scrollPerformanceAnalysis(
    lazyListState: LazyListState,
    enabled: Boolean = true
): Modifier = this.then(
    if (enabled) {
        Modifier // 这里可以添加具体的滑动性能分析逻辑
    } else {
        Modifier
    }
)

/**
 * 性能基准测试
 */
@Composable
fun PerformanceBenchmark(
    testName: String,
    iterations: Int = 100,
    onResult: (BenchmarkResult) -> Unit = {},
    content: @Composable () -> Unit
) {
    var isRunning by remember { mutableStateOf(false) }
    var currentIteration by remember { mutableStateOf(0) }
    
    LaunchedEffect(testName) {
        if (!isRunning) {
            isRunning = true
            val startTime = System.nanoTime()
            
            repeat(iterations) { iteration ->
                currentIteration = iteration + 1
                delay(16) // 模拟16ms帧间隔
            }
            
            val endTime = System.nanoTime()
            val totalTime = (endTime - startTime) / 1_000_000 // 转换为毫秒
            
            val result = BenchmarkResult(
                testName = testName,
                iterations = iterations,
                totalTime = totalTime,
                averageTime = totalTime / iterations,
                fps = 1000f / (totalTime / iterations)
            )
            
            onResult(result)
            isRunning = false
        }
    }
    
    content()
}

/**
 * 基准测试结果
 */
@Stable
data class BenchmarkResult(
    val testName: String,
    val iterations: Int,
    val totalTime: Long,
    val averageTime: Long,
    val fps: Float
)

/**
 * 创建性能监控流
 */
@Composable
fun rememberPerformanceFlow(): Flow<PerformanceMonitor.SystemPerformanceMetrics> {
    val frameRateMonitor = rememberFrameRateMonitor()
    
    return remember {
        flow {
            while (true) {
                frameRateMonitor.onFrame()
                
                val runtime = Runtime.getRuntime()
                val memoryUsage = runtime.totalMemory() - runtime.freeMemory()
                
                val metrics = PerformanceMonitor.SystemPerformanceMetrics(
                    currentFps = frameRateMonitor.getCurrentFps(),
                    averageFps = frameRateMonitor.getAverageFps(),
                    minFps = frameRateMonitor.getMinFps(),
                    maxFps = frameRateMonitor.getMaxFps(),
                    memoryUsage = memoryUsage,
                    cpuUsage = 0f, // 需要系统API支持
                    batteryLevel = 100, // 需要系统API支持
                    thermalState = "Normal" // 需要系统API支持
                )
                
                emit(metrics)
                delay(100)
            }
        }
    }
}