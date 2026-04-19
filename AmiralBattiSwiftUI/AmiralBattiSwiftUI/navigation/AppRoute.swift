//
//  AppRoute.swift
//  AmiralBattiSwiftUI
//
//  Created by Alperen Saraç on 12.04.2026.
//

import Foundation
enum AppRoute: Hashable {
    case lobby
    case placement(roomCode: String, playerId: String, playerName: String)
    case game(
        roomCode: String,
        playerId: String,
        playerName: String,
        firstTurnPlayerId: String,
        ownBoardJson: String
    )
}
