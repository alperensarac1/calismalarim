package com.example.onlinequizjetpack.viewmodel

import com.example.onlinequizjetpack.model.AppScreen
import com.example.onlinequizjetpack.model.QuestionData


/*
    Compose ekranları bu state'i dinleyecek.

    ViewModel state'i günceller.
    Compose otomatik olarak yeniden çizilir.

    Böylece:
    - statusText değişince ekranda mesaj değişir.
    - currentScreen değişince ekran değişir.
    - playersText değişince oyuncu listesi değişir.
    - questionData değişince quiz sorusu değişir.
*/

data class QuizUiState(
    val currentScreen: AppScreen = AppScreen.Home,

    val statusText: String = "",

    val roomCode: String = "",
    val username: String = "",
    val questionTime: Int = 20,

    val playersText: String = "Oyuncular bekleniyor...",

    val questionCount: Int = 0,

    val currentQuestion: QuestionData? = null,
    val remainingTime: Int = 20,

    val selectedAnswerIndex: Int = -1,
    val currentCorrectIndex: Int = -1,
    val answeredCurrentQuestion: Boolean = false,

    val answerResultText: String = "",
    val scoreboardText: String = "Puan tablosu bekleniyor...",

    val winnersJson: String = "[]",
    val scoreboardJson: String = "[]"
)