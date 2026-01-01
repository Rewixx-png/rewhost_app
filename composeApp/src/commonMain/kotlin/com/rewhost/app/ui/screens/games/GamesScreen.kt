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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                    Triple("Plinko", Icons.Default.Circle, MaterialTheme.colorScheme.secondary)
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

class RouletteGameScreen : Screen {
    @Composable
    override fun Content() {
        val api = koinInject<RewHostApi>()
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        var resultText by remember { mutableStateOf("Ready") }
        val rotation = remember { Animatable(0f) }

        Scaffold { padding ->
            Column(Modifier.padding(padding).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { navigator.pop() }, modifier = Modifier.align(Alignment.Start)) { 
                    Icon(Icons.Default.ArrowBack, null) 
                }
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(top = 50.dp)) {
                    Canvas(modifier = Modifier.size(250.dp).rotate(rotation.value)) {
                        drawCircle(Color.DarkGray)
                        val colors = listOf(Color.Red, Color.Black, Color.Red, Color.Black, Color.Green, Color.Black)
                        val sweep = 360f / colors.size
                        colors.forEachIndexed { i, c -> drawArc(c, i * sweep, sweep, true) }
                    }
                    Icon(Icons.Default.ArrowDownward, null, modifier = Modifier.align(Alignment.TopCenter))
                }
                Spacer(Modifier.height(30.dp))
                Text(resultText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(30.dp))
                Button(
                    onClick = {
                        scope.launch {
                            launch { rotation.animateTo(rotation.value + 1080f, tween(2000)) }
                            try {
                                val res = api.spinRoulette()
                                resultText = "Win: ${res.amount}"
                            } catch (e: Exception) { resultText = "Error" }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("SPIN") }
            }
        }
    }
}

class MinesGameScreen : Screen {
    @Composable
    override fun Content() {
        val api = koinInject<RewHostApi>()
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        var gameState by remember { mutableStateOf<GameState?>(null) }
        var bet by remember { mutableStateOf("10") }

        LaunchedEffect(Unit) { try { gameState = api.getMinesStatus() } catch(_:Exception){} }

        Scaffold { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                IconButton(onClick = { navigator.pop() }) { Icon(Icons.Default.ArrowBack, null) }
                
                val board = gameState?.board ?: List(5){List(5){0}}
                
                Column(Modifier.aspectRatio(1f).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))) {
                    for(r in 0 until 5) {
                        Row(Modifier.weight(1f)) {
                            for(c in 0 until 5) {
                                val v = try { board[r][c] } catch(e:Exception){0}
                                Box(
                                    Modifier.weight(1f).fillMaxHeight().padding(2.dp)
                                        .background(
                                            when(v){
                                                1 -> Color.Green
                                                2 -> Color.Red
                                                else -> MaterialTheme.colorScheme.background
                                            }, RoundedCornerShape(4.dp)
                                        )
                                        .clickable {
                                            scope.launch {
                                                try { gameState = api.clickMines(mapOf("cell" to r*5+c)) } catch(_:Exception){}
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                if(gameState?.status == "active") {
                    Button(onClick = { scope.launch { try{ api.cashoutMines(); gameState=null; navigator.pop() }catch(_:Exception){} } }, Modifier.fillMaxWidth()) {
                        Text("CASHOUT")
                    }
                } else {
                    OutlinedTextField(value = bet, onValueChange = { bet = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Bet") })
                    Button(onClick = { scope.launch { try{ gameState = api.startMines(mapOf("amount" to bet.toDouble(), "mines_count" to 3)) }catch(_:Exception){} } }, Modifier.fillMaxWidth()) {
                        Text("PLAY")
                    }
                }
            }
        }
    }
}

class TowersGameScreen : Screen {
    @Composable
    override fun Content() {
        val api = koinInject<RewHostApi>()
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        var bet by remember { mutableStateOf("10") }
        
        Scaffold { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                IconButton(onClick = { navigator.pop() }) { Icon(Icons.Default.ArrowBack, null) }
                Text("Towers", fontSize = 24.sp)
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(value = bet, onValueChange = { bet = it }, label = { Text("Bet") }, modifier = Modifier.fillMaxWidth())
                Button(onClick = { scope.launch { try { api.startTowers(mapOf("amount" to bet.toDouble(), "difficulty" to "easy")) } catch(_:Exception){} } }, Modifier.fillMaxWidth()) {
                    Text("START")
                }
            }
        }
    }
}

class PlinkoGameScreen : Screen {
    @Composable
    override fun Content() {
        val api = koinInject<RewHostApi>()
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        var bet by remember { mutableStateOf("10") }
        var result by remember { mutableStateOf("") }
        
        Scaffold { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                IconButton(onClick = { navigator.pop() }) { Icon(Icons.Default.ArrowBack, null) }
                Text("Plinko", fontSize = 24.sp)
                Spacer(Modifier.height(20.dp))
                Text(result)
                OutlinedTextField(value = bet, onValueChange = { bet = it }, label = { Text("Bet") }, modifier = Modifier.fillMaxWidth())
                Button(onClick = { scope.launch { 
                    try { 
                        val res = api.playPlinko(mapOf("amount" to bet.toDouble(), "rows" to 8)) 
                        result = "Win: ${res.amount}"
                    } catch(e:Exception){ result = "Error" } 
                } }, Modifier.fillMaxWidth()) {
                    Text("PLAY")
                }
            }
        }
    }
}
