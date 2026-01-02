package com.rewhost.app.ui.screens.dashboard.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha // <--- ВОТ ЭТОГО НЕ ХВАТАЛО
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.rewhost.app.data.model.DashboardResponse
import com.rewhost.app.ui.components.BouncyBtn
import com.rewhost.app.ui.components.GlassCard
import com.rewhost.app.ui.screens.ContainerDetailScreen
import com.rewhost.app.ui.screens.shop.ShopScreen
import com.rewhost.app.ui.theme.*

@Composable
fun BotsTab(data: DashboardResponse) {
    val navigator = LocalNavigator.currentOrThrow
    val containers = data.containers

    fun openShop() {
        navigator.push(ShopScreen(data.servers, data.tariffs, data.images, data.profile.hasUsedFree))
    }

    if (containers.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Здесь используется .alpha(0.3f)
                Icon(Icons.Default.Layers, null, tint = TextGray, modifier = Modifier.size(64.dp).alpha(0.3f))
                Spacer(Modifier.height(16.dp))
                Text("У вас нет активных ботов", color = TextGray)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { openShop() },
                    colors = ButtonDefaults.buttonColors(containerColor = RewBlue)
                ) {
                    Text("Создать бота")
                }
            }
        }
        return
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Мои контейнеры", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)

                Button(
                    onClick = { openShop() },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RewBlue)
                ) {
                    Text("Создать")
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        items(containers) { container ->
            val isRunning = container.status == "running"
            val statusColor = if (isRunning) RewGreen else RewRed
            val statusText = if (isRunning) "ONLINE" else "OFFLINE"

            BouncyBtn(onClick = { navigator.push(ContainerDetailScreen(container)) }) {
                GlassCard(padding = 16.dp, borderColor = statusColor.copy(alpha = 0.2f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Icon Box
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(statusColor.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Dns, null, tint = statusColor)
                        }

                        Spacer(Modifier.width(16.dp))

                        Column(Modifier.weight(1f)) {
                            Text(
                                container.containerName ?: "Unknown",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ContainerTag(container.serverInfo?.name ?: "SRV", Slate700)
                                Spacer(Modifier.width(6.dp))
                                ContainerTag(container.tariffInfo?.name ?: "PLAN", Slate700)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(statusText, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Icon(Icons.Default.ChevronRight, null, tint = TextGray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContainerTag(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text, color = TextWhite, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}