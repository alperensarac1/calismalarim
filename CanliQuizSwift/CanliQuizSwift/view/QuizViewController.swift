//
//  QuizViewController.swift
//  CanliQuizSwift
//
//  Created by Alperen Saraç on 23.05.2026.
//

import Foundation
import UIKit

final class QuizViewController: UIViewController {

    /*
        Quiz ekranı.

        Görevleri:
        1. Server'dan gelen new_question mesajını göstermek
        2. Şıkları buton olarak üretmek
        3. Cevap gönderme
        4. Süre göstermek
        5. Doğru/yanlış renklendirmek
        6. Quiz bitince WinnerViewController'a geçmek
    */

    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var questionCounterLabel: UILabel!
    @IBOutlet weak var timerLabel: UILabel!
    @IBOutlet weak var questionLabel: UILabel!
    @IBOutlet weak var optionsStackView: UIStackView!
    @IBOutlet weak var answerResultLabel: UILabel!
    @IBOutlet weak var scoreboardLabel: UILabel!

    var roomCode: String = ""
    var username: String = ""
    var questionTime: Int = 20
    var isOwner: Bool = false

    private var answeredCurrentQuestion = false
    private var selectedAnswerIndex = -1
    private var currentCorrectIndex = -1

    private var optionButtons: [UIButton] = []

    private var timer: Timer?
    private var remainingTime: Int = 20

    override func viewDidLoad() {
        super.viewDidLoad()

        configureUI()

        WebSocketManager.shared.delegate = self
    }

    deinit {
        timer?.invalidate()

        if WebSocketManager.shared.delegate === self {
            WebSocketManager.shared.delegate = nil
        }
    }

    private func configureUI() {
        title = "Quiz"

        view.backgroundColor = UIColor(
            red: 248 / 255,
            green: 250 / 255,
            blue: 252 / 255,
            alpha: 1
        )

        titleLabel.text = "Quiz"
        titleLabel.font = UIFont.boldSystemFont(ofSize: 27)
        titleLabel.textColor = UIColor(red: 17/255, green: 24/255, blue: 39/255, alpha: 1)

        questionCounterLabel.text = "Soru bekleniyor..."
        questionCounterLabel.font = UIFont.systemFont(ofSize: 15)
        questionCounterLabel.textColor = UIColor(red: 107/255, green: 114/255, blue: 128/255, alpha: 1)

        timerLabel.text = "Süre: \(questionTime)"
        timerLabel.font = UIFont.boldSystemFont(ofSize: 24)
        timerLabel.textColor = UIColor.systemRed

        questionLabel.text = "Quiz başladı. İlk soru bekleniyor."
        questionLabel.numberOfLines = 0
        questionLabel.font = UIFont.boldSystemFont(ofSize: 21)
        questionLabel.textColor = UIColor(red: 17/255, green: 24/255, blue: 39/255, alpha: 1)

        optionsStackView.axis = .vertical
        optionsStackView.spacing = 14

        answerResultLabel.text = ""
        answerResultLabel.numberOfLines = 0
        answerResultLabel.font = UIFont.boldSystemFont(ofSize: 17)
        answerResultLabel.textColor = UIColor(red: 55/255, green: 65/255, blue: 81/255, alpha: 1)

        scoreboardLabel.text = "Puan tablosu bekleniyor..."
        scoreboardLabel.numberOfLines = 0
        scoreboardLabel.font = UIFont.systemFont(ofSize: 15)
        scoreboardLabel.textColor = UIColor(red: 17/255, green: 24/255, blue: 39/255, alpha: 1)
    }

    private func handleSocketMessage(_ message: String) {
        guard let data = message.data(using: .utf8) else {
            return
        }

        do {
            guard let json = try JSONSerialization.jsonObject(with: data) as? [String: Any] else {
                return
            }

            let type = json["type"] as? String ?? ""

            switch type {

            case SocketMessageType.newQuestion.rawValue:
                handleNewQuestion(json)

            case SocketMessageType.answerResult.rawValue:
                handleAnswerResult(json)

            case SocketMessageType.scoreboardUpdated.rawValue:
                let scoreboard = json["scoreboard"] as? [[String: Any]] ?? []
                scoreboardLabel.text = buildScoreboardText(scoreboard)

            case SocketMessageType.timeUp.rawValue:
                handleTimeUp(json)

            case SocketMessageType.quizFinished.rawValue:
                handleQuizFinished(json)

            case SocketMessageType.answerRejected.rawValue:
                answerResultLabel.text = json["message"] as? String ?? "Cevap reddedildi."

            case SocketMessageType.error.rawValue:
                answerResultLabel.text = json["message"] as? String ?? "Bilinmeyen hata oluştu."

            default:
                break
            }

        } catch {
            answerResultLabel.text = "JSON okuma hatası: \(error.localizedDescription)"
        }
    }

    private func handleNewQuestion(_ json: [String: Any]) {
        answeredCurrentQuestion = false
        selectedAnswerIndex = -1
        currentCorrectIndex = -1

        timer?.invalidate()

        answerResultLabel.text = ""

        let questionNumber = json["question_number"] as? Int ?? 0
        let totalQuestions = json["total_questions"] as? Int ?? 0
        let questionText = json["question_text"] as? String ?? ""
        let options = json["options"] as? [String] ?? []
        let serverQuestionTime = json["question_time"] as? Int ?? questionTime
        let scoreboard = json["scoreboard"] as? [[String: Any]] ?? []

        questionTime = serverQuestionTime
        remainingTime = serverQuestionTime

        questionCounterLabel.text = "Soru \(questionNumber) / \(totalQuestions)"
        questionLabel.text = questionText
        scoreboardLabel.text = buildScoreboardText(scoreboard)

        renderOptions(options)
        startLocalTimer(seconds: serverQuestionTime)
    }

    private func renderOptions(_ options: [String]) {
        optionButtons.forEach { button in
            optionsStackView.removeArrangedSubview(button)
            button.removeFromSuperview()
        }

        optionButtons.removeAll()

        for (index, option) in options.enumerated() {
            let button = UIButton(type: .system)

            button.setTitle("\(indexToLetter(index))) \(option)", for: .normal)
            button.titleLabel?.font = UIFont.boldSystemFont(ofSize: 16)
            button.contentHorizontalAlignment = .center
            button.layer.cornerRadius = 14
            button.layer.borderWidth = 1
            button.heightAnchor.constraint(equalToConstant: 58).isActive = true

            applyButtonStyle(
                button,
                backgroundColor: .white,
                borderColor: UIColor(red: 209/255, green: 213/255, blue: 219/255, alpha: 1),
                textColor: UIColor(red: 17/255, green: 24/255, blue: 39/255, alpha: 1)
            )

            button.tag = index
            button.addTarget(self, action: #selector(optionButtonTapped(_:)), for: .touchUpInside)

            optionButtons.append(button)
            optionsStackView.addArrangedSubview(button)
        }
    }

    @objc private func optionButtonTapped(_ sender: UIButton) {
        submitAnswer(sender.tag)
    }

    private func submitAnswer(_ answerIndex: Int) {
        if answeredCurrentQuestion {
            answerResultLabel.text = "Bu soruya zaten cevap verdin."
            return
        }

        answeredCurrentQuestion = true
        selectedAnswerIndex = answerIndex

        markOptionAsWaiting(answerIndex)
        setOptionButtonsEnabled(false)

        let message = SocketMessageFactory.submitAnswer(
            roomCode: roomCode,
            username: username,
            answerIndex: answerIndex
        )

        WebSocketManager.shared.send(message)

        answerResultLabel.text = "Cevabın gönderildi..."
    }

    private func handleAnswerResult(_ json: [String: Any]) {
        let isCorrect = json["is_correct"] as? Bool ?? false
        let earnedScore = json["earned_score"] as? Int ?? 0
        let totalScore = json["total_score"] as? Int ?? 0

        if selectedAnswerIndex >= 0 {
            if isCorrect {
                markOptionAsCorrect(selectedAnswerIndex)
            } else {
                markOptionAsWrong(selectedAnswerIndex)
            }
        }

        if isCorrect {
            answerResultLabel.text = "Doğru cevap! +\(earnedScore) puan | Toplam: \(totalScore)"
        } else {
            answerResultLabel.text = "Yanlış cevap. Puan kazanamadın."
        }
    }

    private func handleTimeUp(_ json: [String: Any]) {
        timer?.invalidate()

        setOptionButtonsEnabled(false)

        currentCorrectIndex = json["correct_index"] as? Int ?? -1
        let scoreboard = json["scoreboard"] as? [[String: Any]] ?? []

        timerLabel.text = "Süre bitti"

        if currentCorrectIndex >= 0 {
            markOptionAsCorrect(currentCorrectIndex)
        }

        if selectedAnswerIndex >= 0 &&
            currentCorrectIndex >= 0 &&
            selectedAnswerIndex != currentCorrectIndex {

            markOptionAsWrong(selectedAnswerIndex)
        }

        if currentCorrectIndex >= 0 {
            answerResultLabel.text = "Süre bitti. Doğru cevap: \(indexToLetter(currentCorrectIndex))"
        } else {
            answerResultLabel.text = "Süre bitti."
        }

        scoreboardLabel.text = buildScoreboardText(scoreboard)
    }

    private func handleQuizFinished(_ json: [String: Any]) {
        timer?.invalidate()

        let winners = json["winners"] as? [[String: Any]] ?? []
        let scoreboard = json["scoreboard"] as? [[String: Any]] ?? []

        let vc = WinnerViewController(
            nibName: "WinnerViewController",
            bundle: nil
        )

        vc.winners = winners
        vc.scoreboard = scoreboard

        navigationController?.pushViewController(vc, animated: true)
    }

    private func startLocalTimer(seconds: Int) {
        timer?.invalidate()

        remainingTime = seconds
        timerLabel.text = "Süre: \(remainingTime)"

        timer = Timer.scheduledTimer(
            withTimeInterval: 1,
            repeats: true
        ) { [weak self] timer in
            guard let self = self else { return }

            self.remainingTime -= 1

            if self.remainingTime <= 0 {
                self.timerLabel.text = "Süre bitti"
                self.setOptionButtonsEnabled(false)
                timer.invalidate()
            } else {
                self.timerLabel.text = "Süre: \(self.remainingTime)"
            }
        }
    }

    private func setOptionButtonsEnabled(_ enabled: Bool) {
        optionButtons.forEach { button in
            button.isEnabled = enabled
            button.alpha = enabled ? 1.0 : 0.85
        }
    }

    private func markOptionAsWaiting(_ index: Int) {
        guard let button = optionButtons[safe: index] else { return }

        applyButtonStyle(
            button,
            backgroundColor: UIColor(red: 254/255, green: 243/255, blue: 199/255, alpha: 1),
            borderColor: UIColor(red: 245/255, green: 158/255, blue: 11/255, alpha: 1),
            textColor: UIColor(red: 146/255, green: 64/255, blue: 14/255, alpha: 1)
        )
    }

    private func markOptionAsCorrect(_ index: Int) {
        guard let button = optionButtons[safe: index] else { return }

        applyButtonStyle(
            button,
            backgroundColor: UIColor(red: 220/255, green: 252/255, blue: 231/255, alpha: 1),
            borderColor: UIColor(red: 22/255, green: 163/255, blue: 74/255, alpha: 1),
            textColor: UIColor(red: 22/255, green: 101/255, blue: 52/255, alpha: 1)
        )
    }

    private func markOptionAsWrong(_ index: Int) {
        guard let button = optionButtons[safe: index] else { return }

        applyButtonStyle(
            button,
            backgroundColor: UIColor(red: 254/255, green: 226/255, blue: 226/255, alpha: 1),
            borderColor: UIColor(red: 220/255, green: 38/255, blue: 38/255, alpha: 1),
            textColor: UIColor(red: 153/255, green: 27/255, blue: 27/255, alpha: 1)
        )
    }

    private func applyButtonStyle(
        _ button: UIButton,
        backgroundColor: UIColor,
        borderColor: UIColor,
        textColor: UIColor
    ) {
        button.backgroundColor = backgroundColor
        button.layer.borderColor = borderColor.cgColor
        button.setTitleColor(textColor, for: .normal)
        button.setTitleColor(textColor, for: .disabled)
    }

    private func buildScoreboardText(_ scoreboard: [[String: Any]]) -> String {
        if scoreboard.isEmpty {
            return "Puan tablosu bekleniyor..."
        }

        var text = "Puan Tablosu:\n\n"

        for (index, item) in scoreboard.enumerated() {
            let name = item["username"] as? String ?? "-"
            let score = item["score"] as? Int ?? 0

            text += "\(index + 1). \(name) - \(score) puan\n"
        }

        return text
    }

    private func indexToLetter(_ index: Int) -> String {
        if index >= 0 && index < 26 {
            let scalar = UnicodeScalar(65 + index)!
            return String(Character(scalar))
        }

        return "\(index + 1)"
    }
}

extension QuizViewController: WebSocketManagerDelegate {

    func webSocketDidConnect() {}

    func webSocketDidReceiveMessage(_ message: String) {
        handleSocketMessage(message)
    }

    func webSocketDidDisconnect() {
        answerResultLabel.text = "Sunucu bağlantısı kapandı."
    }

    func webSocketDidReceiveError(_ error: String) {
        answerResultLabel.text = "Bağlantı hatası: \(error)"
    }
}

extension Array {
    subscript(safe index: Int) -> Element? {
        if indices.contains(index) {
            return self[index]
        }

        return nil
    }
}
