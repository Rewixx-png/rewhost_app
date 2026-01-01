package com.rewhost.app.ui.screens.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Gamepad
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
import com.rewhost.app.data.model.GameState
import com.rewhost.app.ui.components.BouncyBtn
import com.rewhost.app.ui.components.GlassCard
import com.rewhost.app.ui.theme.*
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

// --- ГЛАВНОЕ МЕНЮ ИГР ---
class GamesScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(containerColor = DarkBackground) { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navigator.pop() }) { Icon(Icons.Default.ArrowBack, null, tint = TextWhite) }
                    Text("Игры", color = TextWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(24.dp))

                val games = listOf(
                    "Mines" to Icons.Default.Diamond,
                    "Roulette" to Icons.Default.Casino,
                    "Blackjack" to Icons.Default.Gamepad
                )

                LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(games.size) { idx ->
                        val (name, icon) = games[idx]
                        GameCard(name, icon) {
                            if (name == "Mines") navigator.push(MinesGameScreen())
                            // Остальные игры можно добавить позже
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun GameCard(name: String, icon: ImageVector, onClick: () -> Unit) {
        BouncyBtn(onClick = onClick) {
            GlassCard(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(icon, null, tint = RewPrimary, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text(name, color = TextWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- ИГРА MINES ---
class MinesGameScreen : Screen {
    @Composable
    override fun Content() {
        val api = koinInject<RewHostApi>()
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        
        var gameState by remember { mutableStateOf<GameState?>(null) }
        var betAmount by remember { mutableStateOf("10") }
        var minesCount by remember { mutableStateOf(3) }
        var error by remember { mutableStateOf<String?>(null) }

        // Загрузка статуса при входе
        LaunchedEffect(Unit) {
            try { gameState = api.getMinesStatus() } catch (_: Exception) {}
        }

        Scaffold(containerColor = DarkBackground) { padding ->
            Column(Modifier.padding(padding).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // Header
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navigator.pop() }) { Icon(Icons.Default.ArrowBack, null, tint = TextWhite) }
                    Text("Mines", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    // Баланс (заглушка)
                    Text("Balance: ...", color = RewPrimary) 
                }
                
                Spacer(Modifier.height(24.dp))

                // Игровое поле 5x5
                GlassCard(modifier = Modifier.aspectRatio(1f)) {
                    val board = gameState?.board ?: List(5) { List(5) { 0 } } // 0 - closed, 1 - gem, 2 - mine
                    
                    Column(Modifier.fillMaxSize()) {
                        for (r in 0 until 5) {
                            Row(Modifier.weight(1f)) {
                                for (c in 0 until 5) {
                                    val cellValue = try { board[r][c] } catch(e: Exception) { 0 } // Safely get value
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                when(cellValue) {
                                                    1 -> SuccessGreen // Gem
                                                    2 -> ErrorRed // Mine
                                                    else -> Color(0xFF334155) // Closed
                                                }
                                            )
                                            .clickable(enabled = gameState?.status == "active" && cellValue == 0) {
                                                scope.launch {
                                                    try {
                                                        // Логика клика (координаты зависят от API, отправляем index)
                                                        val index = r * 5 + c
                                                        gameState = api.clickMines(mapOf("cell" to index)) 
                                                    } catch (e: Exception) { error = e.message }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (cellValue == 1) Icon(Icons.Default.Diamond, null, tint = Color.White)
                                        if (cellValue == 2) Icon(Icons.Default.Casino, null, tint = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Управление
                if (gameState?.status == "active") {
                    Button(
                        onClick = { 
                            scope.launch { 
                                try { 
                                    api.cashoutMines()
                                    gameState = null // Reset or fetch new status
                                    navigator.pop() // Выход или рестарт
                                } catch(e: Exception) { error = e.message }
                            } 
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Text("ЗАБРАТЬ ДЕНЬГИ", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = betAmount,
                            onValueChange = { betAmount = it },
                            label = { Text("Ставка") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            )
                        )
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        gameState = api.startMines(mapOf("amount" to betAmount.toDouble(), "mines_count" to minesCount))
                                    } catch (e: Exception) { error = e.message }
                                }
                            },
                            modifier = Modifier.weight(1f).height(64.dp), // Height to match TextField
                            shape = RoundedCornerShape(4.dp),
                             colors = ButtonDefaults.buttonColors(containerColor = RewPrimary)
                        ) {
                            Text("ИГРАТЬ", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                if (error != null) {
                    Text(error!!, color = ErrorRed, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}
