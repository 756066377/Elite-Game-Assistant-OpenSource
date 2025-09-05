package com.gameassistant.elite.presentation.components.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.gameassistant.elite.presentation.theme.LightBlue
import com.gameassistant.elite.presentation.theme.LightCyan
import com.gameassistant.elite.presentation.theme.MistGray

/**
 * GPU加速的动画流背景组件
 * 使用GraphicsLayer和drawWithCache优化渲染性能
 */
@Composable
fun AnimatedFlowBackground(
    modifier: Modifier = Modifier,
    enableAnimation: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    // 稳定的颜色配置
    val colors = remember {
        listOf(
            LightCyan.copy(alpha = 0.8f),
            LightBlue.copy(alpha = 0.7f),
            MistGray.copy(alpha = 0.8f)
        )
    }

    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    
    // 使用remember缓存屏幕尺寸计算
    val screenDimensions = remember(configuration.screenWidthDp, configuration.screenHeightDp) {
        with(density) {
            Pair(
                configuration.screenWidthDp.dp.toPx(),
                configuration.screenHeightDp.dp.toPx()
            )
        }
    }
    
    val (screenWidth, screenHeight) = screenDimensions


    
    // 动画配置 - 只在启用动画时创建
    val infiniteTransition = if (enableAnimation) {
        rememberInfiniteTransition(label = "background_flow")
    } else null

    // 动画偏移值
    val offset1 = if (enableAnimation && infiniteTransition != null) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = screenWidth,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 8000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "offset1"
        ).value
    } else {
        screenWidth * 0.3f
    }

    val offset2 = if (enableAnimation && infiniteTransition != null) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = screenHeight,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 10000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "offset2"
        ).value
    } else {
        screenHeight * 0.2f
    }

    val offset3 = if (enableAnimation && infiniteTransition != null) {
        infiniteTransition.animateFloat(
            initialValue = screenWidth,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 9000, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ), label = "offset3"
        ).value
    } else {
        screenWidth * 0.7f
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                // 创建渐变画笔
                val brush = Brush.linearGradient(
                    colors = colors,
                    start = Offset(offset1, offset2),
                    end = Offset(offset3, screenHeight)
                )
                // 直接绘制
                drawRect(brush = brush)
            }
    ) {
        content()
    }
}



/**
 * 高性能版本的动画流背景（用于性能敏感场景）
 */
@Composable
fun PerformanceAnimatedFlowBackground(
    modifier: Modifier = Modifier,
    reducedMotion: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    AnimatedFlowBackground(
        modifier = modifier,
        enableAnimation = !reducedMotion,
        content = content
    )
}