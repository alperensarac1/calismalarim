//
//  SocketMessageFactory.swift
//  CanliQuizSwiftUI
//
//  Created by Alperen Saraç on 24.05.2026.
//

import Foundation

final class SocketMessageFactory {

    static func createRoom(username: String, questionTime: Int) -> String {
        makeJSONString([
            "type": "create_room",
            "username": username,
            "question_time": questionTime
        ])
    }

    static func joinRoom(roomCode: String, username: String) -> String {
        makeJSONString([
            "type": "join_room",
            "room_code": roomCode,
            "username": username
        ])
    }

    static func addQuestion(
        roomCode: String,
        questionText: String,
        options: [String],
        correctIndex: Int
    ) -> String {
        makeJSONString([
            "type": "add_question",
            "room_code": roomCode,
            "question_text": questionText,
            "options": options,
            "correct_index": correctIndex
        ])
    }

    static func startQuiz(roomCode: String) -> String {
        makeJSONString([
            "type": "start_quiz",
            "room_code": roomCode
        ])
    }

    static func submitAnswer(
        roomCode: String,
        username: String,
        answerIndex: Int
    ) -> String {
        makeJSONString([
            "type": "submit_answer",
            "room_code": roomCode,
            "username": username,
            "answer_index": answerIndex
        ])
    }

    private static func makeJSONString(_ dictionary: [String: Any]) -> String {
        do {
            let data = try JSONSerialization.data(withJSONObject: dictionary)
            return String(data: data, encoding: .utf8) ?? "{}"
        } catch {
            return "{}"
        }
    }
}
