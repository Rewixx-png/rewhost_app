package com.rewhost.app.utils

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

actual class PlatformSettingsFactory(private val context: Context) {
    actual fun create(): Settings {
        val prefs = context.getSharedPreferences("rewhost_app_prefs", Context.MODE_PRIVATE)
        return SharedPreferencesSettings(prefs)
    }
}