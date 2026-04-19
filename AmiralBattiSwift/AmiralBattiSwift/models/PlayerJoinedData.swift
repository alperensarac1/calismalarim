//
//  PlayerJoinedData.swift
//  AmiralBattiSwift
//
//  Created by Alperen Saraç on 10.04.2026.
//

import Foundation
struct PlayerJoinedData: Codable {
    let roomCode: String
    let players: [PlayerInfo]
    let message: String
}
