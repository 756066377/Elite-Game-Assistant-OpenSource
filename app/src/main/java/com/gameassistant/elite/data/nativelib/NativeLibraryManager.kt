package com.gameassistant.elite.data.nativelib

/**
 * Native 库加载与接口桥接
 * - 确保在调用 external 方法前，先调用 ensureLoaded 对应游戏库
 * - 约定 .so 最终打包名：libdelta.so / libpubg.so / libvalorant.so（加载名分别为 delta/pubg/valorant）
 */
object NativeLibraryManager {

    enum class GameLib(val libName: String) {
        Delta("delta"),
        Pubg("pubg"),
        Valorant("valorant")
    }

    @Volatile
    private val loaded: MutableSet<String> = mutableSetOf()

    /**
     * 按需加载对应 .so（lib{libName}.so）
     */
    fun ensureLoaded(game: GameLib) {
        if (loaded.contains(game.libName)) return
        synchronized(this) {
            if (!loaded.contains(game.libName)) {
                System.loadLibrary(game.libName)
                loaded.add(game.libName)
            }
        }
    }

    // 示例 external 方法（按需补充/修改）
    external fun nativeInit(gameFlag: Int): Int
    external fun startGameEnhancement(gameFlag: Int, options: Int = 0): Int
    external fun stopGameEnhancement(): Int
}