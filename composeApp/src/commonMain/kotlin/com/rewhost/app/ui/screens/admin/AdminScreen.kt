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
                    Text("User: ${user.username ?: user.userId}", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text("Balance: ${user.balance}")
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
