package com.rewhost.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop 
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
// !!! FIX: Добавлены недостающие импорты
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.rewhost.app.ui.components.BouncyBtn
import com.rewhost.app.ui.components.GlassCard
import com.rewhost.app.ui.components.SimpleLineChart
import com.rewhost.app.ui.theme.SuccessGreen
import com.rewhost.app.ui.theme.ErrorRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive
import org.koin.compose.koinInject

data class ContainerDetailScreen(val initialContainer: Container) : Screen {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val api = koinInject<RewHostApi>()
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }

        var container by remember { mutableStateOf(initialContainer) }
        val cpuPoints = remember { mutableStateListOf<Float>().apply { addAll(List(20) { 0f }) } }
        var isProcessing by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            while (isActive) {
                try {
                    val fresh = api.getContainerDetails(container.id)
                    container = fresh
                    
                    val cpuVal = fresh.stats?.cpuUsage?.jsonPrimitive?.content?.let { str ->
                         str.replace("%", "").trim().toDoubleOrNull() ?: 0.0
                    } ?: 0.0
                    
                    cpuPoints.add(cpuVal.toFloat())
                    if (cpuPoints.size > 20) cpuPoints.removeAt(0)
                } catch (_: Exception) {}
                delay(3000)
            }
        }

        fun doAction(action: String) {
            if (isProcessing) return
            scope.launch {
                isProcessing = true
                try {
                    when(action) {
                        "delete" -> { 
                            api.deleteContainer(container.id)
                            snackbarHostState.showSnackbar("Контейнер удален")
                            navigator.pop() 
                        }
                        "reinstall" -> {
                            api.reinstallContainer(container.id)
                            snackbarHostState.showSnackbar("Запущена переустановка. Подождите...")
                        }
                        else -> {
                            api.containerAction(container.id, action)
                            snackbarHostState.showSnackbar("Команда '$action' отправлена!")
                            delay(500)
                            try {
                                val updated = api.getContainerDetails(container.id)
                                container = updated
                            } catch(_: Exception) {}
                        }
                    }
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Ошибка: ${e.message}")
                } finally {
                    isProcessing = false
                }
            }
        }

        val isRunning = container.status == "running"
        val statusColor = if (isRunning) SuccessGreen else ErrorRed

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(container.containerName ?: "Unknown Container") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, null)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                GlassCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(60.dp)
                                .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(18.dp))
                                .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = statusColor)
                            } else {
                                Icon(Icons.Default.PlayArrow, null, tint = statusColor)
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(if (isRunning) "Active" else "Offline", color = statusColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Node: ${container.serverInfo?.name ?: "?"}", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                GlassCard(padding = 0.dp) {
                    Column(Modifier.padding(20.dp)) {
                        Text("CPU LOAD (%)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(Modifier.height(10.dp))
                        Box(Modifier.height(200.dp)) {
                            SimpleLineChart(
                                points = cpuPoints.toList(),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text("Управление", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    maxItemsInEachRow = 2
                ) {
                    val btnModifier = Modifier.weight(1f).height(100.dp)
                    ActionBtn(Icons.Default.PlayArrow, "Start", Color.Green, btnModifier) { doAction("start") }
                    ActionBtn(Icons.Default.Stop, "Stop", Color.Red, btnModifier) { doAction("stop") }
                    ActionBtn(Icons.Default.Refresh, "Restart", Color.Magenta, btnModifier) { doAction("restart") }
                    ActionBtn(Icons.Default.Delete, "Delete", Color.Red, btnModifier) { doAction("delete") }
                }
            }
        }
    }
}

@Composable
fun ActionBtn(icon: ImageVector, label: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    BouncyBtn(onClick = onClick, modifier = modifier) {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.padding(10.dp).background(color.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.height(10.dp))
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}