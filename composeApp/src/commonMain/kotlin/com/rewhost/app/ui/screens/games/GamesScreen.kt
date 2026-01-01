package com.rewhost.app.ui.screens.games

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
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
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

class GamesScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Scaffold { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navigator.pop() }) { Icon(Icons.Default.ArrowBack, null) }
                    Text("Игры", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(24.dp))

                val games = listOf(
                    Triple("Mines", Icons.Default.Diamond, MaterialTheme.colorScheme.primary),
                    Triple("Roulette", Icons.Default.Casino, MaterialTheme.colorScheme.error),
                    Triple("Towers", Icons.Default.Apartment, MaterialTheme.colorScheme.tertiary),
                    Triple("Plinko", Icons.Default.Circle, MaterialTheme.colorScheme.secondary),
                    Triple("Blackjack", Icons.Default.Style, Color(0xFF6366F1)),
                    Triple("Durak", Icons.Default.PlayingCards, Color(0xFFEC4899))
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(games.size) { idx ->
                        val (name, icon, color) = games[idx]
                        Card(
                            onClick = {
                                when(name) {
                                    "Mines" -> navigator.push(MinesGameScreen())
                                    "Roulette" -> navigator.push(RouletteGameScreen())
                                    "Towers" -> navigator.push(TowersGameScreen())
                                    "Plinko" -> navigator.push(PlinkoGameScreen())
                                    "Blackjack" -> navigator.push(BlackjackScreen())
                                    "Durak" -> navigator.push(DurakScreen())
                                }
                            },
                            modifier = Modifier.height(120.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(icon, null, tint = color, modifier = Modifier.size(40.dp))
                                Spacer(Modifier.height(8.dp))
                                Text(name, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Заглушки для новых игр, чтобы компилировалось
class BlackjackScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val api = koinInject<RewHostApi>()
        val scope = rememberCoroutineScope()
        Scaffold { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                IconButton(onClick = { navigator.pop() }) { Icon(Icons.Default.ArrowBack, null) }
                Text("Blackjack", fontSize = 24.sp)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { scope.launch { try{api.createBlackjackRoom()}catch(_:Exception){} } }) { Text("Create Room") }
            }
        }
    }
}

class DurakScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val api = koinInject<RewHostApi>()
        val scope = rememberCoroutineScope()
        Scaffold { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                IconButton(onClick = { navigator.pop() }) { Icon(Icons.Default.ArrowBack, null) }
                Text("Durak", fontSize = 24.sp)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { scope.launch { try{api.createDurakPve()}catch(_:Exception){} } }) { Text("Play PvE") }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { scope.launch { try{api.createDurakRoom()}catch(_:Exception){} } }) { Text("Create Room") }
            }
        }
    }
}

// ... Остальные классы игр (Roulette, Mines, Towers, Plinko) остаются из предыдущего ответа ...
// Убедись, что ты вставил классы MinesGameScreen, RouletteGameScreen и т.д. сюда, или используй версию файла из прошлого ответа, добавив в начало новые классы.
// Для краткости я не дублирую их тут, но они ДОЛЖНЫ быть в файле.
class RouletteGameScreen : Screen { @Composable override fun Content() { /* код из прошлого ответа */ } }
class MinesGameScreen : Screen { @Composable override fun Content() { /* код из прошлого ответа */ } }
class TowersGameScreen : Screen { @Composable override fun Content() { /* код из прошлого ответа */ } }
class PlinkoGameScreen : Screen { @Composable override fun Content() { /* код из прошлого ответа */ } }
