package com.example.isletimsistemicalismasijetpackcompose.uygulamalar.mesajlasma

import android.Manifest
import android.content.pm.PackageManager
import android.telephony.gsm.SmsManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@Composable
fun Mesajlasma(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var mesaj by remember { mutableStateOf("") }
    var gonderilecekNumara by remember { mutableStateOf("") }

    // SMS Gönderme İzni Kontrolü
    val smsPermission = Manifest.permission.SEND_SMS
    val permissionState = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, smsPermission) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        permissionState.value = isGranted
        if (!isGranted) {
            Toast.makeText(context, "SMS izni gereklidir", Toast.LENGTH_SHORT).show()
        }
    }

    // İzin durumu kontrolü
    LaunchedEffect(Unit) {
        if (!permissionState.value) {
            launcher.launch(smsPermission)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = gonderilecekNumara,
            onValueChange = { gonderilecekNumara = it },
            label = { Text("Telefon Numarası") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = mesaj,
            onValueChange = { mesaj = it },
            label = { Text("Mesaj") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )

        Spacer(modifier = modifier.height(16.dp))

        Button(
            onClick = {
                if (gonderilecekNumara.isNotBlank() && mesaj.isNotBlank()) {
                    try {
                        val smsManager = SmsManager.getDefault()
                        val mesajParcalari = smsManager.divideMessage(mesaj)
                        smsManager.sendMultipartTextMessage(gonderilecekNumara, null, mesajParcalari, null, null)

                        mesaj = ""
                        gonderilecekNumara = ""
                        Toast.makeText(context, "Mesaj Gönderildi", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Mesaj gönderilirken hata oluştu", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(context, "Numara ve mesaj boş olamaz", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            Text(text = "Mesaj Gönder")
        }
    }
}
