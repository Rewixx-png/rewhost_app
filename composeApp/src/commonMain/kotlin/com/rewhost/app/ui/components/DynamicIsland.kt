package com.rewhost.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rewhost.app.ui.theme.ErrorRed
import com.rewhost.app.ui.theme.RewPrimary
import com.rewhost.app.ui.theme.SuccessGreen

enum class IslandState {
    IDLE, LOADING, ALERT
}

@Composable
fun RewDynamicIsland(
    state: IslandState = IslandState.IDLE,
    mainText: String = "RewHost",
    subText: String = ""
) {
    // Анимация размеров (Пружинистый эффект как в iOS)
    val width by animateDpAsState(
        targetValue = when (state) {
            IslandState.IDLE -> 120.dp
            IslandState.LOADING -> 200.dp
            IslandState.ALERT -> 340.dp
        },
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow)
    )

    val height by animateDpAsState(
        targetValue = if (state == IslandState.ALERT) 80.dp else 36.dp,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow)
    )

    Box(
        modifier = Modifier
            .padding(top = 10.dp) // Отступ от верхнего края (Status Bar)
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(40.dp))
            .background(Color.Black) // Черный цвет острова
            .padding(horizontal = 12.dp)
    ) {
        // Контент внутри острова
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Левая иконка (всегда видна, но меняется)
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1C1C1E)),
                contentAlignment = Alignment.Center
            ) {
                when (state) {
                    IslandState.LOADING -> CircularProgressIndicator(modifier = Modifier.size(14.dp), color = RewPrimary, strokeWidth = 2.dp)
                    IslandState.ALERT -> Icon(Icons.Default.Warning, null, tint = ErrorRed, modifier = Modifier.size(14.dp))
                    else -> Icon(Icons.Default.Dns, null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                }
            }

            // Текст (показывается только если остров расширен)
            if (width > 130.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = mainText,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }

            // Правая часть (видна при полном раскрытии ALERT)
            if (state == IslandState.ALERT) {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.End
                ) {
                    Text("ALERT", color = ErrorRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(subText, color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}