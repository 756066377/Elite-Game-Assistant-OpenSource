package com.gameassistant.elite.data.repository

import android.content.Context
import android.content.pm.PackageManager
import com.gameassistant.elite.R
import com.gameassistant.elite.data.repository.SystemMonitorRepository
import com.gameassistant.elite.domain.model.AuthStatus
import com.gameassistant.elite.domain.model.FeatureCategory
import com.gameassistant.elite.domain.model.GameFeature
import com.gameassistant.elite.domain.model.GameInfo
import com.gameassistant.elite.domain.model.GameLaunchStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 游戏数据仓库实现类
 */
@Singleton
class GameRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val systemMonitorRepository: SystemMonitorRepository
) : GameRepository {
    
    private val _gameLaunchStatus = MutableStateFlow(GameLaunchStatus.IDLE)
    
    override suspend fun getSupportedGames(): List<GameInfo> {
        return listOf(
            GameInfo(
                id = "pubg_mobile",
                name = "和平精英",
                packageName = "com.tencent.tmgp.pubgmhd",
                iconResId = R.drawable.ic_game_pubg,
                isInstalled = checkGameInstallation("com.tencent.tmgp.pubgmhd"),
                features = listOf(
                    GameFeature(
                        category = FeatureCategory.VISUAL_ENHANCEMENT,
                        name = "人物绘制",
                        description = "高亮显示敌方玩家轮廓"
                    ),
                    GameFeature(
                        category = FeatureCategory.VISUAL_ENHANCEMENT,
                        name = "物资绘制",
                        description = "显示重要物资位置"
                    ),
                    GameFeature(
                        category = FeatureCategory.TACTICAL_ASSIST,
                        name = "掩体判断",
                        description = "智能分析掩体位置"
                    ),
                    GameFeature(
                        category = FeatureCategory.AIM_ASSIST,
                        name = "子弹追踪",
                        description = "显示子弹轨迹路径"
                    ),
                    GameFeature(
                        category = FeatureCategory.AIM_ASSIST,
                        name = "自适应自瞄",
                        description = "智能瞄准辅助系统"
                    )
                ),
                description = "专业级战术竞技辅助功能包"
            ),
            GameInfo(
                id = "delta_force",
                name = "三角洲行动",
                packageName = "com.tencent.tmgp.deltaforce",
                iconResId = R.drawable.ic_game_delta,
                isInstalled = checkGameInstallation("com.tencent.tmgp.deltaforce"),
                features = listOf(
                    GameFeature(
                        category = FeatureCategory.VISUAL_ENHANCEMENT,
                        name = "人物绘制",
                        description = "敌方玩家高亮显示"
                    ),
                    GameFeature(
                        category = FeatureCategory.VISUAL_ENHANCEMENT,
                        name = "高价值物资过滤",
                        description = "筛选显示重要装备"
                    ),
                    GameFeature(
                        category = FeatureCategory.AIM_ASSIST,
                        name = "陀螺仪辅助自瞄",
                        description = "结合陀螺仪的精准瞄准"
                    )
                ),
                description = "现代战术射击辅助功能包"
            ),
            GameInfo(
                id = "valorant_mobile",
                name = "无畏契约手游",
                packageName = "com.riotgames.valorantmobile",
                iconResId = R.drawable.ic_game_valorant,
                isInstalled = checkGameInstallation("com.riotgames.valorantmobile"),
                features = listOf(
                    GameFeature(
                        category = FeatureCategory.VISUAL_ENHANCEMENT,
                        name = "人物绘制",
                        description = "敌方角色轮廓显示"
                    ),
                    GameFeature(
                        category = FeatureCategory.VISUAL_ENHANCEMENT,
                        name = "物资绘制",
                        description = "武器和道具位置标记"
                    ),
                    GameFeature(
                        category = FeatureCategory.AIM_ASSIST,
                        name = "陀螺仪辅助自瞄",
                        description = "精确瞄准辅助系统"
                    )
                ),
                description = "竞技射击辅助功能包"
            )
        )
    }
    
    override suspend fun checkGameInstallation(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    
    override suspend fun getAuthStatus(): AuthStatus {
        return try {
            // 优先使用 libsu 读取卡密
            val cardKey = systemMonitorRepository.readCardKey()
            if (!cardKey.isNullOrEmpty()) {
                // 这里简化处理，实际验证由SO文件完成
                AuthStatus(
                    isAuthorized = true,
                    cardKey = cardKey,
                    expiryTime = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000, // 30天
                    remainingDays = 30
                )
            } else {
                // 备选方案：从应用私有目录读取
                val fallbackFile = File(context.filesDir, "uCard.txt")
                if (fallbackFile.exists()) {
                    val fallbackKey = fallbackFile.readText().trim()
                    if (fallbackKey.isNotEmpty()) {
                        AuthStatus(
                            isAuthorized = true,
                            cardKey = fallbackKey,
                            expiryTime = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
                            remainingDays = 30
                        )
                    } else {
                        AuthStatus()
                    }
                } else {
                    AuthStatus()
                }
            }
        } catch (e: Exception) {
            AuthStatus()
        }
    }
    
    override suspend fun saveCardKey(cardKey: String): Boolean {
        return try {
            // 只使用 libsu 进行 Root 权限写入
            systemMonitorRepository.writeCardKey(cardKey)
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun launchGameAssist(gameId: String): Boolean {
        return try {
            _gameLaunchStatus.value = GameLaunchStatus.LOADING
            
            // 检查授权状态
            val authStatus = getAuthStatus()
            if (!authStatus.isAuthorized) {
                _gameLaunchStatus.value = GameLaunchStatus.UNAUTHORIZED
                return false
            }
            
            // 模拟SO文件加载过程
            delay(2000)
            
            // 这里应该调用实际的SO文件加载逻辑
            // System.loadLibrary("gameassist")
            // 调用native方法启动对应游戏的辅助功能
            
            _gameLaunchStatus.value = GameLaunchStatus.RUNNING
            true
        } catch (e: Exception) {
            _gameLaunchStatus.value = GameLaunchStatus.ERROR
            false
        }
    }
    
    override fun getGameLaunchStatusFlow(): Flow<GameLaunchStatus> {
        return _gameLaunchStatus.asStateFlow()
    }
    
    override suspend fun stopGameAssist(): Boolean {
        return try {
            // 这里应该调用SO文件的停止方法
            _gameLaunchStatus.value = GameLaunchStatus.IDLE
            true
        } catch (e: Exception) {
            false
        }
    }
}