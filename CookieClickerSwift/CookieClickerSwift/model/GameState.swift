//
//  GameState.swift
//  CookieClickerSwift
//
//  Created by Alperen Saraç on 14.09.2025.
//

import Foundation

struct GameState: Codable {
    var score: Double = 0
    var cps: Double = 0
    var baseTap: Int = 1
    var extraTap: Int = 0
    var prestigeLevel: Int = 0
    var prestigePoints: Int = 0
    // İstersen perk seviyeleri eklenir
}

final class Save {
    private static let key = "cookie_state_v1"
    static func load() -> GameState {
        guard let data = UserDefaults.standard.data(forKey: key),
              let s = try? JSONDecoder().decode(GameState.self, from: data) else { return GameState() }
        return s
    }
    static func save(_ s: GameState) {
        if let data = try? JSONEncoder().encode(s) {
            UserDefaults.standard.set(data, forKey: key)
        }
    }
}
