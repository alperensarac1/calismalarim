package com.example.onlinequizjava.network;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class SocketMessageFactory {

    /*
        Python WebSocket server'a gönderilecek JSON mesajlarını burada üretiyoruz.

        Böylece Fragment içinde JSON kodları dağılmaz.
    */

    public static String createRoom(String username, int questionTime) {
        try {
            JSONObject json = new JSONObject();

            json.put("type", "create_room");
            json.put("username", username);
            json.put("question_time", questionTime);

            return json.toString();

        } catch (Exception e) {
            return "{}";
        }
    }

    public static String joinRoom(String roomCode, String username) {
        try {
            JSONObject json = new JSONObject();

            json.put("type", "join_room");
            json.put("room_code", roomCode);
            json.put("username", username);

            return json.toString();

        } catch (Exception e) {
            return "{}";
        }
    }

    public static String addQuestion(
            String roomCode,
            String questionText,
            List<String> options,
            int correctIndex
    ) {
        try {
            JSONObject json = new JSONObject();

            json.put("type", "add_question");
            json.put("room_code", roomCode);
            json.put("question_text", questionText);

            JSONArray optionArray = new JSONArray();

            for (String option : options) {
                optionArray.put(option);
            }

            json.put("options", optionArray);
            json.put("correct_index", correctIndex);

            return json.toString();

        } catch (Exception e) {
            return "{}";
        }
    }

    public static String startQuiz(String roomCode) {
        try {
            JSONObject json = new JSONObject();

            json.put("type", "start_quiz");
            json.put("room_code", roomCode);

            return json.toString();

        } catch (Exception e) {
            return "{}";
        }
    }

    public static String submitAnswer(
            String roomCode,
            String username,
            int answerIndex
    ) {
        try {
            JSONObject json = new JSONObject();

            json.put("type", "submit_answer");
            json.put("room_code", roomCode);
            json.put("username", username);
            json.put("answer_index", answerIndex);

            return json.toString();

        } catch (Exception e) {
            return "{}";
        }
    }
}
