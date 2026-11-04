package com.xubopos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val POSColorScheme = lightColorScheme(
    primary = Slate800,
    onPrimary = PureWhite,
    primaryContainer = Slate100,
    onPrimaryContainer = Slate900,
    secondary = Amber600,
    onSecondary = PureWhite,
    secondaryContainer = Amber100,
    onSecondaryContainer = Amber700,
    tertiary = Emerald600,
    onTertiary = PureWhite,
    tertiaryContainer = Emerald100,
    onTertiaryContainer = Emerald700,
    background = Slate50,
    onBackground = Slate900,
    surface = PureWhite,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    error = Rose600,
    onError = PureWhite,
    errorContainer = Rose100,
    onErrorContainer = Rose700,
    outline = Slate200
)

@Composable
fun AppTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = POSColorScheme,
        typography = Typography,
        content = content
    )
}
