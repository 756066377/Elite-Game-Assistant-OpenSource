package com.gameassistant.elite

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * 游戏助手应用程序入口类
 * 使用Hilt进行依赖注入管理
 */
@HiltAndroidApp
class GameAssistantApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // 初始化应用程序配置
        initializeApp()
    }
    
    /**
     * 初始化应用程序
     */
    private fun initializeApp() {
        // 这里可以添加应用启动时的初始化逻辑
        // 例如：日志配置、崩溃报告、性能监控等
    }
}