package com.gameassistant.elite.presentation.utils

import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

/**
 * 触摸事件优化器
 * 提供高性能的滑动手势处理和惯性滚动优化
 */
class TouchEventOptimizer {
    
    companion object {
        // 触摸响应阈值
        const val TOUCH_SLOP_DP = 8f
        const val MIN_FLING_VELOCITY = 400f
        const val MAX_FLING_VELOCITY = 8000f
        
        // 惯性滚动参数
        const val FLING_DECAY_RATE = 0.35f
        const val VELOCITY_THRESHOLD = 50f
        
        // 性能优化参数
        const val TOUCH_EVENT_BUFFER_SIZE = 16
        const val VELOCITY_TRACKER_HISTORY_SIZE = 20
    }
    
    /**
     * 触摸事件数据
     */
    @Stable
    data class TouchEvent(
        val position: Offset,
        val timestamp: Long,
        val eventType: TouchEventType
    )
    
    /**
     * 触摸事件类型
     */
    enum class TouchEventType {
        DOWN, MOVE, UP, CANCEL
    }
    
    /**
     * 速度追踪器
     */
    @Stable
    class VelocityTracker {
        private val events = mutableListOf<TouchEvent>()
        
        fun addEvent(event: TouchEvent) {
            events.add(event)
            // 保持历史记录在合理范围内
            if (events.size > VELOCITY_TRACKER_HISTORY_SIZE) {
                events.removeAt(0)
            }
        }
        
        fun calculateVelocity(): Velocity {
            if (events.size < 2) return Velocity.Zero
            
            val recent = events.takeLast(5)
            if (recent.size < 2) return Velocity.Zero
            
            val first = recent.first()
            val last = recent.last()
            val timeDelta = (last.timestamp - first.timestamp).coerceAtLeast(1)
            
            val velocityX = (last.position.x - first.position.x) / timeDelta * 1000
            val velocityY = (last.position.y - first.position.y) / timeDelta * 1000
            
            return Velocity(velocityX, velocityY)
        }
        
        fun clear() {
            events.clear()
        }
    }
}

/**
 * 优化的滑动手势修饰符
 * 提供更流畅的触摸响应和惯性滚动
 */
@Composable
fun Modifier.optimizedScrollGestures(
    lazyListState: LazyListState,
    enabled: Boolean = true,
    reverseDirection: Boolean = false
): Modifier {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    
    // 触摸事件缓冲区
    val touchEventChannel = remember { Channel<TouchEventOptimizer.TouchEvent>(TouchEventOptimizer.TOUCH_EVENT_BUFFER_SIZE) }
    val velocityTracker = remember { TouchEventOptimizer.VelocityTracker() }
    
    // 触摸阈值
    val touchSlop = with(density) { TouchEventOptimizer.TOUCH_SLOP_DP.dp.toPx() }
    
    // 滑动状态
    var isDragging by remember { mutableStateOf(false) }
    var totalDelta by remember { mutableStateOf(0f) }
    
    return if (enabled) {
        this.pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { offset ->
                    isDragging = true
                    totalDelta = 0f
                    velocityTracker.clear()
                    
                    val event = TouchEventOptimizer.TouchEvent(
                        position = offset,
                        timestamp = System.currentTimeMillis(),
                        eventType = TouchEventOptimizer.TouchEventType.DOWN
                    )
                    velocityTracker.addEvent(event)
                },
                onDragEnd = {
                    if (isDragging) {
                        val velocity = velocityTracker.calculateVelocity()
                        val finalVelocity = if (reverseDirection) -velocity.y else velocity.y
                        
                        // 启动惯性滚动
                        if (abs(finalVelocity) > TouchEventOptimizer.MIN_FLING_VELOCITY) {
                            coroutineScope.launch {
                                lazyListState.scrollBy(finalVelocity * TouchEventOptimizer.FLING_DECAY_RATE)
                            }
                        }
                    }
                    
                    isDragging = false
                    velocityTracker.clear()
                },
                onDrag = { change, dragAmount ->
                    val currentTime = System.currentTimeMillis()
                    val event = TouchEventOptimizer.TouchEvent(
                        position = change.position,
                        timestamp = currentTime,
                        eventType = TouchEventOptimizer.TouchEventType.MOVE
                    )
                    velocityTracker.addEvent(event)
                    
                    // 累积滑动距离
                    val delta = if (reverseDirection) -dragAmount.y else dragAmount.y
                    totalDelta += delta
                    
                    // 只有超过触摸阈值才开始滚动
                    if (abs(totalDelta) > touchSlop) {
                        coroutineScope.launch {
                            lazyListState.scrollBy(delta)
                        }
                    }
                }
            )
        }
    } else {
        this
    }
}

/**
 * 高性能滑动配置
 */
@Stable
data class HighPerformanceScrollConfig(
    val enableOverscroll: Boolean = true,
    val enableNestedScroll: Boolean = true,
    val flingBehavior: androidx.compose.foundation.gestures.FlingBehavior? = null,
    val touchSlop: Float = 8f,
    val velocityThreshold: Float = 50f
)

/**
 * 创建高性能滑动配置
 */
@Composable
fun rememberHighPerformanceScrollConfig(
    enableOverscroll: Boolean = true,
    enableNestedScroll: Boolean = true
): HighPerformanceScrollConfig {
    val density = LocalDensity.current
    
    val touchSlop = with(density) { 8.dp.toPx() }
    val flingBehavior = ScrollableDefaults.flingBehavior()
    
    return remember(enableOverscroll, enableNestedScroll, touchSlop) {
        HighPerformanceScrollConfig(
            enableOverscroll = enableOverscroll,
            enableNestedScroll = enableNestedScroll,
            flingBehavior = flingBehavior,
            touchSlop = touchSlop,
            velocityThreshold = 50f
        )
    }
}

/**
 * 触摸事件性能监控器
 */
@Stable
class TouchPerformanceMonitor {
    private var touchStartTime = 0L
    private var lastFrameTime = 0L
    private val frameTimes = mutableListOf<Long>()
    
    fun onTouchStart() {
        touchStartTime = System.nanoTime()
        lastFrameTime = touchStartTime
        frameTimes.clear()
    }
    
    fun onFrame() {
        val currentTime = System.nanoTime()
        if (lastFrameTime > 0) {
            frameTimes.add(currentTime - lastFrameTime)
        }
        lastFrameTime = currentTime
    }
    
    fun onTouchEnd(): TouchPerformanceMetrics {
        val totalTime = System.nanoTime() - touchStartTime
        val avgFrameTime = if (frameTimes.isNotEmpty()) {
            frameTimes.average()
        } else 0.0
        
        return TouchPerformanceMetrics(
            totalTouchTime = totalTime / 1_000_000.0, // 转换为毫秒
            averageFrameTime = avgFrameTime / 1_000_000.0, // 转换为毫秒
            frameCount = frameTimes.size,
            droppedFrames = frameTimes.count { it > 16_666_666 } // 超过16.67ms的帧
        )
    }
}

/**
 * 触摸性能指标
 */
@Stable
data class TouchPerformanceMetrics(
    val totalTouchTime: Double,
    val averageFrameTime: Double,
    val frameCount: Int,
    val droppedFrames: Int
) {
    val fps: Double get() = if (averageFrameTime > 0) 1000.0 / averageFrameTime else 0.0
    val frameDropRate: Double get() = if (frameCount > 0) droppedFrames.toDouble() / frameCount else 0.0
}

/**
 * 创建触摸性能监控器
 */
@Composable
fun rememberTouchPerformanceMonitor(): TouchPerformanceMonitor {
    return remember { TouchPerformanceMonitor() }
}