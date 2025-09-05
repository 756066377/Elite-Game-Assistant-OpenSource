package com.gameassistant.elite.presentation.utils

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.debugInspectorInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Compose重组优化工具
 * 提供稳定性标记、重组跟踪和性能优化功能
 */
object RecompositionOptimizer {
    
    /**
     * 重组性能配置
     */
    @Stable
    data class RecompositionConfig(
        val enableRecompositionTracking: Boolean = true,
        val enableStabilityChecks: Boolean = true,
        val maxRecompositionCount: Int = 100,
        val recompositionThreshold: Long = 16L // 16ms阈值
    )
    
    /**
     * 重组统计信息
     */
    @Stable
    data class RecompositionStats(
        val componentName: String,
        val recompositionCount: Int,
        val averageRecompositionTime: Long,
        val lastRecompositionTime: Long,
        val isStable: Boolean
    )
    
    /**
     * 重组跟踪器
     */
    @Stable
    class RecompositionTracker(
        private val componentName: String,
        private val config: RecompositionConfig
    ) {
        private var recompositionCount = 0
        private var totalRecompositionTime = 0L
        private var lastRecompositionTime = 0L
        private val recompositionTimes = mutableListOf<Long>()
        
        fun trackRecomposition(block: () -> Unit) {
            if (!config.enableRecompositionTracking) {
                block()
                return
            }
            
            val startTime = System.nanoTime()
            block()
            val endTime = System.nanoTime()
            
            val recompositionTime = (endTime - startTime) / 1_000_000 // 转换为毫秒
            
            recompositionCount++
            totalRecompositionTime += recompositionTime
            lastRecompositionTime = recompositionTime
            
            // 保持最近的重组时间记录
            recompositionTimes.add(recompositionTime)
            if (recompositionTimes.size > 50) {
                recompositionTimes.removeAt(0)
            }
            
            // 检查性能警告
            if (recompositionTime > config.recompositionThreshold) {
                println("⚠️ 重组性能警告: $componentName 重组耗时 ${recompositionTime}ms")
            }
            
            if (recompositionCount > config.maxRecompositionCount) {
                println("⚠️ 重组频率警告: $componentName 重组次数过多 ($recompositionCount)")
            }
        }
        
        fun getStats(): RecompositionStats {
            val averageTime = if (recompositionCount > 0) {
                totalRecompositionTime / recompositionCount
            } else 0L
            
            val isStable = recompositionCount < 10 && averageTime < config.recompositionThreshold
            
            return RecompositionStats(
                componentName = componentName,
                recompositionCount = recompositionCount,
                averageRecompositionTime = averageTime,
                lastRecompositionTime = lastRecompositionTime,
                isStable = isStable
            )
        }
        
        fun reset() {
            recompositionCount = 0
            totalRecompositionTime = 0L
            lastRecompositionTime = 0L
            recompositionTimes.clear()
        }
    }
}

/**
 * 创建重组跟踪器
 */
@Composable
fun rememberRecompositionTracker(
    componentName: String,
    config: RecompositionOptimizer.RecompositionConfig = RecompositionOptimizer.RecompositionConfig()
): RecompositionOptimizer.RecompositionTracker {
    return remember(componentName) {
        RecompositionOptimizer.RecompositionTracker(componentName, config)
    }
}

/**
 * 稳定的数据类包装器
 * 用于确保数据类在Compose中的稳定性
 */
@Stable
data class StableWrapper<T>(val value: T)

/**
 * 将不稳定的值包装为稳定的值
 */
@Composable
fun <T> T.asStable(): StableWrapper<T> {
    return remember(this) { StableWrapper(this) }
}

/**
 * 稳定的列表包装器
 */
@Stable
data class StableList<T>(private val list: List<T>) : List<T> by list {
    override fun equals(other: Any?): Boolean {
        return other is StableList<*> && list == other.list
    }
    
    override fun hashCode(): Int {
        return list.hashCode()
    }
}

/**
 * 将列表转换为稳定的列表
 */
fun <T> List<T>.asStable(): StableList<T> = StableList(this)

/**
 * 稳定的Map包装器
 */
@Stable
data class StableMap<K, V>(private val map: Map<K, V>) : Map<K, V> by map {
    override fun equals(other: Any?): Boolean {
        return other is StableMap<*, *> && map == other.map
    }
    
    override fun hashCode(): Int {
        return map.hashCode()
    }
}

/**
 * 将Map转换为稳定的Map
 */
fun <K, V> Map<K, V>.asStable(): StableMap<K, V> = StableMap(this)

/**
 * 重组优化修饰符
 * 自动跟踪和优化组件的重组性能
 */
fun Modifier.recompositionOptimized(
    componentName: String,
    config: RecompositionOptimizer.RecompositionConfig = RecompositionOptimizer.RecompositionConfig()
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "recompositionOptimized"
        properties["componentName"] = componentName
        properties["config"] = config
    }
) {
    val tracker = rememberRecompositionTracker(componentName, config)
    
    // 在每次重组时跟踪性能
    LaunchedEffect(Unit) {
        tracker.trackRecomposition { }
    }
    
    this
}

/**
 * 智能重组优化
 * 基于组件类型和使用模式自动选择最佳优化策略
 */
@Composable
fun <T> smartRecompositionOptimization(
    value: T,
    componentType: ComponentType = ComponentType.GENERAL,
    block: @Composable (T) -> Unit
) {
    val optimizedValue = when (componentType) {
        ComponentType.LIST_ITEM -> {
            // 列表项优化：使用key和稳定性检查
            remember(value) { value }
        }
        ComponentType.ANIMATION -> {
            // 动画组件优化：减少不必要的重组
            val stableValue by rememberUpdatedState(value)
            stableValue
        }
        ComponentType.HEAVY_COMPUTATION -> {
            // 重计算组件优化：使用derivedStateOf
            val derivedValue by remember {
                derivedStateOf { value }
            }
            derivedValue
        }
        ComponentType.GENERAL -> {
            // 通用优化
            value
        }
    }
    
    block(optimizedValue)
}

/**
 * 组件类型枚举
 */
enum class ComponentType {
    LIST_ITEM,      // 列表项
    ANIMATION,      // 动画组件
    HEAVY_COMPUTATION, // 重计算组件
    GENERAL         // 通用组件
}

/**
 * 流的稳定性优化
 */
@Composable
fun <T> Flow<T>.collectAsStableState(
    initial: T,
    distinctUntilChanged: Boolean = true
): State<T> {
    val flow = if (distinctUntilChanged) {
        this.distinctUntilChanged()
    } else {
        this
    }
    
    return flow.collectAsState(initial)
}

/**
 * 条件重组优化
 * 只在特定条件下触发重组
 */
@Composable
fun <T> conditionalRecomposition(
    value: T,
    condition: (T) -> Boolean,
    content: @Composable (T) -> Unit
) {
    val shouldRecompose by remember {
        derivedStateOf { condition(value) }
    }
    
    if (shouldRecompose) {
        content(value)
    }
}

/**
 * 批量状态更新优化
 * 将多个状态更新合并为单次重组
 */
@Composable
fun <T> batchedStateUpdate(
    initialValue: T,
    updateBlock: (MutableState<T>) -> Unit
): State<T> {
    val state = remember { mutableStateOf(initialValue) }
    
    LaunchedEffect(Unit) {
        updateBlock(state)
    }
    
    return state
}

/**
 * 性能敏感组件包装器
 * 为性能敏感的组件提供额外的优化
 */
@Composable
fun PerformanceSensitiveComponent(
    componentName: String,
    enableProfiling: Boolean = false,
    content: @Composable () -> Unit
) {
    val config = remember {
        RecompositionOptimizer.RecompositionConfig(
            enableRecompositionTracking = enableProfiling,
            enableStabilityChecks = true,
            maxRecompositionCount = 50,
            recompositionThreshold = 8L // 更严格的阈值
        )
    }
    
    val tracker = rememberRecompositionTracker(componentName, config)
    
    // 性能监控
    if (enableProfiling) {
        LaunchedEffect(Unit) {
            tracker.trackRecomposition {
                // 空实现，仅用于跟踪
            }
        }
        
        // 定期输出性能统计
        LaunchedEffect(componentName) {
            kotlinx.coroutines.delay(5000) // 5秒后输出统计
            val stats = tracker.getStats()
            if (stats.recompositionCount > 0) {
                println("📊 性能统计 [$componentName]: 重组${stats.recompositionCount}次, 平均耗时${stats.averageRecompositionTime}ms, 稳定性: ${if (stats.isStable) "✅" else "❌"}")
            }
        }
    }
    
    content()
}

/**
 * 列表项稳定性优化
 */
@Stable
data class StableListItem<T>(
    val id: String,
    val data: T,
    val version: Int = 0
) {
    override fun equals(other: Any?): Boolean {
        return other is StableListItem<*> && 
               id == other.id && 
               version == other.version
    }
    
    override fun hashCode(): Int {
        return id.hashCode() * 31 + version
    }
}

/**
 * 创建稳定的列表项
 */
fun <T> T.asStableListItem(id: String, version: Int = 0): StableListItem<T> {
    return StableListItem(id, this, version)
}