package com.gameassistant.elite.presentation.components.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.gameassistant.elite.presentation.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * 液态玻璃风格卡片组件
 * 具有玻璃拟态效果、液态流动边框和动态光影
 */
@Composable
fun GlassMorphismCard(
    modifier: Modifier = Modifier,
    cornerRadius: Int = 20,
    borderWidth: Int = 2,
    content: @Composable BoxScope.() -> Unit
) {
    // 液态流动动画
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // 光影位置动画
    val lightOffset by animateFloatAsState(
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PureWhite.copy(alpha = 0.7f),
                        PureWhite.copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius.dp)
            )
            .border(
                width = borderWidth.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        AccentBlue.copy(alpha = 0.3f),
                        LightCyan.copy(alpha = 0.1f),
                        AccentBlue.copy(alpha = 0.3f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(
                        x = animatedProgress * 100,
                        y = animatedProgress * 100
                    )
                ),
                shape = RoundedCornerShape(cornerRadius.dp)
            )
            .blur(radius = 8.dp, edgeTreatment = BlurredEdgeTreatment.Rectangle)
            .graphicsLayer {
                // 添加轻微的3D效果
                rotationX = 2f
                rotationY = 1f
                shadowElevation = 8f
            }
            .drawWithCache {
                // 添加液态高光效果
                onDrawWithContent {
                    drawContent()
                    // 玻璃表面高光
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.1f),
                                Color.Transparent
                            ),
                            center = Offset(
                                size.width * lightOffset,
                                size.height * 0.3f
                            ),
                            radius = size.width * 0.5f
                        ),
                        blendMode = BlendMode.Overlay
                    )
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            content()
        }
    }
}

/**
 * 水滴涟漪效果修饰符
 */
@Composable
fun Modifier.liquidRippleEffect(): Modifier = then(
    Modifier.graphicsLayer {
        // 添加液态表面张力效果
        alpha = 0.95f
    }.drawWithCache {
        onDrawBehind {
            // 模拟水滴表面的微扰动
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AccentBlue.copy(alpha = 0.05f),
                        Color.Transparent
                    )
                ),
                radius = size.minDimension * 0.1f,
                center = Offset(size.width * 0.8f, size.height * 0.2f)
            )
        }
    }
)

/**
 * 液态玻璃背景容器
 * 用于整个页面的玻璃拟态背景
 */
@Composable
fun LiquidGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        LightCyan.copy(alpha = 0.1f),
                        MistGray.copy(alpha = 0.05f),
                        PureWhite.copy(alpha = 0.08f)
                    )
                )
            )
            .blur(radius = 12.dp)
    ) {
        content()
    }
}