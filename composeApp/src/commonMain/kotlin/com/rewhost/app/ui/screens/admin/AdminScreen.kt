package com.rewhost.app.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import org.koin.compose.koinInject

class AdminScreen : Screen {
    @Composable
    override fun Content() {
        val api = koinInject<RewHostApi>()
        val navigator = LocalNavigator.currentOrThrow
        var users by remember { mutableStateOf<List<UserProfile>>(emptyList()) }

        LaunchedEffect(Unit) {
            try { users = api.getAdminUsers(0) } catch (_: Exception) {}
        }

        Scaffold { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navigator.pop() }) { Icon(Icons.Default.ArrowBack, null) }
                    Text("Admin Panel", style = MaterialTheme.typography.titleLarge)
                }
                Spacer(Modifier.height(16.dp))
                
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { Text("Users", style = MaterialTheme.typography.titleMedium) }
                    items(users) { user ->
                        Card {
                            Column(Modifier.padding(16.dp).fillMaxWidth()) {
                                Text(user.username ?: "ID: ${user.userId}", style = MaterialTheme.typography.bodyLarge)
                                Text("Balance: ${user.balance}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
