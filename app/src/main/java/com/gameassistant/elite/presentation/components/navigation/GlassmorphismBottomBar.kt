package com.gameassistant.elite.presentation.components.navigation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.gameassistant.elite.presentation.navigation.BottomNavItem
import com.gameassistant.elite.presentation.theme.AccentBlue
import com.gameassistant.elite.presentation.theme.MistGray
import com.gameassistant.elite.presentation.theme.PureWhite

/**
 * 带有液态玻璃和动态光泽效果的底部导航栏
 * @param navController 导航控制器
 * @param items 导航项列表
 */
@Composable
fun GlassmorphismBottomBar(
    navController: NavController,
    items: List<BottomNavItem>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val infiniteTransition = rememberInfiniteTransition(label = "shine-transition")
    val shineOffset by infiniteTransition.animateFloat(
        initialValue = -1.0f,
        targetValue = 2.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "shine-offset"
    )

    val shineBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.0f),
            Color.White.copy(alpha = 0.3f),
            Color.White.copy(alpha = 0.0f)
        ),
        start = Offset(0f, 0f),
        end = Offset(1f, 1f)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .height(64.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = AccentBlue.copy(alpha = 0.3f),
                ambientColor = AccentBlue.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        PureWhite.copy(alpha = 0.7f),
                        MistGray.copy(alpha = 0.5f)
                    )
                )
            )
            .graphicsLayer { alpha = 0.99f } // 开启离屏缓冲以应用混合模式
            .drawWithContent {
                drawContent()
                // 绘制动态光泽
                val width = size.width
                val height = size.height
                translate(left = shineOffset * width - width / 2, top = -height / 2) {
                    drawRect(
                        brush = shineBrush,
                        blendMode = BlendMode.Plus,
                        size = size * 1.5f
                    )
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                GlassmorphismBottomNavItem(
                    item = item,
                    isSelected = isSelected,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }
}

/**
 * 液态玻璃风格的导航项
 * @param item 导航项数据
 * @param isSelected 是否被选中
 * @param onClick 点击事件
 */
@Composable
private fun GlassmorphismBottomNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "nav-item-scale"
    )
    val contentColor = if (isSelected) AccentBlue else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Icon(
            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
            contentDescription = item.title,
            tint = contentColor,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = item.title,
            color = contentColor,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}