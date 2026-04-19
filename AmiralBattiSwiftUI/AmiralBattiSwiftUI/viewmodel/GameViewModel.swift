//
//  GameViewModel.swift
//  AmiralBattiSwiftUI
//
//  Created by Alperen Saraç on 12.04.2026.
//

import Foundation
import SwiftUI

final class GameViewModel: ObservableObject {

    @Published var roomCode: String = ""
    @Published var playerId: String = ""
    @Published var playerName: String = ""

    @Published var turnText: String = "Sıra bilgisi"
    @Published var statusText: String = "Durum"

    @Published var ownBoardCells: [BoardCell] = []
    @Published var enemyBoardCells: [BoardCell] = []

    @Published var showGameOverDialog: Bool = false
    @Published var showPlayerLeftDialog: Bool = false
    @Published var gameOverWinner: Bool = false

    @Published var shouldNavigateToPlacement: Bool = false

    private let socketManager = SocketManager.shared
    private let decoder = JSONDecoder()

    private let boardSize = 10

    private var currentTurnPlayerId: String = ""
    private var firstTurnPlayerId: String = ""

    private var isFireRequestPending: Bool = false
    private var isRematchRequested: Bool = false
    private var isInitialized: Bool = false

    func initialize(
        roomCode: String,
        playerId: String,
        playerName: String,
        firstTurnPlayerId: String,
        ownBoardJson: String
    ) {
        guard !isInitialized else { return }
        isInitialized = true

        self.roomCode = roomCode
        self.playerId = playerId
        self.playerName = playerName
        self.firstTurnPlayerId = firstTurnPlayerId
        self.currentTurnPlayerId = firstTurnPlayerId

        buildOwnBoardFromJson(ownBoardJson)
        buildEnemyBoard()
        updateTurnText()
        statusText = "Oyun başladı"

    }
    func activateSocketListener() {
        socketManager.setDelegate(self)
    }

    func deactivateSocketListener() {
        socketManager.clearDelegate(self)
    }
    private func buildOwnBoardFromJson(_ json: String) {
        ownBoardCells.removeAll()

        guard let data = json.data(using: .utf8),
              let matrix = try? JSONSerialization.jsonObject(with: data, options: []) as? [[Int]] else {
            for row in 0..<boardSize {
                for col in 0..<boardSize {
                    ownBoardCells.append(BoardCell(row: row, col: col, state: .empty))
                }
            }
            return
        }

        for row in 0..<boardSize {
            for col in 0..<boardSize {
                let value = row < matrix.count && col < matrix[row].count ? matrix[row][col] : 0
                ownBoardCells.append(
                    BoardCell(
                        row: row,
                        col: col,
                        state: value == 1 ? .ship : .empty
                    )
                )
            }
        }
    }

    private func buildEnemyBoard() {
        enemyBoardCells.removeAll()

        for row in 0..<boardSize {
            for col in 0..<boardSize {
                enemyBoardCells.append(BoardCell(row: row, col: col, state: .empty))
            }
        }
    }

    private func updateTurnText() {
        turnText = currentTurnPlayerId == playerId ? "Sıra sende" : "Rakibin sırası"
    }

    func onEnemyCellTapped(row: Int, col: Int) {
        guard currentTurnPlayerId == playerId else {
            statusText = "Sıra sende değil"
            return
        }

        guard !isFireRequestPending else {
            statusText = "Önce önceki atışın sonucunu bekle"
            return
        }

        let index = row * boardSize + col
        let state = enemyBoardCells[index].state

        guard state != .hit && state != .miss else {
            statusText = "Bu hücreye zaten ateş ettin"
            return
        }

        let message: [String: Any] = [
            "type": "FIRE",
            "data": [
                "roomCode": roomCode,
                "playerId": playerId,
                "row": row,
                "col": col
            ]
        ]

        isFireRequestPending = true
        socketManager.send(dictionary: message)
        statusText = "Atış gönderildi..."
    }

    private func handleFireResult(_ result: FireResultData) {
        isFireRequestPending = false

        let index = result.row * boardSize + result.col
        guard index >= 0 && index < boardSize * boardSize else { return }

        let shooterIsMe = result.shooterPlayerId == playerId

        if shooterIsMe {
            enemyBoardCells[index].state = result.hit ? .hit : .miss
        } else {
            ownBoardCells[index].state = result.hit ? .hit : .miss
        }

        statusText = result.message

        if result.gameOver {
            gameOverWinner = (result.winnerPlayerId == playerId)
            turnText = gameOverWinner ? "Oyun bitti: Kazandın" : "Oyun bitti: Kaybettin"
            isRematchRequested = false
            showGameOverDialog = true
            return
        }

        currentTurnPlayerId = result.nextTurnPlayerId ?? ""
        updateTurnText()
    }

    func requestRematch() {
        guard !isRematchRequested else {
            statusText = "Zaten yeniden oyun isteği gönderdin"
            return
        }

        let message: [String: Any] = [
            "type": "REQUEST_REMATCH",
            "data": [
                "roomCode": roomCode,
                "playerId": playerId
            ]
        ]

        isRematchRequested = true
        showGameOverDialog = false
        socketManager.send(dictionary: message)
        statusText = "Yeniden oyun isteği gönderildi. Rakip bekleniyor..."
    }

    func dismissGameOverDialog() {
        showGameOverDialog = false
    }

    func dismissPlayerLeftDialog() {
        showPlayerLeftDialog = false
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
        case "FIRE_RESULT":
            if let decoded = try? decoder.decode(FireResultData.self, from: data) {
                handleFireResult(decoded)
            }

        case "REMATCH_STATUS":
            if let decoded = try? decoder.decode(RematchStatusData.self, from: data) {
                statusText = "\(decoded.message)\n\(formatRematchPlayers(decoded.players))"
            }

        case "REMATCH_STARTED":
            if let decoded = try? decoder.decode(RematchStartedData.self, from: data) {
                statusText = decoded.message
                showGameOverDialog = false
                shouldNavigateToPlacement = true
            }

        case "PLAYER_LEFT":
            statusText = "Rakip oyundan ayrıldı"
            showPlayerLeftDialog = true

        case "ERROR":
            if let decoded = try? decoder.decode(ErrorData.self, from: data) {
                isFireRequestPending = false
                statusText = "Hata: \(decoded.message)"
            }

        default:
            break
        }
    }

    private func formatRematchPlayers(_ players: [RematchPlayerInfo]) -> String {
        guard !players.isEmpty else { return "-" }

        return players.map {
            "\($0.name): \($0.wantsRematch ? "hazır" : "bekleniyor")"
        }
        .joined(separator: " | ")
    }
}
extension GameViewModel: SocketManagerDelegate {
    func socketDidConnect() {
        statusText = "Bağlantı aktif"
    }

    func socketDidDisconnect() {
        statusText = "Bağlantı kesildi"
    }

    func socketDidReceiveMessage(_ text: String) {
        handleSocketMessage(text)
    }

    func socketDidReceiveError(_ errorMessage: String) {
        isFireRequestPending = false
        statusText = "Hata: \(errorMessage)"
    }
}
