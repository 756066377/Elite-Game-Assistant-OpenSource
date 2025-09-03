package com.gameassistant.elite.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gameassistant.elite.data.repository.SystemMonitorRepository
import com.gameassistant.elite.domain.model.BatteryInfo
import com.gameassistant.elite.domain.model.CpuInfo
import com.gameassistant.elite.domain.model.GpuInfo
import com.gameassistant.elite.domain.model.MemoryInfo
import com.gameassistant.elite.domain.model.SystemInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 仪表盘视图模型
 * 管理系统监控数据的获取和状态管理
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val systemMonitorRepository: SystemMonitorRepository
) : ViewModel() {
    
    private val _systemInfo = MutableStateFlow(SystemInfo())
    val systemInfo: StateFlow<SystemInfo> = _systemInfo.asStateFlow()
    
    private val _cpuInfo = MutableStateFlow(CpuInfo())
    val cpuInfo: StateFlow<CpuInfo> = _cpuInfo.asStateFlow()
    
    private val _gpuInfo = MutableStateFlow(GpuInfo())
    val gpuInfo: StateFlow<GpuInfo> = _gpuInfo.asStateFlow()
    
    private val _memoryInfo = MutableStateFlow(MemoryInfo())
    val memoryInfo: StateFlow<MemoryInfo> = _memoryInfo.asStateFlow()
    
    private val _batteryInfo = MutableStateFlow(BatteryInfo())
    val batteryInfo: StateFlow<BatteryInfo> = _batteryInfo.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadSystemInfo()
        startMonitoring()
        startRootStatusMonitoring()
    }
    
    /**
     * 加载系统基本信息
     */
    private fun loadSystemInfo() {
        viewModelScope.launch {
            try {
                val info = systemMonitorRepository.getSystemInfo()
                _systemInfo.value = info
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "加载系统信息失败: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 开始监控Root状态
     * 每5秒检查一次Root状态，以便及时响应权限变化
     */
    private fun startRootStatusMonitoring() {
        viewModelScope.launch {
            while (true) {
                try {
                    kotlinx.coroutines.delay(5000) // 每5秒检查一次
                    val currentRootStatus = systemMonitorRepository.checkRootStatus()
                    
                    // 如果Root状态发生变化，更新SystemInfo
                    if (currentRootStatus != _systemInfo.value.isRooted) {
                        val updatedInfo = _systemInfo.value.copy(isRooted = currentRootStatus)
                        _systemInfo.value = updatedInfo
                    }
                } catch (e: Exception) {
                    // 静默处理Root状态检查错误，避免干扰用户体验
                }
            }
        }
    }
    
    /**
     * 开始监控系统状态
     */
    private fun startMonitoring() {
        // 监控CPU信息
        viewModelScope.launch {
            systemMonitorRepository.getCpuInfoFlow()
                .catch { e -> _error.value = "CPU监控失败: ${e.message}" }
                .collect { _cpuInfo.value = it }
        }
        
        // 监控GPU信息
        viewModelScope.launch {
            systemMonitorRepository.getGpuInfoFlow()
                .catch { e -> _error.value = "GPU监控失败: ${e.message}" }
                .collect { _gpuInfo.value = it }
        }
        
        // 监控内存信息
        viewModelScope.launch {
            systemMonitorRepository.getMemoryInfoFlow()
                .catch { e -> _error.value = "内存监控失败: ${e.message}" }
                .collect { _memoryInfo.value = it }
        }
        
        // 监控电池信息
        viewModelScope.launch {
            systemMonitorRepository.getBatteryInfoFlow()
                .catch { e -> _error.value = "电池监控失败: ${e.message}" }
                .collect { _batteryInfo.value = it }
        }
    }
    
    /**
     * 清除错误信息
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * 刷新数据
     */
    fun refresh() {
        _isLoading.value = true
        _error.value = null
        loadSystemInfo()
    }
    
    /**
     * 手动刷新Root状态
     * 当用户刚授予Root权限时可以调用此方法立即更新状态
     */
    fun refreshRootStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentRootStatus = systemMonitorRepository.checkRootStatus()
                val updatedInfo = _systemInfo.value.copy(isRooted = currentRootStatus)
                _systemInfo.value = updatedInfo
            } catch (e: Exception) {
                _error.value = "Root状态检查失败: ${e.message}"
            }
        }
    }
}