//
//  RoomListView.swift
//  CanliYayinSwiftUI
//
//  Created by Alperen Saraç on 14.05.2026.
//

import Foundation
import SwiftUI

struct RoomListView: View {

    @StateObject private var socketManager = LiveSocketManager()

    @State private var rooms: [RoomModel] = []
    @State private var statusText = "Sunucuya bağlanıyor..."

    var body: some View {
        VStack {
            Text(statusText)
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .padding(.top, 8)

            if rooms.isEmpty {
                Spacer()

                Text("Aktif yayın yok")
                    .foregroundStyle(.secondary)

                Spacer()
            } else {
                List(rooms) { room in
                    NavigationLink {
                        ViewerView(room: room)
                    } label: {
                        RoomRowView(room: room)
                    }
                }
            }
        }
        .navigationTitle("Aktif Yayınlar")
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
                statusText = "Sunucuya bağlandı"
            }

            socketManager.sendJson([
                "type": "get_rooms"
            ])
        }

        socketManager.onMessage = { message in
            guard let data = message.toJsonDictionary(),
                  let type = data["type"] as? String else {
                return
            }

            if type == "rooms_list" {
                handleRoomsList(data)
            }

            if type == "error" {
                DispatchQueue.main.async {
                    statusText = data["message"] as? String ?? "Bilinmeyen hata"
                }
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

    private func handleRoomsList(_ data: [String: Any]) {
        guard let roomArray = data["rooms"] as? [[String: Any]] else {
            return
        }

        let parsedRooms = roomArray.map { RoomModel(json: $0) }

        DispatchQueue.main.async {
            rooms = parsedRooms

            statusText = parsedRooms.isEmpty
                ? "Aktif yayın yok"
                : "\(parsedRooms.count) aktif yayın var"
        }
    }
}

struct RoomRowView: View {

    let room: RoomModel

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(room.title)
                .font(.headline)

            Text("Yayıncı: \(room.broadcasterName)")
                .font(.subheadline)

            Text("İzleyici: \(room.viewerCount)")
                .font(.subheadline)

            Text("Başlama: \(room.createdAt)")
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .padding(.vertical, 6)
    }
}
