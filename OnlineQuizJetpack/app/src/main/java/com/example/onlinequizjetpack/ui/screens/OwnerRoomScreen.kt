package com.example.onlinequizjetpack.ui.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizjetpack.ui.components.ScreenContainer

@Composable
fun OwnerRoomScreen(
    roomCode: String,
    username: String,
    questionTime: Int,
    playersText: String,
    questionCount: Int,
    statusText: String,
    onAddQuestion: (String, List<String>, Int) -> Unit,
    onStartQuiz: () -> Unit
) {
    /*
        Bu ekran sadece UI state tutar:
        - soru metni
        - dinamik şık inputları
        - seçili doğru cevap indexi

        Server'a gönderme, validasyon ve WebSocket işlemleri ViewModel'dedir.
    */

    var questionText by remember {
        mutableStateOf("")
    }

    val optionTexts = remember {
        mutableStateListOf("", "")
    }

    var selectedCorrectIndex by remember {
        mutableStateOf(-1)
    }

    /*
        Soru başarıyla eklendiğinde ViewModel'deki questionCount artar.
        Bu değişimi yakalayıp formu temizliyoruz.
    */
    LaunchedEffect(questionCount) {
        if (questionCount > 0) {
            questionText = ""
            optionTexts.clear()
            optionTexts.add("")
            optionTexts.add("")
            selectedCorrectIndex = -1
        }
    }

    ScreenContainer {
        Text(
            text = "Oda Sahibi Paneli",
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Oda Kodu: $roomCode",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6D28D9)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Kullanıcı: $username\nSoru Süresi: $questionTime saniye",
            fontSize = 15.sp,
            color = Color(0xFF374151)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = playersText,
            fontSize = 15.sp,
            color = Color(0xFF111827)
        )

        Spacer(modifier = Modifier.height(24.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Soru Ekle",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            value = questionText,
            onValueChange = {
                questionText = it
            },
            label = {
                Text("Soru metni")
            }
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Şıklar",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )

        Spacer(modifier = Modifier.height(8.dp))

        optionTexts.forEachIndexed { index, optionText ->
            OptionEditRow(
                index = index,
                optionText = optionText,
                selectedCorrectIndex = selectedCorrectIndex,
                canDelete = optionTexts.size > 2,
                onTextChange = { newText ->
                    optionTexts[index] = newText
                },
                onSelectCorrect = {
                    selectedCorrectIndex = index
                },
                onDelete = {
                    if (optionTexts.size > 2) {
                        optionTexts.removeAt(index)

                        if (selectedCorrectIndex == index) {
                            selectedCorrectIndex = -1
                        } else if (selectedCorrectIndex > index) {
                            selectedCorrectIndex--
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            onClick = {
                optionTexts.add("")
            }
        ) {
            Text("+ Şık Ekle")
        }

        Spacer(modifier = Modifier.height(22.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            onClick = {
                onAddQuestion(
                    questionText,
                    optionTexts.toList(),
                    selectedCorrectIndex
                )
            }
        ) {
            Text("Soruyu Ekle")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            onClick = onStartQuiz
        ) {
            Text("Quizi Başlat")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Eklenen soru: $questionCount",
            fontSize = 15.sp,
            color = Color(0xFF374151)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = statusText,
            fontSize = 15.sp,
            color = Color(0xFF374151)
        )
    }
}

@Composable
fun OptionEditRow(
    index: Int,
    optionText: String,
    selectedCorrectIndex: Int,
    canDelete: Boolean,
    onTextChange: (String) -> Unit,
    onSelectCorrect: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selectedCorrectIndex == index,
            onClick = onSelectCorrect
        )

        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = optionText,
            onValueChange = onTextChange,
            label = {
                Text("Şık ${index + 1}")
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            enabled = canDelete,
            onClick = onDelete
        ) {
            Text("Sil")
        }
    }
}