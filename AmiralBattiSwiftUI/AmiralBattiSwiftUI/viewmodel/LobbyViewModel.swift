//
//  LobbyViewModel.swift
//  AmiralBattiSwiftUI
//
//  Created by Alperen Saraç on 12.04.2026.
//

import Foundation
import SwiftUI

final class LobbyViewModel: ObservableObject {

    @Published var playerName: String = ""
    @Published var roomCode: String = ""

    @Published var roomInfoText: String = "Oda: -"
    @Published var playersText: String = "Oyuncular: -"
    @Published var statusText: String = "Durum: Hazır"

    @Published var currentRoomCode: String = ""
    @Published var currentPlayerId: String = ""
    @Published var shouldNavigateToPlacement: Bool = false

    private let socketManager = SocketManager.shared
    private let decoder = JSONDecoder()

    
    init() {
        
    }

    func activateSocketListener() {
        socketManager.setDelegate(self)
    }

    func deactivateSocketListener() {
        socketManager.clearDelegate(self)
    }
    
    func connectToServer() {
        statusText = "Durum: Sunucuya bağlanılıyor..."
        socketManager.setDelegate(self)
        socketManager.connect()
    }

    func createRoom() {
        let trimmedName = playerName.trimmingCharacters(in: .whitespacesAndNewlines)

        guard !trimmedName.isEmpty else {
            statusText = "Hata: Oyuncu adı gir"
            return
        }

        let message: [String: Any] = [
            "type": "CREATE_ROOM",
            "data": [
                "playerName": trimmedName
            ]
        ]

        socketManager.send(dictionary: message)
    }

    func joinRoom() {
        let trimmedName = playerName.trimmingCharacters(in: .whitespacesAndNewlines)
        let trimmedRoomCode = roomCode.trimmingCharacters(in: .whitespacesAndNewlines)

        guard !trimmedName.isEmpty, !trimmedRoomCode.isEmpty else {
            statusText = "Hata: Oyuncu adı ve oda kodu gir"
            return
        }

        let message: [String: Any] = [
            "type": "JOIN_ROOM",
            "data": [
                "playerName": trimmedName,
                "roomCode": trimmedRoomCode
            ]
        ]

        socketManager.send(dictionary: message)
    }

    func consumePlacementNavigation() {
        shouldNavigateToPlacement = false
    }

    private func handleSocketMessage(_ text: String) {
        guard let jsonData = text.data(using: .utf8),
              let jsonObject = try? JSONSerialization.jsonObject(with: jsonData, options: []),
              let jsonDict = jsonObject as? [String: Any],
              let type = jsonDict["type"] as? String,
              let dataDict = jsonDict["data"] as? [String: Any],
              let data = try? JSONSerialization.data(withJSONObject: dataDict, options: []) else {
            statusText = "Mesaj ayrıştırılamadı"
            return
        }

        switch type {
        case "ROOM_CREATED":
            if let decoded = try? decoder.decode(RoomCreatedData.self, from: data) {
                currentRoomCode = decoded.roomCode
                currentPlayerId = decoded.playerId
                roomCode = decoded.roomCode
                roomInfoText = "Oda: \(decoded.roomCode)"
                playersText = "Oyuncular: \(formatPlayers(decoded.players))"
                statusText = decoded.message
            }

        case "JOINED_ROOM":
            if let decoded = try? decoder.decode(JoinedRoomData.self, from: data) {
                currentRoomCode = decoded.roomCode
                currentPlayerId = decoded.playerId
                roomCode = decoded.roomCode
                roomInfoText = "Oda: \(decoded.roomCode)"
                playersText = "Oyuncular: \(formatPlayers(decoded.players))"
                statusText = decoded.message
            }

        case "PLAYER_JOINED":
            if let decoded = try? decoder.decode(PlayerJoinedData.self, from: data) {
                roomInfoText = "Oda: \(decoded.roomCode)"
                playersText = "Oyuncular: \(formatPlayers(decoded.players))"
                statusText = decoded.message

                if decoded.players.count == 2 {
                    shouldNavigateToPlacement = true
                }
            }

        case "PLAYER_LEFT":
            if let decoded = try? decoder.decode(PlayerJoinedData.self, from: data) {
                playersText = "Oyuncular: \(formatPlayers(decoded.players))"
                statusText = decoded.message
            }

        case "ERROR":
            if let decoded = try? decoder.decode(ErrorData.self, from: data) {
                statusText = "Hata: \(decoded.message)"
            }

        default:
            statusText = "Bilinmeyen mesaj: \(type)"
        }
    }

    private func formatPlayers(_ players: [PlayerInfo]) -> String {
        guard !players.isEmpty else { return "-" }
        return players.map { $0.name }.joined(separator: " | ")
    }
}
extension LobbyViewModel: SocketManagerDelegate {
    func socketDidConnect() {
        statusText = "Durum: Sunucuya bağlandı"
    }

    func socketDidDisconnect() {
        statusText = "Durum: Bağlantı kesildi"
    }

    func socketDidReceiveMessage(_ text: String) {
        handleSocketMessage(text)
    }

    func socketDidReceiveError(_ errorMessage: String) {
        statusText = "Hata: \(errorMessage)"
    }
}
