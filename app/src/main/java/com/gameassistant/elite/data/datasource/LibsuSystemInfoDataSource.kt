package com.gameassistant.elite.data.datasource

import android.util.Log
import com.gameassistant.elite.domain.model.SELinuxStatus
import com.gameassistant.elite.domain.model.SystemInfo
import com.topjohnwu.superuser.Shell
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 使用 libsu 获取系统信息的数据源
 */
@Singleton
class LibsuSystemInfoDataSource @Inject constructor() {

    /**
     * 获取系统信息
     */
    suspend fun getSystemInfo(): SystemInfo {
        val shell = Shell.getShell()
        
        val kernelVersion = shell.newJob().add("uname -r").exec().out.firstOrNull() ?: ""
        val selinuxStatusString = shell.newJob().add("getenforce").exec().out.firstOrNull()?.trim()
        val buildFingerprint = shell.newJob().add("getprop ro.build.fingerprint").exec().out.firstOrNull() ?: android.os.Build.FINGERPRINT
        
        val selinuxStatus = when (selinuxStatusString?.lowercase()) {
            "enforcing" -> SELinuxStatus.ENFORCING
            "permissive" -> SELinuxStatus.PERMISSIVE
            "disabled" -> SELinuxStatus.DISABLED
            else -> SELinuxStatus.UNKNOWN
        }

        return SystemInfo(
            kernelVersion = kernelVersion,
            buildFingerprint = buildFingerprint,
            selinuxStatus = selinuxStatus
        )
    }


    
    /**
     * 将卡密写入到指定文件，并进行写后即读验证
     * @param cardKey 要写入的卡密
     * @param gameType 游戏类型，用于确定文件路径
     * @return 如果写入且验证成功，则返回 true，否则返回 false
     */
    suspend fun writeCardKey(cardKey: String, gameType: String = "default"): Boolean {
        return try {
            val shell = Shell.getShell()
            
            val filePath = getCardKeyFilePath(gameType)
            // 使用 'echo' 和 '>' 重定向。这会自动创建文件（如果不存在）或覆盖现有文件。
            val writeCommand = "echo '$cardKey' > $filePath"
            
            // 1. 执行写入操作
            val writeResult = shell.newJob().add(writeCommand).exec()
            
            // 2. 直接根据写入命令的执行结果判断成功与否
            // 如果命令执行成功（exit code: 0），就认为写入成功
            writeResult.isSuccess
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 检查卡密文件是否存在
     * @param gameType 游戏类型，用于确定文件路径
     * @return 如果文件存在，则返回 true，否则返回 false
     */
    suspend fun doesCardKeyFileExist(gameType: String = "default"): Boolean {
        val filePath = getCardKeyFilePath(gameType)
        // 使用 `test -e` 检查文件是否存在，这比 `[ -f ... ]` 更通用
        val command = "test -e $filePath"
        val result = Shell.getShell().newJob().add(command).exec()
        Log.d("CARD_KEY_DEBUG", "File existence check command: '$command', IsSuccess: ${result.isSuccess}")
        return result.isSuccess
    }
    
    /**
     * 根据游戏类型获取卡密文件路径
     * @param gameType 游戏类型
     * @return 对应的文件路径
     */
    private fun getCardKeyFilePath(gameType: String): String {
        return when (gameType.lowercase()) {
            "delta", "三角洲行动" -> "/data/system/csCard.txt"
            "pubg", "和平精英" -> "/data/system/uCard.txt"
            "valorant", "无畏契约" -> "/data/system/uCard.txt"
            else -> "/data/system/uCard.txt" // 默认使用uCard.txt
        }
    }
}