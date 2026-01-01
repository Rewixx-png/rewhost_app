package com.rewhost.app.ui.screens.support

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.rewhost.app.api.RewHostApi
import com.rewhost.app.data.model.SupportTicket
import com.rewhost.app.data.model.TicketMessage
import com.rewhost.app.ui.components.GlassCard
import com.rewhost.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

// --- СПИСОК ТИКЕТОВ ---
class SupportScreen : Screen {
    @Composable
    override fun Content() {
        val api = koinInject<RewHostApi>()
        val navigator = LocalNavigator.currentOrThrow
        var tickets by remember { mutableStateOf<List<SupportTicket>>(emptyList()) }

        LaunchedEffect(Unit) {
            try { tickets = api.getSupportTickets() } catch (_: Exception) {}
        }

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navigator.push(CreateTicketScreen()) },
                    containerColor = RewPrimary
                ) { Icon(Icons.Default.Add, null) }
            },
            containerColor = DarkBackground
        ) { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextWhite)
                    }
                    Text("Поддержка", color = TextWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(16.dp))
                
                if (tickets.isEmpty()) {
                    Text("Нет обращений", color = TextGray, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top=50.dp))
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(tickets) { ticket ->
                        GlassCard(modifier = Modifier.fillMaxWidth().clickable { 
                            navigator.push(TicketChatScreen(ticket.id, ticket.title)) 
                        }) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(ticket.title, color = TextWhite, fontWeight = FontWeight.Bold)
                                    Text(ticket.status, color = if(ticket.status == "open") SuccessGreen else TextGray, fontSize = 12.sp)
                                }
                                if (ticket.unreadCount > 0) {
                                    Box(Modifier.background(RewPrimary, CircleShape).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                        Text(ticket.unreadCount.toString(), color = Color.Black, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- СОЗДАНИЕ ТИКЕТА ---
class CreateTicketScreen : Screen {
    @Composable
    override fun Content() {
        val api = koinInject<RewHostApi>()
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        
        var title by remember { mutableStateOf("") }
        var message by remember { mutableStateOf("") }
        var isSending by remember { mutableStateOf(false) }

        Scaffold(containerColor = DarkBackground) { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                IconButton(onClick = { navigator.pop() }) { Icon(Icons.Default.ArrowBack, null, tint = TextWhite) }
                Text("Новое обращение", color = TextWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = title, 
                    onValueChange = { title = it }, 
                    label = { Text("Тема") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = message, 
                    onValueChange = { message = it }, 
                    label = { Text("Сообщение") },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                )
                Spacer(Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        if (title.isBlank() || message.isBlank()) return@Button
                        isSending = true
                        scope.launch {
                            try {
                                api.createSupportTicket(mapOf("title" to title, "message" to message, "category" to "general"))
                                navigator.pop()
                            } catch (_: Exception) {
                                isSending = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isSending,
                    colors = ButtonDefaults.buttonColors(containerColor = RewPrimary)
                ) {
                    if (isSending) CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
                    else Text("Создать", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- ЧАТ ТИКЕТА ---
data class TicketChatScreen(val ticketId: Long, val title: String) : Screen {
    @Composable
    override fun Content() {
        val api = koinInject<RewHostApi>()
        val navigator = LocalNavigator.currentOrThrow
        var messages by remember { mutableStateOf<List<TicketMessage>>(emptyList()) }
        var inputText by remember { mutableStateOf("") }
        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()

        fun loadMessages() {
            scope.launch {
                try { messages = api.getTicketMessages(ticketId) } catch (_: Exception) {}
            }
        }

        LaunchedEffect(Unit) {
            loadMessages()
            while(true) {
                delay(5000)
                loadMessages()
            }
        }

        Scaffold(
            bottomBar = {
                GlassCard(modifier = Modifier.fillMaxWidth(), padding = 0.dp) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Сообщение...", color = TextGray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                        IconButton(onClick = {
                            if (inputText.isNotBlank()) {
                                scope.launch {
                                    try {
                                        api.replyToTicket(ticketId, mapOf("message" to inputText))
                                        inputText = ""
                                        loadMessages()
                                    } catch (_: Exception) {}
                                }
                            }
                        }) {
                            Icon(Icons.Default.Send, null, tint = RewPrimary)
                        }
                    }
                }
            },
            containerColor = DarkBackground
        ) { padding ->
            Column(Modifier.padding(padding)) {
                Box(Modifier.fillMaxWidth().padding(16.dp)) {
                    IconButton(onClick = { navigator.pop() }, modifier = Modifier.align(Alignment.CenterStart)) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextWhite)
                    }
                    Text(title, color = TextWhite, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    reverseLayout = true
                ) {
                    items(messages.reversed()) { msg ->
                        ChatBubble(msg)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    @Composable
    fun ChatBubble(msg: TicketMessage) {
        val isMe = !msg.isAdmin
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp,
                        bottomStart = if (isMe) 16.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 16.dp
                    ))
                    .background(if (isMe) RewPrimary else Color(0xFF334155))
                    .padding(12.dp)
            ) {
                Text(msg.message, color = if (isMe) Color.Black else TextWhite)
            }
        }
    }
}
