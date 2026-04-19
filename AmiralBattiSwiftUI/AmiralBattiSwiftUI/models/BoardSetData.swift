//
//  BoardSetData.swift
//  AmiralBattiSwiftUI
//
//  Created by Alperen Saraç on 12.04.2026.
//

import Foundation
struct BoardSetData: Codable {
    let roomCode: String
    let players: [PlayerInfo]
    let message: String
}
