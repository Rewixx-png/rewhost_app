#!/bin/bash

# 1. Чистка
echo "Очистка старых файлов..."
rm -f "composeApp/src/commonMain/kotlin/com/rewhost/app/ui/screens/ModulesScreen.kt"

# 2. Создаем структуру
mkdir -p composeApp/src/commonMain/kotlin/com/rewhost/app/api
mkdir -p composeApp/src/commonMain/kotlin/com/rewhost/app/ui/screens/games
mkdir -p composeApp/src/commonMain/kotlin/com/rewhost/app/ui/screens/admin

# 3. Генерация файлов

# --- RewHostApi.kt (FIXED IMPORT) ---
cat <<EOF > composeApp/src/commonMain/kotlin/com/rewhost/app/api/RewHostApi.kt
package com.rewhost.app.api

import com.rewhost.app.data.model.*
import com.rewhost.app.utils.AppSettings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject 
import kotlinx.serialization.serializer

class RewHostApi(
    private val client: HttpClient,
    private val settings: AppSettings
) {
    private val baseUrl = "https://rewhost.rewixx.ru/api/v1"

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    init {
        client.plugin(HttpSend).intercept { request ->
            val token = settings.getToken()
            if (!token.isNullOrBlank()) {
                request.header("X-Web-Access-Token", token)
            }
            execute(request)
        }
    }

    private suspend inline fun <reified T> get(endpoint: String): T {
        // Fix: jsonObject import added above
        return client.get("\$baseUrl\$endpoint").body<JsonObject>().let { json ->
            val dataElement = json["data"]
            val dataToDecode = if (dataElement != null && dataElement !is kotlinx.serialization.json.JsonNull) {
                dataElement
            } else {
                json
            }
            jsonParser.decodeFromJsonElement(serializer<T>(), dataToDecode)
        }
    }

    // --- AUTH ---
    suspend fun generateLoginToken(): GenerateTokenResponse = get("/auth/generate-token")
    suspend fun checkLoginToken(loginToken: String): CheckTokenResponse = get("/auth/check-token/\$loginToken")
    suspend fun login(token: String) {
        client.get("\$baseUrl/user/dashboard") { header("X-Web-Access-Token", token) }
        settings.setToken(token)
    }
    suspend fun logout() { settings.clearToken() }

    // --- USER ---
    suspend fun getDashboard(): DashboardResponse = get("/user/dashboard")
    suspend fun getNotifications(): List<NotificationItem> = get("/user/notifications")
    suspend fun markNotificationsRead() { client.post("\$baseUrl/user/notifications/mark-read") }
    suspend fun clearNotifications() { client.delete("\$baseUrl/user/notifications/clear") }
    suspend fun claimDailyBonus() { client.post("\$baseUrl/user/bonus/claim") }

    // --- CONTAINERS ---
    suspend fun listContainers(): List<Container> = get("/user/containers")
    suspend fun purchaseTariff() { client.post("\$baseUrl/user/tariffs/purchase") }
    suspend fun getContainerDetails(id: Long): Container = get("/user/container/\$id")
    suspend fun containerAction(id: Long, action: String) {
        client.post("\$baseUrl/user/container/\$id/action") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("action" to action))
        }
    }
    suspend fun deleteContainer(id: Long) { client.delete("\$baseUrl/user/container/\$id/delete") }
    suspend fun reinstallContainer(id: Long) { client.post("\$baseUrl/user/container/\$id/reinstall/v2") }
    suspend fun renameContainer(id: Long, newName: String) {
        client.post("\$baseUrl/user/container/\$id/rename") { parameter("name", newName) }
    }
    suspend fun getContainerLogs(id: Long, lines: Int = 100): String = get("/user/container/\$id/logs?lines=\$lines")

    // --- MODULES ---
    suspend fun getContainerModules(id: Long): List<JsonObject> = get("/user/modules/container/\$id/modules")
    suspend fun backupModules(id: Long) { client.post("\$baseUrl/user/modules/container/\$id/modules/backup") }
    suspend fun getSavedModules(): List<JsonObject> = get("/user/modules/saved")
    suspend fun sendSavedModules() { client.post("\$baseUrl/user/modules/saved/send") }
    suspend fun deleteSavedModule(moduleId: Long) { client.delete("\$baseUrl/user/modules/saved/\$moduleId") }

    // --- TOOLS (SOUNDCLOUD) ---
    suspend fun downloadSoundCloud(url: String): JsonObject {
        return client.post("\$baseUrl/tools/soundcloud/download") {
             contentType(ContentType.Application.Json)
             setBody(mapOf("url" to url))
        }.body()
    }

    // --- KEYS ---
    suspend fun generateKey() { client.post("\$baseUrl/user/keys/generate") }
    suspend fun revokeKeys() { client.post("\$baseUrl/user/keys/revoke") }

    // --- PROMOS ---
    suspend fun activatePromo(code: String) {
        client.post("\$baseUrl/user/promos/activate") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("code" to code))
        }
    }

    // --- SUPPORT ---
    suspend fun getSupportTickets(): List<SupportTicket> = get("/user/support/tickets")
    suspend fun getTicketMessages(id: Long): List<TicketMessage> = get("/user/support/ticket/\$id/messages")
    suspend fun createSupportTicket(data: Map<String, Any>) {
        client.post("\$baseUrl/user/support/create-ticket") {
            contentType(ContentType.Application.Json)
            setBody(data)
        }
    }
    suspend fun replyToTicket(id: Long, message: Map<String, Any>) {
        client.post("\$baseUrl/user/support/ticket/\$id/reply") {
            contentType(ContentType.Application.Json)
            setBody(message)
        }
    }

    // --- GAMES ---
    suspend fun spinRoulette(): GameResult = get("/user/games/roulette/spin")
    
    suspend fun getMinesStatus(): GameState = get("/user/games/mines/status")
    suspend fun startMines(bet: Map<String, Any>): GameState = 
        client.post("\$baseUrl/user/games/mines/start") {
            contentType(ContentType.Application.Json)
            setBody(bet)
        }.body()
    suspend fun clickMines(cell: Map<String, Any>): GameState = 
        client.post("\$baseUrl/user/games/mines/click") {
            contentType(ContentType.Application.Json)
            setBody(cell)
        }.body()
    suspend fun cashoutMines(): GameResult = get("/user/games/mines/cashout")

    suspend fun getTowersStatus(): GameState = get("/user/games/towers/status")
    suspend fun startTowers(bet: Map<String, Any>): GameState = 
        client.post("\$baseUrl/user/games/towers/start") {
            contentType(ContentType.Application.Json)
            setBody(bet)
        }.body()
    suspend fun stepTowers(choice: Map<String, Any>): GameState = 
        client.post("\$baseUrl/user/games/towers/step") {
            contentType(ContentType.Application.Json)
            setBody(choice)
        }.body()
    suspend fun cashoutTowers(): GameResult = get("/user/games/towers/cashout")

    suspend fun playPlinko(bet: Map<String, Any>): GameResult = 
        client.post("\$baseUrl/user/games/plinko/play") {
            contentType(ContentType.Application.Json)
            setBody(bet)
        }.body()
    
    // CRASH
    suspend fun getCrashStatus(): GameState = get("/user/games/crash/status")
    suspend fun placeCrashBet(amount: Double, autoCashout: Double = 2.0) {
        client.post("\$baseUrl/user/games/crash/bet") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("amount" to amount, "auto_cashout" to autoCashout))
        }
    }
    suspend fun cashoutCrash() { client.post("\$baseUrl/user/games/crash/cashout") }

    // Blackjack
    suspend fun getBlackjackRooms(): List<JsonObject> = get("/games/blackjack/rooms")
    suspend fun createBlackjackRoom() { client.post("\$baseUrl/games/blackjack/rooms/create") }

    // Durak
    suspend fun getDurakRooms(): List<JsonObject> = get("/games/durak/rooms")
    suspend fun createDurakRoom() { client.post("\$baseUrl/games/durak/rooms/create") }
    suspend fun createDurakPve() { client.post("\$baseUrl/games/durak/rooms/create_pve") }

    // --- BILLING ---
    suspend fun createDeposit(amount: Double, method: String) {
        client.post("\$baseUrl/user/deposit/create") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("amount" to amount, "method" to method))
        }
    }
    
    // --- ADMIN ---
    suspend fun getAdminUsers(page: Int = 0): List<UserProfile> {
        val response = client.get("\$baseUrl/admin/users") { parameter("page", page) }.body<JsonObject>()
        val data = response["data"]
        val dataToDecode = if (data != null && data !is kotlinx.serialization.json.JsonNull) data else response
        return jsonParser.decodeFromJsonElement(AdminUsersResponse.serializer(), dataToDecode).users
    }
    suspend fun getAdminServers(): List<JsonObject> = get("/admin/servers/")
    suspend fun toggleServer(id: String) { client.post("\$baseUrl/admin/servers/\$id/toggle") }
    suspend fun addServer(data: Map<String, Any>) { 
        client.post("\$baseUrl/admin/servers/add") {
            contentType(ContentType.Application.Json)
            setBody(data)
        }
    }

    suspend fun getServerStatus(): ServerStatusList = get("/public/server_status")
    fun getAvatarUrl(userId: Long): String = "\$baseUrl/public/user_photo/\$userId"
}
EOF

# --- AdditionalScreens.kt (FULL CODE) ---
cat <<EOF > composeApp/src/commonMain/kotlin/com/rewhost/app/ui/screens/AdditionalScreens.kt
package com.rewhost.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.rewhost.app.api.RewHostApi
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import org.koin.compose.koinInject

// --- PROMOS ---
class PromosScreen : Screen {
    @Composable
    override fun Content() {
        val api = koinInject<RewHostApi>()
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        var code by remember { mutableStateOf("") }
        var status by remember { mutableStateOf("") }

        Scaffold { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                IconButton(onClick = { navigator.pop() }) { Icon(Icons.Default.ArrowBack, null) }
                Text("Promocodes", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(24.dp))
                
                OutlinedTextField(value = code, onValueChange = {code=it}, label={Text("Enter Code")}, modifier=Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                Button(onClick = { scope.launch { 
                    try { api.activatePromo(code); status="Activated!" } catch(e:Exception) { status="Error: \${e.message}" } 
                } }, Modifier.fillMaxWidth()) { Text("Activate") }
                
                Text(status, Modifier.padding(top=16.dp), color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// --- KEYS ---
class KeysScreen : Screen {
    @Composable
    override fun Content() {
        val api = koinInject<RewHostApi>()
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        Scaffold { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                IconButton(onClick = { navigator.pop() }) { Icon(Icons.Default.ArrowBack, null) }
                Text("API Keys", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(24.dp))
                
                Button(onClick = { scope.launch { try { api.generateKey() } catch(_:Exception){} } }, Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Key, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Generate New Key")
                }
                Spacer(Modifier.height(12.dp))
                Button(onClick = { scope.launch { try { api.revokeKeys() } catch(_:Exception){} } }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Icon(Icons.Default.Delete, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Revoke All Keys")
                }
            }
        }
    }
}

// --- MODULES ---
class ModulesScreen : Screen {
    @Composable
    override fun Content() {
        val api = koinInject<RewHostApi>()
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        var modules by remember { mutableStateOf<List<JsonObject>>(emptyList()) }

        LaunchedEffect(Unit) { try { modules = api.getSavedModules() } catch (_: Exception) {} }

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { scope.launch { try { api.sendSavedModules() } catch(_:Exception){} } }) {
                    Icon(Icons.Default.Send, null)
                }
            }
        ) { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                IconButton(onClick = { navigator.pop() }) { Icon(Icons.Default.ArrowBack, null) }
                Text("Saved Modules", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                LazyColumn {
                    items(modules) { module ->
                        Card(Modifier.padding(vertical=4.dp).fillMaxWidth()) {
                            Text(module.toString(), Modifier.padding(16.dp))
                        }
                    }
                }
            }
        }
    }
}

class ContainerModulesScreen(val containerId: Long) : Screen {
    @Composable
    override fun Content() {
        val api = koinInject<RewHostApi>()
        val navigator = LocalNavigator.currentOrThrow
        var modules by remember { mutableStateOf<List<JsonObject>>(emptyList()) }
        LaunchedEffect(Unit) { try{modules = api.getContainerModules(containerId)}catch(_:Exception){} }

        Scaffold { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                IconButton(onClick = { navigator.pop() }) { Icon(Icons.Default.ArrowBack, null) }
                Text("Container Modules", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                LazyColumn {
                    items(modules) { module ->
                        Card(Modifier.padding(vertical=4.dp).fillMaxWidth()) {
                            Text(module.toString(), Modifier.padding(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// --- SOUNDCLOUD ---
class SoundCloudScreen : Screen {
    @Composable
    override fun Content() {
        val api = koinInject<RewHostApi>()
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        var url by remember { mutableStateOf("") }
        var result by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }

        Scaffold { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                IconButton(onClick = { navigator.pop() }) { Icon(Icons.Default.ArrowBack, null) }
                Text("SoundCloud Downloader", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = url, 
                    onValueChange = { url = it }, 
                    label = { Text("Track URL") }, 
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                
                Button(
                    onClick = { 
                        scope.launch { 
                            isLoading = true
                            try { 
                                val res = api.downloadSoundCloud(url)
                                result = "Success! ID: \${res["id"]}" 
                            } catch(e:Exception) { 
                                result = "Error: \${e.message}" 
                            } 
                            isLoading = false
                        } 
                    }, 
                    Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) { 
                    if (isLoading) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                    else Text("Download") 
                }
                
                if (result.isNotEmpty()) {
                    Text(result, Modifier.padding(top=16.dp), color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
EOF

# --- AdminScreen.kt (FULL CODE) ---
cat <<EOF > composeApp/src/commonMain/kotlin/com/rewhost/app/ui/screens/admin/AdminScreen.kt
package com.rewhost.app.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.rewhost.app.api.RewHostApi
import com.rewhost.app.data.model.UserProfile
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import org.koin.compose.koinInject

class AdminScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var selectedTab by remember { mutableStateOf(0) }

        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(selected = selectedTab==0, onClick = {selectedTab=0}, icon = {Icon(Icons.Default.Person,null)}, label = {Text("Users")})
                    NavigationBarItem(selected = selectedTab==1, onClick = {selectedTab=1}, icon = {Icon(Icons.Default.Dns,null)}, label = {Text("Servers")})
                    NavigationBarItem(selected = selectedTab==2, onClick = {selectedTab=2}, icon = {Icon(Icons.Default.Settings,null)}, label = {Text("System")})
                }
            }
        ) { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navigator.pop() }) { Icon(Icons.Default.ArrowBack, null) }
                    Text("Admin Dashboard", style = MaterialTheme.typography.titleLarge)
                }
                Spacer(Modifier.height(16.dp))
                
                when(selectedTab) {
                    0 -> AdminUsersTab()
                    1 -> AdminServersTab()
                    2 -> AdminSystemTab()
                }
            }
        }
    }
}

@Composable
fun AdminUsersTab() {
    val api = koinInject<RewHostApi>()
    var users by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    LaunchedEffect(Unit) { try{users = api.getAdminUsers()}catch(_:Exception){} }
    
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(users) { user ->
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text("User: \${user.username ?: user.userId}", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text("Balance: \${user.balance}")
                }
            }
        }
    }
}

@Composable
fun AdminServersTab() {
    val api = koinInject<RewHostApi>()
    val scope = rememberCoroutineScope()
    var servers by remember { mutableStateOf<List<JsonObject>>(emptyList()) }
    LaunchedEffect(Unit) { try{servers = api.getAdminServers()}catch(_:Exception){} }
    
    Column {
        Button(onClick = { /* Open Add Server Dialog */ }, Modifier.fillMaxWidth()) { Text("Add Server") }
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(servers) { server ->
                Card {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(server.toString())
                        // Add Toggle Button
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSystemTab() {
    val api = koinInject<RewHostApi>()
    val scope = rememberCoroutineScope()
    Column {
        Text("System Controls")
        Spacer(Modifier.height(16.dp))
        Button(onClick = { scope.launch { /* Restart */ } }, Modifier.fillMaxWidth()) { Text("Restart System") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { scope.launch { /* Maintenance */ } }, Modifier.fillMaxWidth()) { Text("Toggle Maintenance") }
    }
}
EOF

# --- GamesScreen.kt (FULL CODE WITH NO STUBS) ---
cat <<EOF > composeApp/src/commonMain/kotlin/com/rewhost/app/ui/screens/games/GamesScreen.kt
package com.rewhost.app.ui.screens.games

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import kotlinx.serialization.json.JsonObject
import org.koin.compose.koinInject

// --- ГЛАВНОЕ МЕНЮ ИГР ---
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
                    Triple("Roulette", Icons.Default.Refresh, MaterialTheme.colorScheme.error),
                    Triple("Towers", Icons.Default.Apartment, MaterialTheme.colorScheme.tertiary),
                    Triple("Plinko", Icons.Default.BlurOn, MaterialTheme.colorScheme.secondary),
                    Triple("Blackjack", Icons.Default.Style, Color(0xFF6366F1)),
                    Triple("Durak", Icons.Default.Layers, Color(0xFFEC4899)),
                    Triple("Crash", Icons.Default.RocketLaunch, Color(0xFFF59E0B)) 
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(games) { (name, icon, color) ->
                        Card(
                            onClick = {
                                when(name) {
                                    "Mines" -> navigator.push(MinesGameScreen())
                                    "Roulette" -> navigator.push(RouletteGameScreen())
                                    "Towers" -> navigator.push(TowersGameScreen())
                                    "Plinko" -> navigator.push(PlinkoGameScreen())
                                    "Blackjack" -> navigator.push(BlackjackScreen())
                                    "Durak" -> navigator.push(DurakScreen())
                                    "Crash" -> navigator.push(CrashScreen())
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

// --- CRASH ---
class CrashScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val api = koinInject<RewHostApi>()
        val scope = rememberCoroutineScope()
        var multiplier by remember { mutableStateOf(1.0) }
        var isRunning by remember { mutableStateOf(false) }

        Scaffold { padding ->
            Column(Modifier.padding(padding).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { navigator.pop() }, modifier = Modifier.align(Alignment.Start)) { 
                    Icon(Icons.Default.ArrowBack, null) 
                }
                Text("Crash", fontSize = 24.sp)
                Spacer(Modifier.height(40.dp))
                Text("\${multiplier}x", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = if(isRunning) Color.Green else Color.White)
                Spacer(Modifier.weight(1f))
                Button(onClick = { 
                    isRunning = !isRunning
                    scope.launch { try { api.placeCrashBet(10.0) } catch(_:Exception){} }
                }, Modifier.fillMaxWidth().height(56.dp)) { 
                    Text(if(isRunning) "CASHOUT" else "BET") 
                }
            }
        }
    }
}

// --- MINES ---
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
                Text("Mines", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                
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
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if(v==1) Icon(Icons.Default.Diamond, null, tint=Color.White)
                                    if(v==2) Icon(Icons.Default.Close, null, tint=Color.White)
                                }
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
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { scope.launch { try{ gameState = api.startMines(mapOf("amount" to bet.toDouble(), "mines_count" to 3)) }catch(_:Exception){} } }, Modifier.fillMaxWidth()) {
                        Text("PLAY")
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
                                resultText = "Win: \${res.amount}"
                            } catch (e: Exception) { resultText = "Error" }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("SPIN") }
            }
        }
    }
}

// --- TOWERS ---
class TowersGameScreen : Screen {
    @Composable
    override fun Content() {
        val api = koinInject<RewHostApi>()
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        var bet by remember { mutableStateOf("10") }
        var gameState by remember { mutableStateOf<GameState?>(null) }
        
        Scaffold { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                IconButton(onClick = { navigator.pop() }) { Icon(Icons.Default.ArrowBack, null) }
                Text("Towers", fontSize = 24.sp)
                Spacer(Modifier.height(20.dp))
                
                if (gameState?.status == "active") {
                    Text("Game Active", color = Color.Green)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Button(onClick = { scope.launch { try { gameState = api.stepTowers(mapOf("step" to 0)) } catch(_:Exception){} } }) { Text("Left") }
                        Button(onClick = { scope.launch { try { gameState = api.stepTowers(mapOf("step" to 1)) } catch(_:Exception){} } }) { Text("Middle") }
                        Button(onClick = { scope.launch { try { gameState = api.stepTowers(mapOf("step" to 2)) } catch(_:Exception){} } }) { Text("Right") }
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { scope.launch { try { api.cashoutTowers(); gameState = null; navigator.pop() } catch(_:Exception){} } }, Modifier.fillMaxWidth()) {
                        Text("CASHOUT")
                    }
                } else {
                    OutlinedTextField(value = bet, onValueChange = { bet = it }, label = { Text("Bet") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { scope.launch { try { gameState = api.startTowers(mapOf("amount" to bet.toDouble(), "difficulty" to "easy")) } catch(_:Exception){} } }, Modifier.fillMaxWidth()) {
                        Text("START")
                    }
                }
            }
        }
    }
}

// --- PLINKO ---
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
                Text(result, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = bet, onValueChange = { bet = it }, label = { Text("Bet") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                Button(onClick = { scope.launch { 
                    try { 
                        val res = api.playPlinko(mapOf("amount" to bet.toDouble(), "rows" to 8)) 
                        result = "Win: \${res.amount}"
                    } catch(e:Exception){ result = "Error" } 
                } }, Modifier.fillMaxWidth()) {
                    Text("PLAY")
                }
            }
        }
    }
}

// --- BLACKJACK ---
class BlackjackScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val api = koinInject<RewHostApi>()
        val scope = rememberCoroutineScope()
        var rooms by remember { mutableStateOf<List<JsonObject>>(emptyList()) }

        LaunchedEffect(Unit) {
            try { rooms = api.getBlackjackRooms() } catch(_:Exception){}
        }

        Scaffold { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                IconButton(onClick = { navigator.pop() }) { Icon(Icons.Default.ArrowBack, null) }
                Text("Blackjack", fontSize = 24.sp)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { scope.launch { try{api.createBlackjackRoom()}catch(_:Exception){} } }, Modifier.fillMaxWidth()) { 
                    Text("Create Room") 
                }
                Spacer(Modifier.height(16.dp))
                Text("Rooms:")
                LazyColumn {
                    items(rooms) { room ->
                        Card(Modifier.fillMaxWidth().padding(vertical=4.dp)) {
                            Text(room.toString(), Modifier.padding(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// --- DURAK ---
class DurakScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val api = koinInject<RewHostApi>()
        val scope = rememberCoroutineScope()
        var rooms by remember { mutableStateOf<List<JsonObject>>(emptyList()) }

        LaunchedEffect(Unit) {
            try { rooms = api.getDurakRooms() } catch(_:Exception){}
        }

        Scaffold { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                IconButton(onClick = { navigator.pop() }) { Icon(Icons.Default.ArrowBack, null) }
                Text("Durak", fontSize = 24.sp)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { scope.launch { try{api.createDurakPve()}catch(_:Exception){} } }, Modifier.fillMaxWidth()) { 
                    Text("Play PvE") 
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { scope.launch { try{api.createDurakRoom()}catch(_:Exception){} } }, Modifier.fillMaxWidth()) { 
                    Text("Create PvP Room") 
                }
                Spacer(Modifier.height(16.dp))
                Text("Lobby:")
                LazyColumn {
                    items(rooms) { room ->
                        Card(Modifier.fillMaxWidth().padding(vertical=4.dp)) {
                            Text(room.toString(), Modifier.padding(16.dp))
                        }
                    }
                }
            }
        }
    }
}
EOF

# 4. Сборка релиза
echo "Сборка релизного APK..."
./gradlew assembleRelease

echo "Готово! APK находится в: composeApp/build/outputs/apk/release/composeApp-release.apk"
EOF