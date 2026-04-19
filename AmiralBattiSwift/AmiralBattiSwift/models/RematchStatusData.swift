//
//  RematchStatusData.swift
//  AmiralBattiSwift
//
//  Created by Alperen Saraç on 11.04.2026.
//

import Foundation

struct RematchStatusData: Codable {
    let roomCode: String
    let players: [RematchPlayerInfo]
    let message: String
}
