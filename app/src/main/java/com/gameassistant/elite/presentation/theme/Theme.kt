package com.gameassistant.elite.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 空灵纯白主题配置
 */
private val LightColorScheme = lightColorScheme(
    primary = AccentBlue,
    onPrimary = PureWhite,
    primaryContainer = LightBlue,
    onPrimaryContainer = TextPrimary,
    
    secondary = SoftGray,
    onSecondary = TextPrimary,
    secondaryContainer = MistGray,
    onSecondaryContainer = TextSecondary,
    
    tertiary = LightCyan,
    onTertiary = TextPrimary,
    tertiaryContainer = LightCyan,
    onTertiaryContainer = TextSecondary,
    
    background = PureWhite,
    onBackground = TextPrimary,
    
    surface = PureWhite,
    onSurface = TextPrimary,
    surfaceVariant = MistGray,
    onSurfaceVariant = TextSecondary,
    
    outline = BorderMedium,
    outlineVariant = BorderLight,
    
    error = ErrorRed,
    onError = PureWhite,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = ErrorRed
)

@Composable
fun GameAssistantTheme(
    content: @Composable () -> Unit
) {
    // 当前只使用浅色主题，符合空灵纯白设计
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}