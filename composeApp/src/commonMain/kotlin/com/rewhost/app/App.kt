package com.rewhost.app

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.rewhost.app.ui.screens.LoginScreen
import com.rewhost.app.ui.screens.dashboard.DashboardScreen
import com.rewhost.app.ui.theme.RewHostTheme
import com.rewhost.app.utils.AppSettings
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

@Composable
fun App() {
    KoinContext {
        RewHostTheme {
            val settings = koinInject<AppSettings>()
            val initialScreen = if (settings.isLoggedIn) DashboardScreen() else LoginScreen()

            Navigator(initialScreen) { navigator ->
                SlideTransition(navigator)
            }
        }
    }
}