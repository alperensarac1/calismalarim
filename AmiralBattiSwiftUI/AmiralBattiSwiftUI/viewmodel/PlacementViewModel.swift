//
//  PlacementViewModel.swift
//  AmiralBattiSwiftUI
//
//  Created by Alperen Saraç on 12.04.2026.
//

import Foundation
import SwiftUI

final class ShipPlacementViewModel: ObservableObject {

    @Published var roomCode: String = ""
    @Published var playerId: String = ""
    @Published var playerName: String = ""

    @Published var boardCells: [BoardCell] = []
    @Published var currentShipText: String = "Seçili gemi: -"
    @Published var orientationText: String = "Yön: Yatay"
    @Published var statusText: String = "Durum: Gemileri yerleştir"
    @Published var readyEnabled: Bool = false

    @Published var shouldNavigateToGame: Bool = false
    @Published var firstTurnPlayerId: String = ""
    @Published var ownBoardJson: String = ""

    private let socketManager = SocketManager.shared
    private let decoder = JSONDecoder()

    private let boardSize = 10

    private var shipsToPlace: [Ship] = [
        Ship(size: 4, placed: false),
        Ship(size: 3, placed: false),
        Ship(size: 3, placed: false),
        Ship(size: 2, placed: false),
        Ship(size: 2, placed: false),
        Ship(size: 1, placed: false),
        Ship(size: 1, placed: false)
    ]

    private var currentShipIndex: Int = 0
    private var currentOrientation: ShipOrientation = .horizontal

    init() {
        createBoard()
        updateTexts()
    }
    func activateSocketListener() {
        socketManager.setDelegate(self)
    }

    func deactivateSocketListener() {
        socketManager.clearDelegate(self)
    }
    func initialize(roomCode: String, playerId: String, playerName: String) {
        self.roomCode = roomCode
        self.playerId = playerId
        self.playerName = playerName
        socketManager.delegate = self
    }

    func rotateShip() {
        currentOrientation = currentOrientation == .horizontal ? .vertical : .horizontal
        updateTexts()
    }

    func resetBoard() {
        currentShipIndex = 0
        currentOrientation = .horizontal

        for index in shipsToPlace.indices {
            shipsToPlace[index].placed = false
        }

        createBoard()
        readyEnabled = false
        statusText = "Durum: Gemileri yerleştir"
        updateTexts()
    }

    func onCellTapped(row: Int, col: Int) {
        guard currentShipIndex < shipsToPlace.count else { return }

        let ship = shipsToPlace[currentShipIndex]

        guard canPlaceShip(
            startRow: row,
            startCol: col,
            shipSize: ship.size,
            orientation: currentOrientation
        ) else {
            statusText = "Durum: Gemi burada konumlanamaz"
            return
        }

        for i in 0..<ship.size {
            let targetRow = currentOrientation == .vertical ? row + i : row
            let targetCol = currentOrientation == .horizontal ? col + i : col
            let index = targetRow * boardSize + targetCol
            boardCells[index].state = .ship
        }

        shipsToPlace[currentShipIndex].placed = true
        currentShipIndex += 1

        if currentShipIndex >= shipsToPlace.count {
            statusText = "Durum: Tüm gemiler yerleştirildi. Hazırım butonuna bas."
            readyEnabled = true
        } else {
            statusText = "Durum: Gemileri yerleştir"
        }

        updateTexts()
    }

    private func createBoard() {
        boardCells.removeAll()

        for row in 0..<boardSize {
            for col in 0..<boardSize {
                boardCells.append(BoardCell(row: row, col: col, state: .empty))
            }
        }
    }

    private func canPlaceShip(
        startRow: Int,
        startCol: Int,
        shipSize: Int,
        orientation: ShipOrientation
    ) -> Bool {
        var targetCells: [(Int, Int)] = []

        for i in 0..<shipSize {
            let row = orientation == .vertical ? startRow + i : startRow
            let col = orientation == .horizontal ? startCol + i : startCol

            if row >= boardSize || col >= boardSize {
                return false
            }

            targetCells.append((row, col))
        }

        for (row, col) in targetCells {
            for r in (row - 1)...(row + 1) {
                for c in (col - 1)...(col + 1) {
                    if r < 0 || r >= boardSize || c < 0 || c >= boardSize {
                        continue
                    }

                    let index = r * boardSize + c
                    if boardCells[index].state == .ship {
                        return false
                    }
                }
            }
        }

        return true
    }

    private func updateTexts() {
        if currentShipIndex < shipsToPlace.count {
            currentShipText = "Seçili gemi: \(shipsToPlace[currentShipIndex].size) hücrelik gemi"
        } else {
            currentShipText = "Seçili gemi: Tüm gemiler yerleştirildi"
        }

        orientationText = currentOrientation == .horizontal ? "Yön: Yatay" : "Yön: Dikey"
    }

    private func buildBoardMatrix() -> [[Int]] {
        var matrix = Array(
            repeating: Array(repeating: 0, count: boardSize),
            count: boardSize
        )

        for cell in boardCells {
            matrix[cell.row][cell.col] = cell.state == .ship ? 1 : 0
        }

        return matrix
    }

    func sendBoardToServer() {
        let message: [String: Any] = [
            "type": "SET_BOARD",
            "data": [
                "roomCode": roomCode,
                "playerId": playerId,
                "board": buildBoardMatrix()
            ]
        ]

        socketManager.send(dictionary: message)
        statusText = "Durum: Tahta gönderildi, rakip bekleniyor..."
        readyEnabled = false
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
        case "BOARD_SET":
            if let decoded = try? decoder.decode(BoardSetData.self, from: data) {
                statusText = decoded.message
            }

        case "GAME_STARTED":
            if let decoded = try? decoder.decode(GameStartedData.self, from: data) {
                firstTurnPlayerId = decoded.firstTurnPlayerId

                if let boardData = try? JSONSerialization.data(withJSONObject: buildBoardMatrix(), options: []),
                   let jsonString = String(data: boardData, encoding: .utf8) {
                    ownBoardJson = jsonString
                }

                shouldNavigateToGame = true
            }

        case "ERROR":
            if let decoded = try? decoder.decode(ErrorData.self, from: data) {
                statusText = "Hata: \(decoded.message)"
                readyEnabled = true
            }

        default:
            break
        }
    }

    func consumeGameNavigation() {
        shouldNavigateToGame = false
    }
}
extension ShipPlacementViewModel: SocketManagerDelegate {
    func socketDidConnect() {
        statusText = "Durum: Bağlantı hazır"
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
