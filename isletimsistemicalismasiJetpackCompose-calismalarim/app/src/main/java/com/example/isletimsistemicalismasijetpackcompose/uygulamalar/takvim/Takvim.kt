package com.example.isletimsistemicalismasijetpackcompose.uygulamalar.takvim

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import java.util.Date
import java.util.Locale

@Composable
fun Takvim(modifier: Modifier = Modifier) {
    var selectedDate by remember { mutableStateOf(getCurrentDateTime()) }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Bugünün Tarihi: $selectedDate", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = modifier.height(16.dp))

        AndroidView(
            factory = { context ->
                android.widget.CalendarView(context).apply {
                    date = System.currentTimeMillis()
                    setOnDateChangeListener { _, year, month, dayOfMonth ->
                        selectedDate = "$dayOfMonth-${month + 1}-$year"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Seçilen Tarih: $selectedDate", style = MaterialTheme.typography.bodyLarge)
    }
}

fun getCurrentDateTime(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date())
}

@Preview(showBackground = true)
@Composable
fun TakvimPreview() {
    Takvim()
}
