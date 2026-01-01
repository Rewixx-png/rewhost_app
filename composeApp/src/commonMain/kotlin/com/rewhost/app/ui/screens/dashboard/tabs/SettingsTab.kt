package com.rewhost.app.ui.screens.dashboard.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.rewhost.app.api.RewHostApi
import com.rewhost.app.ui.components.GlassCard
import com.rewhost.app.ui.screens.LoginScreen
import com.rewhost.app.ui.screens.support.SupportScreen
import com.rewhost.app.ui.theme.ErrorRed
import com.rewhost.app.ui.theme.TextGray
import com.rewhost.app.ui.theme.TextWhite
import com.rewhost.app.utils.AppSettings
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun SettingsTab(api: RewHostApi) {
    val navigator = LocalNavigator.currentOrThrow
    val settings = koinInject<AppSettings>()
    val scope = rememberCoroutineScope()

    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        item {
            Text("Настройки", color = TextWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 24.dp))
        }

        item {
            SettingsGroup("Аккаунт") {
                SettingsItem(Icons.Default.SupportAgent, "Поддержка") {
                    navigator.push(SupportScreen()) // Теперь нативный экран!
                }
                SettingsItem(Icons.Default.AdminPanelSettings, "Админ панель") {
                    // Пока нет полноценной админки, но можно открыть список серверов
                    // navigator.push(AdminServersScreen()) 
                    // Для примера оставим пустым или тост
                }
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
            SettingsGroup("Приложение") {
                SettingsItem(Icons.Default.Logout, "Выйти", color = ErrorRed) {
                    scope.launch {
                        api.logout()
                        navigator.replaceAll(LoginScreen())
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title, color = TextGray, fontSize = 14.sp, modifier = Modifier.padding(start = 12.dp, bottom = 8.dp))
        GlassCard(modifier = Modifier.fillMaxWidth(), padding = 0.dp) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, color: Color = TextWhite, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Text(title, color = color, fontSize = 16.sp)
        Spacer(Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = TextGray.copy(alpha = 0.5f))
    }
}