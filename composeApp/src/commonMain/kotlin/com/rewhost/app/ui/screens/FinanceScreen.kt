package com.rewhost.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.rewhost.app.api.RewHostApi
import com.rewhost.app.data.model.Transaction
import com.rewhost.app.ui.components.BouncyBtn
import com.rewhost.app.ui.components.GlassCard
import com.rewhost.app.ui.theme.*
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

class FinanceScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val api = koinInject<RewHostApi>()
        
        var selectedTab by remember { mutableStateOf(0) } 
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Финансы", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkBackground,
                        titleContentColor = TextWhite,
                        navigationIconContentColor = TextGray
                    )
                )
            },
            containerColor = DarkBackground
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(DarkSurface, RoundedCornerShape(16.dp))
                        .padding(4.dp)
                ) {
                    TabButton("Пополнить", selectedTab == 0) { selectedTab = 0 }
                    TabButton("История", selectedTab == 1) { selectedTab = 1 }
                }

                if (selectedTab == 0) {
                    DepositTab(api)
                } else {
                    HistoryTab(api)
                }
            }
        }
    }

    @Composable
    fun RowScope.TabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
        val bgColor = if (isSelected) RewPrimary else Color.Transparent
        val textColor = if (isSelected) Color.Black else TextGray
        
        Box(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(text, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun DepositTab(api: RewHostApi) {
    var amount by remember { mutableStateOf("100") }
    var selectedMethod by remember { mutableStateOf("sbp") } 
    var bankName by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val methods = listOf(
        "sbp" to "СБП (Fast)",
        "card" to "Карта РФ"
    )

    Column(Modifier.padding(horizontal = 20.dp).fillMaxSize()) {
        GlassCard {
            Column {
                Text("Сумма пополнения (₽)", color = TextGray, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = amount,
                    onValueChange = { if (it.all { char -> char.isDigit() }) amount = it },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextWhite),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = RewPrimary,
                        unfocusedIndicatorColor = TextGray
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text("Метод оплаты", color = TextGray, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            methods.forEach { (key, name) ->
                val isSelected = selectedMethod == key
                val borderColor = if (isSelected) RewPrimary else Color.Transparent
                val bgColor = if (isSelected) RewPrimary.copy(alpha = 0.1f) else DarkSurface

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                        .clickable { selectedMethod = key },
                    contentAlignment = Alignment.Center
                ) {
                    Text(name, color = if(isSelected) RewPrimary else TextGray, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        GlassCard {
            Column {
                Text("Ваш банк (для проверки)", color = TextGray, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    placeholder = { Text("Например: Сбербанк", color = TextGray.copy(0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = RewPrimary,
                        unfocusedIndicatorColor = TextGray
                    )
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        if (message != null) {
            Text(message!!, color = SuccessGreen, modifier = Modifier.padding(bottom = 16.dp))
        }

        BouncyBtn(
            onClick = {
                if (amount.isBlank() || bankName.isBlank()) return@BouncyBtn
                scope.launch {
                    isProcessing = true
                    try {
                        api.createDeposit(
                            amount = amount.toDouble(),
                            method = selectedMethod,
                            details = mapOf("bank_name" to bankName)
                        )
                        message = "✅ Заявка создана! Ожидайте подтверждения."
                        amount = ""
                        bankName = ""
                    } catch (e: Exception) {
                        message = "❌ Ошибка: ${e.message}"
                    } finally {
                        isProcessing = false
                    }
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(RewPrimary),
                contentAlignment = Alignment.Center
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Text("Создать заявку", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
        
        Spacer(Modifier.height(12.dp))
        Text(
            "После нажатия заявка отправится администратору. Переведите средства по реквизитам в боте или дождитесь инструкции.",
            color = TextGray, fontSize = 12.sp, textAlign = TextAlign.Center
        )
    }
}

@Composable
fun HistoryTab(api: RewHostApi) {
    var history by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            history = api.getFinanceHistory()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = RewPrimary)
        }
        return
    }

    if (history.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("История пуста", color = TextGray)
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(history) { item ->
            GlassCard(padding = 16.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val isDeposit = item.method in listOf("sbp", "card", "crypto", "stars")
                        val icon = if (isDeposit) Icons.Default.Add else Icons.Default.CreditCard
                        val iconBg = if (isDeposit) SuccessGreen.copy(alpha = 0.2f) else ErrorRed.copy(alpha = 0.2f)
                        val iconTint = if (isDeposit) SuccessGreen else ErrorRed

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(iconBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, null, tint = iconTint)
                        }

                        Spacer(Modifier.width(12.dp))

                        Column {
                            val title = when(item.method) {
                                "sbp" -> "Пополнение СБП"
                                "card" -> "Пополнение Карта"
                                "crypto" -> "Crypto Pay"
                                "stars" -> "Telegram Stars"
                                else -> "Транзакция"
                            }
                            Text(title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(item.createdAt ?: "Дата неизвестна", color = TextGray, fontSize = 10.sp)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        val sign = if (item.method in listOf("sbp", "card", "crypto", "stars")) "+" else "-"
                        val color = if (sign == "+") SuccessGreen else TextWhite
                        
                        Text("$sign${item.amount} ₽", color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        
                        val statusColor = when(item.status) {
                            "approved" -> SuccessGreen
                            "declined" -> ErrorRed
                            else -> WarningOrange
                        }
                        Text(item.status.uppercase(), color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                if (item.declineReason != null) {
                    Spacer(Modifier.height(8.dp))
                    Text("Причина отказа: ${item.declineReason}", color = ErrorRed, fontSize = 11.sp)
                }
            }
        }
    }
}