package com.rewhost.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

class FinanceScreen : Screen {
    @Composable
    override fun Content() {
        val api = koinInject<RewHostApi>()
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        var amount by remember { mutableStateOf("") }
        var method by remember { mutableStateOf("sbp") }

        Scaffold { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                IconButton(onClick = { navigator.pop() }) { Icon(Icons.Default.ArrowBack, null) }
                Text("Finance", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                
                OutlinedTextField(amount, { amount = it }, label = { Text("Amount") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                
                Text("Method: $method")
                Row {
                    Button(onClick = { method = "sbp" }) { Text("SBP") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { method = "card" }) { Text("Card") }
                }
                
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    scope.launch {
                        try { api.createDeposit(amount.toDoubleOrNull() ?: 100.0, method); navigator.pop() } catch(_:Exception){}
                    }
                }, Modifier.fillMaxWidth()) { Text("Deposit") }
                
                Spacer(Modifier.height(24.dp))
                Text("History (Placeholder)")
                LazyColumn {
                    // History list
                }
            }
        }
    }
}
