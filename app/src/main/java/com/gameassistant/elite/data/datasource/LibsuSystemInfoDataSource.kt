package com.gameassistant.elite.data.datasource

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
     * 获取系统信息，包括真实的 Root 状态
     */
    suspend fun getSystemInfo(): SystemInfo {
        val shell = Shell.getShell()
        
        // 使用统一的Root检测逻辑
        val isRooted = checkRootStatus()
        
        val kernelVersion = shell.newJob().add("uname -r").exec().out.firstOrNull() ?: ""
        val selinuxStatusString = shell.newJob().add("getenforce").exec().out.firstOrNull()?.trim()
        
        val selinuxStatus = when (selinuxStatusString) {
            "Enforcing" -> SELinuxStatus.ENFORCING
            "Permissive" -> SELinuxStatus.PERMISSIVE
            "Disabled" -> SELinuxStatus.DISABLED
            else -> SELinuxStatus.UNKNOWN
        }

        return SystemInfo(
            kernelVersion = kernelVersion,
            selinuxStatus = selinuxStatus,
            isRooted = isRooted
        )
    }

    /**
     * 检查设备的 Root 状态
     * @return 如果设备已 Root，则返回 true，否则返回 false
     */
    suspend fun checkRootStatus(): Boolean {
        return try {
            // 确保Shell已初始化
            val shell = Shell.getShell()
            
            // 双重验证：既检查Shell状态，也执行一个简单的Root命令
            if (!shell.isRoot) {
                return false
            }
            
            // 执行一个简单的Root命令来验证实际权限
            val testResult = shell.newJob().add("id").exec()
            val output = testResult.out.firstOrNull() ?: ""
            
            // 检查命令是否成功执行，并且输出包含uid=0（root用户）
            testResult.isSuccess && output.contains("uid=0")
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 将卡密写入到指定文件，并进行写后即读验证
     * @param cardKey 要写入的卡密
     * @return 如果写入且验证成功，则返回 true，否则返回 false
     */
    suspend fun writeCardKey(cardKey: String): Boolean {
        val shell = Shell.getShell()
        if (!shell.isRoot) {
            return false // 如果没有Root权限，直接返回失败
        }

        val filePath = "/data/system/uCard.txt"
        // 使用 'echo' 和 '>' 重定向。这会自动创建文件（如果不存在）或覆盖现有文件。
        val writeCommand = "echo '$cardKey' > $filePath"
        
        // 1. 执行写入操作
        val writeResult = shell.newJob().add(writeCommand).exec()
        if (!writeResult.isSuccess) {
            return false // 写入命令执行失败
        }

        // 2. 写后即读，进行验证
        val readResult = shell.newJob().add("cat $filePath").exec()
        if (!readResult.isSuccess) {
            return false // 读取命令执行失败
        }

        val contentRead = readResult.out.firstOrNull()?.trim()

        // 3. 比较读取的内容是否与写入的完全一致
        return contentRead == cardKey && contentRead.isNotEmpty()
    }

    /**
     * 从指定文件读取卡密
     * @return 如果读取成功，则返回卡密字符串，否则返回 null
     */
    suspend fun readCardKey(): String? {
        val command = "cat /data/system/uCard.txt"
        val result = Shell.getShell().newJob().add(command).exec()
        return if (result.isSuccess) {
            result.out.firstOrNull()?.trim()
        } else {
            null
        }
    }
}