package com.gameassistant.elite.data.model

/**
 * 游戏类型枚举
 */
enum class GameType(
    val displayName: String,
    val packageName: String,
    val features: List<String>
) {
    PUBG_MOBILE(
        displayName = "和平精英",
        packageName = "com.tencent.tmgp.pubgmhd",
        features = listOf("子弹追踪", "自瞄", "掩体判断", "人物绘制", "物资绘制")
    ),
    
    DELTA_FORCE(
        displayName = "三角洲行动",
        packageName = "com.tencent.tmgp.deltaforce",
        features = listOf("人物绘制", "物质绘制", "高价值物资过滤", "陀螺仪自瞄")
    ),
    
    VALORANT_MOBILE(
        displayName = "无畏契约手游",
        packageName = "com.riotgames.valorant.mobile",
        features = listOf("人物绘制", "物资绘制", "陀螺仪自瞄")
    )
}