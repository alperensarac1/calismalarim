package com.example.adisyonuygulamajetpack.view.masadetay

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.adisyonuygulamajetpack.R
import com.example.adisyonuygulamakotlin.model.Masa

@Composable
fun MasaDetayHeader(
    masa: Masa,
    onBackPressed: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackPressed,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_remove),
                contentDescription = "Geri",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Masa ${masa.id}",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
