package com.rewhost.app.ui.screens.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.rewhost.app.api.RewHostApi
import com.rewhost.app.data.model.*
import com.rewhost.app.ui.components.*
import com.rewhost.app.ui.theme.*
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

data class ShopScreen(
    val servers: Map<String, ServerConfig>,
    val tariffs: Map<String, TariffConfig>,
    val images: Map<String, ImageConfig>,
    val hasUsedFree: Boolean
) : Screen {
    @Composable
    override fun Content() {
        val api = koinInject<RewHostApi>()
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        var selectedServerId by remember { mutableStateOf<String?>(servers.keys.firstOrNull()) }
        var selectedTariffId by remember { mutableStateOf<String?>("basic") }
        var selectedImageId by remember { mutableStateOf<String?>("hikka") }
        var isBuying by remember { mutableStateOf(false) }

        AppBackground {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navigator.pop() }) { Icon(Icons.Default.ArrowBack, null, tint = TextWhite) }
                    Text("Конструктор", style = MaterialTheme.typography.titleLarge, color = TextWhite)
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item { Text("1. Локация", color = RewPurple, fontWeight = FontWeight.Bold) }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            servers.forEach { (id, conf) ->
                                if (conf.active) SelectionCard(conf.name, conf.ip ?: "Hidden", selectedServerId == id) { selectedServerId = id }
                            }
                        }
                    }

                    item { Text("2. Тариф", color = RewBlue, fontWeight = FontWeight.Bold) }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            tariffs.forEach { (id, conf) ->
                                val price = if (id == "free") "Free" else "${conf.price.toInt()}₽"
                                SelectionCard(conf.name, "${conf.ram}MB RAM", selectedTariffId == id, rightText = price) { selectedTariffId = id }
                            }
                        }
                    }

                    item { Text("3. Образ", color = RewGreen, fontWeight = FontWeight.Bold) }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            images.forEach { (id, conf) ->
                                SelectionCard(conf.name, conf.dockerImage, selectedImageId == id) { selectedImageId = id }
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (selectedServerId != null && selectedTariffId != null && selectedImageId != null) {
                                    scope.launch {
                                        isBuying = true
                                        try { api.purchaseTariff(selectedServerId!!, selectedTariffId!!, selectedImageId!!)
                                            navigator.pop()
                                        } catch(_:Exception){}
                                        isBuying = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RewBlue)
                        ) {
                            if (isBuying) CircularProgressIndicator(color = Color.White) else Text("Создать сервер")
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun SelectionCard(title: String, subtitle: String, isSelected: Boolean, rightText: String? = null, onClick: () -> Unit) {
        val bg = if (isSelected) RewBlue.copy(alpha = 0.2f) else Slate800
        val border = if (isSelected) RewBlue else Color.Transparent
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(bg).border(1.dp, border, RoundedCornerShape(12.dp))
                .clickable(onClick = onClick).padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, color = TextWhite, fontWeight = FontWeight.Bold)
                Text(subtitle, color = TextGray, fontSize = 12.sp)
            }
            if (rightText != null) Text(rightText, color = RewGreen, fontWeight = FontWeight.Bold)
            else if (isSelected) Icon(Icons.Default.CheckCircle, null, tint = RewBlue)
        }
    }
}