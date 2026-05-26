//
//  Models.swift
//  CanliQuizSwiftUI
//
//  Created by Alperen Saraç on 24.05.2026.
//

import Foundation

struct QuestionData {
    let questionNumber: Int
    let totalQuestions: Int
    let questionText: String
    let options: [String]
    let questionTime: Int
}

struct ScoreItem: Identifiable {
    let id = UUID()
    let username: String
    let score: Int
}
