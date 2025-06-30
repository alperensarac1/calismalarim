package com.example.isletimsistemicalismasijetpackcompose.uygulamalar.notdefteri

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.navigation.NavController
import java.util.Date
import java.util.Locale

@Composable
fun NotDefteriEkleSayfa(modifier: Modifier = Modifier,navController: NavController,viewModel: NotDefteriViewModel) {
    var baslik by remember { mutableStateOf("") }
    var notMetin by remember { mutableStateOf("") }
    val tarih = remember {
        SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", Locale.getDefault()).format(Date())
    }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = baslik,
            onValueChange = { baslik = it },
            label = { Text(text = "Başlık") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = notMetin,
            onValueChange = { notMetin = it },
            label = { Text(text = "Not") },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = tarih, style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            viewModel.yeniNot(Not(0,baslik,notMetin,"","",tarih,""))
            navController.popBackStack()

        }) {
            Text("KAYDET")
        }

    }
}