package com.example.isletimsistemicalismasijetpackcompose.uygulamalar.qrkodokuyucu

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@Composable
fun QrOkuyucuScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    var scanResult by remember { mutableStateOf("") }
    var isDialogOpen by remember { mutableStateOf(false) }
    var urlToOpen by remember { mutableStateOf("") }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let { content ->
            scanResult = content
            isDialogOpen = true
        }
    }

    val cameraPermission = Manifest.permission.CAMERA
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                scanCode(scanLauncher)
            } else {
                Toast.makeText(context, "Kamera izni gerekli!", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            when {
                ContextCompat.checkSelfPermission(context, cameraPermission) == PackageManager.PERMISSION_GRANTED -> {
                    scanCode(scanLauncher)
                }
                else -> {
                    permissionLauncher.launch(cameraPermission)
                }
            }
        }) {
            Text(text = "QR Tara")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (scanResult.isNotEmpty()) {
                urlToOpen = scanResult
            } else {
                Toast.makeText(context, "Önce bir QR kodu tarayın!", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text(text = "Siteyi Aç")
        }

        if (urlToOpen.isNotEmpty()) {
            AndroidView(factory = {
                WebView(it).apply {
                    settings.javaScriptEnabled = true
                    loadUrl(urlToOpen)
                }
            }, modifier = Modifier.fillMaxSize())
        }
    }

    if (isDialogOpen) {
        AlertDialog(
            onDismissRequest = { isDialogOpen = false },
            title = { Text(text = "Sonuç") },
            text = { Text(text = scanResult) },
            confirmButton = {
                Button(onClick = {
                    val clipData = ClipData.newPlainText("text", scanResult)
                    clipboardManager.setPrimaryClip(clipData)
                    Toast.makeText(context, "Panoya Kopyalandı", Toast.LENGTH_SHORT).show()
                    isDialogOpen = false
                }) {
                    Text("Kopyala")
                }
            },
            dismissButton = {
                Button(onClick = { isDialogOpen = false }) {
                    Text("Kapat")
                }
            }
        )
    }
}

fun scanCode(launcher: ActivityResultLauncher<ScanOptions>) {
    val options = ScanOptions().apply {
        setPrompt("Flash açmak için ses arttırma tuşuna basın.")
        setBeepEnabled(true)
        setOrientationLocked(true)
        setCaptureActivity(CaptureAct::class.java)
    }
    launcher.launch(options)
}
