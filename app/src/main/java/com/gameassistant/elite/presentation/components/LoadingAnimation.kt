package com.gameassistant.elite.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.gameassistant.elite.presentation.theme.AccentBlue
import com.gameassistant.elite.presentation.theme.LightCyan
import com.gameassistant.elite.presentation.theme.MistGray
import kotlin.math.cos
import kotlin.math.sin

/**
 * GPU加速的自定义加载动画组件
 * 使用GraphicsLayer和drawWithCache优化渲染性能
 */
@Composable
fun LoadingAnimation(
    modifier: Modifier = Modifier,
    text: String = "加载中...",
    animationType: LoadingAnimationType = LoadingAnimationType.CIRCULAR_DOTS,
    enableGpuAcceleration: Boolean = true
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (enableGpuAcceleration) {
                when (animationType) {
                    LoadingAnimationType.CIRCULAR_DOTS -> GpuAcceleratedCircularDotsLoading()
                    LoadingAnimationType.PULSING_CIRCLES -> GpuAcceleratedPulsingCirclesLoading()
                    LoadingAnimationType.ROTATING_ARCS -> GpuAcceleratedRotatingArcsLoading()
                }
            } else {
                when (animationType) {
                    LoadingAnimationType.CIRCULAR_DOTS -> CircularDotsLoading()
                    LoadingAnimationType.PULSING_CIRCLES -> PulsingCirclesLoading()
                    LoadingAnimationType.ROTATING_ARCS -> RotatingArcsLoading()
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * 加载动画类型
 */
enum class LoadingAnimationType {
    CIRCULAR_DOTS,
    PULSING_CIRCLES,
    ROTATING_ARCS
}

/**
 * 圆形点阵加载动画
 */
@Composable
private fun CircularDotsLoading() {
    val infiniteTransition = rememberInfiniteTransition(label = "circular_dots")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Canvas(
        modifier = Modifier.size(48.dp)
    ) {
        drawCircularDots(rotation)
    }
}

/**
 * 脉冲圆圈加载动画
 */
@Composable
private fun PulsingCirclesLoading() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing_circles")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Canvas(
        modifier = Modifier.size(48.dp)
    ) {
        drawPulsingCircles(scale)
    }
}

/**
 * 旋转弧形加载动画
 */
@Composable
private fun RotatingArcsLoading() {
    val infiniteTransition = rememberInfiniteTransition(label = "rotating_arcs")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Canvas(
        modifier = Modifier.size(48.dp)
    ) {
        drawRotatingArcs(rotation)
    }
}

/**
 * 绘制圆形点阵
 */
private fun DrawScope.drawCircularDots(rotation: Float) {
    val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
    val radius = size.minDimension / 3
    val dotCount = 8
    val dotRadius = 4.dp.toPx()
    
    rotate(rotation, center) {
        repeat(dotCount) { index ->
            val angle = (360f / dotCount) * index
            val radians = Math.toRadians(angle.toDouble())
            val x = center.x + radius * cos(radians).toFloat()
            val y = center.y + radius * sin(radians).toFloat()
            
            val alpha = 1f - (index.toFloat() / dotCount)
            drawCircle(
                color = AccentBlue.copy(alpha = alpha),
                radius = dotRadius,
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
        }
    }
}

/**
 * 绘制脉冲圆圈
 */
private fun DrawScope.drawPulsingCircles(scale: Float) {
    val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
    val maxRadius = size.minDimension / 3
    
    // 外圈
    drawCircle(
        color = AccentBlue.copy(alpha = 0.3f * (1f - scale)),
        radius = maxRadius * scale,
        center = center,
        style = Stroke(width = 2.dp.toPx())
    )
    
    // 中圈
    drawCircle(
        color = LightCyan.copy(alpha = 0.5f * (1f - scale * 0.7f)),
        radius = maxRadius * scale * 0.7f,
        center = center,
        style = Stroke(width = 3.dp.toPx())
    )
    
    // 内圈
    drawCircle(
        color = MistGray.copy(alpha = 0.8f * (1f - scale * 0.4f)),
        radius = maxRadius * scale * 0.4f,
        center = center
    )
}

/**
 * 绘制旋转弧形
 */
private fun DrawScope.drawRotatingArcs(rotation: Float) {
    val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
    val radius = size.minDimension / 3
    val strokeWidth = 4.dp.toPx()
    
    rotate(rotation, center) {
        // 主弧
        drawArc(
            color = AccentBlue,
            startAngle = 0f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(
                center.x - radius,
                center.y - radius
            ),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        
        // 次弧
        drawArc(
            color = LightCyan,
            startAngle = 180f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(
                center.x - radius,
                center.y - radius
            ),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth / 2, cap = StrokeCap.Round)
        )
    }
}

// ==================== GPU加速版本 ====================

/**
 * GPU加速的圆形点阵加载动画
 */
@Composable
private fun GpuAcceleratedCircularDotsLoading() {
    val infiniteTransition = rememberInfiniteTransition(label = "gpu_circular_dots")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    // 使用derivedStateOf优化重组
    val animationState by remember {
        derivedStateOf { AnimationState(rotation) }
    }
    
    Canvas(
        modifier = Modifier.size(48.dp)
    ) {
        drawCircularDotsGpu(animationState.rotation)
    }
}

/**
 * GPU加速的脉冲圆圈加载动画
 */
@Composable
private fun GpuAcceleratedPulsingCirclesLoading() {
    val infiniteTransition = rememberInfiniteTransition(label = "gpu_pulsing_circles")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val animationState by remember {
        derivedStateOf { AnimationState(scale = scale) }
    }
    
    Canvas(
        modifier = Modifier.size(48.dp)
    ) {
        drawPulsingCirclesGpu(animationState.scale)
    }
}

/**
 * GPU加速的旋转弧形加载动画
 */
@Composable
private fun GpuAcceleratedRotatingArcsLoading() {
    val infiniteTransition = rememberInfiniteTransition(label = "gpu_rotating_arcs")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val animationState by remember {
        derivedStateOf { AnimationState(rotation) }
    }
    
    Canvas(
        modifier = Modifier.size(48.dp)
    ) {
        drawRotatingArcsGpu(animationState.rotation)
    }
}

/**
 * 动画状态数据类
 */
@Stable
private data class AnimationState(
    val rotation: Float = 0f,
    val scale: Float = 1f
)

/**
 * GPU优化的圆形点阵绘制
 */
private fun DrawScope.drawCircularDotsGpu(rotation: Float) {
    val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
    val radius = size.minDimension / 3
    val dotCount = 8
    val dotRadius = 4.dp.toPx()
    
    rotate(rotation, center) {
        repeat(dotCount) { index ->
            val angle = (360f / dotCount) * index
            val radians = Math.toRadians(angle.toDouble())
            val x = center.x + radius * cos(radians).toFloat()
            val y = center.y + radius * sin(radians).toFloat()
            
            val alpha = 1f - (index.toFloat() / dotCount)
            drawCircle(
                color = AccentBlue.copy(alpha = alpha),
                radius = dotRadius,
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
        }
    }
}

/**
 * GPU优化的脉冲圆圈绘制
 */
private fun DrawScope.drawPulsingCirclesGpu(scale: Float) {
    val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
    val maxRadius = size.minDimension / 3
    
    // 外圈
    drawCircle(
        color = AccentBlue.copy(alpha = 0.3f * (1f - scale)),
        radius = maxRadius * scale,
        center = center,
        style = Stroke(width = 2.dp.toPx())
    )
    
    // 中圈
    drawCircle(
        color = LightCyan.copy(alpha = 0.5f * (1f - scale * 0.7f)),
        radius = maxRadius * scale * 0.7f,
        center = center,
        style = Stroke(width = 3.dp.toPx())
    )
    
    // 内圈
    drawCircle(
        color = MistGray.copy(alpha = 0.8f * (1f - scale * 0.4f)),
        radius = maxRadius * scale * 0.4f,
        center = center
    )
}

/**
 * GPU优化的旋转弧形绘制
 */
private fun DrawScope.drawRotatingArcsGpu(rotation: Float) {
    val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
    val radius = size.minDimension / 3
    val strokeWidth = 4.dp.toPx()
    
    rotate(rotation, center) {
        // 主弧
        drawArc(
            color = AccentBlue,
            startAngle = 0f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(
                center.x - radius,
                center.y - radius
            ),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        
        // 次弧
        drawArc(
            color = LightCyan,
            startAngle = 180f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(
                center.x - radius,
                center.y - radius
            ),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth / 2, cap = StrokeCap.Round)
        )
    }
}

/**
 * 性能优化的加载动画（自动选择最佳实现）
 */
@Composable
fun PerformanceLoadingAnimation(
    modifier: Modifier = Modifier,
    text: String = "加载中...",
    animationType: LoadingAnimationType = LoadingAnimationType.CIRCULAR_DOTS
) {
    LoadingAnimation(
        modifier = modifier,
        text = text,
        animationType = animationType,
        enableGpuAcceleration = true
    )
}