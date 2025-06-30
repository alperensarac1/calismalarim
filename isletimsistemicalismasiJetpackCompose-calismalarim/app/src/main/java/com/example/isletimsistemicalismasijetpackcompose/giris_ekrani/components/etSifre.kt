package com.example.isletimsistemicalismasijetpackcompose.giris_ekrani.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.sharp.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun EtSifre(modifier: Modifier = Modifier,tf: MutableState<String>) {
    val parolaGoster = remember { mutableStateOf(false) }


    TextField(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 50.dp),
        value = tf.value,
        onValueChange = { tf.value = it },
        placeholder = { Text("Şifrenizi giriniz.") },
        visualTransformation = if (parolaGoster.value) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Password
        ),
        trailingIcon = {
            Icon(
                imageVector = if (parolaGoster.value) Icons.Default.Lock else Icons.Sharp.Lock,
                "",
                modifier = Modifier.clickable {
                    parolaGoster.value = !parolaGoster.value
                })
        },
    )


}