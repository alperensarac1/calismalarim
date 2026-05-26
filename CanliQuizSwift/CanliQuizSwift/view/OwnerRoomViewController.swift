//
//  OwnerRoomViewController.swift
//  CanliQuizSwift
//
//  Created by Alperen Saraç on 23.05.2026.
//

import Foundation
import UIKit

final class OwnerRoomViewController: UIViewController {

    /*
        Oda sahibi ekranı.

        Görevleri:
        1. Oda kodunu göstermek
        2. Oyuncu listesini göstermek
        3. Dinamik şık eklemek
        4. Doğru cevabı seçmek
        5. Soruyu server'a göndermek
        6. Quizi başlatmak
    */

    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var roomCodeLabel: UILabel!
    @IBOutlet weak var infoLabel: UILabel!
    @IBOutlet weak var playersLabel: UILabel!

    @IBOutlet weak var questionTextView: UITextView!
    @IBOutlet weak var optionsStackView: UIStackView!

    @IBOutlet weak var addOptionButton: UIButton!
    @IBOutlet weak var addQuestionButton: UIButton!
    @IBOutlet weak var startQuizButton: UIButton!

    @IBOutlet weak var questionCountLabel: UILabel!
    @IBOutlet weak var statusLabel: UILabel!

    var roomCode: String = ""
    var username: String = ""
    var questionTime: Int = 20

    private var questionCount: Int = 0

    /*
        Dinamik şık satırlarını burada tutuyoruz.
    */
    private var optionRows: [OptionRowView] = []

    /*
        Doğru cevap olarak seçilen şıkkın index değeri.
        Hiç seçim yoksa -1.
    */
    private var selectedCorrectIndex: Int = -1

    override func viewDidLoad() {
        super.viewDidLoad()

        configureUI()
        configureInitialOptions()

        WebSocketManager.shared.delegate = self
    }

    deinit {
        if WebSocketManager.shared.delegate === self {
            WebSocketManager.shared.delegate = nil
        }
    }

    private func configureUI() {
        title = "Oda Sahibi"

        view.backgroundColor = UIColor(
            red: 248 / 255,
            green: 250 / 255,
            blue: 252 / 255,
            alpha: 1
        )

        titleLabel.text = "Oda Sahibi Paneli"
        titleLabel.font = UIFont.boldSystemFont(ofSize: 27)
        titleLabel.textColor = UIColor(red: 17/255, green: 24/255, blue: 39/255, alpha: 1)

        roomCodeLabel.text = "Oda Kodu: \(roomCode)"
        roomCodeLabel.font = UIFont.boldSystemFont(ofSize: 24)
        roomCodeLabel.textColor = UIColor.systemPurple

        infoLabel.text = """
        Kullanıcı: \(username)
        Soru Süresi: \(questionTime) saniye

        Bu kodu diğer kullanıcılara ver.
        """
        infoLabel.numberOfLines = 0
        infoLabel.font = UIFont.systemFont(ofSize: 15)
        infoLabel.textColor = UIColor(red: 55/255, green: 65/255, blue: 81/255, alpha: 1)

        playersLabel.text = "Oyuncular bekleniyor..."
        playersLabel.numberOfLines = 0
        playersLabel.font = UIFont.systemFont(ofSize: 15)
        playersLabel.textColor = UIColor(red: 17/255, green: 24/255, blue: 39/255, alpha: 1)

        questionTextView.layer.cornerRadius = 10
        questionTextView.layer.borderWidth = 1
        questionTextView.layer.borderColor = UIColor.systemGray4.cgColor
        questionTextView.font = UIFont.systemFont(ofSize: 16)
        questionTextView.textContainerInset = UIEdgeInsets(top: 12, left: 10, bottom: 12, right: 10)

        optionsStackView.axis = .vertical
        optionsStackView.spacing = 10

        styleButton(addOptionButton, title: "+ Şık Ekle")
        styleButton(addQuestionButton, title: "Soruyu Ekle")
        styleButton(startQuizButton, title: "Quizi Başlat")

        questionCountLabel.text = "Eklenen soru: 0"
        questionCountLabel.font = UIFont.systemFont(ofSize: 15)
        questionCountLabel.textColor = UIColor(red: 55/255, green: 65/255, blue: 81/255, alpha: 1)

        statusLabel.text = ""
        statusLabel.numberOfLines = 0
        statusLabel.font = UIFont.systemFont(ofSize: 15)
        statusLabel.textColor = UIColor(red: 55/255, green: 65/255, blue: 81/255, alpha: 1)
    }

    private func styleButton(_ button: UIButton, title: String) {
        button.setTitle(title, for: .normal)
        button.backgroundColor = UIColor.systemPurple
        button.tintColor = .white
        button.layer.cornerRadius = 12
        button.titleLabel?.font = UIFont.boldSystemFont(ofSize: 16)
    }

    private func configureInitialOptions() {
        /*
            En az 2 şık zorunlu olduğu için başlangıçta 2 boş şık ekliyoruz.
        */
        addOptionRow()
        addOptionRow()
    }

    @IBAction func addOptionButtonTapped(_ sender: UIButton) {
        addOptionRow()
    }

    @IBAction func addQuestionButtonTapped(_ sender: UIButton) {
        addQuestion()
    }

    @IBAction func startQuizButtonTapped(_ sender: UIButton) {
        startQuiz()
    }

    private func addOptionRow() {
        let row = OptionRowView()

        row.configure(
            index: optionRows.count,
            optionText: ""
        )

        row.onSelect = { [weak self, weak row] in
            guard let self = self, let row = row else { return }
            self.selectCorrectOption(row)
        }

        row.onDelete = { [weak self, weak row] in
            guard let self = self, let row = row else { return }
            self.deleteOptionRow(row)
        }

        optionRows.append(row)
        optionsStackView.addArrangedSubview(row)

        updateOptionIndexes()
    }

    private func selectCorrectOption(_ selectedRow: OptionRowView) {
        for (index, row) in optionRows.enumerated() {
            let isSelected = row === selectedRow
            row.setSelected(isSelected)

            if isSelected {
                selectedCorrectIndex = index
            }
        }
    }

    private func deleteOptionRow(_ row: OptionRowView) {
        if optionRows.count <= 2 {
            statusLabel.text = "En az 2 şık kalmalı."
            return
        }

        guard let removedIndex = optionRows.firstIndex(where: { $0 === row }) else {
            return
        }

        optionsStackView.removeArrangedSubview(row)
        row.removeFromSuperview()
        optionRows.remove(at: removedIndex)

        if selectedCorrectIndex == removedIndex {
            selectedCorrectIndex = -1
            optionRows.forEach { $0.setSelected(false) }
        } else if selectedCorrectIndex > removedIndex {
            selectedCorrectIndex -= 1
        }

        updateOptionIndexes()
    }

    private func updateOptionIndexes() {
        for (index, row) in optionRows.enumerated() {
            row.configure(index: index, optionText: row.optionText)
            row.setDeleteEnabled(optionRows.count > 2)
        }
    }

    private func addQuestion() {
        let questionText = questionTextView.text.trimmingCharacters(in: .whitespacesAndNewlines)

        guard questionText.isEmpty == false else {
            statusLabel.text = "Soru metni boş olamaz."
            return
        }

        guard selectedCorrectIndex != -1 else {
            statusLabel.text = "Doğru cevabı seçmelisin."
            return
        }

        var filledOptions: [String] = []
        var correctIndexInFilledOptions = -1

        for (originalIndex, row) in optionRows.enumerated() {
            let optionText = row.optionText.trimmingCharacters(in: .whitespacesAndNewlines)

            if optionText.isEmpty == false {
                if originalIndex == selectedCorrectIndex {
                    correctIndexInFilledOptions = filledOptions.count
                }

                filledOptions.append(optionText)
            }
        }

        guard filledOptions.count >= 2 else {
            statusLabel.text = "En az 2 dolu şık girmelisin."
            return
        }

        guard correctIndexInFilledOptions != -1 else {
            statusLabel.text = "Doğru cevap olarak seçtiğin şık boş olamaz."
            return
        }

        let message = SocketMessageFactory.addQuestion(
            roomCode: roomCode,
            questionText: questionText,
            options: filledOptions,
            correctIndex: correctIndexInFilledOptions
        )

        WebSocketManager.shared.send(message)

        statusLabel.text = "Soru gönderildi..."
    }

    private func startQuiz() {
        guard questionCount > 0 else {
            statusLabel.text = "Quiz başlatmak için en az 1 soru eklemelisin."
            return
        }

        let message = SocketMessageFactory.startQuiz(roomCode: roomCode)

        WebSocketManager.shared.send(message)

        statusLabel.text = "Quiz başlatma isteği gönderildi..."
    }

    private func clearQuestionForm() {
        questionTextView.text = ""

        optionRows.forEach { row in
            optionsStackView.removeArrangedSubview(row)
            row.removeFromSuperview()
        }

        optionRows.removeAll()
        selectedCorrectIndex = -1

        addOptionRow()
        addOptionRow()
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

            case SocketMessageType.playerListUpdated.rawValue:
                let players = json["players"] as? [String] ?? []
                playersLabel.text = buildPlayersText(players)

            case SocketMessageType.questionAdded.rawValue:
                questionCount = json["question_count"] as? Int ?? questionCount + 1
                questionCountLabel.text = "Eklenen soru: \(questionCount)"
                statusLabel.text = json["message"] as? String ?? "Soru eklendi."
                clearQuestionForm()

            case SocketMessageType.roomQuestionCountUpdated.rawValue:
                questionCount = json["question_count"] as? Int ?? questionCount
                questionCountLabel.text = "Eklenen soru: \(questionCount)"

            case SocketMessageType.quizStarted.rawValue:
                let vc = QuizViewController(
                    nibName: "QuizViewController",
                    bundle: nil
                )

                vc.roomCode = roomCode
                vc.username = username
                vc.questionTime = questionTime
                vc.isOwner = true

                navigationController?.pushViewController(vc, animated: true)

            case SocketMessageType.error.rawValue:
                statusLabel.text = json["message"] as? String ?? "Bilinmeyen hata oluştu."

            default:
                break
            }

        } catch {
            statusLabel.text = "JSON okuma hatası: \(error.localizedDescription)"
        }
    }

    private func buildPlayersText(_ players: [String]) -> String {
        if players.isEmpty {
            return "Oyuncular bekleniyor..."
        }

        var text = "Oyuncular:\n\n"

        for (index, player) in players.enumerated() {
            text += "\(index + 1). \(player)\n"
        }

        return text
    }
}

extension OwnerRoomViewController: WebSocketManagerDelegate {

    func webSocketDidConnect() {}

    func webSocketDidReceiveMessage(_ message: String) {
        handleSocketMessage(message)
    }

    func webSocketDidDisconnect() {
        statusLabel.text = "Sunucu bağlantısı kapandı."
    }

    func webSocketDidReceiveError(_ error: String) {
        statusLabel.text = "Bağlantı hatası: \(error)"
    }
}
