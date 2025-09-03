package com.gameassistant.elite.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gameassistant.elite.domain.model.FeatureCategory
import com.gameassistant.elite.domain.model.GameInfo
import com.gameassistant.elite.domain.model.GameLaunchStatus
import com.gameassistant.elite.presentation.theme.AccentBlue
import com.gameassistant.elite.presentation.theme.AnimationConfig
import com.gameassistant.elite.presentation.theme.ButtonFill
import com.gameassistant.elite.presentation.theme.ErrorRed
import com.gameassistant.elite.presentation.theme.LightCyan
import com.gameassistant.elite.presentation.theme.MistGray
import com.gameassistant.elite.presentation.theme.PureWhite
import com.gameassistant.elite.presentation.theme.ShadowLight
import com.gameassistant.elite.presentation.theme.SuccessGreen
import com.gameassistant.elite.presentation.theme.clickableWithScale
import com.gameassistant.elite.presentation.theme.pulseAnimation
import kotlinx.coroutines.delay

/**
 * 带动画效果的游戏卡片组件
 */
@Composable
fun AnimatedGameCard(
    game: GameInfo,
    isSelected: Boolean = false,
    launchStatus: GameLaunchStatus = GameLaunchStatus.IDLE,
    onGameClick: (GameInfo) -> Unit,
    onLaunchClick: (String) -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var cardVisible by remember { mutableStateOf(false) }
    var featuresVisible by remember { mutableStateOf(false) }
    
    // 卡片出现动画
    LaunchedEffect(Unit) {
        delay(100)
        cardVisible = true
        delay(300)
        featuresVisible = true
    }
    
    // 选中状态的缩放动画
    val selectedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = AnimationConfig.SPRING_SMOOTH,
        label = "selected_scale"
    )
    
    // 运行状态的发光效果
    val isRunning = launchStatus == GameLaunchStatus.RUNNING
    
    AnimatedVisibility(
        visible = cardVisible,
        enter = fadeIn(tween(AnimationConfig.NORMAL_DURATION)) + 
                slideInHorizontally(tween(AnimationConfig.NORMAL_DURATION)) { it / 2 },
        exit = fadeOut(tween(AnimationConfig.FAST_DURATION)) + 
               slideOutHorizontally(tween(AnimationConfig.FAST_DURATION)) { -it / 2 }
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = selectedScale
                    scaleY = selectedScale
                }
                .shadow(
                    elevation = if (isSelected) 6.dp else 2.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = ShadowLight,
                    spotColor = ShadowLight
                )
                .clickableWithScale { onGameClick(game) },
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
                            colors = when {
                                isRunning -> listOf(
                                    PureWhite,
                                    SuccessGreen.copy(alpha = 0.1f),
                                    SuccessGreen.copy(alpha = 0.05f)
                                )
                                isSelected -> listOf(
                                    PureWhite,
                                    LightCyan.copy(alpha = 0.4f),
                                    AccentBlue.copy(alpha = 0.1f)
                                )
                                else -> listOf(
                                    PureWhite,
                                    MistGray.copy(alpha = 0.3f),
                                    LightCyan.copy(alpha = 0.2f)
                                )
                            }
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp)
            ) {
                Column {
                    // 游戏标题和图标行
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = game.iconResId),
                                contentDescription = game.name,
                                modifier = Modifier
                                    .size(48.dp)
                                    .pulseAnimation(enabled = isRunning)
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column {
                                Text(
                                    text = game.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (game.isInstalled) Icons.Filled.CheckCircle else Icons.Filled.Error,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (game.isInstalled) SuccessGreen else ErrorRed
                                    )
                                    
                                    Spacer(modifier = Modifier.width(4.dp))
                                    
                                    Text(
                                        text = if (game.isInstalled) "已安装" else "未安装",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (game.isInstalled) SuccessGreen else ErrorRed
                                    )
                                }
                            }
                        }
                        
                        // 状态指示器
                        AnimatedContent(
                            targetState = launchStatus,
                            transitionSpec = {
                                fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                            },
                            label = "status_indicator"
                        ) { status ->
                            when (status) {
                                GameLaunchStatus.RUNNING -> {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(
                                                color = SuccessGreen,
                                                shape = androidx.compose.foundation.shape.CircleShape
                                            )
                                            .pulseAnimation()
                                    )
                                }
                                GameLaunchStatus.LOADING -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 2.dp,
                                        color = AccentBlue
                                    )
                                }
                                GameLaunchStatus.ERROR -> {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(
                                                color = ErrorRed,
                                                shape = androidx.compose.foundation.shape.CircleShape
                                            )
                                    )
                                }
                                else -> {
                                    Spacer(modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 游戏描述
                    Text(
                        text = game.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 功能特性列表（带动画）
                    AnimatedVisibility(
                        visible = featuresVisible && game.features.isNotEmpty(),
                        enter = expandHorizontally(tween(AnimationConfig.NORMAL_DURATION)) + 
                                fadeIn(tween(AnimationConfig.NORMAL_DURATION)),
                        exit = shrinkHorizontally(tween(AnimationConfig.FAST_DURATION)) + 
                               fadeOut(tween(AnimationConfig.FAST_DURATION))
                    ) {
                        Column {
                            Text(
                                text = "功能特性",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(game.features) { feature ->
                                    AnimatedFeatureChip(
                                        feature = feature.name,
                                        category = feature.category,
                                        isActive = isRunning
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                    
                    // 动画启动按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        AnimatedContent(
                            targetState = launchStatus,
                            transitionSpec = {
                                slideInHorizontally(tween(200)) { it } togetherWith 
                                slideOutHorizontally(tween(200)) { -it }
                            },
                            label = "launch_button"
                        ) { status ->
                            when (status) {
                                GameLaunchStatus.IDLE -> {
                                    Button(
                                        onClick = { onLaunchClick(game.id) },
                                        enabled = game.isInstalled,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AccentBlue,
                                            contentColor = PureWhite,
                                            disabledContainerColor = ButtonFill,
                                            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.clickableWithScale { 
                                            if (game.isInstalled) onLaunchClick(game.id) 
                                        }
                                    ) {
                                        Text(
                                            text = if (game.isInstalled) "启动辅助" else "游戏未安装",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                                
                                GameLaunchStatus.LOADING -> {
                                    Button(
                                        onClick = { },
                                        enabled = false,
                                        colors = ButtonDefaults.buttonColors(
                                            disabledContainerColor = ButtonFill,
                                            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "加载中...",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                                
                                GameLaunchStatus.RUNNING -> {
                                    Button(
                                        onClick = { onStopClick() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ErrorRed,
                                            contentColor = PureWhite
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .pulseAnimation(enabled = true, minScale = 0.98f, maxScale = 1.02f)
                                            .clickableWithScale { onStopClick() }
                                    ) {
                                        Text(
                                            text = "停止辅助",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                                
                                GameLaunchStatus.ERROR, GameLaunchStatus.UNAUTHORIZED -> {
                                    Button(
                                        onClick = { onLaunchClick(game.id) },
                                        enabled = game.isInstalled,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ErrorRed,
                                            contentColor = PureWhite,
                                            disabledContainerColor = ButtonFill,
                                            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.clickableWithScale { 
                                            if (game.isInstalled) onLaunchClick(game.id) 
                                        }
                                    ) {
                                        Text(
                                            text = "重试启动",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // 动态边框效果
                if (isSelected || isRunning) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        if (isRunning) SuccessGreen.copy(alpha = 0.6f) else AccentBlue.copy(alpha = 0.6f),
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
 * 带动画效果的功能特性标签组件
 */
@Composable
private fun AnimatedFeatureChip(
    feature: String,
    category: FeatureCategory,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val chipColor = when (category) {
        FeatureCategory.VISUAL_ENHANCEMENT -> AccentBlue
        FeatureCategory.AIM_ASSIST -> SuccessGreen
        FeatureCategory.TACTICAL_ASSIST -> Color(0xFFFF9800)
    }
    
    Box(
        modifier = modifier
            .background(
                color = if (isActive) chipColor.copy(alpha = 0.2f) else chipColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .pulseAnimation(enabled = isActive, minScale = 0.98f, maxScale = 1.02f)
    ) {
        Text(
            text = feature,
            style = MaterialTheme.typography.bodySmall,
            color = if (isActive) chipColor.copy(alpha = 0.9f) else chipColor,
            fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal
        )
    }
}