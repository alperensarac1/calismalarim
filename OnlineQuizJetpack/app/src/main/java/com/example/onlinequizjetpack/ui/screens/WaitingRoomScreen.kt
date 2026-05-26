package com.example.onlinequizjetpack.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizjetpack.ui.components.ScreenContainer

@Composable
fun WaitingRoomScreen(
    roomCode: String,
    username: String,
    questionTime: Int,
    playersText: String,
    statusText: String
) {
    ScreenContainer {
        Text(
            text = "Bekleme Odası",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = """
                Kullanıcı: $username
                Oda Kodu: $roomCode
                Soru Süresi: $questionTime saniye
                
                Oda sahibi quizi başlatınca sorular ekrana gelecek.
            """.trimIndent(),
            fontSize = 16.sp,
            color = Color(0xFF374151)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = statusText,
            fontSize = 15.sp,
            color = Color(0xFF6D28D9),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = playersText,
            fontSize = 15.sp,
            color = Color(0xFF111827)
        )
    }
}