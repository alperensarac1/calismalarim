//
//  FireResultData.swift
//  AmiralBattiSwiftUI
//
//  Created by Alperen Saraç on 12.04.2026.
//

import Foundation
struct FireResultData: Codable {
    let roomCode: String
    let shooterPlayerId: String
    let targetPlayerId: String
    let row: Int
    let col: Int
    let hit: Bool
    let nextTurnPlayerId: String?
    let gameOver: Bool
    let winnerPlayerId: String?
    let message: String
}
