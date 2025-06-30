package com.example.isletimsistemicalismasijetpackcompose.uygulamalar.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.Date
import java.util.Locale

@Composable
fun Alarm(context: Context) {
    var selectedTime = remember { mutableStateOf("Saat Seç") }
    var calendar = remember { mutableStateOf<Calendar?>(null) }
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val currentDateTime = remember { mutableStateOf(getCurrentDateTime()) }
    val coroutineScope = rememberCoroutineScope()

    // Zamanlayıcı güncelleme için LaunchedEffect kullanıyoruz
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentDateTime.value = getCurrentDateTime()
        }
    }

    // Zaman seçimi için Dialog
    var showTimePicker = remember { mutableStateOf(false) }
    if (showTimePicker.value) {
        val timePickerDialog = TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                calendar.value = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                selectedTime.value = formatSelectedTime(hourOfDay, minute)
                showTimePicker.value = false
            },
            12, 0, false
        )
        timePickerDialog.show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = currentDateTime.value, )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { showTimePicker.value = true }) {
            Text(text = selectedTime.value)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (calendar == null) {
                Toast.makeText(context, "Lütfen bir saat seçin.", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(context, AlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context, 0, intent, PendingIntent.FLAG_IMMUTABLE
                )

                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar!!.value!!.timeInMillis,
                    pendingIntent
                )
                Toast.makeText(context, "Alarm Ayarlandı", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Alarm Kur")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            Toast.makeText(context, "Alarm İptal Edildi", Toast.LENGTH_SHORT).show()
        }) {
            Text("Alarmı İptal Et")
        }
    }
}

// Seçilen saati biçimlendirme fonksiyonu
fun formatSelectedTime(hour: Int, minute: Int): String {
    val isPM = hour >= 12
    val formattedHour = when {
        hour > 12 -> hour - 12
        hour == 0 -> 12
        else -> hour
    }
    return String.format("%02d:%02d %s", formattedHour, minute, if (isPM) "PM" else "AM")
}

// Şu anki tarih ve saati döndüren fonksiyon
fun getCurrentDateTime(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    return sdf.format(Date())
}
