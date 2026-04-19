//
//  RoomCreatedData.swift
//  AmiralBattiSwiftUI
//
//  Created by Alperen Saraç on 12.04.2026.
//

import Foundation
struct RoomCreatedData: Codable {
    let roomCode: String
    let playerId: String
    let players: [PlayerInfo]
    let message: String
}
