package com.rewhost.app.ui.screens.dashboard.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
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
import com.rewhost.app.ui.screens.games.GamesScreen
import com.rewhost.app.ui.screens.support.SupportScreen
import com.rewhost.app.ui.theme.*
import kotlin.math.max

@Composable
fun HomeTab(data: DashboardResponse, api: RewHostApi) {
    val navigator = LocalNavigator.currentOrThrow
    val profile = data.profile
    val avatarUrl = api.getAvatarUrl(profile.userId)

    // Level Logic
    val level = profile.levelInfo?.level ?: 1
    val xp = profile.levelInfo?.xp ?: 0
    val nextXp = profile.levelInfo?.nextLevelXp ?: 100
    val progress = (xp.toFloat() / max(1L, nextXp).toFloat()).coerceIn(0f, 1f)

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- HEADER ---
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Slate700)
                        .border(2.dp, RewBlue.copy(alpha=0.5f), CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = profile.firstName ?: "User",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextWhite)
                    )
                    Text(
                        text = "ID: ${profile.userId}",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextGray)
                    )
                }
                Spacer(Modifier.weight(1f))
                GlassCard(padding = 8.dp, shape = RoundedCornerShape(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = RewYellow, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Lvl $level", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- BALANCE CARD (GRADIENT) ---
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
            ) {
                // Декор
                Box(Modifier.size(200.dp).offset(100.dp, (-50).dp).background(Color.White.copy(0.1f), CircleShape))

                Column(
                    modifier = Modifier.padding(24.dp).fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Основной баланс", color = TextWhite.copy(0.8f), fontSize = 14.sp)
                        Icon(Icons.Default.Wallet, null, tint = TextWhite.copy(0.8f))
                    }

                    Text(
                        "${profile.balance} ₽",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite
                    )

                    Row {
                        Text(
                            "•••• ${profile.userId.toString().takeLast(4)}",
                            color = TextWhite.copy(0.6f),
                            fontSize = 16.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // --- QUICK ACTIONS ---
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Deposit
                BouncyBtn(
                    onClick = { navigator.push(FinanceScreen()) },
                    modifier = Modifier.weight(1f)
                ) {
                    QuickActionButton("Пополнить", Icons.Default.Add, RewGreen)
                }

                // Games
                BouncyBtn(
                    onClick = { navigator.push(GamesScreen()) },
                    modifier = Modifier.weight(1f)
                ) {
                    QuickActionButton("Игры", Icons.Default.Gamepad, RewPurple)
                }

                // Support
                BouncyBtn(
                    onClick = { navigator.push(SupportScreen()) },
                    modifier = Modifier.weight(1f)
                ) {
                    QuickActionButton("Поддержка", Icons.Default.HeadsetMic, Slate400)
                }
            }
        }

        // --- STATS ---
        item {
            Text("Статистика", color = TextGray, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Active Bots
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column {
                        Icon(Icons.Default.Dns, null, tint = RewBlue)
                        Spacer(Modifier.height(8.dp))
                        Text(data.containers.size.toString(), color = TextWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("Активные боты", color = TextGray, fontSize = 12.sp)
                    }
                }

                // XP
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Bolt, null, tint = RewYellow)
                            Text("${(progress * 100).toInt()}%", color = RewYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = RewYellow,
                            trackColor = Slate700,
                            strokeCap = StrokeCap.Round
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("$xp / $nextXp XP", color = TextGray, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(text: String, icon: ImageVector, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Slate800)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color)
            Text(text, color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}