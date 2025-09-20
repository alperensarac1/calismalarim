//
//  PrestigePerk.swift
//  CookieClickerSwift
//
//  Created by Alperen Saraç on 15.09.2025.
//

import Foundation
import Foundation

struct PrestigePerk: Codable {
    let key: String           // "perk_gprod" vs
    let title: String
    let desc: String
    let baseCost: Int
    let costScaling: Double
    var level: Int
    let maxLevel: Int

    func costForNext() -> Int {
        Int(Double(baseCost) * pow(costScaling, Double(level)))
    }
}

struct PerkStore: Codable {
    var points: Int = 0
    var gprod: Int = 0        // %5/level global çarpan
    var crit: Int = 0         // %1/level pasif crit şansı
    var discount: Int = 0     // %2/level indirim (tavan %50)
    var tapTop: Int = 0       // +1/level kalıcı tap gücü
}

enum PerkSave {
    private static let key = "cookie_perks_v1"
    static func load() -> PerkStore {
        if let d = UserDefaults.standard.data(forKey: key),
           let s = try? JSONDecoder().decode(PerkStore.self, from: d) { return s }
        return PerkStore()
    }
    static func save(_ s: PerkStore) {
        if let d = try? JSONEncoder().encode(s) {
            UserDefaults.standard.set(d, forKey: key)
        }
    }
}
