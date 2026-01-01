package com.rewhost.app.ui.screens.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.rewhost.app.api.RewHostApi
import com.rewhost.app.data.model.DashboardResponse
import com.rewhost.app.ui.components.IslandState
import com.rewhost.app.ui.components.RewDynamicIsland
import com.rewhost.app.ui.screens.LoginScreen
import com.rewhost.app.ui.screens.dashboard.tabs.BotsTab
import com.rewhost.app.ui.screens.dashboard.tabs.HomeTab
import com.rewhost.app.ui.screens.dashboard.tabs.SettingsTab
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

class DashboardScreen : Screen {
    @Composable
    override fun Content() {
        val api = koinInject<RewHostApi>()
        val navigator = LocalNavigator.currentOrThrow
        
        var data by remember { mutableStateOf<DashboardResponse?>(null) }
        var selectedTab by remember { mutableStateOf(0) }
        
        var islandState by remember { mutableStateOf(IslandState.IDLE) }
        var islandText by remember { mutableStateOf("System Normal") }

        val scope = rememberCoroutineScope()

        fun loadData() {
            scope.launch {
                try {
                    islandState = IslandState.LOADING
                    islandText = "Updating..."
                    data = api.getDashboard()
                    islandState = IslandState.IDLE
                    islandText = "${data?.containers?.count { it.status == "running" } ?: 0} Bots Active"
                } catch (e: Exception) {
                    if (e.message?.contains("401") == true) {
                        navigator.replace(LoginScreen())
                    } else {
                        islandState = IslandState.ALERT
                        islandText = "Connection Error"
                    }
                }
            }
        }

        LaunchedEffect(Unit) {
            loadData()
            while (isActive) {
                delay(30000)
                try {
                    val status = api.getServerStatus()
                    val highLoad = status.data.any { (it.cpu?.replace("%","")?.toIntOrNull() ?: 0) > 80 }
                    if (highLoad) {
                        islandState = IslandState.ALERT
                        islandText = "High Server Load"
                    }
                } catch (_: Exception) {}
            }
        }

        Scaffold(
            contentWindowInsets = WindowInsets.systemBars,
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Home, null) },
                        label = { Text("Главная") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Dns, null) },
                        label = { Text("Боты") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.Settings, null) },
                        label = { Text("Меню") }
                    )
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                if (data != null) {
                    when (selectedTab) {
                        0 -> HomeTab(data!!, api)
                        1 -> BotsTab(data!!.containers)
                        2 -> SettingsTab(api)
                    }
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .windowInsetsPadding(WindowInsets.systemBars) 
                        .padding(top = 8.dp)
                ) {
                    RewDynamicIsland(state = islandState, mainText = islandText)
                }
            }
        }
    }
}
