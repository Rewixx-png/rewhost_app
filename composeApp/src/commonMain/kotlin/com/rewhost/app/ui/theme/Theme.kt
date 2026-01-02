package com.rewhost.app.ui.theme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = RewBlue,
    onPrimary = Color.White,
    background = Slate950,
    surface = Slate800,
    onBackground = TextWhite,
    onSurface = TextWhite,
    error = ErrorRed
)

@Composable
fun RewHostTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColorScheme, content = content)
}