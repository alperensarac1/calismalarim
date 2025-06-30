package com.example.isletimsistemicalismasijetpackcompose.uygulamalar.tarayici

import android.webkit.WebView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun Tarayici(modifier: Modifier = Modifier) {
    var aramaKelimesi by remember { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = aramaKelimesi,
            onValueChange = { aramaKelimesi = it },
            label = { Text("Arama Yap") },
            modifier = modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                aramaKelimesi = aramaKelimesi.trim()
            })
        )

        Button(
            onClick = { aramaKelimesi = aramaKelimesi.trim() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ara")
        }

        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    loadUrl("https://www.google.com") // Varsayılan olarak Google yüklenir
                }
            },
            update = { webView ->
                if (aramaKelimesi.isNotEmpty()) {
                    val query = aramaKelimesi.replace(" ", "+")
                    webView.loadUrl("https://www.google.com/search?q=$query")
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

