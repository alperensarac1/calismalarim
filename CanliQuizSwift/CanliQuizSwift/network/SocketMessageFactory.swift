//
//  SocketMessageFactory.swift
//  CanliQuizSwift
//
//  Created by Alperen Saraç on 21.05.2026.
//

import Foundation

final class SocketMessageFactory {

    /*
        Python WebSocket server'a gönderilecek JSON mesajlarını burada üretiyoruz.

        Böylece ViewController içinde elle JSON yazma kalabalığı oluşmaz.
    */

    static func createRoom(username: String, questionTime: Int) -> String {
        let json: [String: Any] = [
            "type": "create_room",
            "username": username,
            "question_time": questionTime
        ]

        return makeJSONString(json)
    }

    static func joinRoom(roomCode: String, username: String) -> String {
        let json: [String: Any] = [
            "type": "join_room",
            "room_code": roomCode,
            "username": username
        ]

        return makeJSONString(json)
    }

    static func addQuestion(
        roomCode: String,
        questionText: String,
        options: [String],
        correctIndex: Int
    ) -> String {
        let json: [String: Any] = [
            "type": "add_question",
            "room_code": roomCode,
            "question_text": questionText,
            "options": options,
            "correct_index": correctIndex
        ]

        return makeJSONString(json)
    }

    static func startQuiz(roomCode: String) -> String {
        let json: [String: Any] = [
            "type": "start_quiz",
            "room_code": roomCode
        ]

        return makeJSONString(json)
    }

    static func submitAnswer(
        roomCode: String,
        username: String,
        answerIndex: Int
    ) -> String {
        let json: [String: Any] = [
            "type": "submit_answer",
            "room_code": roomCode,
            "username": username,
            "answer_index": answerIndex
        ]

        return makeJSONString(json)
    }

    private static func makeJSONString(_ dictionary: [String: Any]) -> String {
        do {
            let data = try JSONSerialization.data(
                withJSONObject: dictionary,
                options: []
            )

            return String(data: data, encoding: .utf8) ?? "{}"

        } catch {
            return "{}"
        }
    }
}
