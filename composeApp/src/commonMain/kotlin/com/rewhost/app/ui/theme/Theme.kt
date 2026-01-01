package com.rewhost.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.rewhost.app.utils.AppSettings
import com.rewhost.app.utils.AppTheme
import org.koin.compose.koinInject

@Composable
fun RewHostTheme(
    content: @Composable () -> Unit
) {
    val settings = koinInject<AppSettings>()
    val theme by settings.theme.collectAsState()

    val colorScheme = when (theme) {
        AppTheme.LIGHT -> lightColorScheme(
            primary = Color(0xFF007AFF), // Blue for Light Mode
            background = LightBackground,
            surface = LightSurface,
            onBackground = TextDark,
            onSurface = TextDark
        )
        AppTheme.DARK -> darkColorScheme(
            primary = RewPrimary,
            background = DarkBackground,
            surface = DarkSurface,
            onBackground = TextWhite,
            onSurface = TextWhite
        )
        AppTheme.NEW_YEAR -> darkColorScheme(
            primary = RewPrimary,
            background = DarkBackground,
            surface = DarkSurface,
            onBackground = TextWhite,
            onSurface = TextWhite
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
