package com.gameassistant.elite

import android.app.Application
import com.topjohnwu.superuser.Shell
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
        // 初始化libsu Shell
        initializeShell()
        // 这里可以添加应用启动时的初始化逻辑
        // 例如：日志配置、崩溃报告、性能监控等
    }
    
    /**
     * 初始化libsu Shell
     * 必须在应用启动时调用，以确保Root检测功能正常工作
     */
    private fun initializeShell() {
        try {
            // 使用ApplicationInfo来判断调试模式，避免BuildConfig引用问题
            val isDebug = (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
            Shell.enableVerboseLogging = isDebug
            Shell.setDefaultBuilder(Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10))
        } catch (e: Exception) {
            // 静默处理初始化异常
        }
    }
}