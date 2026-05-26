//
//  QuizViewModel.swift
//  CanliQuizSwiftUI
//
//  Created by Alperen Saraç on 24.05.2026.
//

import Foundation
import SwiftUI

final class QuizViewModel: ObservableObject {

    @Published var path: [AppScreen] = []

    @Published var statusText = ""

    @Published var roomCode = ""
    @Published var username = ""
    @Published var questionTime = 20

    @Published var playersText = "Oyuncular bekleniyor..."
    @Published var questionCount = 0

    @Published var currentQuestion: QuestionData?
    @Published var remainingTime = 20

    @Published var selectedAnswerIndex = -1
    @Published var correctAnswerIndex = -1
    @Published var answeredCurrentQuestion = false

    @Published var answerResultText = ""
    @Published var scoreboardText = "Puan tablosu bekleniyor..."

    @Published var winnersText = ""
    @Published var finalScoreboardText = ""

    var pendingUsername = ""
    var pendingRoomCode = ""
    var pendingQuestionTime = 20

    enum PendingAction {
        case none
        case createRoom
        case joinRoom
    }

    var pendingAction: PendingAction = .none
    var timer: Timer?

    init() {
        WebSocketManager.shared.delegate = self
    }

    func goHome() {
        path.removeAll()
    }

    func openCreateRoom() {
        statusText = ""
        path.append(.createRoom)
    }

    func openJoinRoom() {
        statusText = ""
        path.append(.joinRoom)
    }

    func createRoom(username: String, questionTime: Int) {
        let cleanUsername = username.trimmingCharacters(in: .whitespacesAndNewlines)

        guard !cleanUsername.isEmpty else {
            statusText = "Kullanıcı adı boş olamaz."
            return
        }

        guard questionTime >= 5 else {
            statusText = "Süre en az 5 saniye olmalı."
            return
        }

        pendingUsername = cleanUsername
        pendingQuestionTime = questionTime
        pendingAction = .createRoom

        statusText = "Sunucuya bağlanılıyor..."

        if WebSocketManager.shared.isConnected {
            sendCreateRoom()
        } else {
            WebSocketManager.shared.connect()
        }
    }

    func sendCreateRoom() {
        pendingAction = .none

        WebSocketManager.shared.send(
            SocketMessageFactory.createRoom(
                username: pendingUsername,
                questionTime: pendingQuestionTime
            )
        )

        statusText = "Oda oluşturma isteği gönderildi..."
    }

    func joinRoom(roomCode: String, username: String) {
        let cleanRoomCode = roomCode.trimmingCharacters(in: .whitespacesAndNewlines)
        let cleanUsername = username.trimmingCharacters(in: .whitespacesAndNewlines)

        guard !cleanUsername.isEmpty else {
            statusText = "Kullanıcı adı boş olamaz."
            return
        }

        guard !cleanRoomCode.isEmpty else {
            statusText = "Oda kodu boş olamaz."
            return
        }

        pendingRoomCode = cleanRoomCode
        pendingUsername = cleanUsername
        pendingAction = .joinRoom

        statusText = "Sunucuya bağlanılıyor..."

        if WebSocketManager.shared.isConnected {
            sendJoinRoom()
        } else {
            WebSocketManager.shared.connect()
        }
    }

    func sendJoinRoom() {
        pendingAction = .none

        WebSocketManager.shared.send(
            SocketMessageFactory.joinRoom(
                roomCode: pendingRoomCode,
                username: pendingUsername
            )
        )

        statusText = "Odaya katılma isteği gönderildi..."
    }

    func addQuestion(
        questionText: String,
        options: [String],
        selectedCorrectIndex: Int
    ) {
        let cleanQuestion = questionText.trimmingCharacters(in: .whitespacesAndNewlines)

        guard !cleanQuestion.isEmpty else {
            statusText = "Soru metni boş olamaz."
            return
        }

        guard selectedCorrectIndex != -1 else {
            statusText = "Doğru cevabı seçmelisin."
            return
        }

        var filledOptions: [String] = []
        var correctIndexInFilledOptions = -1

        for (index, option) in options.enumerated() {
            let cleanOption = option.trimmingCharacters(in: .whitespacesAndNewlines)

            if !cleanOption.isEmpty {
                if index == selectedCorrectIndex {
                    correctIndexInFilledOptions = filledOptions.count
                }

                filledOptions.append(cleanOption)
            }
        }

        guard filledOptions.count >= 2 else {
            statusText = "En az 2 dolu şık girmelisin."
            return
        }

        guard correctIndexInFilledOptions != -1 else {
            statusText = "Doğru cevap boş şık olamaz."
            return
        }

        WebSocketManager.shared.send(
            SocketMessageFactory.addQuestion(
                roomCode: roomCode,
                questionText: cleanQuestion,
                options: filledOptions,
                correctIndex: correctIndexInFilledOptions
            )
        )

        statusText = "Soru gönderildi..."
    }

    func startQuiz() {
        guard questionCount > 0 else {
            statusText = "Quiz başlatmak için en az 1 soru eklemelisin."
            return
        }

        WebSocketManager.shared.send(
            SocketMessageFactory.startQuiz(roomCode: roomCode)
        )

        statusText = "Quiz başlatma isteği gönderildi..."
    }

    func submitAnswer(_ index: Int) {
        guard !answeredCurrentQuestion else {
            answerResultText = "Bu soruya zaten cevap verdin."
            return
        }

        answeredCurrentQuestion = true
        selectedAnswerIndex = index
        answerResultText = "Cevabın gönderildi..."

        WebSocketManager.shared.send(
            SocketMessageFactory.submitAnswer(
                roomCode: roomCode,
                username: username,
                answerIndex: index
            )
        )
    }

    func disconnectAndHome() {
        timer?.invalidate()
        WebSocketManager.shared.disconnect()
        path.removeAll()
    }
}
