//
//  BroadcasterPreview.swift
//  CanliYayinSwiftUI
//
//  Created by Alperen Saraç on 14.05.2026.
//

import Foundation
import SwiftUI

struct BroadcasterView: View {

    @StateObject private var socketManager = LiveSocketManager()
    @StateObject private var cameraManager = BroadcasterCameraManager()

    @State private var statusText = "Hazırlanıyor..."
    @State private var broadcastTitle = ""
    @State private var viewerCount = 0
    @State private var roomId: String?

    @State private var messageText = ""
    @State private var chatMessages: [ChatMessageModel] = []

    var body: some View {
        VStack(spacing: 10) {

            Text(statusText)
                .font(.caption)
                .foregroundStyle(.secondary)

            TextField(
                "Yayın başlığı yaz...",
                text: $broadcastTitle
            )
            .textFieldStyle(.roundedBorder)
            .disabled(roomId != nil)

            Button(roomId == nil ? "Yayını Başlat" : "Yayın Başladı") {
                startBroadcast()
            }
            .buttonStyle(.borderedProminent)
            .disabled(roomId != nil)

            Text("İzleyici: \(viewerCount)")
                .font(.subheadline)

            CameraPreviewView(session: cameraManager.session)
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
                TextField(
                    "Mesaj yaz...",
                    text: $messageText
                )
                .textFieldStyle(.roundedBorder)

                Button("Gönder") {
                    sendChatMessage()
                }
            }

            Button("Yayını Bitir") {
                stopBroadcast()
            }
            .buttonStyle(.bordered)
            .tint(.red)
        }
        .padding()
        .navigationTitle("Yayın Aç")
        .onAppear {
            setupSocket()
            cameraManager.startSession()
        }
        .onDisappear {
            socketManager.disconnect()
            cameraManager.stopSession()
        }
    }

    private func setupSocket() {

        socketManager.onConnected = {
            DispatchQueue.main.async {
                statusText = "Sunucuya bağlandı. Başlık yazıp yayını başlat."
            }
        }

        socketManager.onMessage = { message in

            guard let data = message.toJsonDictionary(),
                  let type = data["type"] as? String else {
                return
            }

            switch type {

            case "room_created":

                DispatchQueue.main.async {
                    roomId = data["room_id"] as? String
                    statusText = "Yayın başladı"
                }

            case "viewer_count":

                let count = data["viewer_count"] as? Int ?? 0

                DispatchQueue.main.async {
                    viewerCount = count
                }

            case "chat_message":

                let chat = ChatMessageModel(json: data)

                DispatchQueue.main.async {
                    chatMessages.append(chat)
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

        cameraManager.onFrameCaptured = { image in
            sendFrame(image)
        }
    }

    private func startBroadcast() {

        let title = broadcastTitle
            .trimmingCharacters(in: .whitespacesAndNewlines)

        guard !title.isEmpty else {
            statusText = "Yayın başlığı yazmalısın"
            return
        }

        socketManager.sendJson([
            "type": "create_room",
            "title": title,
            "broadcaster_name": "SwiftUI Yayıncı"
        ])

        statusText = "Oda oluşturuluyor..."
    }

    private func sendChatMessage() {

        let message = messageText
            .trimmingCharacters(in: .whitespacesAndNewlines)

        guard !message.isEmpty else {
            return
        }

        socketManager.sendJson([
            "type": "chat_message",
            "message": message
        ])

        messageText = ""
    }

    private func sendFrame(_ image: UIImage) {

        guard roomId != nil else {
            return
        }

        guard let imageData = image.jpegData(
            compressionQuality: 0.35
        ) else {
            return
        }

        let base64Frame = imageData.base64EncodedString()

        socketManager.sendJson([
            "type": "video_frame",
            "frame": base64Frame
        ])
    }

    private func stopBroadcast() {
        socketManager.disconnect()
        cameraManager.stopSession()
    }
}
