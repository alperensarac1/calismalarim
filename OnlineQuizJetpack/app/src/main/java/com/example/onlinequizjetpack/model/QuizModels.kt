package com.example.onlinequizjetpack.model

/*
    Uygulama içinde kullanacağımız veri modelleri.

    Server JSON gönderir.
    Biz bu JSON'u UI tarafında daha rahat kullanmak için
    Kotlin data class'lara dönüştürürüz.
*/

data class ScoreItem(
    val username: String,
    val score: Int
)

data class QuestionData(
    val questionNumber: Int,
    val totalQuestions: Int,
    val questionText: String,
    val options: List<String>,
    val questionTime: Int
)

data class OptionInput(
    val text: String = ""
)