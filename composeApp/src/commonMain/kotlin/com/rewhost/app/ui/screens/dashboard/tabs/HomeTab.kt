package com.rewhost.app.ui.screens.dashboard.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.rewhost.app.api.RewHostApi
import com.rewhost.app.data.model.DashboardResponse
import com.rewhost.app.ui.components.BouncyBtn
import com.rewhost.app.ui.components.GlassCard
import com.rewhost.app.ui.screens.FinanceScreen
import com.rewhost.app.ui.theme.RewPrimary
import com.rewhost.app.ui.theme.TextGray
import com.rewhost.app.ui.theme.TextWhite
import kotlin.math.max

@Composable
fun HomeTab(data: DashboardResponse, api: RewHostApi) {
    val navigator = LocalNavigator.currentOrThrow
    val profile = data.profile
    val balanceStr = String.format("%.2f", profile.balance)
    val uriHandler = LocalUriHandler.current
    
    val level = profile.levelInfo?.level ?: 1
    val xp = profile.levelInfo?.xp ?: 0L
    val nextXp = profile.levelInfo?.nextLevelXp ?: 100L
    val progress = (xp.toFloat() / max(1L, nextXp).toFloat()).coerceIn(0f, 1f)
    
    val avatarUrl = api.getAvatarUrl(profile.userId)

    LazyColumn(contentPadding = PaddingValues(20.dp)) {
        // --- HEADER ---
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                // Аватар
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF334155))
                        .border(2.dp, RewPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Spacer(Modifier.width(16.dp))
                
                Column {
                    Text("Добро пожаловать,", color = TextGray, fontSize = 14.sp)
                    Text(
                        profile.firstName ?: "Пользователь", 
                        color = TextWhite, 
                        fontSize = 20.sp, 
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "@${profile.username ?: "unknown"}", 
                        color = RewPrimary, 
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(Modifier.weight(1f))
                
                GlassCard(padding = 8.dp) {
                   Text("USER", color = TextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- BALANCE CARD ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF2563EB), Color(0xFF1E40AF))
                        )
                    )
                    .padding(24.dp)
            ) {
                Box(Modifier.align(Alignment.TopEnd).size(120.dp).padding(top=20.dp, end=20.dp)
                    .background(Color.White.copy(alpha=0.1f), CircleShape))
                
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Основной баланс", color = TextWhite.copy(alpha = 0.7f), fontSize = 14.sp)
                        Icon(Icons.Default.Bolt, null, tint = RewPrimary, modifier = Modifier.size(24.dp))
                    }
                    
                    Text(
                        "$balanceStr ₽", 
                        color = TextWhite, 
                        fontSize = 36.sp, 
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("•••• •••• •••• ${profile.userId.toString().takeLast(4)}", color = TextWhite.copy(alpha = 0.5f), fontSize = 14.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        // --- QUICK ACTIONS ---
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Кнопка Пополнить
                BouncyBtn(onClick = { navigator.push(FinanceScreen()) }, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .height(50.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(RewPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Пополнить", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
                
                // Кнопка Поддержка
                BouncyBtn(onClick = { uriHandler.openUri("https://rewhost.rewixx.ru/support") }, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .height(50.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF334155)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SupportAgent, null, tint = TextWhite, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Поддержка", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
        
        // --- STATS GRID ---
        item {
            Text("Статистика", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassCard(modifier = Modifier.weight(1f), padding = 16.dp) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Icon(Icons.Default.Layers, null, tint = Color(0xFF60A5FA), modifier = Modifier.size(28.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(data.containers.size.toString(), color = TextWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("Активные боты", color = TextGray, fontSize = 12.sp)
                    }
                }
                
                GlassCard(modifier = Modifier.weight(1f), padding = 16.dp) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Bolt, null, tint = RewPrimary, modifier = Modifier.size(28.dp))
                            Text("Lvl $level", color = RewPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = RewPrimary,
                            trackColor = Color.White.copy(alpha = 0.1f),
                            strokeCap = StrokeCap.Round
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("$xp / $nextXp XP", color = TextGray, fontSize = 11.sp)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}