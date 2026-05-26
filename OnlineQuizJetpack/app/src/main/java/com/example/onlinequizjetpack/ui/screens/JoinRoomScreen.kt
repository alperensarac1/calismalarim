package com.example.onlinequizjetpack.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizjetpack.ui.components.ScreenContainer

@Composable
fun JoinRoomScreen(
    statusText: String,
    onJoinRoom: (String, String) -> Unit,
    onBack: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var roomCode by remember { mutableStateOf("") }

    ScreenContainer {
        Text(
            text = "Odaya Giriş Yap",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Kullanıcı adını ve oda kodunu gir.",
            fontSize = 15.sp,
            color = Color(0xFF6B7280)
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = username,
            onValueChange = {
                username = it
            },
            label = {
                Text("Kullanıcı adı")
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = roomCode,
            onValueChange = {
                roomCode = it
            },
            label = {
                Text("Oda kodu")
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            onClick = {
                onJoinRoom(roomCode, username)
            }
        ) {
            Text("Odaya Katıl")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = onBack
        ) {
            Text("Geri dön")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = statusText,
            color = Color(0xFF374151),
            fontSize = 15.sp
        )
    }
}