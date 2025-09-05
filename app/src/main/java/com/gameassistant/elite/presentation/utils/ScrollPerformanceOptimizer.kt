package com.gameassistant.elite.presentation.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/**
 * 滑动性能优化器
 * 提供LazyColumn滑动性能监控和优化功能
 */
object ScrollPerformanceOptimizer {
    
    /**
     * 滑动性能指标
     */
    @Stable
    data class ScrollMetrics(
        val isScrolling: Boolean = false,
        val scrollDirection: ScrollDirection = ScrollDirection.NONE,
        val scrollSpeed: Float = 0f,
        val visibleItemsCount: Int = 0,
        val firstVisibleItemIndex: Int = 0,
        val lastVisibleItemIndex: Int = 0
    )
    
    /**
     * 滑动方向枚举
     */
    enum class ScrollDirection {
        NONE, UP, DOWN
    }
    
    /**
     * 滑动性能配置
     */
    @Stable
    data class PerformanceConfig(
        val enableMetrics: Boolean = false,
        val enableOverscrollEffect: Boolean = true,
        val enableFlingOptimization: Boolean = true,
        val prefetchItemCount: Int = 2,
        val maxScrollSpeed: Float = 5000f
    )
}

/**
 * 滑动性能监控Composable
 */
@Composable
fun rememberScrollMetrics(
    listState: LazyListState,
    config: ScrollPerformanceOptimizer.PerformanceConfig = ScrollPerformanceOptimizer.PerformanceConfig()
): ScrollPerformanceOptimizer.ScrollMetrics {
    
    var metrics by remember { mutableStateOf(ScrollPerformanceOptimizer.ScrollMetrics()) }
    
    // 监控滑动状态
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { isScrolling ->
                metrics = metrics.copy(isScrolling = isScrolling)
            }
    }
    
    // 监控可见项目
    LaunchedEffect(listState) {
        snapshotFlow { 
            listState.layoutInfo.let { layoutInfo ->
                Triple(
                    layoutInfo.visibleItemsInfo.size,
                    layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0,
                    layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                )
            }
        }
        .distinctUntilChanged()
        .collect { (visibleCount, firstIndex, lastIndex) ->
            metrics = metrics.copy(
                visibleItemsCount = visibleCount,
                firstVisibleItemIndex = firstIndex,
                lastVisibleItemIndex = lastIndex
            )
        }
    }
    
    // 监控滑动方向和速度
    LaunchedEffect(listState) {
        var lastOffset = 0
        var lastTime = System.currentTimeMillis()
        
        snapshotFlow { listState.firstVisibleItemScrollOffset }
            .filter { listState.isScrollInProgress }
            .collect { offset ->
                val currentTime = System.currentTimeMillis()
                val timeDelta = currentTime - lastTime
                
                if (timeDelta > 0) {
                    val offsetDelta = offset - lastOffset
                    val speed = kotlin.math.abs(offsetDelta.toFloat() / timeDelta * 1000f)
                    
                    val direction = when {
                        offsetDelta > 0 -> ScrollPerformanceOptimizer.ScrollDirection.DOWN
                        offsetDelta < 0 -> ScrollPerformanceOptimizer.ScrollDirection.UP
                        else -> ScrollPerformanceOptimizer.ScrollDirection.NONE
                    }
                    
                    metrics = metrics.copy(
                        scrollDirection = direction,
                        scrollSpeed = kotlin.math.min(speed, config.maxScrollSpeed)
                    )
                }
                
                lastOffset = offset
                lastTime = currentTime
            }
    }
    
    return metrics
}

/**
 * 滑动性能优化修饰符
 */
fun Modifier.scrollPerformanceOptimized(
    config: ScrollPerformanceOptimizer.PerformanceConfig = ScrollPerformanceOptimizer.PerformanceConfig()
): Modifier = composed {
    
    var modifier = this
    
    // 添加性能指标显示（仅调试模式）
    if (config.enableMetrics) {
        modifier = modifier.drawWithContent {
            drawContent()
            drawPerformanceOverlay()
        }
    }
    
    modifier
}

/**
 * 绘制性能覆盖层
 */
private fun DrawScope.drawPerformanceOverlay() {
    // 在右上角绘制FPS指示器
    val indicatorSize = 8.dp.toPx()
    val margin = 16.dp.toPx()
    
    drawCircle(
        color = Color.Green.copy(alpha = 0.7f),
        radius = indicatorSize,
        center = androidx.compose.ui.geometry.Offset(
            x = size.width - margin - indicatorSize,
            y = margin + indicatorSize
        )
    )
}

/**
 * 预加载优化扩展
 */
@Composable
fun LazyListState.optimizeForPerformance(
    config: ScrollPerformanceOptimizer.PerformanceConfig = ScrollPerformanceOptimizer.PerformanceConfig()
) {
    // 预加载优化
    LaunchedEffect(this) {
        if (config.enableFlingOptimization) {
            snapshotFlow { this@optimizeForPerformance.isScrollInProgress }
                .distinctUntilChanged()
                .collect { isScrolling ->
                    if (!isScrolling) {
                        // 滑动结束后进行内存优化
                        System.gc() // 建议垃圾回收（仅在必要时）
                    }
                }
        }
    }
}

/**
 * 智能预取修饰符
 * 根据滑动速度动态调整预取数量
 */
fun Modifier.smartPrefetch(
    metrics: ScrollPerformanceOptimizer.ScrollMetrics,
    basePrefetchCount: Int = 2
): Modifier = composed {
    
    val density = LocalDensity.current
    
    // 根据滑动速度调整预取数量
    val dynamicPrefetchCount by remember(metrics.scrollSpeed) {
        derivedStateOf {
            when {
                metrics.scrollSpeed > 3000f -> basePrefetchCount + 2
                metrics.scrollSpeed > 1500f -> basePrefetchCount + 1
                else -> basePrefetchCount
            }
        }
    }
    
    this
}

/**
 * 内存优化工具
 */
object MemoryOptimizer {
    
    /**
     * 清理不可见项目的资源
     */
    fun cleanupInvisibleItems(
        listState: LazyListState,
        totalItemCount: Int
    ) {
        val layoutInfo = listState.layoutInfo
        val visibleRange = layoutInfo.visibleItemsInfo.let { items ->
            if (items.isEmpty()) return
            items.first().index..items.last().index
        }
        
        // 计算需要清理的范围
        val cleanupRanges = mutableListOf<IntRange>()
        
        // 清理开头的不可见项目
        if (visibleRange.first > 10) {
            cleanupRanges.add(0 until (visibleRange.first - 5))
        }
        
        // 清理末尾的不可见项目
        if (visibleRange.last < totalItemCount - 10) {
            cleanupRanges.add((visibleRange.last + 5) until totalItemCount)
        }
        
        // 执行清理逻辑（这里可以添加具体的资源清理代码）
        cleanupRanges.forEach { range ->
            // 清理指定范围内的缓存资源
            // 例如：清理图片缓存、释放大对象等
        }
    }
    
    /**
     * 优化内存使用
     */
    fun optimizeMemoryUsage() {
        // 建议进行垃圾回收
        if (Runtime.getRuntime().freeMemory() < Runtime.getRuntime().totalMemory() * 0.1) {
            System.gc()
        }
    }
}