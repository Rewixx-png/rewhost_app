package com.rewhost.app.ui.screens.support

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.rewhost.app.api.RewHostApi
import com.rewhost.app.data.model.SupportTicket
import com.rewhost.app.data.model.TicketMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

class SupportScreen : Screen {
    @Composable
    override fun Content() {
        val api = koinInject<RewHostApi>()
        val navigator = LocalNavigator.currentOrThrow
        var tickets by remember { mutableStateOf<List<SupportTicket>>(emptyList()) }

        LaunchedEffect(Unit) { try { tickets = api.getSupportTickets() } catch (_: Exception) {} }

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { navigator.push(CreateTicketScreen()) }) {
                    Icon(Icons.Default.Add, null)
                }
            }
        ) { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navigator.pop() }) { Icon(Icons.Default.ArrowBack, null) }
                    Text("Support", style = MaterialTheme.typography.titleLarge)
                }
                Spacer(Modifier.height(16.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tickets) { ticket ->
                        Card(onClick = { navigator.push(TicketChatScreen(ticket.id, ticket.title)) }) {
                            Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                // Исправлено: text =
                                Text(text = ticket.title)
                                if(ticket.unreadCount > 0) Badge { Text(text = "${ticket.unreadCount}") }
                            }
                        }
                    }
                }
            }
        }
    }
}

class CreateTicketScreen : Screen {
    @Composable
    override fun Content() {
        val api = koinInject<RewHostApi>()
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        var title by remember { mutableStateOf("") }
        var msg by remember { mutableStateOf("") }

        Scaffold { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                IconButton(onClick = { navigator.pop() }) { Icon(Icons.Default.ArrowBack, null) }
                Text("New Ticket", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(title, { title = it }, label = { Text("Topic") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(msg, { msg = it }, label = { Text("Message") }, modifier = Modifier.fillMaxWidth().height(150.dp))
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    scope.launch {
                        try { api.createSupportTicket(mapOf("title" to title, "message" to msg, "category" to "general")); navigator.pop() } catch(_:Exception){}
                    }
                }, Modifier.fillMaxWidth()) { Text("Create") }
            }
        }
    }
}

data class TicketChatScreen(val id: Long, val title: String) : Screen {
    @Composable
    override fun Content() {
        val api = koinInject<RewHostApi>()
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        var msgs by remember { mutableStateOf<List<TicketMessage>>(emptyList()) }
        var input by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            while(true) {
                try { msgs = api.getTicketMessages(id) } catch(_:Exception){}
                delay(3000)
            }
        }

        Scaffold(
            bottomBar = {
                Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(input, { input = it }, Modifier.weight(1f))
                    IconButton(onClick = {
                        scope.launch { try { api.replyToTicket(id, mapOf("message" to input)); input = ""; msgs = api.getTicketMessages(id) } catch(_:Exception){} }
                    }) { Icon(Icons.Default.Send, null) }
                }
            }
        ) { padding ->
            Column(Modifier.padding(padding)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navigator.pop() }) { Icon(Icons.Default.ArrowBack, null) }
                    Text(title, style = MaterialTheme.typography.titleMedium)
                }
                LazyColumn(Modifier.weight(1f).padding(horizontal = 16.dp), reverseLayout = true) {
                    items(msgs.reversed()) { msg ->
                        Box(Modifier.fillMaxWidth(), contentAlignment = if(!msg.isAdmin) Alignment.CenterEnd else Alignment.CenterStart) {
                            Surface(
                                color = if(!msg.isAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Text(msg.message, Modifier.padding(8.dp), color = if(!msg.isAdmin) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }
    }
}
