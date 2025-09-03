package com.gameassistant.elite.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.gameassistant.elite.presentation.navigation.GameAssistantNavigation
import com.gameassistant.elite.presentation.theme.GameAssistantTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * 主活动类
 * 应用程序的入口点，负责设置Compose UI和导航
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
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
}