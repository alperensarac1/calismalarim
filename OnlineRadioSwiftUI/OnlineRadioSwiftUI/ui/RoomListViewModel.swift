//
//  RoomListViewModel.swift
//  OnlineRadioSwiftUI
//
//  Created by Alperen Saraç on 2.05.2026.
//

import Foundation

final class RoomListViewModel: ObservableObject {

    @Published var rooms: [RadioRoom] = []
    @Published var statusText: String = "Sunucuya bağlanılıyor..."

    private let socket = RadioSocketManager.shared

    func start() {
        socket.onConnected = { [weak self] in
            self?.statusText = "Sunucuya bağlandı"
            self?.socket.getRooms()
        }

        socket.onMessage = { [weak self] message in
            self?.handleMessage(message)
        }

        socket.onError = { [weak self] error in
            self?.statusText = "Hata: \(error)"
        }

        socket.connect()
        socket.getRooms()
    }

    func refreshRooms() {
        socket.getRooms()
    }

    private func handleMessage(_ message: String) {
        guard let data = message.data(using: .utf8),
              let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
              let type = json["type"] as? String else {
            return
        }

        if type == "ROOM_LIST" || type == "ROOM_UPDATED" {
            guard let roomArray = json["rooms"] as? [[String: Any]] else {
                return
            }

            let parsedRooms: [RadioRoom] = roomArray.compactMap { item in
                guard let id = item["id"] as? Int,
                      let roomName = item["roomName"] as? String else {
                    return nil
                }

                return RadioRoom(
                    id: id,
                    roomName: roomName,
                    currentMusic: item["currentMusic"] as? String,
                    isPlaying: item["isPlaying"] as? Bool ?? false,
                    listenerCount: item["listenerCount"] as? Int ?? 0
                )
            }

            self.rooms = parsedRooms
        }
    }
}
