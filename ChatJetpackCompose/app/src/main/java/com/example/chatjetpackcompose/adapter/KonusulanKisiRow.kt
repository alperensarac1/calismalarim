package com.example.chatjetpackcompose.adapter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chatkotlin.model.KonusulanKisi

@Composable
fun KonusulanKisiRow(
    kisi: KonusulanKisi,
    onClick: (KonusulanKisi) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(kisi) }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(kisi.ad, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = kisi.son_mesaj.take(30) +
                        if (kisi.son_mesaj.length > 30) "..." else "",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
        }//:Column
        Text(
            text = kisi.tarih,
            style = MaterialTheme.typography.bodySmall
        )
    }//:Row
    Divider()
}
