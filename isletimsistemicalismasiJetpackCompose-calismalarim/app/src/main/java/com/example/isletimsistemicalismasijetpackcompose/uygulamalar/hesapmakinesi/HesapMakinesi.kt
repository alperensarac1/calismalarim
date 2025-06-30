package com.example.isletimsistemicalismasijetpackcompose.uygulamalar.hesapmakinesi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HesapMakinesiScreen(modifier: Modifier = Modifier) {
    var val1 by remember { mutableStateOf(Double.NaN) }
    var val2 by remember { mutableStateOf(0.0) }
    var action by remember { mutableStateOf(' ') }
    var display by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    // İşlem Sabitleri
    val ADDITION = '+'
    val SUBTRACTION = '-'
    val MULTIPLICATION = '*'
    val DIVISION = '/'
    val EQU = '='

    // İşlem Fonksiyonları
    fun operate(op: Char) {
        if (!val1.isNaN()) {
            val2 = display.toDoubleOrNull() ?: 0.0
            val1 = when (action) {
                ADDITION -> val1 + val2
                SUBTRACTION -> val1 - val2
                MULTIPLICATION -> val1 * val2
                DIVISION -> if (val2 != 0.0) val1 / val2 else Double.NaN
                else -> val1
            }
        } else {
            val1 = display.toDoubleOrNull() ?: Double.NaN
        }
        action = op
        result = "$val1 $op"
        display = ""
    }

    // UI Tasarımı
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = result,
            modifier = modifier.fillMaxWidth(),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = display,
            modifier = modifier.fillMaxWidth(),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Sayı Butonları
        val numberButtons = listOf(
            listOf("7", "8", "9"),
            listOf("4", "5", "6"),
            listOf("1", "2", "3"),
            listOf("0")
        )

        numberButtons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { number ->
                    Button(
                        onClick = { display += number },
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                    ) {
                        Text(text = number, fontSize = 24.sp)
                    }
                }
            }
        }

        Spacer(modifier = modifier.height(8.dp))

        // İşlem Butonları
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { operate(ADDITION) },
                modifier = modifier.weight(1f)
            ) {
                Text(text = "+", fontSize = 24.sp)
            }

            Button(
                onClick = { operate(SUBTRACTION) },
                modifier = modifier.weight(1f)
            ) {
                Text(text = "-", fontSize = 24.sp)
            }

            Button(
                onClick = { operate(MULTIPLICATION) },
                modifier = modifier.weight(1f)
            ) {
                Text(text = "*", fontSize = 24.sp)
            }

            Button(
                onClick = { operate(DIVISION) },
                modifier = modifier.weight(1f)
            ) {
                Text(text = "/", fontSize = 24.sp)
            }
        }

        Spacer(modifier = modifier.height(8.dp))

        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (!val1.isNaN()) {
                        val2 = display.toDoubleOrNull() ?: 0.0
                        val1 = when (action) {
                            ADDITION -> val1 + val2
                            SUBTRACTION -> val1 - val2
                            MULTIPLICATION -> val1 * val2
                            DIVISION -> if (val2 != 0.0) val1 / val2 else Double.NaN
                            else -> val1
                        }
                    } else {
                        val1 = display.toDoubleOrNull() ?: Double.NaN
                    }
                    action = EQU
                    result = ""
                    display = val1.toString()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "=", fontSize = 24.sp)
            }

            Button(
                onClick = {
                    if (display.isNotEmpty()) {
                        display = display.dropLast(1)
                    } else {
                        val1 = Double.NaN
                        val2 = Double.NaN
                        display = ""
                        result = ""
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "C", fontSize = 24.sp)
            }
        }
    }
}
