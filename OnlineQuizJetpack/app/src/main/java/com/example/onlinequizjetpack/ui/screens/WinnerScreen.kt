package com.example.onlinequizjetpack.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizjetpack.ui.components.ScreenContainer
import com.example.onlinequizjetpack.viewmodel.buildScoreboardText
import com.example.onlinequizjetpack.viewmodel.buildWinnersText

import org.json.JSONArray

@Composable
fun WinnerScreen(
    winnersJson: String,
    scoreboardJson: String,
    onBackHome: () -> Unit
) {
    val winners = remember(winnersJson) {
        JSONArray(winnersJson)
    }

    val scoreboard = remember(scoreboardJson) {
        JSONArray(scoreboardJson)
    }

    ScreenContainer(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Quiz Bitti",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Bunlar Kazandı",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6D28D9)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = buildWinnersText(winners),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = buildScoreboardText(scoreboard),
            fontSize = 15.sp,
            color = Color(0xFF374151)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            onClick = onBackHome
        ) {
            Text("Ana Sayfaya Dön")
        }
    }
}