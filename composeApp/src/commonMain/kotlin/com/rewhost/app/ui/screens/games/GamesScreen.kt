package com.rewhost.app.ui.screens.games

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
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
import kotlin.math.cos
import kotlin.math.sin

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
                    Triple("Mines", Icons.Default.Diamond, Color(0xFF10B981)),
                    Triple("Roulette", Icons.Default.Casino, Color(0xFFEF4444)),
                    Triple("Towers", Icons.Default.Apartment, Color(0xFFF59E0B))
                )

                LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(games.size) { idx ->
                        val (name, icon, color) = games[idx]
                        GameCard(name, icon, color) {
                            when(name) {
                                "Mines" -> navigator.push(MinesGameScreen())
                                "Roulette" -> navigator.push(RouletteGameScreen())
                                "Towers" -> navigator.push(TowersGameScreen())
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun GameCard(name: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
        BouncyBtn(onClick = onClick) {
            GlassCard(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                Box(Modifier.fillMaxSize()) {
                    Icon(icon, null, tint = color.copy(alpha=0.1f), modifier = Modifier.size(100.dp).align(Alignment.BottomEnd).offset(x=20.dp, y=20.dp))
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(icon, null, tint = color, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(name, color = TextWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- ROULETTE ---
class RouletteGameScreen : Screen {
    @Composable
    override fun Content() {
        val api = koinInject<RewHostApi>()
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        
        var resultText by remember { mutableStateOf("Ready") }
        var isSpinning by remember { mutableStateOf(false) }
        val rotation = remember { Animatable(0f) }

        Scaffold(containerColor = DarkBackground) { padding ->
            Column(Modifier.padding(padding).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(Modifier.fillMaxWidth()) {
                    IconButton(onClick = { navigator.pop() }) { Icon(Icons.Default.ArrowBack, null, tint = TextWhite) }
                    Text("Roulette", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top=12.dp))
                }
                Spacer(Modifier.height(40.dp))

                // Колесо
                Box(contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(250.dp).rotate(rotation.value)) {
                        drawCircle(Color(0xFF1E293B))
                        val colors = listOf(Color.Red, Color.Black, Color.Red, Color.Black, Color.Green, Color.Black, Color.Red, Color.Black)
                        val sweep = 360f / colors.size
                        colors.forEachIndexed { i, color ->
                            drawArc(color, startAngle = i * sweep, sweepAngle = sweep, useCenter = true)
                        }
                    }
                    Icon(Icons.Default.ArrowDownward, null, tint = RewPrimary, modifier = Modifier.align(Alignment.TopCenter).offset(y = (-10).dp))
                }

                Spacer(Modifier.height(40.dp))
                
                Text(resultText, color = RewPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                
                Spacer(Modifier.weight(1f))

                Button(
                    onClick = {
                        if (isSpinning) return@Button
                        isSpinning = true
                        resultText = "Spinning..."
                        scope.launch {
                            // Анимация
                            launch { rotation.animateTo(rotation.value + 1080f, animationSpec = tween(2000, easing = LinearEasing)) }
                            try {
                                val res = api.spinRoulette() // Или spinRouletteMultiple()
                                resultText = "Win: ${res.amount ?: 0.0} (x${res.multiplier})"
                            } catch (e: Exception) {
                                resultText = "Error"
                            }
                            isSpinning = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RewPrimary)
                ) {
                    Text("SPIN", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- TOWERS (Placeholder logic based on API) ---
class TowersGameScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        Scaffold(containerColor = DarkBackground) { padding ->
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Towers", color = TextWhite, fontSize = 24.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("В разработке", color = TextGray)
                    Button(onClick = { navigator.pop() }) { Text("Назад") }
                }
            }
        }
    }
}

// --- MINES (Предыдущий код Mines, сокращенно для примера, используй полный из прошлого ответа) ---
class MinesGameScreen : Screen {
    @Composable
    override fun Content() {
        val api = koinInject<RewHostApi>()
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        var gameState by remember { mutableStateOf<GameState?>(null) }
        
        // ... (Используй код Mines из моего прошлого ответа, он рабочий) ...
        // Для краткости я продублирую только стартовый UI
         Scaffold(containerColor = DarkBackground) { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                IconButton(onClick = { navigator.pop() }) { Icon(Icons.Default.ArrowBack, null, tint = TextWhite) }
                Text("Mines - Загрузи код из прошлого шага", color = TextGray)
            }
        }
    }
}
