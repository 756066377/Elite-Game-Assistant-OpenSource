package com.gameassistant.elite.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gameassistant.elite.presentation.theme.AccentBlue
import com.gameassistant.elite.presentation.theme.AnimationConfig
import com.gameassistant.elite.presentation.theme.LightCyan
import com.gameassistant.elite.presentation.theme.MistGray
import com.gameassistant.elite.presentation.theme.PureWhite
import com.gameassistant.elite.presentation.theme.ShadowLight
import com.gameassistant.elite.presentation.theme.clickableWithScale
import com.gameassistant.elite.presentation.theme.floatingAnimation
import kotlinx.coroutines.delay

/**
 * 带动画效果的监控卡片组件
 */
@Composable
fun AnimatedMonitorCard(
    title: String,
    icon: ImageVector,
    value: String,
    subtitle: String = "",
    progress: Float = 0f,
    showProgress: Boolean = false,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    isVisible: Boolean = true,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var cardVisible by remember { mutableStateOf(false) }
    var progressAnimated by remember { mutableStateOf(0f) }
    
    // 卡片出现动画
    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(100) // 稍微延迟让动画更自然
            cardVisible = true
        } else {
            cardVisible = false
        }
    }
    
    // 进度条动画
    LaunchedEffect(progress) {
        if (showProgress) {
            delay(300) // 等待卡片动画完成
            progressAnimated = progress
        }
    }
    
    val animatedProgress by animateFloatAsState(
        targetValue = progressAnimated,
        animationSpec = tween(
            durationMillis = AnimationConfig.SLOW_DURATION,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "progress"
    )
    
    AnimatedVisibility(
        visible = cardVisible,
        enter = fadeIn(
            animationSpec = tween(AnimationConfig.NORMAL_DURATION)
        ) + expandVertically(
            animationSpec = tween(AnimationConfig.NORMAL_DURATION)
        ),
        exit = fadeOut(
            animationSpec = tween(AnimationConfig.FAST_DURATION)
        ) + shrinkVertically(
            animationSpec = tween(AnimationConfig.FAST_DURATION)
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = ShadowLight,
                    spotColor = ShadowLight
                )
                .floatingAnimation(enabled = showProgress && progress > 80)
                .let { cardModifier ->
                    if (onClick != null) {
                        cardModifier.clickableWithScale(onClick = onClick)
                    } else {
                        cardModifier
                    }
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = PureWhite
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                PureWhite,
                                MistGray.copy(alpha = 0.3f),
                                LightCyan.copy(alpha = 0.2f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp)
            ) {
                Column {
                    // 标题和图标行
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 主要数值
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // 副标题
                    if (subtitle.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    
                    // 动画进度条
                    if (showProgress) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AnimatedCircularProgress(
                                progress = animatedProgress / 100f,
                                color = progressColor,
                                modifier = Modifier.size(32.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column {
                                Text(
                                    text = "${animatedProgress.toInt()}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                // 状态指示器
                                val statusText = when {
                                    animatedProgress > 85 -> "高负载"
                                    animatedProgress > 70 -> "中等负载"
                                    else -> "正常"
                                }
                                
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = progressColor
                                )
                            }
                        }
                    }
                }
                
                // 动态内发光效果
                if (showProgress && animatedProgress > 80) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        progressColor.copy(alpha = 0.4f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .align(Alignment.TopCenter)
                    )
                }
            }
        }
    }
}

/**
 * 动画圆形进度条组件
 */
@Composable
private fun AnimatedCircularProgress(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 3.dp.value
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = AnimationConfig.SLOW_DURATION,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "circular_progress"
    )
    
    Canvas(modifier = modifier) {
        drawCircularProgress(
            progress = animatedProgress,
            color = color,
            strokeWidth = strokeWidth
        )
    }
}

/**
 * 绘制圆形进度条
 */
private fun DrawScope.drawCircularProgress(
    progress: Float,
    color: Color,
    strokeWidth: Float
) {
    val center = Offset(size.width / 2, size.height / 2)
    val radius = (size.minDimension - strokeWidth) / 2
    
    // 背景圆环
    drawCircle(
        color = color.copy(alpha = 0.2f),
        radius = radius,
        center = center,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
    
    // 进度圆弧
    if (progress > 0) {
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = Offset(
                center.x - radius,
                center.y - radius
            ),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}