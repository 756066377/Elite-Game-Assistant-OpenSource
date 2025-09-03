package com.gameassistant.elite.presentation.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.offset
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * 动画配置常量
 */
object AnimationConfig {
    // 持续时间
    const val FAST_DURATION = 150
    const val NORMAL_DURATION = 300
    const val SLOW_DURATION = 500
    
    // 弹簧动画配置
    val SPRING_BOUNCY = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    val SPRING_SMOOTH = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    // 缓动函数
    val EASE_OUT_SLOW_IN = tween<Float>(
        durationMillis = NORMAL_DURATION,
        easing = FastOutSlowInEasing
    )
}

/**
 * 点击缩放动画修饰符
 */
fun Modifier.clickableWithScale(
    enabled: Boolean = true,
    scaleDown: Float = 0.95f,
    animationSpec: AnimationSpec<Float> = AnimationConfig.SPRING_SMOOTH,
    onClick: () -> Unit
) = composed {
    val scale = remember { Animatable(1f) }
    val interactionSource = remember { MutableInteractionSource() }
    val scope = rememberCoroutineScope()

    this
        .scale(scale.value)
        .clickable(
            enabled = enabled,
            interactionSource = interactionSource,
            indication = rememberRipple(
                bounded = true,
                radius = 24.dp,
                color = AccentBlue.copy(alpha = 0.3f)
            ),
            onClick = {
                // 执行缩放动画
                scope.launch {
                    scale.animateTo(scaleDown, animationSpec)
                    scale.animateTo(1f, animationSpec)
                }
                onClick()
            }
        )
}

/**
 * 悬浮动画修饰符
 */
fun Modifier.floatingAnimation(
    enabled: Boolean = true,
    offsetY: Float = 4f,
    durationMillis: Int = 2000
) = composed {
    if (!enabled) return@composed this
    
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val animatedOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = offsetY,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )
    
    this.offset(y = animatedOffsetY.dp)
}

/**
 * 脉冲动画修饰符
 */
fun Modifier.pulseAnimation(
    enabled: Boolean = true,
    minScale: Float = 0.98f,
    maxScale: Float = 1.02f,
    durationMillis: Int = 1000
) = composed {
    if (!enabled) return@composed this
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    this.scale(scale)
}

/**
 * 旋转动画修饰符
 */
fun Modifier.rotateAnimation(
    enabled: Boolean = true,
    durationMillis: Int = 2000
) = composed {
    if (!enabled) return@composed this
    
    val infiniteTransition = rememberInfiniteTransition(label = "rotate")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    this.graphicsLayer {
        rotationZ = rotation
    }
}