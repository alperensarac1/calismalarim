//
//  JoinRoomViewController.swift
//  CanliQuizSwift
//
//  Created by Alperen Saraç on 23.05.2026.
//

import Foundation
import UIKit

final class JoinRoomViewController: UIViewController {

    /*
        Odaya katılma ekranı.

        Akış:
        1. Kullanıcı adını girer
        2. Oda kodunu girer
        3. WebSocket bağlantısı kurulur
        4. join_room mesajı Python server'a gönderilir
        5. room_joined cevabı gelirse WaitingRoomViewController açılır
    */

    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var usernameTextField: UITextField!
    @IBOutlet weak var roomCodeTextField: UITextField!
    @IBOutlet weak var joinButton: UIButton!
    @IBOutlet weak var statusLabel: UILabel!

    private var pendingUsername: String = ""
    private var pendingRoomCode: String = ""
    private var shouldSendJoinRoomAfterConnect = false

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
        title = "Odaya Giriş"

        view.backgroundColor = UIColor(
            red: 248 / 255,
            green: 250 / 255,
            blue: 252 / 255,
            alpha: 1
        )

        titleLabel.text = "Odaya Giriş Yap"
        titleLabel.font = UIFont.boldSystemFont(ofSize: 28)
        titleLabel.textColor = UIColor(red: 17/255, green: 24/255, blue: 39/255, alpha: 1)

        usernameTextField.placeholder = "Kullanıcı adı"
        usernameTextField.borderStyle = .roundedRect
        usernameTextField.autocapitalizationType = .none

        roomCodeTextField.placeholder = "Oda kodu"
        roomCodeTextField.borderStyle = .roundedRect
        roomCodeTextField.keyboardType = .numberPad

        joinButton.setTitle("Odaya Katıl", for: .normal)
        joinButton.backgroundColor = UIColor.systemPurple
        joinButton.tintColor = .white
        joinButton.layer.cornerRadius = 12
        joinButton.titleLabel?.font = UIFont.boldSystemFont(ofSize: 17)

        statusLabel.text = ""
        statusLabel.font = UIFont.systemFont(ofSize: 15)
        statusLabel.textColor = UIColor(red: 55/255, green: 65/255, blue: 81/255, alpha: 1)
        statusLabel.numberOfLines = 0
    }

    @IBAction func joinButtonTapped(_ sender: UIButton) {
        joinRoom()
    }

    private func joinRoom() {
        let username = usernameTextField.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        let roomCode = roomCodeTextField.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""

        guard username.isEmpty == false else {
            statusLabel.text = "Kullanıcı adı boş olamaz."
            return
        }

        guard roomCode.isEmpty == false else {
            statusLabel.text = "Oda kodu boş olamaz."
            return
        }

        pendingUsername = username
        pendingRoomCode = roomCode

        statusLabel.text = "Sunucuya bağlanılıyor..."

        if WebSocketManager.shared.isConnected {
            sendJoinRoomMessage()
        } else {
            shouldSendJoinRoomAfterConnect = true
            WebSocketManager.shared.connect()
        }
    }

    private func sendJoinRoomMessage() {
        shouldSendJoinRoomAfterConnect = false

        let message = SocketMessageFactory.joinRoom(
            roomCode: pendingRoomCode,
            username: pendingUsername
        )

        WebSocketManager.shared.send(message)

        statusLabel.text = "Odaya katılma isteği gönderildi..."
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

            if type == SocketMessageType.roomJoined.rawValue {
                let roomCode = json["room_code"] as? String ?? pendingRoomCode
                let username = json["username"] as? String ?? pendingUsername
                let questionTime = json["question_time"] as? Int ?? 20

                let vc = WaitingRoomViewController(
                    nibName: "WaitingRoomViewController",
                    bundle: nil
                )

                vc.roomCode = roomCode
                vc.username = username
                vc.questionTime = questionTime

                navigationController?.pushViewController(vc, animated: true)

            } else if type == SocketMessageType.error.rawValue {
                statusLabel.text = json["message"] as? String ?? "Bilinmeyen hata oluştu."
            }

        } catch {
            statusLabel.text = "JSON okuma hatası: \(error.localizedDescription)"
        }
    }
}

extension JoinRoomViewController: WebSocketManagerDelegate {

    func webSocketDidConnect() {
        statusLabel.text = "Sunucuya bağlandı."

        if shouldSendJoinRoomAfterConnect {
            sendJoinRoomMessage()
        }
    }

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
