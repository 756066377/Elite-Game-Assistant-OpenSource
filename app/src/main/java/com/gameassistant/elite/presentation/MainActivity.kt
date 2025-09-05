package com.gameassistant.elite.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.gameassistant.elite.presentation.navigation.GameAssistantNavigation
import com.gameassistant.elite.presentation.theme.GameAssistantTheme
import com.gameassistant.elite.presentation.utils.HighRefreshRateManager
import dagger.hilt.android.AndroidEntryPoint

/**
 * 主活动类
 * 应用程序的入口点，负责设置Compose UI和导航
 * 支持高刷新率显示优化
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 启用高刷新率显示模式
        enableHighRefreshRate()
        
        setContent {
            GameAssistantTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GameAssistantNavigation(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
    
    /**
     * 启用高刷新率显示模式和硬件加速
     */
    private fun enableHighRefreshRate() {
        // 启用硬件加速
        HighRefreshRateManager.enableHardwareAcceleration(this)
        
        // 启用高刷新率
        when (val result = HighRefreshRateManager.enableHighRefreshRate(this)) {
            is HighRefreshRateManager.RefreshRateResult.Success -> {
                Log.i("MainActivity", "高刷新率启用成功")
                // 打印刷新率信息（仅在调试模式下）
                Log.d("MainActivity", HighRefreshRateManager.getRefreshRateInfo(this))
            }
            is HighRefreshRateManager.RefreshRateResult.NotSupported -> {
                Log.i("MainActivity", "设备不支持高刷新率显示")
            }
            is HighRefreshRateManager.RefreshRateResult.Error -> {
                Log.w("MainActivity", "高刷新率启用失败: ${result.message}")
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // 确保在应用恢复时重新应用高刷新率设置
        enableHighRefreshRate()
    }
}