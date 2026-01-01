package com.rewhost.app.ui.screens.dashboard.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border // !!! FIXED
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.rewhost.app.api.RewHostApi
import com.rewhost.app.ui.components.BouncyBtn
import com.rewhost.app.ui.components.GlassCard
import com.rewhost.app.ui.screens.LoginScreen
import com.rewhost.app.ui.theme.ErrorRed
import com.rewhost.app.ui.theme.TextGray
import com.rewhost.app.ui.theme.TextWhite
import kotlinx.coroutines.launch

@Composable
fun SettingsTab(api: RewHostApi) {
    val navigator = LocalNavigator.currentOrThrow
    val scope = rememberCoroutineScope()
    
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Настройки", color = TextWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 24.dp, start = 8.dp))
        
        GlassCard(modifier = Modifier.fillMaxWidth(), padding = 0.dp) {
            Column {
                SettingItem(icon = Icons.Default.Person, title = "Профиль", subtitle = "Изменить данные")
                Divider()
                SettingItem(icon = Icons.Default.Settings, title = "Приложение", subtitle = "Тема, язык, уведомления")
                Divider()
                SettingItem(icon = Icons.Default.SupportAgent, title = "Поддержка", subtitle = "Связаться с нами")
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        BouncyBtn(onClick = {
            scope.launch {
                api.logout()
                navigator.replaceAll(LoginScreen())
            }
        }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ErrorRed.copy(alpha = 0.15f))
                    .border(1.dp, ErrorRed.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Выйти из аккаунта", color = ErrorRed, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(Modifier.weight(1f))
        Text("RewHost App v1.0.0", color = TextGray.copy(alpha = 0.5f), fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
fun SettingItem(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = TextGray, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = TextGray, fontSize = 12.sp)
        }
        Icon(Icons.Default.ChevronRight, null, tint = TextGray.copy(alpha = 0.3f))
    }
}

@Composable
fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.White.copy(alpha = 0.05f))
    )
}