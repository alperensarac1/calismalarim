//
//  AppModels.swift
//  CanliQuizSwift
//
//  Created by Alperen Saraç on 21.05.2026.
//

import Foundation

struct ScoreItem {
    let username: String
    let score: Int
}

struct QuestionData {
    let questionNumber: Int
    let totalQuestions: Int
    let questionText: String
    let options: [String]
    let questionTime: Int
}

enum SocketMessageType: String {
    case roomCreated = "room_created"
    case roomJoined = "room_joined"
    case playerListUpdated = "player_list_updated"
    case questionAdded = "question_added"
    case roomQuestionCountUpdated = "room_question_count_updated"
    case quizStarted = "quiz_started"
    case newQuestion = "new_question"
    case answerResult = "answer_result"
    case scoreboardUpdated = "scoreboard_updated"
    case timeUp = "time_up"
    case quizFinished = "quiz_finished"
    case answerRejected = "answer_rejected"
    case error = "error"
}
