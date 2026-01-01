package com.rewhost.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Цветовая палитра (можно настроить под себя)
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFD700),       // Золотой
    secondary = Color(0xFF6C63FF),     // Фиолетовый
    tertiary = Color(0xFF32D74B),      // Зеленый
    background = Color(0xFF0F172A),    // Темно-синий фон
    surface = Color(0xFF1E293B),       // Чуть светлее фон карточек
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    error = Color(0xFFEF4444)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFEAB308),
    secondary = Color(0xFF4F46E5),
    tertiary = Color(0xFF22C55E),
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A)
)

@Composable
fun RewHostTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // В commonMain используем стандартные схемы.
    // Если нужны Dynamic Colors (Material You), их нужно выносить в platform-specific код.
    // Пока сделаем стабильную тему, которая работает везде.
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
