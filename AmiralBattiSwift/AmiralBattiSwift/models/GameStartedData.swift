//
//  GameStartedData.swift
//  AmiralBattiSwift
//
//  Created by Alperen Saraç on 11.04.2026.
//

import Foundation
struct GameStartedData: Codable {
    let roomCode: String
    let firstTurnPlayerId: String
    let players: [PlayerInfo]
    let message: String
}
