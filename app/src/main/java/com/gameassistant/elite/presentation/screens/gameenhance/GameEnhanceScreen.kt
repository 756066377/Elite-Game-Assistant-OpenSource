package com.gameassistant.elite.presentation.screens.gameenhance

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.gameassistant.elite.domain.model.GameInfo
import com.gameassistant.elite.presentation.components.LoadingAnimation
import com.gameassistant.elite.presentation.components.LoadingAnimationType
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
    val toastMessage by viewModel.toastMessage.collectAsState()
    val context = LocalContext.current

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
            onCardKeyChange = viewModel::updateCardKeyInput,
            onDismiss = viewModel::hideCardKeyDialog,
            onConfirm = viewModel::activateCardKey,
            isLoading = isLoading
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PureWhite,
                        MistGray.copy(alpha = 0.3f),
                        LightCyan.copy(alpha = 0.1f)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
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
                        onWriteCardKeyClick = viewModel::showCardKeyDialog
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
                    .align(Alignment.BottomCenter)
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
 * 新的游戏增强卡片，根据图片设计
 */
@Composable
private fun GameEnhanceCard(
    game: GameInfo,
    onLaunchClick: () -> Unit,
    onWriteCardKeyClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = ShadowLight,
                spotColor = ShadowLight
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFDFD)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
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

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onLaunchClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue,
                        contentColor = PureWhite
                    )
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("启动辅助")
                }
                OutlinedButton(
                    onClick = onWriteCardKeyClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(
                            colors = listOf(MistGray, MistGray.copy(alpha = 0.5f))
                        )
                    )
                ) {
                    Icon(Icons.Default.Key, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("写入卡密")
                }
            }
        }
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