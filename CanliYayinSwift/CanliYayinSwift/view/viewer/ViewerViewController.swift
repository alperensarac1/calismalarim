//
//  File.swift
//  CanliYayinSwift
//
//  Created by Alperen Saraç on 13.05.2026.
//

import Foundation
import UIKit

final class ViewerViewController: UIViewController {

    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var viewerCountLabel: UILabel!
    @IBOutlet weak var statusLabel: UILabel!
    @IBOutlet weak var liveImageView: UIImageView!
    @IBOutlet weak var chatTitleLabel: UILabel!
    @IBOutlet weak var chatTableView: UITableView!
    @IBOutlet weak var messageTextField: UITextField!
    @IBOutlet weak var sendButton: UIButton!

    var room: RoomModel!

    private var socketManager: LiveSocketManager?
    private var chatMessages: [ChatMessageModel] = []

    override func viewDidLoad() {
        super.viewDidLoad()

        configureUI()
        configureChatTableView()
        connectSocket()
    }

    private func configureUI() {
        view.backgroundColor = .systemBackground
        title = "Yayın İzle"

        titleLabel.text = room.title
        titleLabel.font = .boldSystemFont(ofSize: 22)

        viewerCountLabel.text = "İzleyici: \(room.viewerCount)"
        viewerCountLabel.font = .systemFont(ofSize: 14)

        statusLabel.text = "Sunucuya bağlanıyor..."
        statusLabel.font = .systemFont(ofSize: 14)
        statusLabel.textColor = .secondaryLabel

        liveImageView.backgroundColor = .black
        liveImageView.contentMode = .scaleAspectFit
        liveImageView.clipsToBounds = true

        chatTitleLabel.text = "Canlı Sohbet"
        chatTitleLabel.font = .boldSystemFont(ofSize: 18)

        messageTextField.placeholder = "Mesaj yaz..."
        messageTextField.borderStyle = .roundedRect

        sendButton.setTitle("Gönder", for: .normal)
    }

    private func configureChatTableView() {
        chatTableView.dataSource = self
        chatTableView.delegate = self

        let nib = UINib(
            nibName: ChatTableViewCell.identifier,
            bundle: nil
        )

        chatTableView.register(
            nib,
            forCellReuseIdentifier: ChatTableViewCell.identifier
        )

        chatTableView.rowHeight = UITableView.automaticDimension
        chatTableView.estimatedRowHeight = 60
    }

    private func connectSocket() {
        let manager = LiveSocketManager(urlString: AppConfig.serverURL)
        manager.delegate = self
        manager.connect()

        socketManager = manager
    }

    private func joinRoom() {
        socketManager?.sendJson([
            "type": "join_room",
            "room_id": room.roomId,
            "username": "iOS İzleyici"
        ])
    }

    @IBAction func sendButtonTapped(_ sender: UIButton) {
        let message = messageTextField.text?.trimmingCharacters(
            in: .whitespacesAndNewlines
        ) ?? ""

        guard !message.isEmpty else { return }

        socketManager?.sendJson([
            "type": "chat_message",
            "message": message
        ])

        messageTextField.text = ""
    }

    private func handleVideoFrame(_ data: [String: Any]) {
        guard let base64Frame = data["frame"] as? String,
              let imageData = Data(base64Encoded: base64Frame),
              let image = UIImage(data: imageData) else {
            return
        }

        DispatchQueue.main.async {
            self.liveImageView.image = image
        }
    }

    private func handleChatMessage(_ data: [String: Any]) {
        let chat = ChatMessageModel(json: data)

        DispatchQueue.main.async {
            self.chatMessages.append(chat)
            self.chatTableView.reloadData()

            let lastIndex = IndexPath(
                row: self.chatMessages.count - 1,
                section: 0
            )

            self.chatTableView.scrollToRow(
                at: lastIndex,
                at: .bottom,
                animated: true
            )
        }
    }

    private func handleViewerCount(_ data: [String: Any]) {
        let count = data["viewer_count"] as? Int ?? 0

        DispatchQueue.main.async {
            self.viewerCountLabel.text = "İzleyici: \(count)"
        }
    }

    deinit {
        socketManager?.disconnect()
    }
}

extension ViewerViewController: LiveSocketManagerDelegate {

    func socketDidConnect() {
        DispatchQueue.main.async {
            self.statusLabel.text = "Sunucuya bağlandı, odaya giriliyor..."
        }

        joinRoom()
    }

    func socketDidReceiveMessage(_ message: String) {
        guard let data = message.toJsonDictionary(),
              let type = data["type"] as? String else {
            return
        }

        switch type {
        case "joined_room":
            DispatchQueue.main.async {
                self.statusLabel.text = "Yayına bağlandı"
            }

        case "viewer_count":
            handleViewerCount(data)

        case "video_frame":
            handleVideoFrame(data)

        case "chat_message":
            handleChatMessage(data)

        case "stream_ended":
            DispatchQueue.main.async {
                self.statusLabel.text = "Yayın sona erdi"
            }

        case "error":
            DispatchQueue.main.async {
                self.statusLabel.text = data["message"] as? String ?? "Bilinmeyen hata"
            }

        default:
            break
        }
    }

    func socketDidDisconnect() {
        DispatchQueue.main.async {
            self.statusLabel.text = "Bağlantı kapandı"
        }
    }

    func socketDidReceiveError(_ error: String) {
        DispatchQueue.main.async {
            self.statusLabel.text = "Hata: \(error)"
        }
    }
}

extension ViewerViewController: UITableViewDataSource, UITableViewDelegate {

    func tableView(
        _ tableView: UITableView,
        numberOfRowsInSection section: Int
    ) -> Int {
        return chatMessages.count
    }

    func tableView(
        _ tableView: UITableView,
        cellForRowAt indexPath: IndexPath
    ) -> UITableViewCell {
        guard let cell = tableView.dequeueReusableCell(
            withIdentifier: ChatTableViewCell.identifier,
            for: indexPath
        ) as? ChatTableViewCell else {
            return UITableViewCell()
        }

        cell.configure(with: chatMessages[indexPath.row])
        return cell
    }
}
