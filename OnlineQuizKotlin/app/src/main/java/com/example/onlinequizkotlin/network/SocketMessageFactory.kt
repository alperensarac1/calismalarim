package com.example.onlinequizkotlin.network


import org.json.JSONArray
import org.json.JSONObject

object SocketMessageFactory {

    /*
        Android'den Python WebSocket server'a gönderilecek JSON mesajlarını
        bu sınıfta oluşturuyoruz.

        Böylece Fragment içinde JSON oluşturma kodu kalabalıklaşmaz.
    */

    fun createRoom(
        username: String,
        questionTime: Int
    ): String {
        val json = JSONObject()

        json.put("type", "create_room")
        json.put("username", username)
        json.put("question_time", questionTime)

        return json.toString()
    }

    fun joinRoom(
        roomCode: String,
        username: String
    ): String {
        val json = JSONObject()

        json.put("type", "join_room")
        json.put("room_code", roomCode)
        json.put("username", username)

        return json.toString()
    }

    fun addQuestion(
        roomCode: String,
        questionText: String,
        options: List<String>,
        correctIndex: Int
    ): String {
        val json = JSONObject()

        json.put("type", "add_question")
        json.put("room_code", roomCode)
        json.put("question_text", questionText)

        val optionsArray = JSONArray()

        for (option in options) {
            optionsArray.put(option)
        }

        json.put("options", optionsArray)
        json.put("correct_index", correctIndex)

        return json.toString()
    }

    fun startQuiz(
        roomCode: String
    ): String {
        val json = JSONObject()

        json.put("type", "start_quiz")
        json.put("room_code", roomCode)

        return json.toString()
    }

    fun submitAnswer(
        roomCode: String,
        username: String,
        answerIndex: Int
    ): String {
        val json = JSONObject()

        json.put("type", "submit_answer")
        json.put("room_code", roomCode)
        json.put("username", username)
        json.put("answer_index", answerIndex)

        return json.toString()
    }
}