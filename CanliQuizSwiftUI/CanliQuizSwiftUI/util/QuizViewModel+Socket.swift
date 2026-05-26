//
//  QuizViewModel+Socket.swift
//  CanliQuizSwiftUI
//
//  Created by Alperen Saraç on 24.05.2026.
//

import Foundation

extension QuizViewModel: WebSocketManagerDelegate {

    func didConnect() {
        statusText = "Sunucuya bağlandı."

        switch pendingAction {
        case .createRoom:
            sendCreateRoom()

        case .joinRoom:
            sendJoinRoom()

        case .none:
            break
        }
    }

    func didReceiveMessage(_ message: String) {
        guard let data = message.data(using: .utf8) else { return }

        do {
            guard let json = try JSONSerialization.jsonObject(with: data) as? [String: Any] else {
                return
            }

            let type = json["type"] as? String ?? ""

            switch type {
            case "room_created":
                handleRoomCreated(json)

            case "room_joined":
                handleRoomJoined(json)

            case "player_list_updated":
                let players = json["players"] as? [String] ?? []
                playersText = buildPlayersText(players)

            case "question_added":
                questionCount = json["question_count"] as? Int ?? questionCount + 1
                statusText = json["message"] as? String ?? "Soru eklendi."

            case "room_question_count_updated":
                questionCount = json["question_count"] as? Int ?? questionCount

            case "quiz_started":
                path.append(.quiz)

            case "new_question":
                handleNewQuestion(json)

            case "answer_result":
                handleAnswerResult(json)

            case "scoreboard_updated":
                let scoreboard = json["scoreboard"] as? [[String: Any]] ?? []
                scoreboardText = buildScoreboardText(scoreboard)

            case "time_up":
                handleTimeUp(json)

            case "quiz_finished":
                handleQuizFinished(json)

            case "answer_rejected":
                answerResultText = json["message"] as? String ?? "Cevap reddedildi."

            case "error":
                statusText = json["message"] as? String ?? "Bilinmeyen hata oluştu."
                answerResultText = statusText

            default:
                break
            }

        } catch {
            statusText = "JSON okuma hatası: \(error.localizedDescription)"
        }
    }

    func didDisconnect() {
        statusText = "Sunucu bağlantısı kapandı."
    }

    func didReceiveError(_ error: String) {
        statusText = "Bağlantı hatası: \(error)"
    }

    func handleRoomCreated(_ json: [String: Any]) {
        roomCode = json["room_code"] as? String ?? ""
        username = json["username"] as? String ?? pendingUsername
        questionTime = json["question_time"] as? Int ?? pendingQuestionTime

        questionCount = 0
        playersText = "Oyuncular bekleniyor..."

        path.append(.ownerRoom)
    }

    func handleRoomJoined(_ json: [String: Any]) {
        roomCode = json["room_code"] as? String ?? pendingRoomCode
        username = json["username"] as? String ?? pendingUsername
        questionTime = json["question_time"] as? Int ?? 20

        playersText = "Oyuncular yükleniyor..."

        path.append(.waitingRoom)
    }

    func handleNewQuestion(_ json: [String: Any]) {
        timer?.invalidate()

        let options = json["options"] as? [String] ?? []

        let question = QuestionData(
            questionNumber: json["question_number"] as? Int ?? 0,
            totalQuestions: json["total_questions"] as? Int ?? 0,
            questionText: json["question_text"] as? String ?? "",
            options: options,
            questionTime: json["question_time"] as? Int ?? questionTime
        )

        currentQuestion = question
        remainingTime = question.questionTime
        selectedAnswerIndex = -1
        correctAnswerIndex = -1
        answeredCurrentQuestion = false
        answerResultText = ""

        let scoreboard = json["scoreboard"] as? [[String: Any]] ?? []
        scoreboardText = buildScoreboardText(scoreboard)

        startTimer(seconds: question.questionTime)
    }

    func handleAnswerResult(_ json: [String: Any]) {
        let isCorrect = json["is_correct"] as? Bool ?? false
        let earnedScore = json["earned_score"] as? Int ?? 0
        let totalScore = json["total_score"] as? Int ?? 0

        if isCorrect {
            answerResultText = "Doğru cevap! +\(earnedScore) puan | Toplam: \(totalScore)"
        } else {
            answerResultText = "Yanlış cevap. Puan kazanamadın."
        }
    }

    func handleTimeUp(_ json: [String: Any]) {
        timer?.invalidate()

        correctAnswerIndex = json["correct_index"] as? Int ?? -1
        remainingTime = 0

        if correctAnswerIndex >= 0 {
            answerResultText = "Süre bitti. Doğru cevap: \(indexToLetter(correctAnswerIndex))"
        } else {
            answerResultText = "Süre bitti."
        }

        let scoreboard = json["scoreboard"] as? [[String: Any]] ?? []
        scoreboardText = buildScoreboardText(scoreboard)
    }

    func handleQuizFinished(_ json: [String: Any]) {
        timer?.invalidate()

        let winners = json["winners"] as? [[String: Any]] ?? []
        let scoreboard = json["scoreboard"] as? [[String: Any]] ?? []

        winnersText = buildWinnersText(winners)
        finalScoreboardText = buildScoreboardText(scoreboard)

        path.append(.winner)
    }

    func startTimer(seconds: Int) {
        remainingTime = seconds

        timer = Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { [weak self] timer in
            guard let self else { return }

            if self.remainingTime <= 0 {
                timer.invalidate()
                return
            }

            self.remainingTime -= 1
        }
    }
}
