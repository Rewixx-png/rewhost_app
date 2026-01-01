package com.rewhost.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.rewhost.app.api.RewHostApi
import com.rewhost.app.data.model.DashboardResponse
import com.rewhost.app.ui.components.GlassCard
import com.rewhost.app.ui.screens.LoginScreen
import com.rewhost.app.ui.screens.dashboard.tabs.BotsTab
import com.rewhost.app.ui.screens.dashboard.tabs.HomeTab
import com.rewhost.app.ui.screens.dashboard.tabs.SettingsTab
import com.rewhost.app.ui.theme.DarkBackground
import com.rewhost.app.ui.theme.ErrorRed
import com.rewhost.app.ui.theme.LightBackground
import com.rewhost.app.ui.theme.RewPrimary
import com.rewhost.app.ui.theme.TextGray
import com.rewhost.app.ui.theme.TextWhite
import com.rewhost.app.utils.AppSettings
import com.rewhost.app.utils.AppTheme
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

class DashboardScreen : Screen {
    @Composable
    override fun Content() {
        val api = koinInject<RewHostApi>()
        val navigator = LocalNavigator.currentOrThrow
        val settings = koinInject<AppSettings>()
        val theme by settings.theme.collectAsState()
        val clipboardManager = LocalClipboardManager.current
        
        var data by remember { mutableStateOf<DashboardResponse?>(null) }
        var errorState by remember { mutableStateOf<String?>(null) }
        var selectedTab by remember { mutableStateOf(0) }
        val scope = rememberCoroutineScope()

        fun loadData() {
            errorState = null
            scope.launch {
                try {
                    data = api.getDashboard()
                } catch (e: Exception) {
                    if (e.message?.contains("401") == true || e.message?.contains("AUTH") == true) {
                        navigator.replace(LoginScreen())
                    } else {
                        errorState = e.message ?: "Unknown Error"
                        e.printStackTrace()
                    }
                }
            }
        }

        LaunchedEffect(Unit) { loadData() }

        val bgColors = if (theme == AppTheme.LIGHT) {
            listOf(LightBackground, Color(0xFFE5E5EA))
        } else {
            listOf(DarkBackground, Color(0xFF020617))
        }
        val bgBrush = Brush.verticalGradient(colors = bgColors)

        if (errorState != null) {
            Box(
                Modifier.fillMaxSize().background(bgBrush).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Warning, null, tint = ErrorRed, modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Ошибка загрузки", fontWeight = FontWeight.Bold, color = if(theme == AppTheme.LIGHT) Color.Black else TextWhite, fontSize = 22.sp)
                    Spacer(Modifier.height(8.dp))
                    
                    GlassCard(modifier = Modifier.fillMaxWidth().clickable { clipboardManager.setText(AnnotatedString(errorState!!)) }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                             Text(errorState!!, color = TextGray, fontSize = 13.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ContentCopy, null, tint = RewPrimary, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Нажми, чтобы скопировать", color = RewPrimary, fontSize = 11.sp)
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { loadData() }) { Text("Повторить") }
                }
            }
            return
        }

        if (data == null) {
            Box(Modifier.fillMaxSize().background(bgBrush), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = RewPrimary)
            }
            return
        }

        val d = data!!

        Scaffold(
            bottomBar = {
                BottomAppBar(
                    containerColor = Color(0xFF1E293B).copy(alpha = 0.95f),
                    contentColor = TextGray
                ) {
                    val items = listOf(
                        Triple("Главная", Icons.Default.Home, 0),
                        Triple("Боты", Icons.Default.Dns, 1),
                        Triple("Ещё", Icons.Default.Settings, 2)
                    )
                    items.forEach { (label, icon, idx) ->
                        NavigationBarItem(
                            selected = selectedTab == idx,
                            onClick = { selectedTab = idx },
                            icon = { Icon(icon, null) },
                            label = { Text(label, fontSize = 10.sp, fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TextWhite,
                                selectedTextColor = TextWhite,
                                indicatorColor = RewPrimary.copy(alpha = 0.2f),
                                unselectedIconColor = TextGray,
                                unselectedTextColor = TextGray
                            )
                        )
                    }
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().background(bgBrush).padding(padding)) {
                when (selectedTab) {
                    0 -> HomeTab(d, api)
                    1 -> BotsTab(d.containers)
                    2 -> SettingsTab(api)
                }
            }
        }
    }
}
