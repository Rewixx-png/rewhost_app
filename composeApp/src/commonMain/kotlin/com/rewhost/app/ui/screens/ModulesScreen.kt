package com.rewhost.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
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

class ModulesScreen : Screen {
    @Composable
    override fun Content() {
        val api = koinInject<RewHostApi>()
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        var modules by remember { mutableStateOf<List<JsonObject>>(emptyList()) }

        LaunchedEffect(Unit) {
            try { modules = api.getSavedModules() } catch (_: Exception) {}
        }

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { scope.launch { try { api.sendSavedModules() } catch(_:Exception){} } }) {
                    Icon(Icons.Default.Send, null)
                }
            }
        ) { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navigator.pop() }) { Icon(Icons.Default.ArrowBack, null) }
                    Text("Saved Modules", style = MaterialTheme.typography.titleLarge)
                }
                Spacer(Modifier.height(16.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(modules) { module ->
                        Card {
                            Text(module.toString(), Modifier.padding(16.dp))
                        }
                    }
                }
            }
        }
    }
}
