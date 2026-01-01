package com.rewhost.app.utils

import com.russhwolf.settings.Settings

expect class PlatformSettingsFactory {
    fun create(): Settings
}