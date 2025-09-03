package com.gameassistant.elite.di

import com.gameassistant.elite.data.repository.GameRepository
import com.gameassistant.elite.data.repository.GameRepositoryImpl
import com.gameassistant.elite.data.repository.SystemMonitorRepository
import com.gameassistant.elite.data.repository.SystemMonitorRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 仓库模块依赖注入配置
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindSystemMonitorRepository(
        systemMonitorRepositoryImpl: SystemMonitorRepositoryImpl
    ): SystemMonitorRepository
    
    @Binds
    @Singleton
    abstract fun bindGameRepository(
        gameRepositoryImpl: GameRepositoryImpl
    ): GameRepository
}