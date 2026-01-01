package com.rewhost.app.ui.screens.shop

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.rewhost.app.api.RewHostApi
import com.rewhost.app.ui.components.GlassCard
import com.rewhost.app.ui.theme.*
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

class ShopScreen : Screen {
    @Composable
    override fun Content() {
        val api = koinInject<RewHostApi>()
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        var isBuying by remember { mutableStateOf(false) }

        Scaffold(containerColor = DarkBackground) { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navigator.pop() }) { Icon(Icons.Default.ArrowBack, null, tint = TextWhite) }
                    Text("Создание бота", color = TextWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(Modifier.height(24.dp))
                
                Text("Выберите тариф", color = TextGray, fontSize = 14.sp)
                Spacer(Modifier.height(12.dp))

                GlassCard(modifier = Modifier.fillMaxWidth().clickable { 
                    if (!isBuying) {
                        isBuying = true
                        scope.launch {
                            try {
                                api.purchaseTariff()
                                navigator.pop()
                            } catch (e: Exception) {
                                isBuying = false
                            }
                        }
                    }
                }) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Hikka Userbot", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("1 CPU / 1GB RAM", color = TextGray, fontSize = 12.sp)
                        }
                        if (isBuying) {
                            CircularProgressIndicator(color = RewPrimary, modifier = Modifier.size(24.dp))
                        } else {
                            Button(
                                onClick = { /* handled by card click */ }, 
                                colors = ButtonDefaults.buttonColors(containerColor = RewPrimary),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                Text("Купить", color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}
