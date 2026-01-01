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
                    onClick = { navigator.push(CreateTicketScreen()) }, // <--- ТЕПЕРЬ РАБОТАЕТ
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

// --- ЧАТ (Оставляем как был) ---
data class TicketChatScreen(val ticketId: Long, val title: String) : Screen {
     @Composable
    override fun Content() {
        // ... (Код чата из предыдущего ответа, он рабочий) ...
        // Скопируй сюда класс TicketChatScreen из моего прошлого сообщения
         val navigator = LocalNavigator.currentOrThrow
         Scaffold(containerColor = DarkBackground) { padding -> Text("Chat placeholder", Modifier.padding(padding)) }
    }
}
