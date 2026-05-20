//
//  ViewerView.swift
//  CanliYayinSwiftUI
//
//  Created by Alperen Saraç on 14.05.2026.
//

import Foundation
import SwiftUI

struct ViewerView: View {

    let room: RoomModel

    @StateObject private var socketManager = LiveSocketManager()

    @State private var statusText = "Sunucuya bağlanıyor..."
    @State private var viewerCount: Int
    @State private var liveImage: UIImage?
    @State private var messageText = ""
    @State private var chatMessages: [ChatMessageModel] = []

    init(room: RoomModel) {
        self.room = room
        _viewerCount = State(initialValue: room.viewerCount)
    }

    var body: some View {
        VStack(spacing: 10) {
            Text("İzleyici: \(viewerCount)")
                .font(.subheadline)

            Text(statusText)
                .font(.caption)
                .foregroundStyle(.secondary)

            ZStack {
                Color.black

                if let liveImage {
                    Image(uiImage: liveImage)
                        .resizable()
                        .scaledToFit()
                } else {
                    Text("Görüntü bekleniyor...")
                        .foregroundStyle(.white.opacity(0.7))
                }
            }
            .frame(height: 260)
            .clipShape(RoundedRectangle(cornerRadius: 12))

            Text("Canlı Sohbet")
                .font(.headline)
                .frame(maxWidth: .infinity, alignment: .leading)

            List(chatMessages) { chat in
                VStack(alignment: .leading, spacing: 4) {
                    Text(chat.username)
                        .font(.caption)
                        .fontWeight(.bold)
                        .foregroundStyle(.blue)

                    Text(chat.message)
                        .font(.subheadline)
                }
            }

            HStack {
                TextField("Mesaj yaz...", text: $messageText)
                    .textFieldStyle(.roundedBorder)

                Button("Gönder") {
                    sendChatMessage()
                }
            }
        }
        .padding()
        .navigationTitle(room.title)
        .onAppear {
            connectSocket()
        }
        .onDisappear {
            socketManager.disconnect()
        }
    }

    private func connectSocket() {
        socketManager.onConnected = {
            DispatchQueue.main.async {
                statusText = "Sunucuya bağlandı, odaya giriliyor..."
            }

            socketManager.sendJson([
                "type": "join_room",
                "room_id": room.id,
                "username": "SwiftUI İzleyici"
            ])
        }

        socketManager.onMessage = { message in
            guard let data = message.toJsonDictionary(),
                  let type = data["type"] as? String else {
                return
            }

            switch type {
            case "joined_room":
                DispatchQueue.main.async {
                    statusText = "Yayına bağlandı"
                }

            case "viewer_count":
                let count = data["viewer_count"] as? Int ?? 0
                DispatchQueue.main.async {
                    viewerCount = count
                }

            case "video_frame":
                handleVideoFrame(data)

            case "chat_message":
                let chat = ChatMessageModel(json: data)
                DispatchQueue.main.async {
                    chatMessages.append(chat)
                }

            case "stream_ended":
                DispatchQueue.main.async {
                    statusText = "Yayın sona erdi"
                }

            case "error":
                DispatchQueue.main.async {
                    statusText = data["message"] as? String ?? "Bilinmeyen hata"
                }

            default:
                break
            }
        }

        socketManager.onError = { error in
            DispatchQueue.main.async {
                statusText = "Hata: \(error)"
            }
        }

        socketManager.onDisconnected = {
            DispatchQueue.main.async {
                statusText = "Bağlantı kapandı"
            }
        }

        socketManager.connect(urlString: AppConfig.serverURL)
    }

    private func handleVideoFrame(_ data: [String: Any]) {
        guard let base64Frame = data["frame"] as? String,
              let imageData = Data(base64Encoded: base64Frame),
              let image = UIImage(data: imageData) else {
            return
        }

        DispatchQueue.main.async {
            liveImage = image
        }
    }

    private func sendChatMessage() {
        let message = messageText.trimmingCharacters(in: .whitespacesAndNewlines)

        guard !message.isEmpty else { return }

        socketManager.sendJson([
            "type": "chat_message",
            "message": message
        ])

        messageText = ""
    }
}
