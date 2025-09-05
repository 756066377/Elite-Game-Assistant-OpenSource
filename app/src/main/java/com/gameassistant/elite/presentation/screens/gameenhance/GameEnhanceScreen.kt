package com.gameassistant.elite.presentation.screens.gameenhance

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Gamepad
import androidx.compose.material3.*
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.shadow
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.gameassistant.elite.domain.model.GameInfo
import com.gameassistant.elite.presentation.components.LoadingAnimation
import com.gameassistant.elite.presentation.components.LoadingAnimationType
import com.gameassistant.elite.presentation.components.common.AnimatedFlowBackground
import com.gameassistant.elite.presentation.components.common.ScreenTitle
import com.gameassistant.elite.presentation.theme.*

/**
 * 游戏增强中心屏幕
 * 提供卡密授权、游戏启动和功能配置
 */
@Composable
fun GameEnhanceScreen(
    viewModel: GameEnhanceViewModel = hiltViewModel()
) {
    val games by viewModel.games.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isCardKeyDialogShown by viewModel.isCardKeyDialogShown.collectAsState()
    val cardKeyInput by viewModel.cardKeyInput.collectAsState()
    val existingCardKeyHint by viewModel.existingCardKeyHint.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 显示 Toast 消息
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToastMessage()
        }
    }

    // 当需要显示对话框时，调用 CardKeyInputDialog
    if (isCardKeyDialogShown) {
        CardKeyInputDialog(
            cardKey = cardKeyInput,
            existingCardKeyHint = existingCardKeyHint,
            onCardKeyChange = viewModel::updateCardKeyInput,
            onDismiss = viewModel::hideCardKeyDialog,
            onConfirm = viewModel::activateCardKey,
            isLoading = isLoading
        )
    }

    AnimatedFlowBackground {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ScreenTitle(
                    mainTitle = "U启动器",
                    subtitle = "游戏增强中心"
                )
            }

            if (isLoading && games.isEmpty()) {
                item {
                    LoadingAnimation(
                        text = "正在加载游戏列表...",
                        animationType = LoadingAnimationType.CIRCULAR_DOTS
                    )
                }
            } else {
                items(games) { game ->
                    GameEnhanceCard(
                        game = game,
                        onLaunchClick = { viewModel.launchGameAssist(game.id) },
                        onWriteCardKeyClick = { 
                            coroutineScope.launch {
                                viewModel.selectGameAndShowDialog(game)
                            }
                        }
                    )
                }
            }
        }

        // 错误提示
        error?.let { errorMessage ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = ErrorRed,
                    modifier = Modifier
                        .background(color = PureWhite, shape = RoundedCornerShape(8.dp))
                        .padding(12.dp)
                )
            }
        }
    }
}

/**
 * 新的游戏增强卡片 - 优化视觉设计版本
 */
@Composable
private fun GameEnhanceCard(
    game: GameInfo,
    onLaunchClick: () -> Unit,
    onWriteCardKeyClick: () -> Unit
) {
    // 动画状态
    var isPressed by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_scale"
    )
    
    val animatedElevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 6.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_elevation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(animatedScale)
            .shadow(
                elevation = animatedElevation,
                shape = RoundedCornerShape(20.dp),
                ambientColor = ShadowLight.copy(alpha = 0.3f),
                spotColor = ShadowLight.copy(alpha = 0.5f)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD).copy(alpha = 0.6f),
                        Color(0xFFF3E5F5).copy(alpha = 0.4f),
                        Color(0xFFE8F5E8).copy(alpha = 0.6f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .drawBehind {
                // 装饰性角落元素
                drawCornerDecorations()
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = !isPressed
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFDFDFD).copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 游戏标题和状态
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = game.iconResId),
                    contentDescription = game.name,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = game.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "就绪",
                        style = MaterialTheme.typography.bodySmall,
                        color = SuccessGreen,
                        modifier = Modifier
                            .background(SuccessGreen.copy(alpha = 0.1f), CircleShape)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MistGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            // 功能介绍标题
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "功能介绍",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "功能介绍",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 功能列表
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                game.features.forEach { feature ->
                    FeatureItem(
                        icon = feature.category.toIcon(),
                        name = feature.name,
                        description = feature.description
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 操作按钮 - 增强版本
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 启动辅助按钮
                var isLaunchPressed by remember { mutableStateOf(false) }
                val launchButtonScale by animateFloatAsState(
                    targetValue = if (isLaunchPressed) 0.95f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessHigh
                    ),
                    label = "launch_button_scale"
                )
                
                Button(
                    onClick = {
                        isLaunchPressed = true
                        onLaunchClick()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .scale(launchButtonScale)
                        .shadow(
                            elevation = if (isLaunchPressed) 2.dp else 4.dp,
                            shape = RoundedCornerShape(12.dp),
                            ambientColor = AccentBlue.copy(alpha = 0.3f),
                            spotColor = AccentBlue.copy(alpha = 0.5f)
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue,
                        contentColor = PureWhite
                    )
                ) {
                    Icon(
                        Icons.Default.FlashOn, 
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "启动辅助",
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                // 写入卡密按钮
                var isCardKeyPressed by remember { mutableStateOf(false) }
                val cardKeyButtonScale by animateFloatAsState(
                    targetValue = if (isCardKeyPressed) 0.95f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessHigh
                    ),
                    label = "cardkey_button_scale"
                )
                
                OutlinedButton(
                    onClick = {
                        isCardKeyPressed = true
                        onWriteCardKeyClick()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .scale(cardKeyButtonScale)
                        .shadow(
                            elevation = if (isCardKeyPressed) 1.dp else 3.dp,
                            shape = RoundedCornerShape(12.dp),
                            ambientColor = MistGray.copy(alpha = 0.2f),
                            spotColor = MistGray.copy(alpha = 0.4f)
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MistGray.copy(alpha = 0.8f), 
                                MistGray.copy(alpha = 0.4f),
                                AccentBlue.copy(alpha = 0.3f)
                            )
                        )
                    )
                ) {
                    Icon(
                        Icons.Default.Key, 
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "写入卡密",
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                // 重置按钮状态
                LaunchedEffect(isLaunchPressed) {
                    if (isLaunchPressed) {
                        delay(150)
                        isLaunchPressed = false
                    }
                }
                
                LaunchedEffect(isCardKeyPressed) {
                    if (isCardKeyPressed) {
                        delay(150)
                        isCardKeyPressed = false
                    }
                }
            }
        }
    }
}

/**
 * 绘制卡片角落装饰元素
 */
private fun DrawScope.drawCornerDecorations() {
    val cornerSize = 24.dp.toPx()
    val strokeWidth = 2.dp.toPx()
    val decorationColors = listOf(
        Color(0xFF2196F3).copy(alpha = 0.6f),
        Color(0xFF9C27B0).copy(alpha = 0.4f),
        Color(0xFF4CAF50).copy(alpha = 0.5f)
    )

    // 左上角装饰
    val topLeftPath = Path().apply {
        moveTo(0f, cornerSize)
        lineTo(0f, cornerSize * 0.3f)
        quadraticBezierTo(0f, 0f, cornerSize * 0.3f, 0f)
        lineTo(cornerSize, 0f)
    }
    drawPath(
        path = topLeftPath,
        brush = Brush.linearGradient(
            colors = decorationColors,
            start = Offset(0f, 0f),
            end = Offset(cornerSize, cornerSize)
        ),
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f), 0f)
        )
    )

    // 右上角装饰
    val topRightPath = Path().apply {
        moveTo(size.width - cornerSize, 0f)
        lineTo(size.width - (cornerSize * 0.3f), 0f)
        quadraticBezierTo(size.width, 0f, size.width, cornerSize * 0.3f)
        lineTo(size.width, cornerSize)
    }
    drawPath(
        path = topRightPath,
        brush = Brush.linearGradient(
            colors = decorationColors.reversed(),
            start = Offset(size.width - cornerSize, 0f),
            end = Offset(size.width, cornerSize)
        ),
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
        )
    )

    // 左下角装饰
    val bottomLeftPath = Path().apply {
        moveTo(0f, size.height - cornerSize)
        lineTo(0f, size.height - (cornerSize * 0.3f))
        quadraticBezierTo(0f, size.height, cornerSize * 0.3f, size.height)
        lineTo(cornerSize, size.height)
    }
    drawPath(
        path = bottomLeftPath,
        brush = Brush.linearGradient(
            colors = decorationColors.takeLast(2),
            start = Offset(0f, size.height - cornerSize),
            end = Offset(cornerSize, size.height)
        ),
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 2f), 0f)
        )
    )

    // 右下角装饰
    val bottomRightPath = Path().apply {
        moveTo(size.width - cornerSize, size.height)
        lineTo(size.width - (cornerSize * 0.3f), size.height)
        quadraticBezierTo(size.width, size.height, size.width, size.height - (cornerSize * 0.3f))
        lineTo(size.width, size.height - cornerSize)
    }
    drawPath(
        path = bottomRightPath,
        brush = Brush.linearGradient(
            colors = listOf(decorationColors[0], decorationColors[2]),
            start = Offset(size.width - cornerSize, size.height),
            end = Offset(size.width, size.height - cornerSize)
        ),
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f), 0f)
        )
    )

    // 中心装饰点
    val centerX = size.width / 2
    val dotRadius = 3.dp.toPx()

    // 绘制几个装饰性的小点
    listOf(
        Offset(centerX - 60.dp.toPx(), 20.dp.toPx()),
        Offset(centerX + 60.dp.toPx(), 20.dp.toPx()),
        Offset(centerX - 60.dp.toPx(), size.height - 20.dp.toPx()),
        Offset(centerX + 60.dp.toPx(), size.height - 20.dp.toPx())
    ).forEachIndexed { index, offset ->
        drawCircle(
            color = decorationColors[index % decorationColors.size],
            radius = dotRadius,
            center = offset
        )
    }
}

/**
 * 功能项 Composable
 */
@Composable
private fun FeatureItem(icon: ImageVector, name: String, description: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = name,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * 卡密输入对话框
 */
@Composable
fun CardKeyInputDialog(
    cardKey: String,
    existingCardKeyHint: String?,
    onCardKeyChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isLoading: Boolean
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            modifier = Modifier.shadow(8.dp, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "激活授权",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "请输入您的卡密以激活全部功能",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(
                    value = cardKey,
                    onValueChange = onCardKeyChange,
                    label = { Text("卡密") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        focusedLabelColor = AccentBlue
                    ),
                    singleLine = true
                )
                
                // 显示现有卡密提示信息
                existingCardKeyHint?.let { hint ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = AccentBlue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = hint,
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentBlue
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        enabled = !isLoading && cardKey.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = PureWhite,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("确认激活")
                        }
                    }
                }
            }
        }
    }
}

/**
 * 将 FeatureCategory 映射到对应的图标
 */
fun com.gameassistant.elite.domain.model.FeatureCategory.toIcon(): ImageVector {
    return when (this) {
        com.gameassistant.elite.domain.model.FeatureCategory.VISUAL_ENHANCEMENT -> Icons.Default.Visibility
        com.gameassistant.elite.domain.model.FeatureCategory.AIM_ASSIST -> Icons.Default.Adjust
        com.gameassistant.elite.domain.model.FeatureCategory.TACTICAL_ASSIST -> Icons.Default.Security
    }
}


/**
 * 单个游戏功能包卡片
 */
@Composable
private fun GameFeaturePackCard(
    gameName: String,
    gameIcon: String,
    features: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 游戏名称
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = gameIcon,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = gameName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentBlue
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 功能列表
            features.forEach { feature ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AccentBlue,
                        modifier = Modifier.padding(end = 8.dp, top = 2.dp)
                    )
                    Text(
                        text = feature,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}