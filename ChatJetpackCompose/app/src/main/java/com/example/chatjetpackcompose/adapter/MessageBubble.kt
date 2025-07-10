package com.example.chatjetpackcompose.adapter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.chatkotlin.model.Mesaj

@Composable
fun MessageBubble(
    mesaj: Mesaj,
    benimId: Int,
    modifier: Modifier = Modifier
) {
    val isMine = mesaj.gonderen_id == benimId
    val bubbleColor = if (isMine) Color(0xFF4F9BFF) else Color(0xFFE0E0E0)
    val textColor   = if (isMine) Color.White else Color.Black
    val alignment   = if (isMine) Arrangement.End else Arrangement.Start

    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = alignment
    ) {
        Column(
            Modifier
                .background(bubbleColor, shape = RoundedCornerShape(12.dp))
                .padding(8.dp)
                .widthIn(max = 260.dp)
        ) {
            // Metin
            mesaj.mesaj_text?.takeIf { it.isNotBlank() }?.let {
                Text(it, color = textColor)
            }

            // Resim (Coil)
            mesaj.resim_url?.takeIf { it.isNotBlank() }?.let { url ->
                Spacer(Modifier.height(6.dp))
                AsyncImage(
                    model = url,
                    contentDescription = "resim",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                )
            }

            // Tarih
            Spacer(Modifier.height(4.dp))
            Text(
                mesaj.tarih,
                color = textColor.copy(alpha = .7f),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
