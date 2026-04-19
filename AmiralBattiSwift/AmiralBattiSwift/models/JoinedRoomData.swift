//
//  JoinedRoomData.swift
//  AmiralBattiSwift
//
//  Created by Alperen Saraç on 10.04.2026.
//

import Foundation

struct JoinedRoomData: Codable {
    let roomCode: String
    let playerId: String
    let players: [PlayerInfo]
    let message: String
}
