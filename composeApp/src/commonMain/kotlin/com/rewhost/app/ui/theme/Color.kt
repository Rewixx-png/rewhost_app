package com.rewhost.app.ui.theme

import androidx.compose.ui.graphics.Color

// --- Palette ---
val Slate950 = Color(0xFF020617)
val Slate900 = Color(0xFF0F172A)
val Slate800 = Color(0xFF1E293B)
val Slate700 = Color(0xFF334155)
val Slate600 = Color(0xFF475569)
val Slate500 = Color(0xFF64748B)
val Slate400 = Color(0xFF94A3B8)
val Slate300 = Color(0xFFCBD5E1)
val Slate200 = Color(0xFFE2E8F0)

val RewBlue = Color(0xFF3B82F6)
val RewPurple = Color(0xFF8B5CF6)
val RewGreen = Color(0xFF10B981)
val RewYellow = Color(0xFFEAB308)
val RewRed = Color(0xFFEF4444)
val RewCyan = Color(0xFF06B6D4)

// --- Aliases (Fixes Unresolved References) ---
val RewPrimary = RewBlue
val RewSecondary = RewPurple
val RewAccent = RewGreen

val ErrorRed = RewRed
val SuccessGreen = RewGreen
val WarningOrange = RewYellow

val DarkBackground = Slate950
val DarkSurface = Slate800

val TextWhite = Color(0xFFF8FAFC)
val TextGray = Slate400
val TextDark = Slate950

// !!! ЭТИ ПЕРЕМЕННЫЕ ВЫЗЫВАЛИ ОШИБКУ !!!
val GlassBorder = Color(0xFFFFFFFF).copy(alpha = 0.08f)
val GlassSurface = Slate800.copy(alpha = 0.6f)