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
                    try { api.activatePromo(code); status="Activated!" } catch(e:Exception) { status="Error: ${e.message}" } 
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
                                result = "Success! ID: ${res["id"]}" 
                            } catch(e:Exception) { 
                                result = "Error: ${e.message}" 
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
