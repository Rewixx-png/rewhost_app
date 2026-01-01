package com.rewhost.app.utils

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppTheme {
    DARK, LIGHT, NEW_YEAR
}

class AppSettings(private val settings: Settings) {
    private val _theme = MutableStateFlow(getInitialTheme())
    val theme: StateFlow<AppTheme> = _theme.asStateFlow()

    private val _fontFamily = MutableStateFlow(settings.getString("font_family", "Inter"))
    val fontFamily: StateFlow<String> = _fontFamily.asStateFlow()

    private fun getInitialTheme(): AppTheme {
        val saved = settings.getString("theme", "NEW_YEAR")
        return try {
            AppTheme.valueOf(saved)
        } catch (e: Exception) {
            AppTheme.NEW_YEAR
        }
    }

    fun setTheme(newTheme: AppTheme) {
        settings["theme"] = newTheme.name
        _theme.value = newTheme
    }

    fun setFont(font: String) {
        settings["font_family"] = font
        _fontFamily.value = font
    }

    fun getToken(): String? = settings.getStringOrNull("api_token")
    fun setToken(token: String) {
        settings["api_token"] = token
    }
    fun clearToken() {
        settings.remove("api_token")
    }

    val isLoggedIn: Boolean
        get() = !getToken().isNullOrBlank()
}