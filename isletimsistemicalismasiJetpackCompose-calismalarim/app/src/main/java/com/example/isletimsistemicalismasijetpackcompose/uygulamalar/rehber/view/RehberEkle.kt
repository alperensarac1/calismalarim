package com.example.isletimsistemicalismasijetpackcompose.uygulamalar.rehber.view

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.isletimsistemicalismasijetpackcompose.uygulamalar.rehber.entity.RehberDao
import com.example.isletimsistemicalismasijetpackcompose.uygulamalar.rehber.entity.RehberVeritabaniYardimcisi
import kotlinx.coroutines.launch

@Composable
fun RehberEkle(modifier: Modifier = Modifier,navController: NavController, context: Context) {
    var kisiIsim by remember { mutableStateOf("") }
    var kisiNumara by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val dao = RehberDao(RehberVeritabaniYardimcisi(context))

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = kisiIsim,
            onValueChange = { kisiIsim = it },
            label = { Text("İsim") },
            modifier = modifier.fillMaxWidth()
        )
        Spacer(modifier = modifier.height(8.dp))
        OutlinedTextField(
            value = kisiNumara,
            onValueChange = { kisiNumara = it },
            label = { Text("Telefon Numarası") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (kisiIsim.isNotBlank() && kisiNumara.isNotBlank()) {
                    dao.kisiEkle(kisiIsim, kisiNumara)
                    scope.launch {
                        snackbarHostState.showSnackbar("Kişi eklendi")
                    }
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ekle")
        }
        Spacer(modifier = Modifier.height(16.dp))
        SnackbarHost(hostState = snackbarHostState)
    }
}
