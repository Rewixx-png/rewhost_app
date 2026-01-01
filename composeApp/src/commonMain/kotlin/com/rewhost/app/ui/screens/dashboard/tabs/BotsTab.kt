package com.rewhost.app.ui.screens.dashboard.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.rewhost.app.data.model.Container
import com.rewhost.app.ui.components.BouncyBtn
import com.rewhost.app.ui.components.GlassCard
import com.rewhost.app.ui.screens.ContainerDetailScreen
import com.rewhost.app.ui.screens.shop.ShopScreen
import com.rewhost.app.ui.theme.ErrorRed
import com.rewhost.app.ui.theme.SuccessGreen
import com.rewhost.app.ui.theme.TextGray
import com.rewhost.app.ui.theme.TextWhite

@Composable
fun BotsTab(containers: List<Container>) {
    val navigator = LocalNavigator.currentOrThrow
    
    if (containers.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Layers, null, tint = TextGray, modifier = Modifier.size(64.dp).alpha(0.3f))
                Spacer(Modifier.height(16.dp))
                Text("У вас нет активных ботов", color = TextGray)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { navigator.push(ShopScreen()) }) { 
                    Text("Создать бота") 
                }
            }
        }
        return
    }

    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        item {
            Text("Мои контейнеры", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp, start = 4.dp))
        }
        
        items(containers) { container ->
            val isRunning = container.status == "running"
            val statusColor = if (isRunning) SuccessGreen else ErrorRed
            val statusText = if (isRunning) "Активен" else "Остановлен"

            BouncyBtn(
                onClick = { navigator.push(ContainerDetailScreen(container)) },
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                GlassCard(modifier = Modifier.fillMaxWidth(), padding = 16.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Dns, null, tint = statusColor, modifier = Modifier.size(24.dp))
                        }
                        
                        Spacer(Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(container.containerName ?: "Unknown", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ContainerBadge(text = container.imageInfo?.name ?: "System", color = Color(0xFF64748B))
                                Spacer(Modifier.width(6.dp))
                                Text(statusText, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = TextGray.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

@Composable
fun ContainerBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}
