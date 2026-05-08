//
//  RadioRoom.swift
//  OnlineRadioSwiftUI
//
//  Created by Alperen Saraç on 2.05.2026.
//

import Foundation

struct RadioRoom: Identifiable, Codable {
    let id: Int
    let roomName: String
    let currentMusic: String?
    let isPlaying: Bool
    let listenerCount: Int
}
