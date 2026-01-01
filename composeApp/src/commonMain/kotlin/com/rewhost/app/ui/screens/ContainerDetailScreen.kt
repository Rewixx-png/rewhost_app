package com.rewhost.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.rewhost.app.ui.components.GlassCard
import com.rewhost.app.ui.theme.ErrorRed
import com.rewhost.app.ui.theme.RewPrimary
import com.rewhost.app.ui.theme.SuccessGreen
import com.rewhost.app.ui.theme.TextGray
import com.rewhost.app.ui.theme.TextWhite
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

data class ContainerDetailScreen(val container: Container) : Screen {
    @Composable
    override fun Content() {
        val api = koinInject<RewHostApi>()
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        var currentContainer by remember { mutableStateOf(container) }
        var isLoading by remember { mutableStateOf(false) }

        Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextWhite)
                    }
                    Text(
                        text = currentContainer.containerName ?: "Bot #${currentContainer.id}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
                
                Spacer(Modifier.height(24.dp))
                
                // Status Card
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Status", color = TextGray)
                            // FIX: Добавлена проверка на null (?.)
                            Text(
                                currentContainer.status?.uppercase() ?: "UNKNOWN", 
                                color = if(currentContainer.status == "running") SuccessGreen else ErrorRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { if(currentContainer.status == "running") 1f else 0f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = SuccessGreen,
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                // Actions Grid
                Text("Управление", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                
                val actions = listOf(
                    Triple("Запустить", Icons.Default.PlayArrow, SuccessGreen),
                    Triple("Остановить", Icons.Default.Stop, ErrorRed),
                    Triple("Перезагрузить", Icons.Default.Refresh, RewPrimary)
                )
                
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    actions.forEach { (name, icon, color) ->
                        Button(
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    try {
                                        val actionCmd = when(name) {
                                            "Запустить" -> "start"
                                            "Остановить" -> "stop"
                                            else -> "restart"
                                        }
                                        api.containerAction(currentContainer.id, actionCmd)
                                        // Update info
                                        currentContainer = api.getContainerDetails(currentContainer.id)
                                    } catch(_:Exception){}
                                    isLoading = false
                                }
                            },
                            modifier = Modifier.weight(1f).height(80.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha=0.2f)),
                            enabled = !isLoading
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(icon, null, tint = color)
                                Spacer(Modifier.height(4.dp))
                                Text(name, fontSize = 10.sp, color = color)
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                // Danger Zone
                Text("Опасная зона", color = ErrorRed, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                
                GlassCard(modifier = Modifier.fillMaxWidth(), borderColor = ErrorRed.copy(alpha=0.3f)) {
                    Column(Modifier.padding(16.dp)) {
                        ActionRow("Переустановить", Icons.Default.Build, RewPrimary) {
                             scope.launch { try { api.reinstallContainer(currentContainer.id) } catch(_:Exception){} }
                        }
                        HorizontalDivider(Modifier.padding(vertical=12.dp), color = Color.White.copy(alpha=0.1f))
                        ActionRow("Удалить", Icons.Default.Delete, ErrorRed) {
                             scope.launch { 
                                 try { 
                                     api.deleteContainer(currentContainer.id)
                                     navigator.pop()
                                 } catch(_:Exception){} 
                             }
                        }
                    }
                }
            }
        }
    }
    
    @Composable
    fun ActionRow(text: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text(text, color = TextWhite)
            }
            Icon(Icons.Default.ChevronRight, null, tint = TextGray)
        }
    }
}
