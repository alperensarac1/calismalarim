//
//  BoardSetData.swift
//  AmiralBattiSwift
//
//  Created by Alperen Saraç on 11.04.2026.
//

import Foundation

struct BoardSetData: Codable {
    let roomCode: String
    let players: [PlayerInfo]
    let message: String
}
