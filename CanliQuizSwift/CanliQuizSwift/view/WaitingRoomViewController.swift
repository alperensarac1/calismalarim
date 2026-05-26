//
//  File.swift
//  CanliQuizSwift
//
//  Created by Alperen Saraç on 23.05.2026.
//

import Foundation
import UIKit

final class WaitingRoomViewController: UIViewController {

    /*
        Odaya katılan normal kullanıcının bekleme ekranı.

        Oda sahibi quizi başlatınca server "quiz_started" mesajı gönderir.
        Bu mesaj gelince QuizViewController ekranına geçilir.
    */

    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var infoLabel: UILabel!
    @IBOutlet weak var playersLabel: UILabel!
    @IBOutlet weak var statusLabel: UILabel!

    var roomCode: String = ""
    var username: String = ""
    var questionTime: Int = 20

    override func viewDidLoad() {
        super.viewDidLoad()

        configureUI()

        WebSocketManager.shared.delegate = self
    }

    deinit {
        if WebSocketManager.shared.delegate === self {
            WebSocketManager.shared.delegate = nil
        }
    }

    private func configureUI() {
        title = "Bekleme Odası"

        view.backgroundColor = UIColor(
            red: 248 / 255,
            green: 250 / 255,
            blue: 252 / 255,
            alpha: 1
        )

        titleLabel.text = "Bekleme Odası"
        titleLabel.font = UIFont.boldSystemFont(ofSize: 28)
        titleLabel.textColor = UIColor(red: 17/255, green: 24/255, blue: 39/255, alpha: 1)

        infoLabel.text = """
        Kullanıcı: \(username)
        Oda Kodu: \(roomCode)
        Soru Süresi: \(questionTime) saniye

        Oda sahibi quizi başlatınca sorular ekrana gelecek.
        """
        infoLabel.numberOfLines = 0
        infoLabel.font = UIFont.systemFont(ofSize: 16)
        infoLabel.textColor = UIColor(red: 55/255, green: 65/255, blue: 81/255, alpha: 1)

        playersLabel.text = "Oyuncular yükleniyor..."
        playersLabel.numberOfLines = 0
        playersLabel.font = UIFont.systemFont(ofSize: 15)
        playersLabel.textColor = UIColor(red: 17/255, green: 24/255, blue: 39/255, alpha: 1)

        statusLabel.text = ""
        statusLabel.numberOfLines = 0
        statusLabel.font = UIFont.boldSystemFont(ofSize: 15)
        statusLabel.textColor = UIColor.systemPurple
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

            case SocketMessageType.quizStarted.rawValue:
                statusLabel.text = "Quiz başladı."

                let vc = QuizViewController(
                    nibName: "QuizViewController",
                    bundle: nil
                )

                vc.roomCode = roomCode
                vc.username = username
                vc.questionTime = questionTime
                vc.isOwner = false

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
            return "Oyuncular yükleniyor..."
        }

        var text = "Odada bulunan oyuncular:\n\n"

        for (index, player) in players.enumerated() {
            text += "\(index + 1). \(player)\n"
        }

        return text
    }
}

extension WaitingRoomViewController: WebSocketManagerDelegate {

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
