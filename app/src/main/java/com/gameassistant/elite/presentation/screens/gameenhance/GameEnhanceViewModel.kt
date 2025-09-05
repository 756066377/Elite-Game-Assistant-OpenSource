package com.gameassistant.elite.presentation.screens.gameenhance

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gameassistant.elite.data.model.GameType
import com.gameassistant.elite.data.nativelib.NativeLibraryManager
import com.gameassistant.elite.data.repository.GameRepository

import com.gameassistant.elite.domain.model.GameInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 游戏增强中心视图模型
 */
@HiltViewModel
class GameEnhanceViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val nativeManager: NativeLibraryManager
) : ViewModel() {
    
    private val _games = MutableStateFlow<List<GameInfo>>(emptyList())
    val games: StateFlow<List<GameInfo>> = _games.asStateFlow()
    
    private val _selectedGame = MutableStateFlow<GameInfo?>(null)
    val selectedGame: StateFlow<GameInfo?> = _selectedGame.asStateFlow()
    
    private val _cardKeyInput = MutableStateFlow("")
    val cardKeyInput: StateFlow<String> = _cardKeyInput.asStateFlow()

    private val _isCardKeyDialogShown = MutableStateFlow(false)
    val isCardKeyDialogShown: StateFlow<Boolean> = _isCardKeyDialogShown.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()
    
    private val _existingCardKeyHint = MutableStateFlow<String?>(null)
    val existingCardKeyHint: StateFlow<String?> = _existingCardKeyHint.asStateFlow()
    
    init {
        loadGames()
    }
    
    /**
     * 加载游戏列表
     */
    private fun loadGames() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val gameList = gameRepository.getSupportedGames()
                _games.value = gameList
            } catch (e: Exception) {
                _error.value = "加载游戏列表失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 更新卡密输入
     */
    fun updateCardKeyInput(cardKey: String) {
        _cardKeyInput.value = cardKey
    }
    
    /**
     * 保存卡密并激活
     */
    fun activateCardKey() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val cardKey = _cardKeyInput.value.trim()
                
                if (cardKey.isEmpty()) {
                    _error.value = "请输入有效的卡密"
                    return@launch
                }
                
                // 根据当前选择的游戏确定游戏类型
                val gameType = when (_selectedGame.value?.name) {
                    "三角洲行动" -> "delta"
                    "和平精英" -> "pubg"
                    "无畏契约" -> "valorant"
                    else -> "default"
                }
                
                val success = gameRepository.saveCardKey(cardKey, gameType)
                if (success) {
                    _toastMessage.value = "卡密写入成功"
                    _cardKeyInput.value = "" // 清空输入
                    hideCardKeyDialog() // 关闭对话框
                } else {
                    _toastMessage.value = "卡密保存失败，请检查权限"
                }
            } catch (e: Exception) {
                _toastMessage.value = "激活失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    

    
    /**
     * 选择游戏
     */
    fun selectGame(game: GameInfo) {
        _selectedGame.value = game
        // 选择游戏后自动检测卡密文件
        viewModelScope.launch {
            checkExistingCardKey(game)
        }
    }
    
    /**
     * 选择游戏并显示卡密对话框
     */
    suspend fun selectGameAndShowDialog(game: GameInfo) {
        _selectedGame.value = game
        // 先检测卡密文件
        checkExistingCardKey(game)
        // 然后显示对话框
        _isCardKeyDialogShown.value = true
    }
    
    /**
     * 检测当前游戏的卡密文件是否存在
     */
    private suspend fun checkExistingCardKey(game: GameInfo) {
        try {
            // 根据游戏名称确定游戏类型
            val gameType = when (game.name) {
                "三角洲行动" -> "delta"
                "和平精英" -> "pubg"
                "无畏契约" -> "valorant"
                else -> "default"
            }
            
            Log.d("CARD_KEY_DEBUG", "Checking card key for game: ${game.name}, type: $gameType")
            
            // 仅检测卡密文件是否存在
            val present = gameRepository.isCardKeyFilePresent(gameType)
            Log.d("CARD_KEY_DEBUG", "Card key file present=$present")
            _existingCardKeyHint.value = if (present) "检测到卡密文件" else "未检测到卡密文件"
        } catch (e: Exception) {
            Log.e("CARD_KEY_DEBUG", "Error checking card key: ${e.message}")
            _existingCardKeyHint.value = "卡密检测失败: ${e.message}"
        }
    }
    
    /**
     * 启动游戏辅助
     */
    fun launchGameAssist(gameId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // 1. 根据游戏ID获取对应的GameType
                val gameType = when (gameId) {
                    "pubg_mobile" -> GameType.PUBG_MOBILE
                    "delta_force" -> GameType.DELTA_FORCE
                    "valorant_mobile" -> GameType.VALORANT_MOBILE
                    else -> {
                        _error.value = "不支持的游戏类型"
                        return@launch
                    }
                }
                
                // 3. 检查SO库是否已加载
                if (!nativeManager.isNativeLibraryLoaded()) {
                    _toastMessage.value = "原生库未加载"
                    return@launch
                }
                
                // 4. 启动对应游戏的辅助功能
                val assistStarted = nativeManager.startGameEnhancement(gameType, "")
                
                if (assistStarted) {
                    _toastMessage.value = "${gameType.displayName} 辅助功能已启动"
                    // 调用原有的游戏启动逻辑
                    gameRepository.launchGameAssist(gameId)
                } else {
                    _error.value = "${gameType.displayName} 辅助功能启动失败"
                }
                
            } catch (e: Exception) {
                _error.value = "启动失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 停止游戏辅助
     */
    fun stopGameAssist() {
        viewModelScope.launch {
            try {
                gameRepository.stopGameAssist()
            } catch (e: Exception) {
                _error.value = "停止失败: ${e.message}"
            }
        }
    }

    /**
     * 显示卡密输入对话框
     */
    fun showCardKeyDialog() {
        _isCardKeyDialogShown.value = true
    }

    /**
     * 隐藏卡密输入对话框
     */
    fun hideCardKeyDialog() {
        _isCardKeyDialogShown.value = false
    }


    
    /**
     * 清除错误信息
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * 清除 Toast 消息
     */
    fun clearToastMessage() {
        _toastMessage.value = null
    }
    
    /**
     * 刷新数据
     */
    fun refresh() {
        loadGames()
    }
}