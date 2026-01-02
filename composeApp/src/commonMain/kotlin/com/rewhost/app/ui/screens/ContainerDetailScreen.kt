package com.rewhost.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.rewhost.app.api.RewHostApi
import com.rewhost.app.data.model.Container
import com.rewhost.app.ui.components.AppBackground
import com.rewhost.app.ui.components.GlassCard
import com.rewhost.app.ui.components.SimpleLineChart
import com.rewhost.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

data class ContainerDetailScreen(val initialContainer: Container) : Screen {
    @Composable
    override fun Content() {
        val api = koinInject<RewHostApi>()
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        var container by remember { mutableStateOf(initialContainer) }
        var isLoadingAction by remember { mutableStateOf(false) }

        // Live stats simulation (т.к. у нас нет реального сокета в этом примере)
        // В реальном проекте используй values из API stats
        val cpuHistory = remember { mutableStateListOf(10f, 20f, 15f, 40f, 30f, 10f) }

        LaunchedEffect(Unit) {
            while(true) {
                try {
                    container = api.getContainerDetails(container.id)
                    // Парсим CPU из строки "15.5%"
                    val cpuVal = (container.stats?.cpuUsage.toString().replace("%","").toDoubleOrNull()?.toFloat() ?: 0f)
                    if (cpuHistory.size > 20) cpuHistory.removeAt(0)
                    cpuHistory.add(cpuVal)
                } catch(_:Exception){}
                delay(3000)
            }
        }

        AppBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
            ) {
                // Navbar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextWhite)
                    }
                    Column(Modifier.padding(start = 8.dp)) {
                        Text(container.containerName ?: "Bot #${container.id}", style = MaterialTheme.typography.titleMedium, color = TextWhite, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).background(if(container.status == "running") RewGreen else RewRed, CircleShape))
                            Spacer(Modifier.width(6.dp))
                            Text(container.status?.uppercase() ?: "UNKNOWN", style = MaterialTheme.typography.bodySmall, color = TextGray)
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // --- CPU GRAPH ---
                    GlassCard(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                        Text("Нагрузка CPU", color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                        SimpleLineChart(points = cpuHistory, color = RewBlue, modifier = Modifier.weight(1f))
                        Text("${cpuHistory.lastOrNull()?.toInt() ?: 0}%", color = TextWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.End))
                    }

                    Spacer(Modifier.height(24.dp))

                    // --- POWER ACTIONS ---
                    Text("Управление питанием", color = TextGray, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ActionButton("Start", Icons.Default.PlayArrow, RewGreen, isLoadingAction) {
                            scope.launch {
                                isLoadingAction = true
                                try { api.containerAction(container.id, "start") } catch(_:Exception){}
                                isLoadingAction = false
                            }
                        }
                        ActionButton("Stop", Icons.Default.Stop, RewRed, isLoadingAction) {
                            scope.launch {
                                isLoadingAction = true
                                try { api.containerAction(container.id, "stop") } catch(_:Exception){}
                                isLoadingAction = false
                            }
                        }
                        ActionButton("Restart", Icons.Default.Refresh, RewYellow, isLoadingAction) {
                            scope.launch {
                                isLoadingAction = true
                                try { api.containerAction(container.id, "restart") } catch(_:Exception){}
                                isLoadingAction = false
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // --- INFO ---
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        InfoRow("Server", container.serverInfo?.name ?: "N/A")
                        InfoRow("Image", container.imageInfo?.name ?: "N/A")
                        InfoRow("Tariff", container.tariffInfo?.name ?: "N/A")
                        // Исправлено: ramUsage вместо memoryUsage
                        InfoRow("RAM Usage", container.stats?.ramUsage ?: "N/A")
                    }



                    Spacer(Modifier.height(16.dp))

                    // --- TOOLS ---
                    Button(
                        onClick = { /* TODO: Log Screen */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate700)
                    ) {
                        Icon(Icons.Default.Terminal, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Открыть консоль / Логи")
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = { navigator.push(ContainerModulesScreen(container.id)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate700)
                    ) {
                        Icon(Icons.Default.Extension, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Управление модулями")
                    }

                    Spacer(Modifier.height(24.dp))

                    // --- DANGER ZONE ---
                    Text("Опасная зона", color = RewRed, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    GlassCard(modifier = Modifier.fillMaxWidth(), borderColor = RewRed.copy(alpha=0.3f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                scope.launch { try { api.deleteContainer(container.id); navigator.pop() } catch(_:Exception){} }
                            }.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Delete, null, tint = RewRed)
                            Spacer(Modifier.width(12.dp))
                            Text("Удалить контейнер", color = RewRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun RowScope.ActionButton(text: String, icon: ImageVector, color: Color, isLoading: Boolean, onClick: () -> Unit) {
        Button(
            onClick = onClick,
            modifier = Modifier.weight(1f).height(70.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.15f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
            enabled = !isLoading
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if(isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = color)
                } else {
                    Icon(icon, null, tint = color)
                    Spacer(Modifier.height(4.dp))
                    Text(text, fontSize = 12.sp, color = color, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    @Composable
    fun InfoRow(label: String, value: String) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = TextGray, fontSize = 14.sp)
            Text(value, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        Divider(color = Color.White.copy(alpha = 0.05f))
    }
}