package com.example.qrkodolusturucujetpack.view

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.qrkodolusturucujetpack.R
import com.example.qrkodolusturucujetpack.components.SegmentedButton
import com.example.qrkodolusturucujetpack.navigation.UrunlerimizNavGraph
import com.example.qrkodolusturucujetpack.viewmodel.MainVM

@Composable
fun MainScreen(viewModel: MainVM = viewModel()) {
    val context = LocalContext.current
    val hediyeKahve by viewModel.hediyeKahveLiveData.observeAsState(initial = 0)
    val kahveSayisi by viewModel.kahveSayisiLiveData.observeAsState(initial = 0)
    val telefonNumarasi by viewModel.telefonNumarasiLiveData.observeAsState()


    val qrGozuksunMu = remember { mutableStateOf(true) }

    var showDialog by remember { mutableStateOf(telefonNumarasi == null) }

    if (viewModel.checkTelefonNumarasi()) {
        
    }else{
        if (showDialog) {
            PhoneInputDialog(
                onPhoneEntered = {
                    viewModel.numarayiKaydet(it)
                    viewModel.kullaniciEkle(it)
                    showDialog = false
                },
                onCancel = {
                    (context as? ComponentActivity)?.finish()
                }
            )
        }
    }



    Column(modifier = Modifier.fillMaxSize().background(color = colorResource(R.color.background)).padding(16.dp), verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(50.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {

            Text("Kullanımlar: $kahveSayisi/5", style = TextStyle(color = colorResource(R.color.textColor), fontSize = 20.sp))
            Text("Hediye Kahve: $hediyeKahve", style = TextStyle(color = colorResource(R.color.textColor), fontSize = 20.sp))
        }
        Spacer(modifier = Modifier.height(60.dp))
        Row(modifier = Modifier.wrapContentWidth().clip(RoundedCornerShape(30.dp)).background(Color.Gray.copy(alpha = 0.7f)), horizontalArrangement = Arrangement.SpaceEvenly) {
            SegmentedButton(qrGozuksunMu, text = "QR Kod", text2 = "Ürünlerimiz")
        }//:Row


        if (qrGozuksunMu.value){
            QRKodOlusturScreen()
        }else{
            UrunlerimizNavGraph()
        }
        //TODO burada qr kod scvreen gözükecek
    }//:Column

}

@Composable
fun PhoneInputDialog(onPhoneEntered: (String) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    var phoneInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Bilgi Girişi") },
        text = {
            OutlinedTextField(
                value = phoneInput,
                onValueChange = { phoneInput = it },
                label = { Text("Lütfen telefon numaranızı giriniz") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            TextButton(onClick = {
                if (phoneInput.isNotEmpty()) {
                    onPhoneEntered(phoneInput)
                    Toast.makeText(context, "Numara kaydedildi!", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Tamam")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("İptal")
            }
        }
    )
}
