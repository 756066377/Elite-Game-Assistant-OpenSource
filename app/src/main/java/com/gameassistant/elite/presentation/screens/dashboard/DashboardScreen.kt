package com.gameassistant.elite.presentation.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gameassistant.elite.presentation.components.LoadingAnimation
import com.gameassistant.elite.presentation.components.common.AnimatedFlowBackground
import com.gameassistant.elite.presentation.components.common.ScreenTitle
import com.gameassistant.elite.presentation.utils.ScrollPerformanceOptimizer
import com.gameassistant.elite.presentation.utils.rememberScrollMetrics
import com.gameassistant.elite.presentation.utils.optimizeForPerformance
import com.gameassistant.elite.presentation.utils.scrollPerformanceOptimized
import com.gameassistant.elite.presentation.utils.smartPrefetch
import com.gameassistant.elite.presentation.utils.optimizedScrollGestures
import com.gameassistant.elite.presentation.utils.rememberHighPerformanceScrollConfig
import com.gameassistant.elite.presentation.utils.rememberTouchPerformanceMonitor
import com.gameassistant.elite.presentation.utils.PerformanceSensitiveComponent
import com.gameassistant.elite.presentation.utils.recompositionOptimized
import com.gameassistant.elite.presentation.utils.asStable
import com.gameassistant.elite.presentation.theme.AccentBlue
import com.gameassistant.elite.presentation.theme.ErrorRed
import com.gameassistant.elite.presentation.theme.LightCyan
import com.gameassistant.elite.presentation.theme.MistGray
import com.gameassistant.elite.presentation.theme.PureWhite
import androidx.compose.foundation.shape.CircleShape
import com.gameassistant.elite.presentation.theme.ShadowLight
import com.gameassistant.elite.presentation.theme.SuccessGreen
import com.gameassistant.elite.presentation.theme.WarningOrange

/**
 * 系统监控仪表盘屏幕
 * 卡片式布局显示设备概览、内核信息、安全状态、使用说明
 */
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    // 使用稳定的状态收集
    val systemInfo by viewModel.systemInfo.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // 将系统信息包装为稳定对象
    val stableSystemInfo = systemInfo.asStable()

    PerformanceSensitiveComponent(
        componentName = "DashboardScreen",
        enableProfiling = false // 关闭性能分析以减少主线程负载
    ) {
        AnimatedFlowBackground {
            if (isLoading) {
                LoadingAnimation(text = "正在加载系统信息...")
            } else {
                OptimizedDashboardContent(
                    systemInfo = stableSystemInfo.value,
                    modifier = Modifier
                        .fillMaxSize()
                        .recompositionOptimized("DashboardContent")
                )
            }
            
            // 错误提示
            error?.let { errorMessage ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.BottomCenter)
                        .recompositionOptimized("ErrorMessage")
                ) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = ErrorRed,
                        modifier = Modifier
                            .background(
                                color = PureWhite,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    )
                }
            }
        }
    }
}

/**
 * 信息卡片组件
 */
@Composable
private fun InfoCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = ShadowLight,
                spotColor = ShadowLight
            ),
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
                            MistGray.copy(alpha = 0.2f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                // 卡片标题
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.size(8.dp))
                    
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 卡片内容
                content()
            }
        }
    }
}

/**
 * 信息行组件
 */
@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 安全状态行组件
 */
@Composable
private fun SecurityRow(
    label: String,
    value: String,
    statusColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable { onClick() } else it }
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = statusColor
        )
    }
}

/**
 * 获取本地化的品牌名称（智能识别常见品牌）
 */
private fun getLocalizedBrandName(brand: String): String {
    val lowerBrand = brand.lowercase()
    
    // 智能识别常见品牌模式
    return when {
        lowerBrand.contains("xiaomi") || lowerBrand.contains("mi ") || lowerBrand.contains("redmi") || lowerBrand.contains("poco") -> "小米"
        lowerBrand.contains("samsung") || lowerBrand.contains("galaxy") -> "三星"
        lowerBrand.contains("huawei") || lowerBrand.contains("honor") -> "华为"
        lowerBrand.contains("oppo") || lowerBrand.contains("realme") || lowerBrand.contains("oneplus") -> "OPPO"
        lowerBrand.contains("vivo") || lowerBrand.contains("iqoo") -> "vivo"
        lowerBrand.contains("meizu") -> "魅族"
        lowerBrand.contains("google") || lowerBrand.contains("pixel") -> "谷歌"
        lowerBrand.contains("sony") || lowerBrand.contains("xperia") -> "索尼"
        lowerBrand.contains("nokia") -> "诺基亚"
        lowerBrand.contains("motorola") || lowerBrand.contains("moto") -> "摩托罗拉"
        lowerBrand.contains("lenovo") -> "联想"
        lowerBrand.contains("zte") -> "中兴"
        lowerBrand.contains("asus") -> "华硕"
        lowerBrand.contains("lg") -> "LG"
        lowerBrand.contains("htc") -> "HTC"
        lowerBrand.contains("blackshark") -> "黑鲨"
        lowerBrand.contains("nubia") -> "努比亚"
        lowerBrand.contains("smartisan") || lowerBrand.contains("hammer") -> "锤子"
        else -> brand // 未知品牌保持原样
    }
}

/**
 * 格式化内核版本号，提取主要版本和Android版本信息
 * 例如: "5.10.177-android12-9-g12345678" → "5.10.177-android12"
 */
private fun formatKernelVersion(fullVersion: String): String {
    // 使用正则表达式匹配主要版本和Android版本
    val pattern = Regex("""^(\d+\.\d+\.\d+)-(android\d+)""")
    val match = pattern.find(fullVersion)
    
    return if (match != null) {
        // 如果匹配成功，返回主要版本和Android版本
        "${match.groupValues[1]}-${match.groupValues[2]}"
    } else {
        // 如果没有匹配到预期格式，返回原始字符串
        fullVersion
    }
}




/**
 * 优化后的仪表盘内容组件
 * 使用性能优化的LazyColumn实现
 */
@Composable
private fun OptimizedDashboardContent(
    systemInfo: com.gameassistant.elite.domain.model.SystemInfo,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    
    // 性能配置
    val performanceConfig = remember {
        ScrollPerformanceOptimizer.PerformanceConfig(
            enableMetrics = true, // 启用性能监控
            enableOverscrollEffect = true,
            enableFlingOptimization = true,
            prefetchItemCount = 3,
            maxScrollSpeed = 5000f
        )
    }
    
    // 高性能滑动配置
    val scrollConfig = rememberHighPerformanceScrollConfig(
        enableOverscroll = true,
        enableNestedScroll = true
    )
    
    // 触摸性能监控
    val touchMonitor = rememberTouchPerformanceMonitor()
    
    // 滑动性能监控
    val scrollMetrics = rememberScrollMetrics(listState, performanceConfig)
    
    // 应用性能优化
    listState.optimizeForPerformance(performanceConfig)
    
    LazyColumn(
        state = listState,
        modifier = modifier
            .scrollPerformanceOptimized(performanceConfig)
            .optimizedScrollGestures(
                lazyListState = listState,
                enabled = true,
                reverseDirection = false
            ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "title") {
            ScreenTitle(
                mainTitle = "U启动器",
                subtitle = "系统监控仪表盘"
            )
        }
        
        item(key = "device_info") {
            InfoCard(
                title = "设备概览",
                icon = Icons.Filled.Smartphone,
                iconColor = AccentBlue,
                content = {
                    InfoRow("管理器版本", "v2.1.3")
                    InfoRow("手机品牌", getLocalizedBrandName(android.os.Build.BRAND.takeIf { it.isNotEmpty() } ?: "Unknown"))
                    InfoRow("设备型号", android.os.Build.MODEL)
                    InfoRow("Android版本", "${systemInfo.androidVersion.takeIf { it.isNotEmpty() } ?: android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})")
                }
            )
        }
        
        item(key = "kernel_info") {
            InfoCard(
                title = "内核信息",
                icon = Icons.Filled.Android,
                iconColor = SuccessGreen,
                content = {
                    InfoRow("内核版本", formatKernelVersion(systemInfo.kernelVersion.takeIf { it.isNotEmpty() } ?: System.getProperty("os.version") ?: "Unknown"))
                    InfoRow("架构", android.os.Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown")
                    InfoRow("编译时间", android.os.Build.TIME.let { 
                        java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                            .format(java.util.Date(it))
                    })
                }
            )
        }
        
        item(key = "security_info") {
            InfoCard(
                title = "安全状态",
                icon = Icons.Filled.Security,
                iconColor = ErrorRed,
                content = {
                    SecurityRow(
                        "SELinux",
                        when (systemInfo.selinuxStatus) {
                            com.gameassistant.elite.domain.model.SELinuxStatus.ENFORCING -> "Enforcing"
                            com.gameassistant.elite.domain.model.SELinuxStatus.PERMISSIVE -> "Permissive"
                            com.gameassistant.elite.domain.model.SELinuxStatus.DISABLED -> "Disabled"
                            com.gameassistant.elite.domain.model.SELinuxStatus.UNKNOWN -> "Unknown"
                        },
                        when (systemInfo.selinuxStatus) {
                            com.gameassistant.elite.domain.model.SELinuxStatus.ENFORCING -> SuccessGreen
                            com.gameassistant.elite.domain.model.SELinuxStatus.PERMISSIVE -> WarningOrange
                            com.gameassistant.elite.domain.model.SELinuxStatus.DISABLED -> AccentBlue
                            com.gameassistant.elite.domain.model.SELinuxStatus.UNKNOWN -> ErrorRed
                        }
                    )
                    SecurityRow(
                        "设备指纹", 
                        systemInfo.buildFingerprint.takeIf { it.isNotEmpty() } ?: "Unknown",
                        AccentBlue
                    )
                }
            )
        }
        
        item(key = "instructions") {
            InfoCard(
                title = "使用说明",
                icon = Icons.Filled.Info,
                iconColor = AccentBlue,
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        StyledInstructionItem("1", "安装支持 KPM的SukiSU-Ultra")
                        StyledInstructionItem("2", "先启动辅助后再进入游戏")
                        StyledInstructionItem("3", "自瞄均为陀螺仪辅助，需在游戏内开启陀螺仪")
                    }
                }
            )
        }
    }
}



/**
 * 带有艺术字序号的使用说明项
 */
@Composable
private fun StyledInstructionItem(number: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(AccentBlue, AccentBlue.copy(alpha = 0.7f))
                    ),
                    shape = CircleShape
                )
                .shadow(2.dp, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = PureWhite,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(modifier = Modifier.size(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
            modifier = Modifier.weight(1f)
        )
    }
}

