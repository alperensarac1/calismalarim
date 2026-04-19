//
//  RematchPlayerInfo.swift
//  AmiralBattiSwiftUI
//
//  Created by Alperen Saraç on 12.04.2026.
//

import Foundation
struct RematchPlayerInfo: Codable, Identifiable {
    let id: String
    let name: String
    let wantsRematch: Bool
}
