package com.gameassistant.elite.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gameassistant.elite.domain.model.FeatureCategory
import com.gameassistant.elite.domain.model.GameInfo
import com.gameassistant.elite.domain.model.GameLaunchStatus
import com.gameassistant.elite.presentation.theme.AccentBlue
import com.gameassistant.elite.presentation.theme.ButtonFill
import com.gameassistant.elite.presentation.theme.ErrorRed
import com.gameassistant.elite.presentation.theme.InnerGlow
import com.gameassistant.elite.presentation.theme.LightCyan
import com.gameassistant.elite.presentation.theme.MistGray
import com.gameassistant.elite.presentation.theme.PureWhite
import com.gameassistant.elite.presentation.theme.ShadowLight
import com.gameassistant.elite.presentation.theme.SuccessGreen

/**
 * 游戏卡片组件
 * 显示游戏信息、功能特性和启动按钮
 */
@Composable
fun GameCard(
    game: GameInfo,
    isSelected: Boolean = false,
    launchStatus: GameLaunchStatus = GameLaunchStatus.IDLE,
    onGameClick: (GameInfo) -> Unit,
    onLaunchClick: (String) -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isSelected) 4.dp else 2.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = ShadowLight,
                spotColor = ShadowLight
            )
            .clickable { onGameClick(game) },
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
                        colors = if (isSelected) {
                            listOf(
                                PureWhite,
                                LightCyan.copy(alpha = 0.4f),
                                AccentBlue.copy(alpha = 0.1f)
                            )
                        } else {
                            listOf(
                                PureWhite,
                                MistGray.copy(alpha = 0.2f)
                            )
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)  // 统一内边距为16dp
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
                            modifier = Modifier.size(48.dp)
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
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 游戏描述
                Text(
                    text = game.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // 功能特性列表
                if (game.features.isNotEmpty()) {
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
                            FeatureChip(
                                feature = feature.name,
                                category = feature.category
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // 启动按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),  // 添加上边距分隔
                    horizontalArrangement = Arrangement.End
                ) {
                    when (launchStatus) {
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
                                shape = RoundedCornerShape(12.dp)
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
                                shape = RoundedCornerShape(12.dp)
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
                                shape = RoundedCornerShape(12.dp)
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
            
            // 内发光效果
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    AccentBlue.copy(alpha = 0.3f),
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

/**
 * 功能特性标签组件
 */
@Composable
private fun FeatureChip(
    feature: String,
    category: FeatureCategory,
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
                color = chipColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = feature,
            style = MaterialTheme.typography.bodySmall,
            color = chipColor,
            fontWeight = FontWeight.Medium
        )
    }
}